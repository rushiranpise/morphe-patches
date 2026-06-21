package app.template.patches.netshare

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Util.isPro(Context)Z
 * Checks whether the companion key app "netshare.key" is installed from "com.android.vending".
 * Fingerprinted by: definingClass + name + returnType + accessFlags.
 */
val UtilIsProFingerprint = Fingerprint(
    definingClass = "Lkha/prog/mikrotik/Util;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * IAB.isPurchased(String, Context)Z
 * Checks reward timer + SharedPref keys ("any", "all") from "kha.prog.mikrotik.pro_pref",
 * then falls back to Util.isPro(). Fingerprinted by: definingClass + name + accessFlags.
 */
val IABIsPurchasedFingerprint = Fingerprint(
    definingClass = "Lkha/prog/mikrotik/IAB;",
    name = "isPurchased",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("kha.prog.mikrotik.pro_pref")
)
