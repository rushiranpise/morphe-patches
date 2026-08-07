package app.template.patches.relane.premium

import app.morphe.patcher.Fingerprint

// ── Smali source: classes3.dex / com/revenuecat/purchases/ ───────────────────
// Verified against Relane VPN 8.0.0 (xorsand.relane, versionCode 16843307).
//
// Architecture: Flutter app (libapp.so = Dart business logic) with RevenueCat
// via purchases_flutter plugin. The Java DEX is only the method channel bridge.
// Dart reads subscription state from the CustomerInfo map produced by
// CustomerInfoMapperKt.mapAsync() → calls EntitlementInfos.getActive() internally.
// Entitlement identifier: "premium_vpn" (confirmed from libapp.so strings).
//
// Patch strategy: intercept at the two deepest stable points in the SDK —
// EntitlementInfos.getActive() returns a synthetic active entitlement, and
// EntitlementInfo.isActive() forces true on any instance — using only the
// existing register frames (v0 only) to avoid DEX verifier rejection.

/**
 * EntitlementInfos.getActive() — returns Map<String, EntitlementInfo> of only
 * currently active entitlements. CustomerInfoMapperKt.mapAsync() calls this to
 * build the "active" sub-map that Dart reads to check subscription status.
 * Patched to return a real EntitlementInfo object with "premium_vpn" active,
 * constructed fully within the existing .registers 2 frame (uses only v0).
 * Stable: non-obfuscated public method; part of RevenueCat core SDK.
 */
internal val entitlementInfosGetActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
    name = "getActive",
    returnType = "Ljava/util/Map;",
    parameters = emptyList(),
)

/**
 * EntitlementInfo.isActive() — boolean field getter.
 * Forces true on every EntitlementInfo instance — covers cached CustomerInfo reads
 * and the "all" entitlements map that also passes through the mapper.
 * Uses only v0; fits within the existing .registers 2 frame.
 * Stable: non-obfuscated public method; part of RevenueCat core SDK.
 */
internal val entitlementInfoIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
)
