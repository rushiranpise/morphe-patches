package app.template.patches.blockingsites.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Pairip's non-obfuscated startup entry point (same shape as Wallverse's).
// The ordered filter verifies it creates a LicenseClient and starts the full
// license flow.
internal object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "initializeLicenseCheck",
            returnType = "V",
        ),
    ),
)


// SubscriptionHelper.isSubscribed() — returns (SubscriptionState.isSubscribed()
// OR AppPreferenceUseCase.getIsUserSubscribed()). Verified in smali.
internal object SubscriptionHelperIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/blockingsites/blocker/subscription_helper/SubscriptionHelper;",
    name = "isSubscribed",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// AppPreferenceUseCase.Companion.getIsUserSubscribed() — the true universal
// choke point. It reads the "is_user_subscribed" SharedPreferences flag and
// is called directly (not only through SubscriptionHelper.isSubscribed()) by
// MainActivity's nav header, DashFragment, AdvancedSettingsActivity,
// AccountabilityPartnerActivity, and TooEasyToDisableFeatureDialogFragment —
// verified in smali across all of those call sites. Patching this instead of
// (or in addition to) SubscriptionHelper.isSubscribed() is required because
// several of those callers OR in SubscriptionState.isSubscribed() directly
// and bypass SubscriptionHelper.isSubscribed() entirely.
internal object AppPreferenceGetIsUserSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/blockingsites/blocker/app_preferences/AppPreferenceUseCase\$Companion;",
    name = "getIsUserSubscribed",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// AppPreferenceUseCase.Companion.getCurrentSubscriptionId() — defaults to the
// localized "no plan" string when the "current_subscription_id" preference
// is unset (which it always is without a real purchase). ProfileActivity and
// other screens compare this value against known product-ID string resources
// (monthly/three-months/six-months/annual/lifetime) to decide which plan
// label to render, and fall back to "no active plan" text if none match —
// independent of isSubscribed()/getIsUserSubscribed(). Verified in smali.
internal object AppPreferenceGetCurrentSubscriptionIdFingerprint : Fingerprint(
    definingClass = "Lcom/blockingsites/blocker/app_preferences/AppPreferenceUseCase\$Companion;",
    name = "getCurrentSubscriptionId",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// SubscriptionUiState.isSubscribed() / getProductId() — the deepest, real
// choke point for the "Premium" bottom-nav screen (BlockerPremiumActivity /
// BlockPPremiumFragment). updateSubscriptionUi() reads THIS object directly
// via subscriptionUiState.isSubscribed()/getProductId() — it does NOT read
// AppPreferenceUseCase or SubscriptionHelper at all. The object is only
// populated by PremiumFragmentViewModel.updateUiIfUserSubscribed(), which in
// turn requires SubscriptionHelper.isSubscribed() AND a logged-in Firebase
// user AND a non-empty Profile.getSubscribedOrderId() — the last of which is
// never set without a real purchase, so the earlier isSubscribed() patches
// alone left this screen showing the Buy Now purchase flow. Overriding the
// data-class getters directly bypasses all of that upstream state logic in
// one shot. Verified in smali (both are simple one-field getters).
internal object SubscriptionUiStateIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/blockingsites/blocker/premium_screen/SubscriptionUiState;",
    name = "isSubscribed",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

internal object SubscriptionUiStateGetProductIdFingerprint : Fingerprint(
    definingClass = "Lcom/blockingsites/blocker/premium_screen/SubscriptionUiState;",
    name = "getProductId",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)
