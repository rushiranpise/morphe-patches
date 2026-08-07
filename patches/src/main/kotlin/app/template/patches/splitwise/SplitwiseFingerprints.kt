package app.template.patches.splitwise

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ────────────────────────────────────────────────────────────────────────────
// Splitwise fingerprints — all non-obfuscated com.Splitwise.SplitwiseMobile paths.
// Smali verified against v26.7.3 (versionCode 950).
// ────────────────────────────────────────────────────────────────────────────

// ── 1. Core Pro entitlement ───────────────────────────────────────────────────
//
// Person.isPro()Z — boolean field backed by "is_pro" from /current_user API.
// Consumed by:
//   - ImageCaptureScreen — receipt image resolution (pro = full res)
//   - TransactionSourceAdjustAutoSplitModalFragment — auto-split UI gate
//   - ImportedTransactionSourceOnboardingScreen — bank import onboarding gate
internal val PersonIsProFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/Person;",
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ImportedTransactionSourceOnboardingScreen.isProUser()Z — local wrapper
// around Person.isPro() for the bank-import onboarding screens.
internal val ImportedTransactionSourceOnboardingIsProUserFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/importedtransactions/ImportedTransactionSourceOnboardingScreen;",
    name = "isProUser",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
)

// ── 2. Central pro-feature gate ───────────────────────────────────────────────
//
// FeatureAdViewModel.accessFeature(String, NavigationKey.SupportsPush) is the
// SINGLE ENTRY POINT for every pro-gated feature in the app:
//   AddDetailFragment        → "receipt_scanning", "receipt_upload"
//   ExpenseDetailsFragment   → "charts", "transaction_import", "receipt_upload"
//   AddExpenseRootFragment   → "itemization", "default_splits"
//   TransactionSourceAdjust* → "default_splits"
//
// Original logic:
//   if (getAdFeature(name).getVisible() && getEnabled()) grantFeatureAccess()
//   else showProAd()
//
// Patching to call grantFeatureAccess() unconditionally short-circuits every
// pro feature gate in one instruction, bypassing the server metadata check.
// grantFeatureAccess() is private on the same class — invoke-virtual is valid.
internal val AccessFeatureFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/views/FeatureAdViewModel;",
    name = "accessFeature",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ldev/enro/core/NavigationKey\$SupportsPush;",
    ),
    strings = listOf("featureName"),
)

// ── 4. Secondary boolean feature flag gate ────────────────────────────────────
//
// FeatureAvailability.isSimpleFeatureEnabled(String)Z — used for boolean
// "is this feature enabled?" queries that bypass the FeatureAdViewModel ad flow:
//   LiveSplitsHandlingActivity      — LIVE_SPLIT toggle
//   RecentPaymentRequestsProvider   — "payment_requests" toggle
//   NavigationRequestHandler        — deeplink routing feature guards
//   UIUtilities                     — "app_store_review_prompt"
//   SplitwiseWalletCard* screens    — wallet feature flags
//
// Returning true makes all simple capability checks pass without consulting
// the server metadata cache.
internal val IsSimpleFeatureEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/utils/FeatureAvailability;",
    name = "isSimpleFeatureEnabled",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("name"),
)

// ── 5. Receipt scanning feature status ────────────────────────────────────────
//
// All boolean getters → true to fully enable the Pro OCR scan feature.
internal val ReceiptScanningGetScanFabVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getScanFabVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal val ReceiptScanningGetHasScanTechniquesFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getHasScanTechniques",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal val ReceiptScanningIsNativeScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isNativeScan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal val ReceiptScanningIsServerScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isServerScan",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal val ReceiptScanningGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

internal val ReceiptScanningGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── 6. Itemization feature status ─────────────────────────────────────────────

internal val ItemizationGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

internal val ItemizationGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── 7. Upsell suppression ─────────────────────────────────────────────────────
//
// These feature status classes control upsell UI shown to free users.
// Returning false hides/disables them — they are NOT related to the feature
// access gate and must remain false to suppress upgrade prompts.

// ProAccountCardFeatureStatus.getVisible() — "Upgrade to Pro" card in Account tab.
// AccountViewModel passes this to AccountDetailsSection constructor:
//   if getVisible()=true → renders upsell card in Account tab
internal val ProAccountCardGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ProDuoSettingsUpsellFeatureStatus.getVisible() — upsell banner in Settings.
// FriendshipSettings / GroupSettingsScreen guard the banner with getVisible().
internal val ProDuoSettingsUpsellGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoSettingsUpsellFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ProDuoCarouselFeatureStatus — friendship/group carousel upsell chip.
// FriendshipCarouselChipFactory: if getVisible() && !getEnabled() → show chip.
// Both false → chip suppressed entirely.
internal val ProDuoCarouselGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getVisible",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

internal val ProDuoCarouselGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getEnabled",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// WebViewNavigationKey.isProFeature() — gates web navigation routes behind Pro.
internal val WebViewNavigationKeyIsProFeatureFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/WebViewNavigationKey;",
    name = "isProFeature",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── 8. Pro Account Card section configuration ─────────────────────────────────
//
// The Account tab uses TWO sections driven by non-null config objects:
//
//   AccountUpsellSection.getItemCount() → 1 if proAdConfiguration != null
//     Shows "Do more with Splitwise Pro / Get Splitwise Pro" upsell card
//   AccountProSection.getItemCount()   → 1 if proSubscriptionConfiguration != null
//     Shows "Splitwise Pro — Annual" subscription management card
//
// Gate:
//   getProAdConfiguration()           → null = no upsell card shown        ← we want null
//   getProSubscriptionConfiguration() → null = no pro card shown           ← we want non-null
//
// Patch strategy:
//   getProAdConfiguration() → returnEarly(null)       hides upsell card
//   getProSubscriptionConfiguration() → returnEarly fake ProSubscriptionConfiguration
//     Returns a ProSubscriptionConfiguration("Annual", "Pro", "Active", "Manage")
//     so the pro section renders instead of the upsell.

internal val ProAccountCardGetProAdConfigurationFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getProAdConfiguration",
    returnType = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus\$ProAdConfiguration;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal val ProAccountCardGetProSubscriptionConfigurationFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getProSubscriptionConfiguration",
    returnType = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus\$ProSubscriptionConfiguration;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── 9. Ad banner suppression via getAdFeature intercept ───────────────────────
//
// AdFeatureStatus.getVisible()Z is used for TWO different purposes:
//   a) Banner display: ActivityFragment, AccountFragment, RecentActivityScreen
//      read getAdFeature("pro_root_screen_ad").getVisible() to show/hide the
//      "Do more with Splitwise Pro" toolbar banner. → want: false (hide banner)
//   b) Feature gate: GroupSettingsFragment.getDefaultSplitsAvailable() reads
//      getAdFeature("default_splits").getVisible() to enable the Default Splits
//      setting. → want: true (enable feature)
//
// Since AdFeatureStatus doesn't know its own feature name, we can't patch
// getVisible() globally. Instead we intercept getAdFeature(String) and for
// banner-only feature names return a fresh AdFeatureStatus() (visible=false,
// enabled=false by Java default) before loadFeatureData() is called.
// For all other feature names we fall through to the normal server data path.
//
// Banner feature names to suppress:
//   "pro_root_screen_ad"   — toolbar banner on Balances/Activity/Account tabs
//   "recent_activity_ad"   — inline feed banner in recent activity
//   "post_add_bill_ad"     — full-screen ad after adding an expense
//   "pro_settings_ad"      — inline banner in settings
//
// Smali verified (v26.7.3, classes4.dex, FeatureAvailability):
//   .method public final getAdFeature(String)AdFeatureStatus
//   .registers 3
//   [0] const-string v0, "featureName"
//   [1] new-instance v0, AdFeatureStatus   ← inject HERE (index 1)
//   [2] invoke-direct {v0}, AdFeatureStatus.<init>()
//   [3] invoke-direct {p0,p1,v0}, loadFeatureData(name, status)
//   [4] return-object v0
internal val GetAdFeatureFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/features/shared/utils/FeatureAvailability;",
    name = "getAdFeature",
    returnType = "Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("featureName"),
)
