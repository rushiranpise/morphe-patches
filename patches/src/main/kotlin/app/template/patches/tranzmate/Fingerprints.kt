package app.template.patches.tranzmate

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── Application entry point ───────────────────────────────────────────────────

internal val MoovitApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/MoovitApplication;",
    name = "onCreate",
    returnType = "V",
)

// ── Subscription state gate ───────────────────────────────────────────────────
//
// classFingerprint resolves the obfuscated SharedPreferences wrapper that holds
// the "subscribed_skus" list. The inner b()Z method returns isSubscribed.
// Both the class and method names are obfuscated but the string key is stable.
internal val SubscriptionStateFingerprint = Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("subscribed_skus"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

// ── Ad resolution ─────────────────────────────────────────────────────────────
//
// Returns the ad unit ID string for a given AdSource. Returning "" suppresses ads.
// "is_ads_free_version" is a stable remote-config key — not obfuscated.
//
// Smali verified (v5.197.0, classes3.dex, Lpg9;->g(AdSource)String):
//   const-string v1, "is_ads_free_version"
internal val AdUnitResolverFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
    strings = listOf("is_ads_free_version"),
)

// ── Ad view hide ──────────────────────────────────────────────────────────────

internal val MoovitAdViewSetSourceFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/ads/MoovitAdView;",
    name = "setAdSource",
    returnType = "V",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
)

internal val MoovitBannerAdViewSetSourceFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/ads/MoovitBannerAdView;",
    name = "setAdSource",
    returnType = "V",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
)

// ── Subscription package state ────────────────────────────────────────────────
//
// Non-obfuscated wrapper class; returns the SubscriptionPackageState enum.
// Both class path and method name stable across versions.
internal val SubscriptionPackageStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/a;",
    name = "b",
    returnType = "Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;",
    parameters = emptyList(),
)

// ── SafeRide feature gate ─────────────────────────────────────────────────────

internal val SafeRideCalculateStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/safety/b;",
    name = "a",
    returnType = "Ljava/lang/Enum;",
    parameters = listOf("Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
)

// ── Paywall / upgrade UI suppression ──────────────────────────────────────────
//
// BlockPaywallGate.a(MoovitActivity)Z — returns true when the paywall should
// block navigation. Class name obfuscated; pinned via stable "block_paywall" key.
//
// Class rename history:
//   v5.196: Ly81;   (classes)
//   v5.197: Lw81;   (classes3)
internal val BlockPaywallGateFingerprint = Fingerprint(
    name = "a",
    returnType = "Z",
    parameters = listOf("Lcom/moovit/MoovitActivity;"),
    filters = listOf(
        string("block_paywall"),
    ),
)

internal val BlockPaywallActivityOnReadyFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/paywall/BlockPaywallActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

internal val MoovitPlusOnboardingActivityFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

internal val MoovitPlusActivityOnReadyFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusActivity;",
    name = "onReady",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

// ── Subscription / promo UI hide ──────────────────────────────────────────────

internal val MoovitPlusHelpCenterMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusHelpCenterMenuItemFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val MoovitPlusMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusMenuItemFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val AdFreeMenuItemFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/AdFreeMenuItemFragment;",
    name = "onCreateView",
    returnType = "Landroid/view/View;",
    parameters = listOf("Landroid/view/LayoutInflater;", "Landroid/view/ViewGroup;", "Landroid/os/Bundle;"),
)

internal val PromoCellFragmentOnViewCreatedFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/MoovitSubscriptionsPromoCellFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val MoovitPlusPackagePopupFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/popup/MoovitPlusPackagePopupFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val MoovitPlusPurchaseFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusPurchaseFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val MoovitPlusPurchaseOffersFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/MoovitPlusPurchaseOffersFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val MoovitPlusOnboardingPrePurchaseFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/plus/onboarding/MoovitPlusOnboardingPrePurchaseFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

internal val FreemiumPopupFragmentFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/feature/freemium/FreemiumPopupFragment;",
    name = "onViewCreated",
    returnType = "V",
    parameters = listOf("Landroid/view/View;", "Landroid/os/Bundle;"),
)

// ── Itinerary smart tips upsell banner ────────────────────────────────────────
//
// Obfuscated flow emitter: reads view_blocked_smart_tips_banner resource,
// inflates a ViewStub, and shows an upsell banner when the user is not
// subscribed. Replacing the MOVE_RESULT that feeds the isSmartTips boolean
// with const/4 v6, 0x0 sends the flow down the insight-banner path instead,
// suppressing the smart tips upsell entirely.
//
// Class rename history:
//   v5.196: Lmy6;  params (Object, Llx2;)
//   v5.197: Liy6;  params (Object, Lix2;)  ← ix2 renamed from lx2
//
// Smali verified (v5.197.0, classes6.dex, Liy6;->emit(Object,Lix2;)Object):
//   invoke-static { v6 }, Llhd;->a(Lvp2;)Z
//   move-result v6                          ← opcode index 439 (0-based)
//   ...
//   sget v8, Lbwb;->view_blocked_smart_tips_banner:I
internal val ItineraryBlockedSmartTipsBannerFingerprint = Fingerprint(
    definingClass = "Liy6;",
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lix2;"),
)

// ── MyMoovitPlus go-premium card ─────────────────────────────────────────────
//
// Obfuscated flow emitter that toggles the "Go Premium" card visibility in the
// MyMoovitPlus fragment. The card is hidden (GONE) when the user is subscribed
// (v7 != 0) and shown (VISIBLE) when not. We replace the VISIBLE branch's
// "move v9, v2" with "move v9, v3" so the card is always GONE.
//
// Class rename history:
//   v5.196: Lzy9;  params (Object, Llx2;)
//   v5.197: Lbz9;  params (Object, Lix2;)  ← ix2 renamed from lx2
//
// Smali verified (v5.197.0, classes6.dex, Lbz9;->emit(Object,Lix2;)Object):
//   opcode 3:  const/4 v2, 0    ← VISIBLE constant
//   opcode 4:  const/16 v3, 8   ← GONE constant
//   opcode 34: const-string v0, "goPremiumCard"  ← null-check marker
//   opcode 37: if-nez v7, :L7   ← v7=isPremium; nez=notZero → card already hidden
//   opcode 38: move v9, v2      ← VISIBLE (isPremium=false) ← replace with move v9, v3
//   opcode 39: goto :L8
//   opcode 40: move v9, v3      ← GONE (isPremium=true)
//   opcode 41: invoke setVisibility(v9)
internal val MyMoovitPlusGoPremiumCardFingerprint = Fingerprint(
    definingClass = "Lbz9;",
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lix2;"),
    strings = listOf("goPremiumCard"),
)
