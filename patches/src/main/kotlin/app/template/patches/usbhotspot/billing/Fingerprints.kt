package app.template.patches.usbhotspot.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// d1.h.e(Context)Z — reads SharedPreferences("pro", 0).getBoolean("pro", false).
// This is the single premium gate called from USBActivity.n()V to show/hide
// the buy button and from any other premium check in the app.
// Stable anchors: non-obfuscated SharedPreferences API calls + string "pro".
object IsPremiumFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("pro"),
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getSharedPreferences"
        ),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        )
    )
)
