package app.template.patches.tranzmate

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val MoovitApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/MoovitApplication;",
    name = "onCreate",
    returnType = "V",
)

val SubscriptionStateFingerprint = Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("subscribed_skus"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

val AdUnitResolverFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
    strings = listOf("is_ads_free_version"),
)

val MoovitAdViewSetSourceFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/ads/MoovitAdView;",
    name = "setAdSource",
    returnType = "V",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
)

val MoovitBannerAdViewSetSourceFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/ads/MoovitBannerAdView;",
    name = "setAdSource",
    returnType = "V",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
)

val SubscriptionPackageStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/a;",
    name = "b",
    returnType = "Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;",
    parameters = emptyList(),
)

val SafeRideCalculateStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/safety/b;",
    name = "a",
    returnType = "Ljava/lang/Enum;",
    parameters = listOf("Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
)

val PromoCellFragmentOnViewCreatedFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/MoovitSubscriptionsPromoCellFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val BlockPaywallGateFingerprint = Fingerprint(
    definingClass = "Lw81;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Lcom/moovit/MoovitActivity;"),
    strings = listOf("block_paywall"),
)

val BlockPaywallActivityOnReadyFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/paywall/BlockPaywallActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

val MoovitPlusOnboardingActivityFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

val MoovitPlusActivityOnReadyFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

val MoovitPlusHelpCenterMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusHelpCenterMenuItemFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val MoovitPlusMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusMenuItemFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val AdFreeMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/AdFreeMenuItemFragment;",
    name = "onCreateView",
    returnType = "Landroid/view/View;",
    parameters = listOf("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;"),
)

val ItineraryBlockedSmartTipsBannerFingerprint = Fingerprint(
    definingClass = "Ljy6;",
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lkx2;"),
)

val MyMoovitPlusGoPremiumCardFingerprint = Fingerprint(
    definingClass = "Laz9;",
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lkx2;"),
    strings = listOf("goPremiumCard"),
)

val MoovitPlusPackagePopupFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/popup/MoovitPlusPackagePopupFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val MoovitPlusPurchaseFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusPurchaseFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val MoovitPlusPurchaseOffersFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusPurchaseOffersFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val MoovitPlusOnboardingPrePurchaseFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingPrePurchaseFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

val FreemiumPopupFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/feature/freemium/FreemiumPopupFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)
