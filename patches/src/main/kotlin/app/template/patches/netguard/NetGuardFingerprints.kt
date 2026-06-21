package app.template.patches.netguard

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * IAB.isPurchased(String, Context) — checks IAB SharedPreferences for a specific SKU key
 * (pro, pro1, support1, support2, donation, android.test.purchased).
 * Prepending `const/4 p0, 0x1 / return p0` always returns true → SKU owned.
 * Fingerprinted by: definingClass + name + parameters + return type + unique strings "support2", "donation".
 */
val IabIsPurchasedFingerprint = Fingerprint(
    definingClass = "Leu/faircode/netguard/IAB;",
    name = "isPurchased",
    parameters = listOf("Ljava/lang/String;", "Landroid/content/Context;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("support2", "donation")
)

/**
 * IAB.isPurchasedAny(Context) — iterates all IAB SharedPreferences keys
 * and returns true if any is true. Used as the top-level pro gate in ActivitySettings/ServiceSinkhole.
 * Prepending `const/4 v0, 0x1 / return v0` always returns true.
 * Fingerprinted by: definingClass + name + parameters + return type.
 */
val IabIsPurchasedAnyFingerprint = Fingerprint(
    definingClass = "Leu/faircode/netguard/IAB;",
    name = "isPurchasedAny",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)