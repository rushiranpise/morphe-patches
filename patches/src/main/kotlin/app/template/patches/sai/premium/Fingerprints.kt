package app.template.patches.sai.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── STABILITY CONTRACT ─────────────────────────────────────────────────────────
// All fingerprints here use ONLY RevenueCat SDK method calls as filters.
// RevenueCat library classes/methods are NEVER renamed or obfuscated by R8 —
// they are part of a published SDK with stable binary API.
// The app wrapper classes (wr0, xr0, ur0) are obfuscated by R8 but are
// identified solely via their RevenueCat SDK call patterns, not by name.
// This makes fingerprints survive app updates as long as RevenueCat is used.
// ───────────────────────────────────────────────────────────────────────────────

// ── PURCHASE CALLBACK — entitlement check on purchase ──────────────────────────
// Targets the invoke(Object, Object)Object method in the purchase callback wrapper
// (currently wr0, R8-obfuscated — name changes every update).
//
// Called by RevenueCat after a successful purchase. The method:
//   1. Casts p2 to CustomerInfo
//   2. Calls CustomerInfo.getEntitlements()
//   3. Calls EntitlementInfos.get("SAI Premium")
//   4. Calls EntitlementInfo.isActive()
//   5. If active → grants premium; else → denies
//
// Strategy: returnEarly() — returns null (Object) before any entitlement check.
// Since the purchase was already made by RevenueCat, this callback only writes
// state; returning null-Object is safe (caller checks null via Kotlin nullability).
//
// Why update-stable:
//   - definingClass intentionally omitted — the obfuscated class name changes
//     every update. The fingerprint matches on returnType + accessFlags +
//     parameters + three sequential RevenueCat SDK calls only.
//   - CustomerInfo, EntitlementInfos, EntitlementInfo are RevenueCat library
//     classes — their names and method signatures are part of the published SDK
//     and never change.
//   - The three-filter sequence (getEntitlements → get → isActive) is highly
//     specific to this exact billing check pattern, ensuring unique match.
//
// Access flags: PUBLIC FINAL (Kotlin lambda invoke).
// DEX: classes4.dex — smali verified against versionCode 46.
internal val PurchaseCallbackFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "get",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "isActive",
        ),
    ),
)

// ── CUSTOMER INFO STATE WRITER ─────────────────────────────────────────────────
// Targets the g(CustomerInfo)V method in the RevenueCat state manager wrapper
// (currently xr0, R8-obfuscated — name changes every update).
//
// Called whenever CustomerInfo is updated (app start, purchase, restore).
// The method checks CustomerInfo.getEntitlements().get("SAI Premium").isActive()
// and persists the result to SharedPreferences / in-memory state.
// This is the gatekeeper that determines whether the app shows premium features.
//
// Strategy: returnEarly() — nops the entire persistence write. Since
// CustomerInfo is still available via RevenueCat's normal cache, UI rendering
// that reads it directly also needs to be covered by PurchaseCallbackFingerprint.
//
// Why update-stable:
//   - definingClass omitted (obfuscated class name changes every update).
//   - returnType=V, params=[CustomerInfo] — a CustomerInfo consumer method.
//   - Filters: same RevenueCat SDK triple (getEntitlements → get → isActive)
//     that uniquely identifies billing-related methods in this app.
//   - The specific parameter type CustomerInfo makes this distinct from
//     PurchaseCallbackFingerprint (Object, Object → Object).
//
// Access flags: PUBLIC FINAL.
// DEX: classes4.dex — smali verified against versionCode 46.
internal val CustomerInfoStateWriterFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "get",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "isActive",
        ),
    ),
)
