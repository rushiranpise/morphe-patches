package app.template.patches.obdandroid.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Targets q5/Y.isAppPurchased()Z
// Root of the premium state machine — reads IS_APP_PURCHASED from SharedPreferences.
// q5/j0 (BillingManager) reads this in its constructor and stores the result in z:Z.
// Stable: non-obfuscated Vulcan SDK class + method names.
object IsAppPurchasedFingerprint : Fingerprint(
    definingClass = "Lq5/Y;",
    name = "isAppPurchased",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
    )
)

// Targets q5/j0.d(Z)V — the master purchase state setter.
// Called by queryPurchasesAsync callbacks, onPurchasesUpdated, and server verify results.
// When called with false (no active purchase), it sets z:Z=false and broadcasts to LiveData,
// overriding the value set via isAppPurchased() in the constructor.
// Filter: t5/c.k() — the "save purchased state to prefs" helper — unique to this method.
object SetPurchasedStateFingerprint : Fingerprint(
    definingClass = "Lq5/j0;",
    name = "d",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(
            definingClass = "Lt5/c;",
            name = "k"
        ),
    )
)

// Targets q5/j0.onBillingSetupFinished(BillingResult)V
// When BillingClient setup returns error codes (-2, 3, 5), sets z:Z=false directly
// bypassing setAppPurchased entirely. Filters on getResponseCode — unique to this method
// since the other BillingResult.getResponseCode call is in a different method signature.
object OnBillingSetupFinishedFingerprint : Fingerprint(
    definingClass = "Lq5/j0;",
    name = "onBillingSetupFinished",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/android/billingclient/api/BillingResult;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/android/billingclient/api/BillingResult;",
            name = "getResponseCode"
        ),
    )
)
