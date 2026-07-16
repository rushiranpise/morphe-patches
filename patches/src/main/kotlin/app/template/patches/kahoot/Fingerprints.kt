package app.template.patches.kahoot

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Kahoot! v6.6.7 (no.mobitroll.kahoot.android) — No pairip/DRM
//
// Entitlement system — two layers:
//
// LAYER 1: Feature flags (hasFeature)
//   AccountManager.hasFeature(Feature)Z — gates all 140+ premium Feature enum values
//   AccountManager.hasFeature(String)Z  — same via feature name string
//   These control feature access but NOT the plan badge/label shown in UI.
//
// LAYER 2: Subscription plan display (drives "Free" label + "7 day trial" CTA)
//   AccountManager.getProductFromMostPremiumStandardSubscription()Product
//     → returns Product.BASIC ("Free") when no active subscription
//     → used by profile badge, compare plans screen, plan logo
//   AccountManager.hasActiveStandardSubscription()Z
//     → false when no subscription → shows "Start 7 day trial" CTA everywhere
//   AccountManager.hasActiveStandardSubscriptionMatchingAppAndDeviceAppStore()Z
//     → same but filtered to current store — also drives trial CTA
//   AccountManager.getPlanLogoType()String
//     → returns null/"free" → used for plan badge rendering
//
// Fix: patch all 4 Layer-2 methods to return UNIVERSAL_GOLD / true / "gold"
// in addition to the existing hasFeature patches.

private const val AM = "Lno/mobitroll/kahoot/android/account/AccountManager;"
private const val PRODUCT = "Lno/mobitroll/kahoot/android/account/billing/Product;"
private const val FEATURE = "Lno/mobitroll/kahoot/android/account/Feature;"

// ── Layer 1: Feature flags ───────────────────────────────────────────────────

val HasFeatureFingerprint = Fingerprint(
    returnType = "Z",
    parameters = listOf(FEATURE),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM && method.name == "hasFeature"
    },
)

val HasFeatureByNameFingerprint = Fingerprint(
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM && method.name == "hasFeature"
    },
)

// ── Layer 2: Subscription plan display ──────────────────────────────────────

// getProductFromMostPremiumStandardSubscription() → UNIVERSAL_GOLD
// Returns Product.BASIC ("Free") when no server subscription.
// UNIVERSAL_GOLD is the "Kahoot! Plus Gold (Personal)" tier.
val GetProductFingerprint = Fingerprint(
    returnType = PRODUCT,
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM &&
            method.name == "getProductFromMostPremiumStandardSubscription"
    },
)

// hasActiveStandardSubscription()Z → true
// Drives "Start 7 day trial" CTA; false = show trial button everywhere.
val HasActiveSubscriptionFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM && method.name == "hasActiveStandardSubscription"
    },
)

// hasActiveStandardSubscriptionMatchingAppAndDeviceAppStore()Z → true
// Store-filtered variant; also drives trial CTA and billing validation.
val HasActiveSubscriptionMatchingStoreFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM &&
            method.name == "hasActiveStandardSubscriptionMatchingAppAndDeviceAppStore"
    },
)

// getPlanLogoType()String → "gold"
// Returns null/"free" for free users; used to render plan badge icon.
val GetPlanLogoTypeFingerprint = Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    custom = { method, classDef ->
        classDef.type == AM && method.name == "getPlanLogoType"
    },
)
