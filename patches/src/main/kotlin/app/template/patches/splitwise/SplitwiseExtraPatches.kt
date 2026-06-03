package app.template.patches.splitwise

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPLITWISE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags


// ── Feature-status fingerprints ──────────────────────────────────────────────
private val ItemizationFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ItemizationFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ProDuoCarouselFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ProDuoCarouselFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val WebViewNavigationKeyIsProFeatureFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/WebViewNavigationKey;",
    name = "isProFeature",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ProAccountCardFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ProAccountCardFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ReceiptScanningFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ReceiptScanningFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ReceiptScanningFeatureStatusGetScanFabVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getScanFabVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ReceiptScanningFeatureStatusGetHasScanTechniquesFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getHasScanTechniques",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ReceiptScanningFeatureStatusIsNativeScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isNativeScan",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ReceiptScanningFeatureStatusIsServerScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isServerScan",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ImportedTransactionSourceOnboardingScreenIsProUserFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/importedtransactions/ImportedTransactionSourceOnboardingScreen;",
    name = "isProUser",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)


private val ProDuoSettingsUpsellFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoSettingsUpsellFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val ProDuoSettingsUpsellFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoSettingsUpsellFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val SplitwisePersonIsProFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/Person;",
    name = "isPro",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)



private val WebViewScreenOnPurchaseUpdatedFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/views/WebViewScreen;",
    name = "onPurchaseUpdated",
    parameters = listOf("Lcom/Splitwise/SplitwiseMobile/features/billing/BillingClientWrapper\$PurchaseResult;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val WebViewScreenOnPurchaseUpdateStartedFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/views/WebViewScreen;",
    name = "onPurchaseUpdateStarted",
    parameters = listOf(),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val WebViewScreenGetWebViewActionFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/views/WebViewScreen;",
    name = "getWebViewAction",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)


private val WebViewScreenOnNewIntentFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/views/WebViewScreen;",
    name = "onNewIntent",
    parameters = listOf("Landroid/content/Intent;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PROTECTED)
)



private val PaymentBalanceChooserShowConvertCurrenciesAlertFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/payment/PaymentBalanceChooserFragment;",
    name = "showConvertCurrenciesAlert",
    parameters = listOf(),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

private val FriendshipScreenShowConvertCurrenciesAlertFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/relationship/friendship/FriendshipScreen;",
    name = "showConvertCurrenciesAlert",
    parameters = listOf(),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

private val PaymentBalanceChooserRunConvertCurrenciesFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/payment/PaymentBalanceChooserFragment;",
    name = "runConvertCurrencies",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

private val FriendshipScreenRunConvertCurrenciesFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/relationship/friendship/FriendshipScreen;",
    name = "runConvertCurrencies",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)
private val ProFeatureUtilsLoadChartsFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/utils/ProFeatureUtils;",
    name = "loadCharts",
    parameters = listOf(
        "Landroid/app/Activity;",
        "Lcom/Splitwise/SplitwiseMobile/utils/ProFeatureUtils\$ChartType;",
        "Ljava/lang/Long;"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
private val PaymentBalanceChooserShowConvertCurrencyButtonFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/payment/PaymentBalanceChooserFragment;",
    name = "showConvertCurrencyButton",
    parameters = listOf(),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)
private val FeatureAdResGetHasAccessFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdRes;",
    name = "getHasAccess",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val FeatureAdViewModelProcessFeatureResultFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdViewModel;",
    name = "processFeatureResult",
    parameters = listOf(
        "Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdRes;",
        "Lkotlin/jvm/functions/Function1;"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val ProAccountCardFeatureStatusGetProSubscriptionConfigurationFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getProSubscriptionConfiguration",
    parameters = listOf(),
    returnType = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus\$ProSubscriptionConfiguration;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ── Tracking / telemetry fingerprints ───────────────────────────────────────

private val FirebaseCrashReporterLogFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/crash/FirebaseCrashReporter;",
    name = "log",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val FirebaseCrashReporterRecordExceptionFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/crash/FirebaseCrashReporter;",
    name = "recordException",
    parameters = listOf("Ljava/lang/Throwable;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val FirebaseCrashReporterSetUserIdFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/crash/FirebaseCrashReporter;",
    name = "setUserId",
    parameters = listOf("J"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val CrashReporterTrackingEndpointLogEventFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/crash/CrashReporterTrackingEndpoint;",
    name = "logEvent",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/Map;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

private val EventTrackingClearUserFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/tracking/EventTracking;",
    name = "clearUser",
    parameters = listOf(),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val EventTrackingIdentifyUserFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/tracking/EventTracking;",
    name = "identifyUser",
    parameters = listOf("Ljava/lang/Long;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private val EventTrackingLogEventFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/tracking/EventTracking;",
    name = "logEvent",
    parameters = listOf("Lcom/Splitwise/SplitwiseMobile/tracking/TrackingEvent;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/*
@Suppress("unused")
val splitwiseMoreFeaturesPatch = bytecodePatch(
    name = "Unlock More Features",
    description = "Enables safer Splitwise Pro-adjacent UI gates without forcing Wallet/Banking modules that require backing account data.",
    default = true
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        listOf(
            AdFeatureStatusGetEnabledFingerprint,
            AdFeatureStatusGetVisibleFingerprint,
            ItemizationFeatureStatusGetEnabledFingerprint,
            ItemizationFeatureStatusGetVisibleFingerprint,
            ReceiptScanningFeatureStatusGetEnabledFingerprint,
            ReceiptScanningFeatureStatusGetVisibleFingerprint,
            ReceiptScanningFeatureStatusGetScanFabVisibleFingerprint,
            ReceiptScanningFeatureStatusGetHasScanTechniquesFingerprint,
            ReceiptScanningFeatureStatusIsNativeScanFingerprint,
            ReceiptScanningFeatureStatusIsServerScanFingerprint,
            ProDuoCarouselFeatureStatusGetEnabledFingerprint,
            ProDuoCarouselFeatureStatusGetVisibleFingerprint,
            ProAccountCardFeatureStatusGetEnabledFingerprint,
            ProAccountCardFeatureStatusGetVisibleFingerprint
        ).forEach { fp ->
            val method = runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull() ?: return@forEach
            method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
    }
}

/*
*/

@Suppress("unused")
val splitwiseUnlockDirectProFeaturesPatch = bytecodePatch(
    name = "Unlock Direct Pro Features",
    description = "Unlocks direct Pro-gated Splitwise features without forcing Wallet/Banking account-dependent modules.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        listOf(
            SplitwisePersonIsProFingerprint,
            ReceiptScanningFeatureStatusGetEnabledFingerprint,
            ReceiptScanningFeatureStatusGetVisibleFingerprint,
            ImportedTransactionSourceOnboardingScreenIsProUserFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }
    }
}



*/

/*
@Suppress("unused")
val splitwiseBypassWebBillingHandlersPatch = bytecodePatch(
    name = "Bypass Web Billing Handlers",
    description = "Stubs only stable Splitwise WebView billing callbacks to avoid fragile fingerprint failures.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        listOf(
            WebViewScreenOnPurchaseUpdateStartedFingerprint,
            WebViewScreenOnPurchaseUpdatedFingerprint
        ).forEach { fp ->
            runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
        }
    }
}

*/

/*
@Suppress("unused")
val splitwiseNeutralizeWebOnNewIntentPatch = bytecodePatch(
    name = "Neutralize WebView onNewIntent",
    description = "Optionally stubs WebViewScreen.onNewIntent to suppress billingFragmentAction subscribe/restore intent handling.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        runCatching { WebViewScreenOnNewIntentFingerprint.match(classDefBy(WebViewScreenOnNewIntentFingerprint.definingClass!!)).method }
            .getOrNull()?.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
    }
}

*/

/*
@Suppress("unused")
val splitwiseSuppressCurrencyConversionPremiumAlertsPatch = bytecodePatch(
    name = "Suppress Currency Conversion Premium Alerts",
    description = "Neutralizes currency conversion premium alert entrypoints so UI-side paywall dialogs do not interrupt conversion flows.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        listOf(
            PaymentBalanceChooserShowConvertCurrenciesAlertFingerprint,
            FriendshipScreenShowConvertCurrenciesAlertFingerprint
        ).forEach { fp ->
            runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
        }
    }
}

*/

/*
@Suppress("unused")
val splitwiseKeepCurrencyConversionClickablePatch = bytecodePatch(
    name = "Keep Currency Conversion Clickable",
    description = "Keeps currency conversion entrypoints alive by stubbing runConvertCurrencies methods to return without premium-side short circuit crashes.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        listOf(
            PaymentBalanceChooserRunConvertCurrenciesFingerprint,
            FriendshipScreenRunConvertCurrenciesFingerprint
        ).forEach { fp ->
            runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                if (implementation == null) return@apply
                // leave callable but no-op to prevent internal premium dead-end hooks from firing
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
        }
    }
}

*/

/*
@Suppress("unused")
val splitwiseForceShowConvertCurrencyButtonPatch = bytecodePatch(
    name = "Force Show Convert Currency Button",
    description = "Minimally rewrites PaymentBalanceChooserFragment.showConvertCurrencyButton to keep the currency conversion button visible without injecting premium dialog routing.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        runCatching {
            PaymentBalanceChooserShowConvertCurrencyButtonFingerprint
                .match(classDefBy(PaymentBalanceChooserShowConvertCurrencyButtonFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                sget-object v0, Lcom/Splitwise/SplitwiseMobile/data/model/UserPreference${'$'}UpsellOpportunity;->SPLIT_WITH_CONVERSION:Lcom/Splitwise/SplitwiseMobile/data/model/UserPreference${'$'}UpsellOpportunity;
                iput-object v0, p0, Lcom/Splitwise/SplitwiseMobile/features/payment/PaymentBalanceChooserFragment;->upsellOpportunity:Lcom/Splitwise/SplitwiseMobile/data/model/UserPreference${'$'}UpsellOpportunity;
                iget-object v0, p0, Lcom/Splitwise/SplitwiseMobile/features/payment/PaymentBalanceChooserFragment;->binding:Lcom/Splitwise/SplitwiseMobile/databinding/FragmentPaymentBalanceChooserBinding;
                const-string v1, "binding"
                if-nez v0, :binding_ok
                invoke-static {v1}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V
                const/4 v0, 0x0
                :binding_ok
                iget-object v0, v0, Lcom/Splitwise/SplitwiseMobile/databinding/FragmentPaymentBalanceChooserBinding;->currencyConversionButton:Lcom/google/android/material/button/MaterialButton;
                const-string v1, "currencyConversionButton"
                invoke-static {v0, v1}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V
                const/4 v1, 0x0
                invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
                return-void
                """.trimIndent()
            )
        }
    }
}

*/

/*
@Suppress("unused")
val splitwiseSuppressChartPaywallRoutePatch = bytecodePatch(
    name = "Suppress Chart Paywall Route",
    description = "Neutralizes Splitwise ProFeatureUtils.loadCharts so chart taps no longer open the premium chart paywall route.",
    default = true
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        runCatching { ProFeatureUtilsLoadChartsFingerprint.match(classDefBy(ProFeatureUtilsLoadChartsFingerprint.definingClass!!)).method }
            .getOrNull()?.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }
    }
}

/*
*/

@Suppress("unused")
val splitwiseForceProSubscriptionConfigPatch = bytecodePatch(
    name = "Force Pro Subscription Config",
    description = "Returns the embedded ProAccountCard subscription configuration object directly for entitled account config consumption paths.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        runCatching {
            ProAccountCardFeatureStatusGetProSubscriptionConfigurationFingerprint
                .match(classDefBy(ProAccountCardFeatureStatusGetProSubscriptionConfigurationFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "iget-object v0, p0, Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;->proSubscriptionConfiguration:Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus\$ProSubscriptionConfiguration;\nreturn-object v0")
        }
    }
}

*/

/*
@Suppress("unused")
val splitwiseBypassWebPaywallPatch = bytecodePatch(
    name = "Bypass Web Paywall",
    description = "Disables Splitwise Pro web/paywall routing and hides the account Pro card by overriding premium navigation and visibility gates.",
    default = false
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {
        WebViewNavigationKeyIsProFeatureFingerprint
            .match(classDefBy(WebViewNavigationKeyIsProFeatureFingerprint.definingClass!!))
            .method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        listOf(
            ProAccountCardFeatureStatusGetVisibleFingerprint,
            ProAccountCardFeatureStatusGetEnabledFingerprint,
            ProDuoSettingsUpsellFeatureStatusGetEnabledFingerprint,
            ProDuoSettingsUpsellFeatureStatusGetVisibleFingerprint
        ).forEach { fp ->
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }
    }
}
*/
