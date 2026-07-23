package app.template.patches.podslink.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Target: LicenseClient.initializeLicenseCheck()V
// DEX: classes.dex — com/pairip/licensecheck/LicenseClient.smali
// Entry point for the Play LVL license check flow. Bypassed by pre-setting
// licenseCheckState to LOCAL_CHECK_OK before any network call is made.
// Non-obfuscated: Pairip SDK class names are stable.
val LicenseClientInitFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// Target: LicenseClient.checkLicense(Context)V
// DEX: classes.dex — static entry point that creates a LicenseClient and calls
// initializeLicenseCheck(). No-opped to prevent the check from ever starting.
val LicenseCheckFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
)

// Target: LicenseClient.processResponse(int, Bundle)V
// DEX: classes.dex — handles NOT_LICENSED (2) response, triggers paywall/exit.
// No-opped to prevent shutdown on invalid license response.
val ProcessResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
)

// Target: LicenseClient.startPaywallActivity(PendingIntent)V
// DEX: classes.dex — launches the paywall UI / calls System.exit() as failsafe.
// No-opped as a final killswitch.
val StartPaywallFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "startPaywallActivity",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Landroid/app/PendingIntent;"),
)

// Target: LicenseResponseHelper.validateResponse(Bundle, String)V
// DEX: classes.dex — validates the JWS signature from Play licensing service.
// Throws LicenseCheckException on failure. No-opped to accept any response.
val ValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)


// Target: AccountInfo.isActive()Z
// DEX: classes2.dex — net/podslink/entity/net/AccountInfo.smali
//
// Called from 12+ places directly after R8 inlined EntitlementManager/MembershipState.
// Patching this covers all UI checks but does NOT fix the cache write-back problem
// (see CacheAccountInfoFingerprint below).
val AccountInfoIsActiveFingerprint = Fingerprint(
    definingClass = "Lnet/podslink/entity/net/AccountInfo;",
    name = "isActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lnet/podslink/entity/net/AccountInfo;",
            name = "active",
            type = "Z"
        ),
    )
)

// Target: AccountManager.cacheAccountInfo(AccountInfo)V
// DEX: classes2.dex — net/podslink/network/manager/AccountManager.smali
//
// Root cause of "still free": every billing refresh cycle (Play Billing query, server sync,
// Google order status check) ends with cacheAccountInfo() serializing the AccountInfo to JSON
// and writing it to SharedPreferences. The serialized object has active=false (no valid purchase),
// so on next launch getAccountInfoFromCache() returns active=false, defeating isActive() patch.
//
// Fix: inject setActive(true) on the AccountInfo object BEFORE it is serialized to JSON.
// This ensures the cache always contains `"active":true` regardless of billing state.
//
// Fingerprint: unique via Gson.toJson() call on AccountInfo + SPHelp.setUserParam + key string.
val CacheAccountInfoFingerprint = Fingerprint(
    definingClass = "Lnet/podslink/network/manager/AccountManager;",
    name = "cacheAccountInfo",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Lnet/podslink/entity/net/AccountInfo;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/gson/Gson;",
            name = "toJson",
            returnType = "Ljava/lang/String;"
        ),
        string("key_account_info"),
        methodCall(
            definingClass = "Lnet/podslink/util/SPHelp;",
            name = "setUserParam"
        ),
    )
)
