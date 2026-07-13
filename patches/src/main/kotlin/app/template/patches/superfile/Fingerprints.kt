package app.template.patches.superfile

import app.morphe.patcher.Fingerprint

private const val SUB_MANAGER = "Lcom/frames/filemanager/billing/SubscriptionManager;"
private const val IH7 = "Lframes/ih7;"

/**
 * SubscriptionManager.m()Z — main isSubscribed gate (34 callers).
 *
 * Chain: m() → frames/ih7.q()Z = !isEmpty(m("key_p_encrypt_st", ""))
 * Same pattern as Ace Ex / RS File Manager (same developer family, "frames" package).
 * Lifetime product id: "idesuper_lifetime" (one-time Play Billing purchase).
 * No pairip. Analytics: Firebase Crashlytics only (AppMetrica SDK bundled but not app-activated).
 */
internal val IsSubscribedFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == SUB_MANAGER && method.name == "m"
    }
)

/**
 * frames/ih7.q()Z — subscription token non-empty check.
 *
 * Returns !TextUtils.isEmpty(m("key_p_encrypt_st", "")) where m() reads the cached
 * subscription token from SharedPreferences.
 * Belt-and-suspenders alongside SubscriptionManager.m().
 */
internal val SubscriptionTokenCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("key_p_encrypt_st"),
    custom = { method, classDef ->
        classDef.type == IH7 && method.name == "q"
    }
)
