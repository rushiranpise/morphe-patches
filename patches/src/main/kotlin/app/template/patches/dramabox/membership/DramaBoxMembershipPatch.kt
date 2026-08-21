package app.template.patches.dramabox.membership

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DRAMABOX_COMPATIBILITY
import app.template.patches.shared.returnEarly

// DramaBox Activation Lock Bypass
//
// ── What this gate does ─────────────────────────────────────────────────────
//   SplashLockActivity is the declared LAUNCHER activity. Every cold launch
//   hits onCreate before anything else. If SharedPreferences key "is_verified"
//   is false (fresh install / data cleared), the app renders a full-screen
//   activation UI:
//     • Title:  "Ingrese su codigo de activacion"
//     • Field:  "Codigo" (EditText)
//     • Button: "ACTIVAR" (calls NetworkThread)
//   NetworkThread validates against http://spotube.panelmx.online/api/check_code.php
//   and only writes is_verified=true on a {"ok":true} response.
//   Without a valid server-issued code the app is permanently unusable.
//
// ── Patch logic ─────────────────────────────────────────────────────────────
//   onCreate layout (smali indices, simplified):
//     0: invoke-super {p0, p1}, Activity.onCreate(Bundle)V   ← KEPT
//     1: const-string v0, "drama_lock_prefs"                 ← REPLACED by our injection
//     2: const/4 v1, 0x0
//     3: invoke-virtual getSharedPreferences(...)
//     4: move-result-object v2
//     5: const-string v3, "is_verified"
//     6: invoke-interface getBoolean("is_verified", false)
//     7: move-result v4
//     8: if-eqz v4, :cond_0  ← branch to lock UI if not verified
//        [falls through to launchMain() if verified]
//
//   We insert at index 1 (after super.onCreate):
//     invoke-direct {p0}, SplashLockActivity.launchMain()V
//     return-void
//
//   launchMain() is private → invoke-direct is correct.
//   It creates an Intent for MainActivity with ACTION_MAIN + CATEGORY_LAUNCHER
//   flags, calls startActivity(), then finish() — identical to the normal
//   verified path. The SharedPrefs check and the entire lock UI are never reached.
//
// ── Why not write is_verified=true instead? ──────────────────────────────────
//   SharedPrefs writes persist across reinstalls on Android 12+ with backup.
//   Calling launchMain() directly is cleaner — it's the exact same code path
//   the app uses when verification succeeds, requires no SharedPrefs mutation,
//   and does not interact with the server at all.

// DramaBox Membership Patch
//
// ── What DramaBox gates behind VIP ──────────────────────────────────────────
//   • Membership badge + privilege UI (MembershipActivityV2/V3, MemberFragment)
//   • Ad-free episode playback (ad load suppressed when s2()=true)
//   • Download quality selection (chargeChapter chapters still server-gated)
//   • Daily bonus coins for VIP members
//   • VIP-exclusive series access (isVipTheater field, server-side)
//
//   NOTE: Chapter-level paywalls (isCharge / chargeChapter) are resolved by
//   the server at content request time — the server returns the video CDN URL
//   only after validating the account's entitlements. These cannot be bypassed
//   at the DEX layer. This patch unlocks the local VIP status layer only.
//
// ── Three-layer attack ───────────────────────────────────────────────────────
//
//   Layer 1 — DataStore setter intercept (VipStateSetterFingerprint):
//     Z6/dramabox.E7(Z)V: inject `const/4 p1, 0x1` before the DataStore write.
//     Every caller of E7 — both the user-info sync path and the billing success
//     path — now always writes VIP=true to the DataStore regardless of what the
//     server returned.
//
//   Layer 2 — DataStore getter override (VipStateGetterFingerprint):
//     Z6/dramabox.s2()Z: returnEarly(true).
//     All runtime VIP reads short-circuit to true before touching DataStore.
//     This covers UI checks that happen before any network call completes
//     (cold launch, background restore, configuration change).
//
//   Layer 3 — Billing success handler (BillingSuccessFingerprint):
//     x9/switch.l1(DramaPurchase)V: returnEarly().
//     The handler reads DramaPurchase.isVip() (also hardcoded 1) and coin
//     deltas. Skipping it prevents any non-VIP coin-only purchase from
//     momentarily writing VIP=false to DataStore before Layer 1 corrects it.
//     The return-void is safe because coin balance updates go through a separate
//     Z6/ll DataStore path not affected by this patch.
//
//   Layers 1+2 together guarantee VIP=true at every read and write.
//   Layer 3 plugs the race window during billing callbacks.
//
//   VipPropagationFingerprint (RT) and UserInfoSyncFingerprint (lO) are
//   intentionally NOT patched with returnEarly — their void bodies do other
//   work (coin sync, token refresh, RxBus events) needed for app stability.
//   Layer 1 intercepts the VIP write inside E7 so those methods can run
//   normally while still storing VIP=true.

@Suppress("unused")
val dramaBoxMembershipPatch = bytecodePatch(
    name = "Unlock Membership",
    description = "Unlocks DramaBox VIP membership status locally by patching the DataStore VIP getter and setter, bypassing the Google Play Billing verification and server-side VIP flag propagation.",
    default = true,
) {
    compatibleWith(DRAMABOX_COMPATIBILITY)

    execute {

        // Insert immediately after super.onCreate (index 0).
        // index 1 in the original method is "const-string v0, drama_lock_prefs" —
        // our two instructions push everything after them down; the original
        // instructions remain intact but are unreachable after return-void.
        SplashLockOnCreateFingerprint.method.addInstructions(
            1, // after invoke-super Activity.onCreate(Bundle)
            """
                invoke-direct {p0}, Lcom/storymatrix/drama/lock/SplashLockActivity;->launchMain()V
                return-void
            """.trimIndent(),
        )

        // ── Layer 1: Force VIP=true into DataStore on every write ─────────
        // Z6/dramabox.E7(Z)V receives the VIP boolean as parameter p1.
        // Inserting `const/4 p1, 0x1` at index 0 overwrites the caller's
        // value before the DataStore write executes — E7 then stores true
        // regardless of what isVip() returned upstream.
        VipStateSetterFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )

        // ── Layer 2: Short-circuit VIP reads to true ──────────────────────
        // Z6/dramabox.s2()Z reads from DataStore via a KProperty delegate.
        // Returning true here before the DataStore read covers all runtime
        // UI guards that fire before or between network syncs.
        VipStateGetterFingerprint.method.returnEarly(true)

        // ── Layer 3: Suppress billing callback VIP write race ─────────────
        // x9/switch.l1(DramaPurchase)V is the Google Play Billing success
        // handler. Its only VIP-relevant action (E7 call) is already covered
        // by Layer 1, so returning early here is safe and plugs the narrow
        // window between billing acknowledgement and DataStore propagation
        // where an isVip=0 DramaPurchase could transiently clear VIP state.
        BillingSuccessFingerprint.method.returnEarly()
    }
}
