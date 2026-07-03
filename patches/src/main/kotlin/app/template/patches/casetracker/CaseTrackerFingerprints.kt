package app.template.patches.casetracker

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * SharedPreferences boolean getter — data/c.a(String, boolean)Z.
 *
 * Used by ads/x.<clinit> to populate the initial z state (isPro, isPlus, isAdsRemoved)
 * from SharedPreferences on every app launch. Forcing it to always return true
 * causes all three purchase flags to be read as true at startup.
 */
val SharedPrefGetBooleanFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/data/c;",
    name = "a",
    parameters = listOf("Ljava/lang/String;", "Z"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * isPro getter — ads/x.a()Z.
 *
 * Reads z.b (isPro boolean) from the MutableStateFlow<z> held in ads/x.b.
 * Callers gate Pro-tier features on this method's return value.
 * Replacing it with `return true` unlocks all Pro gating without touching the StateFlow.
 */
val IsProGetterFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/ads/x;",
    name = "a",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * isSubscribed getter — ads/x.c()Z.
 *
 * Reads z.a (isSubscribed — true when Pro OR Plus OR AdsRemoved) from the
 * MutableStateFlow<z>. Used as a general "has any paid tier" gate.
 * Replacing it with `return true` covers any remaining generic subscription checks.
 */
val IsSubscribedGetterFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/ads/x;",
    name = "c",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * refreshFromBackend coroutine — ads/x.e(ContinuationImpl)Object.
 *
 * The suspend fun that contacts the Revenew/billing backend and calls
 * MutableStateFlow.setValue(z) with the live subscription state.
 * If the backend returns Unsubscribed it would overwrite the flags set by
 * the SharedPrefs patch. Fingerprinted by the vendorId const-strings
 * ("pro", "plus", "ads") and all three HAS_PURCHASED_ keys that appear
 * in its body when it writes results back to SharedPrefs.
 *
 * Prepending return-void prevents any network result from resetting the state.
 */
val RefreshFromBackendFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/ads/x;",
    filters = listOf(
        string("pro"),
        string("plus"),
        string("ads"),
        string("HAS_PURCHASED_PRO_PLAN"),
        string("HAS_PURCHASED_PLUS_PLAN"),
        string("HAS_PURCHASED_REMOVE_ADS")
    )
)
