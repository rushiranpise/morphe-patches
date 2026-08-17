package app.template.patches.splitwise

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPLITWISE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

// Splitwise — Unlock Pro — v26.7.3
//
// All targets use fully-qualified non-obfuscated class paths. No obfuscated
// identifiers anywhere in this patch.
//
// ── Layer 1: Core Pro entitlement ────────────────────────────────────────────
//   Person.isPro()Z → true
//   ImportedTransactionSourceOnboardingScreen.isProUser()Z → true
//
// ── Layer 2: Central pro-feature gate (most important) ────────────────────────
//   FeatureAdViewModel.accessFeature() → call grantFeatureAccess() directly.
//   This is the SINGLE gate for ALL pro features: charts, receipt scanning,
//   receipt upload, default splits, auto-split, transaction import, itemization.
//   Bypassing it here covers every feature in one patch.
//
//   NOTE on AdFeatureStatus.getEnabled():
//   getEnabled() is intentionally NOT patched. accessFeature() is bypassed
//   above so getEnabled() is never consulted for the feature gate path.
//   Patching getEnabled()→true would conflict with wallet/navigation checks
//   that read getEnabled() independently. Patching it→false would shadow
//   the feature gate (accessFeature bypass handles that). Leave untouched.
//
// ── Layer 3: Secondary flag gate ──────────────────────────────────────────────
//   FeatureAvailability.isSimpleFeatureEnabled() → true
//   Gates: live splits, payment requests, deeplink routing, wallet flags.
//
// ── Layer 4: Feature unlock (receipt + itemization) ───────────────────────────
//   ReceiptScanningFeatureStatus (all boolean getters) → true
//   ItemizationFeatureStatus (getEnabled, getVisible) → true
//
// ── Layer 5: Upsell/paywall suppression ───────────────────────────────────────
//   AdFeatureStatus.getVisible() → FALSE
//     Hides all ad banners: pro_root_screen_ad (Account/Activity/Balances tabs),
//     recent_activity_ad, post_add_bill_ad, pro_settings_ad.
//     getEnabled() is NOT patched (see Layer 2 note).
//   ProAccountCardFeatureStatus.getVisible() → false (hides upsell card in Account tab)
//   ProDuoSettingsUpsellFeatureStatus.getVisible() → false (hides settings upsell banner)
//   ProDuoCarouselFeatureStatus (getVisible, getEnabled) → false (hides carousel chip)
//   WebViewNavigationKey.isProFeature() → false (disables web paywall routing)
//   ProFeatureUtils.loadCharts → return-void (prevents chart paywall)

@Suppress("unused")
val splitwiseUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Splitwise Pro features (charts, receipt scanning, itemization, " +
        "default splits, transaction import), forces Person.isPro()=true, and suppresses " +
        "all upsell banners and paywall prompts in the Account, Activity, and Balances tabs.",
    default = true,
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {

        // ── Layer 1: Core Pro entitlement ──────────────────────────────────────
        PersonIsProFingerprint.method.returnEarly(true)
        ImportedTransactionSourceOnboardingIsProUserFingerprint.method.returnEarly(true)

        // ── Layer 2: Central pro-feature gate ──────────────────────────────────
        // Short-circuit accessFeature() to call grantFeatureAccess() after the
        // base feature name and navigation key are set, but before the gate check.
        //
        // accessFeature() instruction order:
        //   [0] const-string v0, "featureName"         ← null-check marker
        //   [1] checkNotNullParameter(p1, v0)
        //   [2] setBaseFeatureName(p1)                 ← MUST run first
        //   [3] setBaseNavigationKey(p2)               ← MUST run second
        //   [4] ← inject HERE: grantFeatureAccess() uses these two fields
        //   [5] getAdFeature(p1) / getVisible() / getEnabled() / gate check
        //
        // Injecting at index 0 meant grantFeatureAccess() was called before
        // setBaseFeatureName/setBaseNavigationKey ran → FeatureAdRes got null
        // featureName and null navigationKey → observer null-checked and did
        // nothing → feature tap appeared frozen/non-responsive.
        AccessFeatureFingerprint.method.addInstructions(
            4,
            """
                invoke-direct {p0}, Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdViewModel;->grantFeatureAccess()V
                return-void
            """.trimIndent(),
        )

        // ── Layer 3: Secondary boolean flag gate ────────────────────────────────
        // isSimpleFeatureEnabled(String)Z — gates live splits, payment requests,
        // deeplink routing, wallet flags.
        // IMPORTANT: "admin" and "testing_tools" must return FALSE — they activate
        // the "Admin tools" debug section in the Account tab (Log in as email,
        // Load a URL, Set a custom server domain). We intercept with a name check
        // and only return false for those two keys; all others return true.
        IsSimpleFeatureEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string v0, "admin"
                    invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-nez v0, :block
                    const-string v0, "testing_tools"
                    invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-nez v0, :block
                    const/4 v0, 0x1
                    return v0
                    :block
                    const/4 v0, 0x0
                    return v0
                """.trimIndent(),
            )
        }

        // ── Layer 3b: Pro Account Card config ──────────────────────────────────
        // AccountUpsellSection.getItemCount() returns 1 only if
        //   getProAdConfiguration() != null → shows "Do more with Splitwise Pro"
        // AccountProSection.getItemCount() returns 1 only if
        //   getProSubscriptionConfiguration() != null → shows pro management card
        //
        // Returning null from getProAdConfiguration() removes the upsell card.
        // Returning null from getProSubscriptionConfiguration() also removes the
        // pro section card — the Account tab shows neither upsell nor pro card.
        //
        // IMPORTANT:
        // These methods return objects, not void. Use returnEarly(null), not
        // returnEarly().
        //
        // returnEarly() without parameters only works for void (V) methods.
        
        ProAccountCardGetProAdConfigurationFingerprint.method.returnEarly(null)
        ProAccountCardGetProSubscriptionConfigurationFingerprint.method.returnEarly(null)

        // ── Layer 4: Feature unlocks ────────────────────────────────────────────

        // Receipt scanning — all boolean gates → true.
        ReceiptScanningGetEnabledFingerprint.method.returnEarly(true)
        ReceiptScanningGetVisibleFingerprint.method.returnEarly(true)
        ReceiptScanningGetScanFabVisibleFingerprint.method.returnEarly(true)
        ReceiptScanningGetHasScanTechniquesFingerprint.method.returnEarly(true)
        ReceiptScanningIsNativeScanFingerprint.method.returnEarly(true)
        ReceiptScanningIsServerScanFingerprint.method.returnEarly(true)

        // Itemization.
        ItemizationGetEnabledFingerprint.method.returnEarly(true)
        ItemizationGetVisibleFingerprint.method.returnEarly(true)

        // ── Layer 5: Upsell & paywall suppression ───────────────────────────────

        // AdFeatureStatus.getVisible() serves two purposes:
        //   - Banner display ("pro_root_screen_ad" etc) → want false (hide banner)
        //   - Feature gate ("default_splits" etc) → want true (enable feature)
        //
        // We can't patch getVisible() globally without breaking feature detection.
        // Instead intercept getAdFeature(String) at index 1 (after the const-string,
        // before new-instance) and for known banner feature names return a blank
        // AdFeatureStatus() whose visible:Z defaults to false, suppressing the banner.
        // All other feature names fall through to loadFeatureData() as normal.
        // Intercept getAdFeature(String) at index 0.
        // For ad-banner feature names → return blank AdFeatureStatus() [visible=false].
        // For all other names → injected code does nothing and falls through to the
        // original const-string instruction at index 0 (now shifted to after injection).
        // No labels referencing existing instructions needed — every branch either
        // returns early or falls off the bottom of the injected block.
        // The final "if-eqz v0, :pass" skips the last return-object for non-matches,
        // and :pass is an internal label defined within the injected block.
        GetAdFeatureFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "pro_root_screen_ad"
                invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :cond_0
                new-instance v0, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;
                invoke-direct { v0 }, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;-><init>()V
                return-object v0
                :cond_0
                const-string v0, "recent_activity_ad"
                invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :cond_1
                new-instance v0, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;
                invoke-direct { v0 }, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;-><init>()V
                return-object v0
                :cond_1
                const-string v0, "post_add_bill_ad"
                invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :cond_2
                new-instance v0, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;
                invoke-direct { v0 }, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;-><init>()V
                return-object v0
                :cond_2
                const-string v0, "pro_settings_ad"
                invoke-virtual { p1, v0 }, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :cond_3
                new-instance v0, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;
                invoke-direct { v0 }, Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;-><init>()V
                return-object v0
                :cond_3
            """.trimIndent(),
        )

        // Pro Account Card ("Get Splitwise Pro" upsell in Account tab).
        // AccountViewModel passes getVisible() to AccountDetailsSection constructor.
        ProAccountCardGetVisibleFingerprint.method.returnEarly(false)

        // Pro/Duo Settings upsell banner.
        ProDuoSettingsUpsellGetVisibleFingerprint.method.returnEarly(false)

        // Pro/Duo Carousel upsell chips in friendship/group carousels.
        ProDuoCarouselGetVisibleFingerprint.method.returnEarly(false)
        ProDuoCarouselGetEnabledFingerprint.method.returnEarly(false)

        // Web paywall routing.
        WebViewNavigationKeyIsProFeatureFingerprint.method.returnEarly(false)

        // NOTE: ProFeatureUtils.loadCharts is intentionally NOT patched.
        // Charts open a WebView loading "charts?create_session=true" on Splitwise's
        // servers. The server decides whether to render the chart or show a paywall
        // based on the session's Pro status — this is server-side validation.
        // Stubbing loadCharts to return-void would prevent charts from opening at all.
        // The original method is left intact so the WebView opens; whether the
        // server shows charts or a paywall depends on the account's server-side state.
    }
}
