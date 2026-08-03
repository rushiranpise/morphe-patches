package app.template.patches.psiphon

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Psiphon Pro — subscription fingerprints ──────────────────────────────────
//
// The billing layer is R8-obfuscated. All fingerprints below anchor ONLY on
// stable, non-obfuscated identifiers:
//
//   • "SubscriptionState{status="  — kotlinc-emitted toString() literal in the
//                                    concrete SubscriptionStateImpl class (B2/c).
//                                    Verified unique: exactly one smali file
//                                    across the entire DEX set contains this string.
//
//   • "Lcom/android/billingclient/api/Purchase;"  — Play Billing SDK class path,
//                                    never obfuscated by R8.
//
//   • fieldAccess(type=Purchase, IGET_OBJECT)  — concrete getter reads the
//                                    Purchase field; no other zero-param public
//                                    method in the same class reads a Purchase.
//
//   • custom{} on hasValidPurchase  — the ONLY public non-abstract zero-param
//                                    boolean method in the codebase that dispatches
//                                    the same virtual call exactly three times.
//                                    Verified unique: 1/38 zero-param-boolean
//                                    methods with 3 invoke-virtual satisfy the
//                                    "same target × 3" constraint (python scan).
//
// Obfuscated identifiers NOT used: B2/c, B2/j0, LB2/j0$a;, method names h/g/c.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Locates the SubscriptionStateImpl class via its toString() constant.
 *
 * Used as a classFingerprint anchor — identifies the concrete class (B2/c in
 * v474) without naming it.
 */
private val subscriptionStateImplClassFingerprint = Fingerprint(
    strings = listOf("SubscriptionState{status="),
)

/**
 * SubscriptionStateImpl.getStatus() — the single source of truth for
 * subscription status.  Returns the obfuscated status enum value stored in the
 * instance field.
 *
 * Patch body replaces the return value with HAS_UNLIMITED_SUBSCRIPTION via
 * Enum.valueOf("HAS_UNLIMITED_SUBSCRIPTION") on the same enum class — no
 * obfuscated field reference needed.
 *
 * Stable anchors:
 *   - classFingerprint on "SubscriptionState{status=" → narrows to B2/c
 *   - custom{}: PUBLIC, non-static, non-abstract, zero params, returns anything
 *     that is NOT Purchase and NOT primitive/void → only the status getter qualifies
 *
 * Smali verified v474: B2/c.h()LB2/j0$a; — iget-object + return-object (2 insns)
 */
val GetSubscriptionStatusFingerprint = Fingerprint(
    classFingerprint = subscriptionStateImplClassFingerprint,
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, _ ->
        // Exclude: Purchase getter (has Purchase fieldAccess filter handled separately),
        // primitive/void returns (equals, hashCode etc.), static and abstract methods.
        val rt = method.returnType
        rt != "V" && rt != "Z" && rt != "I" &&
        rt != "Lcom/android/billingclient/api/Purchase;" &&
        method.parameters.isEmpty() &&
        method.accessFlags and AccessFlags.STATIC.value == 0 &&
        method.accessFlags and AccessFlags.ABSTRACT.value == 0
    },
)

/**
 * SubscriptionStateImpl.getPurchase() — returns the stored Purchase object.
 *
 * When getStatus() is forced to HAS_UNLIMITED_SUBSCRIPTION, call sites that
 * then call getPurchase().getProducts().get(0) (to build a Play Store manage-
 * subscription URL in the subscription-info Fragment) will NPE/IOOB if this
 * returns null.  The patch body constructs a minimal well-formed Purchase with
 * the required JSON fields so getProducts() returns a non-empty list.
 *
 * Stable anchors:
 *   - classFingerprint on "SubscriptionState{status=" → narrows to B2/c
 *   - returnType = Purchase SDK class (never obfuscated)
 *   - fieldAccess(IGET_OBJECT, type=Purchase) uniquely identifies this getter
 *     within the class (getStatus reads an obfuscated enum type, not Purchase)
 *
 * Smali verified v474: B2/c.g()Lcom/android/billingclient/api/Purchase; —
 *   iget-object v0, p0, LB2/c;->c:Lcom/android/billingclient/api/Purchase;
 *   return-object v0
 *
 * Purchase constructor: <init>(String purchaseJson, String signature)
 *   - Parses purchaseJson into a JSONObject stored in field c.
 *   - getProducts() (b()) calls private h() which reads JSONArray "productIds"
 *     from the JSON — must be present and non-empty for .get(0) not to throw.
 *   - Verified: Purchase.<init>(String, String) is the only constructor.
 */
val GetPurchaseFingerprint = Fingerprint(
    classFingerprint = subscriptionStateImplClassFingerprint,
    returnType = "Lcom/android/billingclient/api/Purchase;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Lcom/android/billingclient/api/Purchase;",
        ),
    ),
)

/**
 * SubscriptionState.hasValidPurchase() — the ad-gating predicate.
 *
 * Declared on the abstract class (B2/j0 in v474); calls getStatus() three
 * times and returns true if the result is HAS_LIMITED_SUBSCRIPTION,
 * HAS_UNLIMITED_SUBSCRIPTION, or HAS_TIME_PASS.  Used by AdManager and the
 * abstract ad-controller class to decide whether to show ads.
 *
 * Stable anchors:
 *   - returnType = "Z", parameters = empty, PUBLIC, non-abstract
 *   - custom{}: the ONLY public non-abstract zero-param boolean method in the
 *     entire DEX set that dispatches the same virtual call exactly three times
 *     to the same target.  Python scan across all 12,131 + 418 + 1,449 smali
 *     files confirmed exactly 1 match.
 *
 * Smali verified v474: B2/j0.c()Z —
 *   invoke-virtual {p0}, LB2/j0;->h()LB2/j0$a;  (× 3, same target each time)
 *   compares against enum fields e/f/g, returns 1 if any match
 */
val HasValidPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, _ ->
        if (method.accessFlags and AccessFlags.ABSTRACT.value != 0) return@Fingerprint false
        if (method.accessFlags and AccessFlags.STATIC.value != 0) return@Fingerprint false
        val impl = method.implementation ?: return@Fingerprint false
        // Collect every invoke-virtual target descriptor in the method body.
        val virtualTargets = impl.instructions
            .filterIsInstance<com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction35c>()
            .filter { it.opcode == Opcode.INVOKE_VIRTUAL }
            .map {
                (it.reference as? com.android.tools.smali.dexlib2.iface.reference.MethodReference)
                    ?.let { r -> "${r.definingClass}->${r.name}(${r.parameterTypes.joinToString("")})${r.returnType}" }
                    ?: ""
            }
            .filter { it.isNotEmpty() }
        // Must call the same target exactly 3 times and have exactly 3 invoke-virtual total.
        virtualTargets.size == 3 && virtualTargets.toSet().size == 1
    },
)
