package app.template.patches.sai

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SAI_COMPATIBILITY

/**
 * Unlocks premium features in SAI (com.mtv.sai) v2.2.8.
 *
 * ## Protection layers
 *
 * ### 1 — PairIP (com.pairip.licensecheck)
 * `com.pairip.application.Application.attachBaseContext` calls
 * `LicenseClient.checkLicense(Context)`. The response handler
 * `processResponse(int, Bundle)` fires a paywall Activity on code 2
 * (NOT_LICENSED). `performLocalInstallerCheck()` gates the flow on
 * Play Store install origin.
 *
 * Fix: prepend `return-void` / `return true` to both methods. The
 * original body (and its try/catch table) is kept intact so the dex
 * verifier does not reject the class (removing instructions leaves
 * stale exception-handler entries that cause VerifyError).
 *
 * ### 2 — Subscription StateFlow observer (io3.emit)
 * `io3` is a Flow operator wired to MainActivity's subscription
 * StateFlow (`qy6<j60>`). On every emission it:
 *   - (default case) auto-shows `SubscriptionFragment` as a
 *     BottomSheet — regardless of whether the Boolean value is
 *     true or false.
 *   - (pswitch_0, AdsConsentState) calls `Le7.a(Context)` to
 *     initialise the Google Ads SDK when `canRequestAds=true`.
 *
 * Fix: prepend two instructions that load the COROUTINE_SUSPENDED
 * sentinel (`fq6.a`) into `v0` and return it immediately. Both
 * paths are blocked without touching the original switch table.
 *
 * ### 3 — canRequestAds gate (a7.a)
 * `La7.a()` reads the current `AdsConsentState` from a
 * `MutableStateFlow` and returns `b7.a` (canRequestAds). Called in
 * `InstallerFragment.h0()` and `c0()` to gate banner-ad loading.
 *
 * Fix: prepend `return false`. All in-app ad requests are blocked.
 *
 * ### 4 — Paywall click in install permission dialog (hw2.onClick DEFAULT case)
 * `InstallerFragment.m0()` shows a "allow unknown sources" permission dialog.
 * The dialog's positive button is `hw2(a=0)` whose DEFAULT onClick path opens
 * `SubscriptionFragment` as an upsell. The pswitch_0 path (a=1) correctly opens
 * `MANAGE_UNKNOWN_APP_SOURCES` settings — that path must be preserved.
 *
 * NOTE: m0() is intentionally NOT patched — it is essential for the install flow.
 * Suppressing m0() entirely breaks the "Select File" button on the installer homepage.
 *
 * Fix: prepend `return-void` to hw2.onClick so the DEFAULT upsell path exits
 * immediately. Because the packed-switch at instruction 3 branches to pswitch_0
 * BEFORE our return is reached, the permission-settings path (a=1) is unaffected.
 */
@Suppress("unused")
val saiUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Bypasses PairIP license check, paywall, ads, and pro upsell in SAI.",
    default = true
) {
    compatibleWith(SAI_COMPATIBILITY)

    execute {
        // ── 1a: PairIP — suppress license response (no paywall/error dialog) ──
        ProcessResponseFingerprint
            .match(classDefBy(ProcessResponseFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // ── 1b: PairIP — force Play Store installer check to pass ──
        PerformLocalInstallerCheckFingerprint
            .match(classDefBy(PerformLocalInstallerCheckFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── 2: Block subscription observer — no auto-paywall, no ads SDK init ──
        // Return COROUTINE_SUSPENDED sentinel (fq6.a) so the operator
        // reports suspension without processing its payload.
        SubscriptionObserverEmitFingerprint
            .match(classDefBy(SubscriptionObserverEmitFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    sget-object v0, Lfq6;->a:Lfq6;
                    return-object v0
                    """.trimIndent()
                )
            }

        // ── 3: Block all in-app ad requests ──
        CanRequestAdsFingerprint
            .match(classDefBy(CanRequestAdsFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x0
                    return v0
                    """.trimIndent()
                )
            }

        // ── 4: Suppress hw2 DEFAULT onClick (paywall upsell) in install permission dialog ──
        // hw2 onClick instruction layout:
        //   0: iget p1, hw2.a:I          (switch index: 0=upsell, 1=permission settings)
        //   1: iget-object v0, hw2.c     (InstallerFragment)
        //   2: iget-object p0, hw2.b     (ma/Dialog)
        //   3: packed-switch p1          → pswitch_0 if a=1 (permission settings path)
        //   [DEFAULT fall-through for a=0:]
        //   4: sget p1, m0:I             (synthetic field)
        //   5: invoke-virtual dismiss()  (dismiss the dialog)
        //   6: new-instance SubscriptionFragment  ← INSERT return-void HERE
        //
        // Inserting return-void at index 6 (before SubscriptionFragment creation):
        //   a=0 path: 0→1→2→3(fall-through)→4→5(dismiss)→[return-void] — dialog dismissed, no paywall
        //   a=1 path: 0→1→2→3(jump to pswitch_0) — permission settings opened, unaffected
        InstallerUpsellClickFingerprint
            .match(classDefBy(InstallerUpsellClickFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(6, "return-void")
            }

        // ── 5: Force isPro=true in InstallerFragment subscription observer ──
        // pw2.emit() sets InstallerFragment.g0 (isPro) from the subscription Boolean.
        // False (not subscribed) hides the main content area and triggers ad loading.
        // Instruction layout (0-indexed): iget(0) const(1) sget(2) iget(3)
        // packed-switch(4) check-cast(5) invoke-booleanValue(6) move-result(7)
        // iput-boolean-g0(8). Inserting const/4 p1,1 at index 8 overwrites the
        // booleanValue() result with true before it is stored to g0.
        InstallerSubscriptionObserverFingerprint
            .match(classDefBy(InstallerSubscriptionObserverFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(8, "const/4 p1, 0x1")
            }
    }
}
