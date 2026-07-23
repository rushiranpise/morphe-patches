package app.template.patches.materialcapsule.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Pairip LicenseClient-only variant — no VMRunner/SignatureCheck/native lib.
// Only classes2/com/pairip/licensecheck/ is present.
//
// 4-target bypass covering all entry points and failure paths.

// checkLicense — public static entry point called from Application.onCreate().
// Stable filter: calls isIsolatedProcess() as first check.
object CheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "isIsolatedProcess",
        ),
    ),
)

// initializeLicenseCheck — instance method that starts the async license flow.
// Stable filter: calls connectToLicensingService().
object InitializeLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "connectToLicensingService",
        ),
    ),
)

// processResponse — handles Play LVL response codes (0=LICENSED, 1=NOT_LICENSED, etc.).
// Verified smali (classes2) — no isForeground() call. Actual instruction order:
//   const/4 v0, 0x3 / if-eq p1, v0           ← responseCode==3 guard
//   if-nez p1, :cond_1                         ← responseCode==0 (LICENSED) path
//   sget-object p1, LicenseClient;->packageName
//   invoke-static {p2,p1}, LicenseResponseHelper;->validateResponse(Bundle,String)V ← filter[0]
//   ...
//   invoke-direct {p0,p1}, LicenseClient;->startPaywallActivity(PendingIntent)V     ← filter[1]
//
// Both calls are present in the method body in this order.
object ProcessLicenseResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
            name = "validateResponse",
        ),
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "startPaywallActivity",
        ),
    ),
)

// startPaywallActivity — launches the "not licensed" overlay / exit dialog.
// returnEarly() suppresses the app-kill flow even if processResponse fires.
object StartPaywallActivityFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Landroid/app/PendingIntent;"),
)


// DataStoreRepositoryBilling.saveProStatus(isPro: Boolean, cont: Continuation) 
//
// Verified smali (classes6, line 905). Key instruction sequence:
//
//   :cond_2  ← coroutine label==0 (fresh call)
//     invoke-direct {p0}, DataStoreRepositoryBilling;->getOrCreateSecretKey()   ← filter[0]
//     move-result-object p2
//     if-eqz p1, :cond_3          ← p1 = isPro boolean
//     const-string v2, "yes"       ← filter[1]   true branch
//     goto :goto_1
//     :cond_3
//     const-string v2, "no"        ← not in filter (after branch)
//     :goto_1
//     invoke-direct {p0,v2,p2}, DataStoreRepositoryBilling;->encrypt(String,SecretKey)  ← filter[2]
//
// Note: Boolean is NOT converted via valueOf()/toString() — it's branched
// to literal "yes"/"no" strings. Previous filters were wrong.
//
// Patch strategy: inject at index 0 to set p1=1 (true) before the coroutine
// state-machine dispatch — the "yes" branch will always be taken.
object SaveProStatusFingerprint : Fingerprint(
    definingClass = "Lcom/pryshedko/mtisland/model/datastore/DataStoreRepositoryBilling;",
    name = "saveProStatus",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Z", "Lkotlin/coroutines/Continuation;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pryshedko/mtisland/model/datastore/DataStoreRepositoryBilling;",
            name = "getOrCreateSecretKey",
        ),
        string("yes"),
        methodCall(
            definingClass = "Lcom/pryshedko/mtisland/model/datastore/DataStoreRepositoryBilling;",
            name = "encrypt",
        ),
    ),
)
