package app.template.patches.vizmanga

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// f62.x(Context, String)Z — checks subscription validity ("vm" type)
// Called by a03.i(Context)Z which gates chapter reading access
val VmSubscriptionCheckFingerprint = Fingerprint(
    definingClass = "Lf62;",
    name = "x",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("no")
)

// f62.u(Context, String)J — returns subscription_valid_to_vm timestamp from SharedPreferences
val VmSubscriptionExpiryFingerprint = Fingerprint(
    definingClass = "Lf62;",
    name = "u",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    returnType = "J",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("subscription_valid_to_vm")
)
