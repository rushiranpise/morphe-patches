package app.template.patches.batterypods.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

object BatteryPodsIsPurchasedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("PURCHASED_ITEM_NO_ADS"),
        methodCall(definingClass = "Landroid/content/SharedPreferences;", name = "getBoolean"),
    ),
)

// Verified in the supplied 8.32 base.apk: this class has a unique, public final
// onCreate(Bundle):void method. It no longer invokes MobileAds.a().
object BatteryPodsMainActivityOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lcom/sumyapplications/bluetoothearphone/MainActivity;",
)

// 8.32 BatteryPodsApplication.onCreate():void — flags are loaded here before
// MainActivity.onCreate. Verified to have 8 registers and 1 incoming register.
object BatteryPodsApplicationOnCreateFingerprint : Fingerprint(
    name = "onCreate",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    definingClass = "Lcom/sumyapplications/bluetoothearphone/BatteryPodsApplication;",
)

// 8.32 MainActivity.Q(Boolean):void — writes PURCHASED_ITEM_NO_ADS on
// billing callbacks and could replace the true value with false.
object BatteryPodsBillingStatusUpdateFingerprint : Fingerprint(
    name = "Q",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Boolean;"),
    definingClass = "Lcom/sumyapplications/bluetoothearphone/MainActivity;",
    filters = listOf(string("PURCHASED_ITEM_NO_ADS")),
)

// 8.32 Lf6.b():int is the existing ad-eligibility function.
// Return value 1 corresponds to PURCHASED_ITEM_NO_ADS=true.
object BatteryPodsAdEligibilityFingerprint : Fingerprint(
    name = "b",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    definingClass = "Lf6;",
    filters = listOf(string("PURCHASED_ITEM_NO_ADS")),
)

object PairIPValidateResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    filters = listOf(
        string("RS256"),
        methodCall(definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;", name = "verifySignature"),
    ),
)

object PairIPProcessResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
    filters = listOf(
        string("License check succeeded."),
        methodCall(definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;", name = "getRepeatedCheckMetadata"),
    ),
)

object PairIPGetRepeatedCheckMetadataFingerprint : Fingerprint(
    returnType = "Lcom/pairip/licensecheck/RepeatedCheckMetadata;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "getRepeatedCheckMetadata",
)
