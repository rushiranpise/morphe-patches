package app.template.patches.macrodroid.pro

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Settings.c3(Context)Z — the "vcp_count" device-cap pro check.
object IsProViaDeviceCapFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/preference/PreferenceManager;",
            name = "getDefaultSharedPreferences",
        ),
        string("vcp_count"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getInt",
        ),
    ),
)
