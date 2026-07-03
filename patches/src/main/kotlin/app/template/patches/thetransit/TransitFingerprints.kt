package app.template.patches.thetransit

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * RoyaleStoreSubscriptionStatus.isActive() - getter for isActive field.
 * Force return true so any consumer reading this getter sees an active sub.
 */
val RoyaleIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/thetransitapp/droid/shared/model/cpp/royale/RoyaleStoreSubscriptionStatus;",
    name = "isActive",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * TransitLib.fetchSubscriptionStatus(Z,J) - checks native flag
 * "activate_royale_subscription" via isFeatureActivated(). If true, builds
 * a hardcoded always-active RoyaleStoreSubscriptionStatus and skips RevenueCat.
 * Forcing the result of that check to true takes this built-in unlocked path.
 */
val FetchSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "Lcom/thetransitapp/droid/shared/data/TransitLib;",
    name = "fetchSubscriptionStatus",
    parameters = listOf("Z", "J"),
    returnType = "V",
    strings = listOf("activate_royale_subscription")
)

/**
 * TransitApp.onCreate() - install PackageManager proxy spoofing the original
 * signing cert before Google Maps SDK reads it.
 */
val TransitAppOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/thetransitapp/droid/shared/TransitApp;",
    name = "onCreate",
    returnType = "V"
)
