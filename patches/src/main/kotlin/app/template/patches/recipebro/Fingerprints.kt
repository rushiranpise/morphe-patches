package app.template.patches.recipebro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── RevenueCat SDK — stable, non-obfuscated ───────────────────────────────────

// ── 1. RevenueCat EntitlementInfo.isActive() ─────────────────────────────────
//
// The per-product isActive gate in the JVM RC layer. Called by ne.e() (the
// app's internal RC→app model converter) to set je1.b:Z on each entitlement.
// Simple iget-boolean → return; no try-catch.
//
// Smali verified (v1.7.21, classes.dex):
//   .method public final isActive()Z
//   iget-boolean p0, p0, L.../EntitlementInfo;->isActive:Z
//   return p0
internal val EntitlementInfoIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── 2. RevenueCat kmp EntitlementInfo.isActive() ─────────────────────────────
//
// Same gate in the KMP bridge layer. ne.e() calls this when converting
// kmp EntitlementInfo objects into the app's internal je1 model.
//
// Smali verified (v1.7.21, classes.dex):
//   .method public final isActive()Z
//   iget-boolean p0, p0, L.../kmp/models/EntitlementInfo;->isActive:Z
//   return p0
internal val KmpEntitlementInfoIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/kmp/models/EntitlementInfo;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── App internal layer — obfuscated but stable via method-call filter ─────────
//
// Premium check flow (traced from UI → billing):
//   p12.invokeSuspend / pl0 reads gt0.a (Set<String> activeSubscriptions)
//   → calls Collection.isEmpty() on it
//   → if empty → shows paywall; if non-empty → premium granted
//
//   gt0 is built by ne.d(kmp CustomerInfo) which calls:
//     kmp CustomerInfo.getActiveSubscriptions() → stored as gt0.a (Set field)
//
//   ne.d() is the RC→app model converter. Injecting a non-empty singleton set
//   into v1 at the top of ne.d() overrides whatever getActiveSubscriptions()
//   returns and ensures gt0.a.isEmpty() is always false downstream.
//
// ── 3. ne.d() — app-internal RC→CustomerInfo converter ───────────────────────
//
// ne.d(kmp CustomerInfo) builds the app's gt0 internal model from the RC KMP
// CustomerInfo. The first call it makes is getActiveSubscriptions(), whose
// result (v1) is stored verbatim into gt0.a. We inject a singleton set at
// index 0 so v1 is always non-empty before anything else runs.
//
// Why obfuscated class is used here:
//   ne.d() has a unique and stable signature:
//     public static (kmp CustomerInfo) → gt0
//   Both kmp CustomerInfo and gt0 are uniquely paired; no other method in
//   the DEX has this exact (input type → return type) combination.
//   The methodCall filter on getActiveSubscriptions pins it further.
//
// Smali verified (v1.7.21, classes.dex, Lne;->d):
//   .method public static d(Lcom/revenuecat/purchases/kmp/models/CustomerInfo;)Lgt0;
//   .registers 8
//   invoke-virtual {p0}, L.../kmp/models/CustomerInfo;->getActiveSubscriptions()Set
//   move-result-object v1
//   ...  (builds ke1 entitlement map, constructs gt0)
//   return-object v0
internal val CustomerInfoConverterFingerprint = Fingerprint(
    returnType = "Lgt0;",
    parameters = listOf("Lcom/revenuecat/purchases/kmp/models/CustomerInfo;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/kmp/models/CustomerInfo;",
            name = "getActiveSubscriptions",
        ),
    ),
)

// ── FeatureFlagResponse constructor — unlock all feature flags ────────────────
//
// nh1 is the app's FeatureFlagResponse data class. Its boolean fields gate
// individual features:
//   a:Z = showPremiumAfterOnboarding
//   e:Z = showCategories
//   f:Z = showNewRecipes
//   g:Z = showCookbookSharing   ← used by pl0 state-1 as the isPro gate
//   h:Z = showMealPlan
//
// pl0.invokeSuspend has two coroutine states:
//   State 0: CustomerInfo path → ne.d() → gt0.a.isEmpty() (patched via ne.d)
//   State 1: FeatureFlag path → y84.d() → reads nh1.g:Z as isPro boolean
//
// Patching the real constructor to overwrite all Z fields with true after
// the normal assignments ensures every feature flag and the isPro gate
// (nh1.g) report true regardless of what the server sends.
//
// The real constructor (not the synthetic default-param variant) is called
// by y84 at runtime when building nh1 from the server response.
//
// Smali verified (v1.7.21, classes.dex, Lnh1;-><init>(ZIJIZZZZMap)V):
//   p1→a:Z, p2→b:I, p3/p4→c:J, p5→d:I, p6→e:Z, p7→f:Z, p8→g:Z, p9→h:Z
//   No try-catch blocks.
internal val FeatureFlagResponseConstructorFingerprint = Fingerprint(
    definingClass = "Lnh1;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Z", "I", "J", "I", "Z", "Z", "Z", "Z", "Ljava/util/Map;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)
