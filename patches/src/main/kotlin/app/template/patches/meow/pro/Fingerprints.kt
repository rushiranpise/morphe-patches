package app.template.patches.meow.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// BillingConfig.d()String — the VIP type resolver. Single source of truth.
//
// Smali (classes/com/glgjing/billing/BillingConfig.smali):
//   reads KEY_VIP_PERMANENT_VERIFIED (bool) → returns "sub_vip_permanent" if true
//   else reads KEY_VIP_TYPE from SharedPreferences (default "sub_vip_none")
//
// Cascades to everything:
//   BillingConfig.e()Z       calls d() → returns true if result != "sub_vip_none"
//   BillingConfig.a StateFlow initialized via d() in <clinit> → UI member type display
//   pig/ui/dialog/b.invoke() observes BillingConfig.a and maps:
//     "sub_vip_permanent" → R.string.vip_type_permanent ("Life Member")
//     "sub_vip_monthly"   → vip_type_monthly
//     "sub_vip_annual"    → vip_type_annual
//     else                → vip_type_unsubscribed
//
// returnEarly("sub_vip_permanent") makes e()=true AND shows "Life Member" in UI.
object VipTypeGetterFingerprint : Fingerprint(
    definingClass = "Lcom/glgjing/billing/BillingConfig;",
    name = "d",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    filters = listOf(
        string("KEY_VIP_PERMANENT_VERIFIED"),
        string("sub_vip_permanent"),
        string("KEY_VIP_TYPE"),
        string("sub_vip_none"),
    ),
)

// PairIP LicenseResponseHelper.validateResponse — RSA signature verifier.
//
// Smali (classes/com/pairip/licensecheck/LicenseResponseHelper.smali):
//   .method public static validateResponse(Landroid/os/Bundle;Ljava/lang/String;)V
//     invoke-static getJwsPartsForLicenseData(Bundle)[String  ← no strings in this method
//     ... base64ToJson x2, verifySignature, package name check ...
//
// Filters use the two private-static calls that appear sequentially inside the method.
object PairIpValidateResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
            name = "getJwsPartsForLicenseData",
        ),
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
            name = "verifySignature",
        ),
    ),
)
