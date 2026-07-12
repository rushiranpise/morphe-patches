package app.template.patches.rsexplorer

import app.morphe.patcher.Fingerprint

private const val BILLING_MANAGER = "Lcom/edili/filemanager/billing/BillingManager;"
private const val IJ7 = "Ledili/ij7;"

/**
 * BillingManager.p()Z — main isSubscribed gate.
 *
 * Chain: p() → if ij7.u()=true OR ij7.v()=true → return true
 *              else → r31.f(Context) → return true/false
 *
 * ij7.u()Z = !TextUtils.isEmpty(l()) — subscription token present
 * ij7.v()Z = time-based 3-day trial window check ("key_p_r_encrypt_st")
 * r31.f(Context) = DO NOT PATCH — dual-purpose: checks r31.b (premium flag)
 *                  AND falls through to r31.e() = isSystemDarkTheme() via UiModeManager.
 *                  Patching r31.f()=true forces ALL theme/TV-layout checks to dark mode → UI breaks.
 */
internal val IsSubscribedFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == BILLING_MANAGER && method.name == "p"
    }
)

/**
 * edili/ij7.u()Z — subscription token non-empty check.
 *
 * Returns !TextUtils.isEmpty(l()) where l() = cached subscriptionId.
 * First gate in BillingManager.p(). Patching covers active subscription path.
 */
internal val SubscriptionTokenCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == IJ7 && method.name == "u"
    }
)
