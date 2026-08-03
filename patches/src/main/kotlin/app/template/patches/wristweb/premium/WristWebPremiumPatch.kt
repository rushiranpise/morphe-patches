package app.template.patches.wristweb.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WRISTWEB_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * WristBrowser Premium Unlock
 *
 * WristBrowser uses RevenueCat for subscription management. The entitlement check
 * is centralised in a single static utility method (xu2.C) that is the sole gate
 * between the app UI and premium feature access:
 *
 *   CustomerInfo.getEntitlements() → getAll() → for each: isActive() → Z
 *
 * Returning true unconditionally makes every call site that passes a CustomerInfo
 * object to xu2.C() see an active subscription, enabling all premium features.
 *
 * This is the canonical RevenueCat bypass pattern: target the entitlement check
 * method rather than individual feature gates, so a single patch covers all
 * present and future premium features without needing per-feature fingerprints.
 *
 * Stability: The three-method call chain (getEntitlements → getAll → isActive) on
 * RevenueCat's SDK classes is kept by RevenueCat's own consumer ProGuard rules and
 * will remain stable across app updates as long as RevenueCat is used.
 */
@Suppress("unused")
val wristWebPremiumPatch = bytecodePatch(
    name = "WristBrowser Premium",
    description = "Unlocks all premium features by spoofing RevenueCat entitlement checks as active.",
    default = true,
) {
    compatibleWith(WRISTWEB_COMPATIBILITY)

    execute {
        WristWebEntitlementFingerprint.method.returnEarly(true)
    }
}
