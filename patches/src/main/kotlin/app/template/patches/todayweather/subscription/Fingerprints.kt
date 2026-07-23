package app.template.patches.todayweather.subscription

import app.morphe.patcher.Fingerprint

/**
 * Targets Bd.k()Z — the primary purchase check.
 * Iterates the static ArrayLists f (in-app SKUs) and g (subscription SKUs),
 * reading SharedPreferences keys "purchased_file_google_inapp" and
 * "purchased_file_google_sub" for each SKU. Returns true only when any
 * SKU has value == 1 (purchased). Patched to always return true.
 */
internal object BdIsPurchasedFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "LAd;" &&
            method.name == "k" &&
            method.parameters.isEmpty()
    }
)

/**
 * Targets Bd.j()Z — the "sale-off" / trial gate.
 * Returns true if days since install > 5 AND SharedPref "prefIsOffSale" is true.
 * Used for showing interstitials and restricting features after the trial window.
 * Patched to always return false (no trial restriction).
 */
internal object BdIsTrialRestrictedFingerprint : Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "LAd;" &&
            method.name == "j" &&
            method.parameters.isEmpty()
    }
)
