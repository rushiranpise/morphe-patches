package app.template.patches.casetracker

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * DataManager initialiser — the static block that reads SharedPreferences
 * purchase flags into the in-memory boolean fields:
 *
 *   c.z0  ← HAS_PURCHASED_REMOVE_ADS
 *   c.A0  ← HAS_PURCHASED_PRO_PLAN
 *   c.B0  ← HAS_PURCHASED_PLUS_PLAN
 *
 * Fingerprinted by the three consecutive const-string / invoke-static /
 * sput-boolean sequences that populate those fields on startup.
 * We target the helper a(String, boolean)Z that wraps getBoolean().
 */
val DataManagerInitFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/data/c;",
    // The init coroutine that calls c.a("HAS_PURCHASED_...", false) x3
    filters = listOf(
        string("HAS_PURCHASED_REMOVE_ADS"),
        string("HAS_PURCHASED_PRO_PLAN"),
        string("HAS_PURCHASED_PLUS_PLAN")
    )
)

/**
 * SharedPreferences boolean getter — a(String, boolean)Z.
 *
 * This is the single method that reads every SharedPreferences boolean value
 * (including all three purchase flags).  Forcing it to always return true
 * means all purchase checks that go through it will see "purchased".
 *
 * Fingerprinted by:
 *  - definingClass + name (stable, not obfuscated further)
 *  - parameters and return type
 */
val SharedPrefGetBooleanFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/data/c;",
    name = "a",
    parameters = listOf("Ljava/lang/String;", "Z"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * SubscriptionManager purchase handler (inner class $1).
 *
 * This is the onSuccess callback that writes the three HAS_PURCHASED_* keys
 * to SharedPreferences AND updates the in-memory statics when a real
 * purchase completes.  Fingerprinted by all three vendor-id const-strings
 * that appear in the same method body.
 */
val SubscriptionManagerPurchaseHandlerFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/ads/SubscriptionManager\$1;",
    filters = listOf(
        string("HAS_PURCHASED_REMOVE_ADS"),
        string("HAS_PURCHASED_PLUS_PLAN"),
        string("HAS_PURCHASED_PRO_PLAN")
    )
)

/**
 * ads/w.a — the static method that writes purchase results back to the
 * DataManager statics AND SharedPreferences based on the vendorId string.
 *
 * Fingerprinted by the vendorId const-strings (MP199T0_ads, LP499T0_ads,
 * "pro", "plus") and the three HAS_PURCHASED_ keys.
 *
 * We prepend a block that immediately sets all three static fields to true
 * and writes all three SharedPrefs keys before any billing logic runs.
 */
val AdsWriterFingerprint = Fingerprint(
    definingClass = "Lcom/saldous/casetracker/ads/w;",
    name = "a",
    parameters = listOf(
        "Lcom/saldous/casetracker/ads/v;",
        "Ljava/lang/String;",
        "I"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * RevenewSDK — updateSubscriptionState(List)V.
 *
 * The internal method that parses the list of active subscription objects
 * returned from Google Play Billing / Purchasely and emits a new
 * SubscriptionState (Subscribed / Unsubscribed) into subscriptionObservable.
 *
 * Fingerprinted by definingClass + name — stable as the SDK class is not
 * obfuscated by R8 (it ships as a pre-compiled AAR).
 */
val RevenewUpdateStateFingerprint = Fingerprint(
    definingClass = "Lcom/library/revenew/RevenewSDK;",
    name = "updateSubscriptionState",
    returnType = "V"
)