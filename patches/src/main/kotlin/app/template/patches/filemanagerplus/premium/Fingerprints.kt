package app.template.patches.filemanagerplus.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Matches ax.k3.c.F(String category) -> boolean — primary isPremium gate.
 *
 * Custom RSA-license SDK. F() calls z(category) → y(lb.b) → compares result
 * enum with c$e.X (ACTIVE_SUBSCRIPTION) and c$e.Y (ACTIVE_ONETIME).
 * Requires valid RSA-signed token in SharedPrefs for the given category.
 *
 * Smali (classes/ax/k3/c.smali, line 2986):
 *   .method public F(Ljava/lang/String;)Z   .registers 3
 *   invoke-direct z(String)lb.b
 *   invoke-direct y(lb.b)c$e
 *   sget-object v0, c$e.X; if-eq p1, v0 → return true
 *   sget-object v0, c$e.Y; if-ne p1, v0 → return false
 *   return true
 *
 * Unique stable anchors: calls private y(lb.b)c$e; and private z(String)lb.b
 * combined with custom predicate on BillingClient field in class.
 */
object IsPremiumFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lax/k3/c;",
            name = "z",
        ),
        methodCall(
            definingClass = "Lax/k3/c;",
            name = "y",
        ),
    ),
    custom = { _, classDef ->
        classDef.fields.any { it.type == "Lcom/android/billingclient/api/a;" }
    },
)

/**
 * Matches ax.k3.c.H(String category) -> boolean — hasPremium with logging.
 *
 * Calls F() internally and logs "has premium:" + result. Also called from
 * the UI layer to display premium status to the user. Patching this ensures
 * consistent premium=true even in log paths.
 *
 * Smali (classes/ax/k3/c.smali, line 3041):
 *   .method public H(Ljava/lang/String;)Z
 *   if token null → log "LICENSETOKEN IS NULL" + call F() → return false
 *   else check productType == X (MANAGED/onetime) → return true
 *   else return false
 */
object HasPremiumWithLogFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("has premium:", "LICENSETOKEN IS NULL"),
)

/**
 * Matches ax.k3.c.l(String category) -> boolean — isSubscriptionActive.
 *
 * Returns true only for SUBSCRIPTION type licenses (not one-time).
 * Checks: token exists, productType==SUBSCRIPTION, licenseState != CANCEL.
 *
 * Smali (classes/ax/k3/c.smali, line 5923):
 *   getProductType() == Y (SUBSCRIPTION) && getLicenseState() != X (CANCEL)
 */
object IsSubscriptionActiveFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lax/nb/c;",
            name = "getProductType",
        ),
        methodCall(
            definingClass = "Lax/nb/c;",
            name = "getLicenseState",
        ),
    ),
    custom = { _, classDef ->
        classDef.fields.any { it.type == "Lcom/android/billingclient/api/a;" }
    },
)
