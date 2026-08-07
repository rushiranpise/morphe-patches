package app.template.patches.wallverse.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

// Pairip's non-obfuscated startup entry point. The ordered filter verifies
// that it creates a LicenseClient and starts the full license flow.
internal object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
            name = "initializeLicenseCheck",
            returnType = "V",
        ),
    ),
)


// Wallverse's shared Flow-map dispatcher (`ac0.emit`). One of its inlined
// branches derives `isPremium` by checking whether ANY of the local purchase
// state DataStore keys in `kz7.h` ("lifetime", "lifetime_50", "monthly",
// "yearly") is stored as Boolean.TRUE, then boxes the result with
// Boolean.valueOf(Z). This single Flow feeds every premium check in the app
// (Premium screen, wallpaper unlock/download, etc.) — verified in smali
// against analysis/wallverse/smali/classes5/ac0.smali.
//
// Note: `ac0`/`kz7` are R8-obfuscated names that will change on app rebuilds.
// Re-verify against smali if this fingerprint stops matching a future build.
internal object WallverseIsPremiumFingerprint : Fingerprint(
    definingClass = "Lac0;",
    name = "emit",
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lkz7;",
            name = "h",
            type = "Ljava/util/List;",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf",
            parameters = listOf("Z"),
            returnType = "Ljava/lang/Boolean;",
        ),
    ),
)
