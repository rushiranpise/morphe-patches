package app.template.patches.awake.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Awake v1.10.3 — premium + licence fingerprints ───────────────────────────
//
// TWO independent gating systems:
//
// ① RC Premium (RevenueCat) — gk/f chain
//    gk/f.b (isUserPremium):
//      Purchases.isAnonymous() → if true → loginUser (returns "not logged in")
//                              → if false → gk/f.a (checkPremiumEntitlement)
//    gk/f.a (checkPremiumEntitlement):
//      awaitCustomerInfo → getEntitlements().get("Premium").isActive() → Z
//
//    PATCH: in gk/f.b, replace move-result v2 (isAnonymous result) at [41]
//    with const/4 v2, 0x0 → always "not anonymous" → always calls gk/f.a()
//    PATCH: in gk/f.a, force isActive() result and null-path both to 1 (true)
//
// ② Pairip LicenseClient — Play LVL licence check
//    LicenseContentProvider.onCreate() → LicenseClient.initializeLicenseCheck()
//    → LicenseClient.processResponse(responseCode, bundle)
//    responseCode 0 → success; responseCode 2 → paywall; other → error/exit
//
//    PATCH: killPairIpFull() via shared utility — no-ops initializeLicenseCheck,
//    processResponse, startPaywallActivity. Also covers validateResponse (throws).
//
// ③ Belt+suspenders: fk/f1.<init> DataStore write lambda constructor
// ─────────────────────────────────────────────────────────────────────────────

/**
 * IsUserPremiumFingerprint — gk/f.b(Continuation) in v1.10.3.
 *
 * The isUserPremium gate. Calls Purchases.isAnonymous() at instruction [40];
 * [41] move-result v2 → [42] if-eqz v2 → anonymous=true goes to loginUser (returns false).
 *
 * Patch [41]: const/4 v2, 0x0 — forces "not anonymous" → always calls gk/f.a().
 *
 * Stable anchors:
 *   - string("isUserPremium: Checking if user is premium") — unique (1 match)
 *   - methodCall(Purchases.isAnonymous) — stable RC SDK class
 */
val IsUserPremiumFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lvs/c;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("isUserPremium: Checking if user is premium"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/Purchases;",
            name = "isAnonymous",
        ),
    ),
)

/**
 * CheckPremiumEntitlementFingerprint — gk/f.a(Continuation) in v1.10.3.
 *
 * Live RC entitlement check: awaitCustomerInfo → get("Premium") → isActive().
 * Patch [45] and [47] to force both paths to 1 (true).
 *
 * Stable anchors:
 *   - string("checkPremiumEntitlement: User is premium: ") — unique (1 match)
 *   - filters: getEntitlements → get → isActive (RC SDK, never obfuscated)
 */
val CheckPremiumEntitlementFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Lvs/c;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("checkPremiumEntitlement: User is premium: "),
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

/**
 * SetPremiumInvokeSuspendFingerprint — fk/f1.invokeSuspend in v1.10.3.
 * Used only to obtain the class for DataStore write lambda constructor patch.
 */
val SetPremiumInvokeSuspendFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("setPremium: premium: "),
)
