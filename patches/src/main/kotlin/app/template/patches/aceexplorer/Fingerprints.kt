package app.template.patches.aceexplorer

import app.morphe.patcher.Fingerprint

private const val SUB_MANAGER = "Lcom/ace/fileexplorer/billing/SubscriptionManager;"
private const val TQ7 = "Lace/tq7;"

/**
 * SubscriptionManager.p()Z — main isSubscribed gate (all callers).
 *
 * Chain: p() → if vn3.a()=true → return true (test flag, always false in prod)
 *              else → tq7.u()Z = !TextUtils.isEmpty(l())  where l() = subscriptionId string
 * Patching p() directly covers all call sites.
 * Matched by custom class+method (unique singleton access pattern).
 */
internal val IsSubscribedFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == SUB_MANAGER && method.name == "p"
    }
)

/**
 * ace/tq7.u()Z — subscriptionId non-empty check (underlying VIP state).
 *
 * Returns !TextUtils.isEmpty(l()) where l() returns the cached subscriptionId string.
 * Called by SubscriptionManager.p() when vn3.a()=false (production path).
 * Patching as belt-and-suspenders in case p() is inlined by R8.
 */
internal val SubscriptionIdCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == TQ7 && method.name == "u"
    }
)
