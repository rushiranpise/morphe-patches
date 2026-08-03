package app.template.patches.wristweb.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets xu2.C(CustomerInfo)Z — the RevenueCat entitlement check.
 *
 * ARCHITECTURE:
 * WristBrowser (com.onnex.wristweb) uses RevenueCat for subscription management.
 * The class xu2 is a utility class (R8-generated synthetic static companion) whose
 * method C(CustomerInfo)Z implements the canonical RevenueCat entitlement check:
 *
 *   1. getEntitlements() → EntitlementInfos
 *   2. getAll() → Map<String, EntitlementInfo>
 *   3. if map is empty → return false (no subscription)
 *   4. iterate entries → for each: if isActive() == true → return true
 *   5. if none active → return false
 *
 * Returning true early short-circuits all four steps and makes every call site
 * see an active entitlement regardless of the user's subscription state.
 *
 * WHY xu2.C IS STABLE ENOUGH:
 * xu2 is obfuscated but the method's signature is unique:
 *   - Return type Z (boolean)
 *   - PUBLIC STATIC (R8 static utility companion pattern)
 *   - Single parameter: Lcom/revenuecat/purchases/CustomerInfo; (SDK class, never obfuscated)
 *   - Calls both getEntitlements() AND getAll() then isActive() — a combination that
 *     only ever appears in this specific entitlement-checking pattern
 *
 * The RevenueCat SDK classes (CustomerInfo, EntitlementInfos, EntitlementInfo) are
 * never renamed by R8 because they are referenced by name from the SDK's own ProGuard
 * consumer rules. The three-call chain is therefore a highly stable anchor.
 *
 * VERIFIED v1.1.8 (versionCode 19):
 *   classes/xu2.smali, method C, line 204:
 *     .method public static C(Lcom/revenuecat/purchases/CustomerInfo;)Z
 *     .registers 2
 *     const-string v0, "<this>"
 *     invoke-static {p0, v0}, Luz1;->c(Object;String;)V
 *     invoke-virtual {p0}, CustomerInfo;->getEntitlements()EntitlementInfos;  ← FILTER 1
 *     move-result-object p0
 *     invoke-virtual {p0}, EntitlementInfos;->getAll()Map;                    ← FILTER 2
 *     move-result-object p0
 *     invoke-interface {p0}, Map;->isEmpty()Z
 *     if-eqz v0, :cond_14
 *     goto :goto_36    # empty → return false
 *     :cond_14
 *     ... iterate ...
 *     invoke-virtual {v0}, EntitlementInfo;->isActive()Z                      ← FILTER 3
 *     if-eqz v0, :cond_1c
 *     const/4 p0, 0x1
 *     return p0        # any active → return true
 *     :cond_36
 *     const/4 p0, 0x0
 *     return p0        # none active → return false
 *
 * No try-catch blocks. Safe for returnEarly(true).
 * Access flags: PUBLIC STATIC. Return type: Z. Parameters: [CustomerInfo].
 */
object WristWebEntitlementFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "getAll",
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
            name = "isActive",
        ),
    ),
)
