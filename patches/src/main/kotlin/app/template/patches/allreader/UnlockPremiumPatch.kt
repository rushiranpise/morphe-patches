package app.template.patches.allreader

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ALLREADER_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

// All Reader v3.2.4 — Pairip license verification bypass
//
// This app uses the LicenseClient-based Pairip variant (no VMRunner / native lib).
// The check is initiated in Application.attachBaseContext via
// LicenseClient.checkLicense(Context), which constructs a LicenseClient and
// immediately calls initializeLicenseCheck().
//
// initializeLicenseCheck() dispatches on licenseCheckState.ordinal():
//   0 → CHECK_REQUIRED         → IPC to Play Store → may show paywall/error
//   1 → FULL_CHECK_OK          → re-validates saved response bundle
//   4 → REPEATED_CHECK_REQUIRED→ re-connects to Play Store
//   else (2, 3) → LOCAL_CHECK_OK / LOCAL_CHECK_REPORTED → return-void (no-op)
//
// Patch: clearBody() + return-void makes the method a permanent no-op.
// The LicenseCheckState field remains at its default (CHECK_REQUIRED),
// but none of the blocking paths (paywall Activity, error dialog, System.exit)
// are ever reached. clearBody() is required to remove the try-catch on
// LicenseCheckException, which would cause ART VerifyError if left in place
// after the instruction table is replaced.


// All Reader premium system overview:
// - Premium status is dual-gated: UtilsRepository.isPremiumUser (domain layer)
//   and k.c.b() (SharedPrefs "purchase" key, presentation layer).
// - Interstitial ads are shown via InterstitialPreloadManager.showInterAd()
//   and showPreloadTimeInter(); both check premiumUser before displaying.
//   They accept a c7/a callback that must be invoked so callers can proceed.
// - Native/banner ads are loaded by Z1.a.b(). It reads a static boolean
//   Z1/a.i ("premiumUser") and hides the MaterialCardView container (GONE)
//   when true. Patch: set the card view GONE and return early.
// - k.c.a() ("firstLaunch") skips onboarding; returning false bypasses it.

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks All Reader premium features and removes ads.",
) {
    compatibleWith(ALLREADER_COMPATIBILITY)

    execute {
        InitializeLicenseCheckFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // ── Premium gates ───────────────────────────────────────────────────

        // Domain-layer premium flag → always true.
        IsPremiumUserFingerprint.method.returnEarly(true)

        // SharedPrefs "purchase" gate → always true (skips paywall).
        IsPurchasedFingerprint.method.returnEarly(true)

        // ── Onboarding ──────────────────────────────────────────────────────

        // Skip LanguageActivity onboarding: report firstLaunch = false.
        IsFirstLaunchFingerprint.method.returnEarly(false)

        // ── Interstitial ads ────────────────────────────────────────────────
        // Both methods check premiumUser then show the ad. We replace their
        // bodies so the callback is always invoked immediately and the ad never
        // loads. clearBody() is required because both methods contain try-catch
        // blocks; leaving the try table intact after removing instructions
        // would cause ART VerifyError on the replaced body.
        //
        // showInterAd is static: callback is p1 (second parameter).
        // showPreloadTimeInter is an instance method: callback is p2 (third parameter).

        ShowInterAdFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-interface { p1 }, Lc7/a;->a()Ljava/lang/Object;
                    return-void
                """,
            )
        }

        ShowPreloadTimeInterFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-interface { p2 }, Lc7/a;->a()Ljava/lang/Object;
                    return-void
                """,
            )
        }

        // ── Native / banner ad loader ────────────────────────────────────────
        // Z1.a.b() inflates a native ad into a MaterialCardView stored in
        // instance field "e". We replace the body to unconditionally set that
        // card view to GONE (visibility = 8) and return, preventing any ad
        // inflation. clearBody() needed — method has try-catch in original.
        //
        // Register budget (11 declared):
        //   v0 = GONE constant (8)
        //   v2 = MaterialCardView from field "e"
        //   p0 = this (Z1/a instance)

        LoadNativeAdFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    iget-object v2, p0, LZ1/a;->e:Ljava/lang/Object;
                    check-cast v2, Landroid/view/View;
                    const/16 v0, 0x8
                    invoke-virtual { v2, v0 }, Landroid/view/View;->setVisibility(I)V
                    return-void
                """,
            )
        }
    }
}
