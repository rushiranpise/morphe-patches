package app.template.patches.inure.fullversion

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.INURE_COMPATIBILITY
import app.template.patches.shared.Constants.INURE_GITHUB_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

/**
 * Unlocks all features in Inure App Manager.
 * Targets both the Play flavour (app.simple.inure.play) and the GitHub flavour
 * (app.simple.inure). Class paths are identical across both.
 *
 * ## Premium model (build107.2.0)
 * Inure uses a three-path verification model — no Play Billing SDK:
 *   1. Companion APK: app.simple.inureunlocker — cert SHA1 verified via IPC broadcast
 *   2. Gumroad licence key — verified against api.gumroad.com, stored as "has_license_key"
 *   3. 15-day trial — daysBetween(firstLaunch, today) <= 15
 *
 * On success any path calls TrialPreferences.setFullVersion(true).
 *
 * ## Startup gate flow
 * ```
 * SplashScreen.onViewCreated()
 *   ├─ unlockStateChecker()           ← Layer 9: no-op (defence against setFullVersion(false))
 *   └─ LauncherViewModel.initCheck()
 *        └─ if (!isFullVersion())     ← Layer 1: always true → skips verifyCertificate()
 * ```
 *
 * ## Patch layers (11 total)
 *
 * ── LOAD-BEARING ─────────────────────────────────────────────────────────────
 *
 * Layer 1  — TrialPreferences.isFullVersion()Z                   → true        [REQUIRED]
 *   Deepest gate; first branch in isAppFullVersionEnabled() before date math.
 *   Also the initCheck() gate that skips verifyCertificate() on startup.
 *
 * Layer 9  — SplashScreen.unlockStateChecker()V                  → GONE+return [REQUIRED]
 *   Hides daysLeft synchronously in onViewCreated (fixes flicker) and kills
 *   the setFullVersion(false) call that would overwrite Layer 1.
 *
 * Layer 10 — TrialPreferences.getFirstLaunchDate()J              → now()       [REQUIRED]
 *   Root date fix. daysBetween(now, now) = 0 everywhere. All trial checks
 *   that read this value (50+ callers) see the trial as "started today".
 *
 * Layer 11 — CalendarUtils.getToday()Date                        → epoch       [REQUIRED]
 *   Belt-and-suspenders: getToday() = 1970-01-01. daysBetween(firstLaunch,
 *   epoch) always <= 0 regardless of stored firstLaunchDate.
 *
 * ── REDUNDANT (defence-in-depth, kept for future-proofing) ───────────────────
 *   With Layers 10+11 active, daysBetween is always <= 0, so the following
 *   date-computing methods already return the correct values. They are kept
 *   because they cost nothing and guard against future refactors where a
 *   method might be inlined or the date calls removed.
 *
 * Layer 2  — TrialPreferences.isAppFullVersionEnabled()Z         → true        [redundant: L10+11]
 * Layer 3  — TrialPreferences.isWithinTrialPeriod()Z             → true        [redundant: L10+11]
 * Layer 4  — TrialPreferences.isTrialWithoutFull()Z              → false       [redundant: L1+L10+11]
 * Layer 5  — TrialPreferences.hasLicenceKey()Z                   → true        [redundant: L9]
 * Layer 6  — TrialPreferences.isUnlockerVerificationRequired()Z  → false       [redundant: L9]
 * Layer 7  — BaseActivity.fullVersionCheck()Z                    → true        [redundant: L10+11]
 * Layer 8  — BaseActivity.fullVersionCheck(Function0)Z           → true        [redundant: L10+11]
 */
@Suppress("unused")
val unlockFullVersionPatch = bytecodePatch(
    name = "Unlock Full Version",
    description = "Unlocks all features in Inure App Manager by bypassing the trial period and companion-app verification checks.",
    default = true
) {
    compatibleWith(INURE_COMPATIBILITY)
    compatibleWith(INURE_GITHUB_COMPATIBILITY)

    execute {
        // ── Layer 1: isFullVersion()Z → true ─────────────────────────────────
        IsFullVersionFingerprint.method.returnEarly(true)

        // ── Layer 2: isAppFullVersionEnabled()Z → true ───────────────────────
        IsAppFullVersionEnabledFingerprint.method.returnEarly(true)

        // ── Layer 3: isWithinTrialPeriod()Z → true ───────────────────────────
        IsWithinTrialPeriodFingerprint.method.returnEarly(true)

        // ── Layer 4: isTrialWithoutFull()Z → false ───────────────────────────
        IsTrialWithoutFullFingerprint.method.returnEarly(false)

        // ── Layer 5: hasLicenceKey()Z → true ─────────────────────────────────
        // New in build107.2.0. Routes unlockStateChecker() into the licence key
        // fast-path before it reaches the companion-APK check + setFullVersion(false).
        HasLicenceKeyFingerprint.method.returnEarly(true)

        // ── Layer 6: isUnlockerVerificationRequired()Z → false ───────────────
        // Keeps the "no re-verification" branch active in unlockStateChecker().
        IsUnlockerVerificationRequiredFingerprint.method.returnEarly(false)

        // ── Layer 7: BaseActivity.fullVersionCheck()Z → true ─────────────────
        BaseActivityFullVersionCheckFingerprint.method.returnEarly(true)

        // ── Layer 8: BaseActivity.fullVersionCheck(Function0)Z → true ────────
        BaseActivityFullVersionCheckWithCallbackFingerprint.method.returnEarly(true)

        // ── Layer 10: getFirstLaunchDate()J → System.currentTimeMillis() ─────────
        // Root fix for the date gate. Makes daysBetween(today, today)=0 in ALL
        // callers: isWithinTrialPeriod, isAppFullVersionEnabled, getDaysLeft, etc.
        // The trial always reads as "started today" — expires never.
        GetFirstLaunchDateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
                move-result-wide v0
                return-wide v0
                """.trimIndent()
            )
        }

        // ── Layer 11: CalendarUtils.getToday()Date → new Date(0) ─────────────
        // Belt-and-suspenders: "today" = 1970-01-01 (epoch). daysBetween any
        // future firstLaunchDate and epoch is always <= 0, so the trial gate
        // never expires regardless of what is stored in SharedPreferences.
        GetTodayFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, Ljava/util/Date;
                const-wide/16 v1, 0x0
                invoke-direct {v0, v1, v2}, Ljava/util/Date;-><init>(J)V
                return-object v0
                """.trimIndent()
            )
        }

        // ── Layer 9: SplashScreen.unlockStateChecker()V → hide daysLeft + return ──
        // Belt-and-suspenders: eliminates setFullVersion(false) at the call site.
        // Also fixes the visible flicker: daysLeft is VISIBLE by default in XML,
        // and unlockStateChecker() is called synchronously in onViewCreated() right
        // after daysLeft is assigned via findViewById. A plain returnEarly() leaves
        // the view visible for one frame before the layout pass, causing a brief
        // "%s days of trial left" flicker.
        // Fix: replace the body with setVisibility(GONE) on daysLeft then return.
        // This is a direct View call — no dependency on ViewUtils, survives refactors.
        UnlockStateCheckerFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                iget-object v0, p0, Lapp/simple/inure/ui/launcher/SplashScreen;->daysLeft:Lapp/simple/inure/decorations/typeface/TypeFaceTextView;
                if-eqz v0, :skip
                const/16 v1, 0x8
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                :skip
                return-void
                """.trimIndent()
            )
        }
    }
}
