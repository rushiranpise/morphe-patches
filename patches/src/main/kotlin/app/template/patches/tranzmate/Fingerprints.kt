package app.template.patches.tranzmate

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Application entry point ───────────────────────────────────────────────────
// Non-obfuscated, stable across versions.
// Moved to classes3 in v1801 (was classes in v5.197.0).
internal val MoovitApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/MoovitApplication;",
    name = "onCreate",
    returnType = "V",
)

// ── Subscription state gate ───────────────────────────────────────────────────
// classFingerprint resolves the obfuscated SharedPrefs wrapper that holds the
// "subscribed_skus" list. The inner b()Z method returns isSubscribed.
// "subscribed_skus" is a stable, non-obfuscated key.
// v5.197.0: (unknown), v1801: Lccf;
internal val SubscriptionStateFingerprint = Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("subscribed_skus"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

// ── Ad unit resolver ──────────────────────────────────────────────────────────
// Returns the ad unit ID String for a given AdSource. Returning "" suppresses
// all ads. "is_ads_free_version" is a stable remote-config key.
// Smali verified (v1801, classes3/rha.smali, method f):
//   .method public final f(Lcom/moovit/app/ads/AdSource;)Ljava/lang/String;
//   const-string v1, "is_ads_free_version"
// Note: was named g() in v5.197.0; renamed to f() in v1801.
// Fingerprinted by stable param type + string — not method name.
internal val AdUnitResolverFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lcom/moovit/app/ads/AdSource;"),
    strings = listOf("is_ads_free_version"),
)

// ── Ad view suppression ───────────────────────────────────────────────────────
// Non-obfuscated class and method names — stable across versions.
// MoovitAdView moved to classes6 in v1801; MoovitBannerAdView in classes3.
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
// Non-obfuscated wrapper class; returns the SubscriptionPackageState enum.
// Stable class path and method name across versions.
internal val SubscriptionPackageStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/a;",
    name = "b",
    returnType = "Lcom/moovit/app/subscription/premium/packages/SubscriptionPackageState;",
    parameters = emptyList(),
)

// ── SafeRide feature gate ─────────────────────────────────────────────────────
// Non-obfuscated package path; returns SubscriptionPackageState enum.
internal val SafeRideCalculateStateFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/subscription/premium/packages/safety/b;",
    name = "a",
    returnType = "Ljava/lang/Enum;",
    parameters = listOf("Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
)

// ── Paywall gate ──────────────────────────────────────────────────────────────
// Returns true when the paywall should block navigation.
// Obfuscated class; anchored by stable "block_paywall" remote-config key string.
// Class rename history:
//   v5.196: Ly81    v5.197.0: Lw81    v1801: Ljh1
internal val BlockPaywallGateFingerprint = Fingerprint(
    name = "a",
    returnType = "Z",
    parameters = listOf("Lcom/moovit/MoovitActivity;"),
    filters = listOf(
        string("block_paywall"),
    ),
)

// ── Paywall / onboarding activity suppression ─────────────────────────────────
// Non-obfuscated class paths — stable across versions.
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

// ── Subscription / promo UI suppression ──────────────────────────────────────
// All non-obfuscated class paths. Fragment DEX locations may move between
// classes and classes3/6 but the class descriptors remain stable.
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
// Obfuscated flow emitter. Reads view_blocked_smart_tips_banner resource then
// shows an upsell banner when the user is not subscribed.
// The MOVE_RESULT preceding the sget of view_blocked_smart_tips_banner feeds the
// isSmartTips boolean. Replacing it with const/4 v6, 0x0 routes the flow to
// the insight-banner path instead.
//
// Stable fingerprint strategy: fieldAccess to Li4d;->view_blocked_smart_tips_banner:I
// is the unique anchor — this field access only appears in this one emit() method
// across the entire app. definingClass is intentionally omitted so the fingerprint
// survives obfuscated class renames. The second parameter is also obfuscated; "L"
// is used as a wildcard.
//
// Class rename history (for reference only — no longer needed in fingerprint):
//   v5.196: Lmy6  v5.197.0: Liy6  v1801: Lwq7
internal val ItineraryBlockedSmartTipsBannerFingerprint = Fingerprint(
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "L"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET,
            definingClass = "Li4d;",
            name = "view_blocked_smart_tips_banner",
            type = "I",
        ),
    ),
)

// ── MyMoovitPlus go-premium card ─────────────────────────────────────────────
// Obfuscated flow emitter toggling the "Go Premium" card visibility.
// v7 != 0 → GONE (subscribed). We replace the VISIBLE branch's "move v9, v2"
// with "move v9, v3" so the card is always GONE.
//
// Constants (smali verified, v1801, classes6/r1b.smali):
//   const/4 v2, 0x0   → VISIBLE
//   const/16 v3, 0x8  → GONE
//   if-nez v7, :cond_47   ← v7=isPremium
//   move v9, v2           ← NOT premium branch: replace with move v9, v3
//   goto :goto_48
//   move v9, v3           ← premium branch (already GONE)
//   invoke setVisibility(v9)
//
// Stable fingerprint strategy: "goPremiumCard" string is the unique anchor —
// it only appears in this one emit() method across the entire app (verified v1801).
// definingClass is intentionally omitted to survive obfuscated class renames.
// The second parameter is also obfuscated; "L" is used as a wildcard.
//
// Class rename history (for reference only — no longer needed in fingerprint):
//   v5.196: Lzy9  v5.197.0: Lbz9  v1801: Lr1b
internal val MyMoovitPlusGoPremiumCardFingerprint = Fingerprint(
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "L"),
    strings = listOf("goPremiumCard"),
)

// ── FavoriteLocationEditorActivity — address search enabler ──────────────────
// The "Add Favorite Location" search hardcodes c=false (p5 = 0x0) when
// constructing AppSearchLocationCallback. This disables the qn5 geocode
// address provider, so only transit stops appear — exact addresses cannot
// be set as favorites.
//
// Fix: replaceInstruction at index 5 (const/4 v5, 0x0 → const/4 v5, 0x1).
// This is a Moovit upstream premium gate — not caused by our patches.
//
// Fingerprint: non-obfuscated definingClass + unique "favorites_editor" string
// (only appears in this one method across the entire app, verified v1801).
internal val FavoriteLocationAddressSearchFingerprint = Fingerprint(
    definingClass = "Lcom/moovit/app/home/dashboard/FavoriteLocationEditorActivity;",
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        string("favorites_editor"),
    ),
)
