package app.template.patches.imagedatefixer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * gc.c0.g(Context)Z — master isPremium gate.
 * Returns true if ispremium_sub OR ispremium OR ispremium_lifetime_v5 is set.
 * Patching to always return true unlocks all premium features.
 */
val IsPremiumFingerprint = Fingerprint(
    definingClass = "Lgc/c0;",
    name = "g",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        opcode(Opcode.INVOKE_STATIC),
        opcode(Opcode.MOVE_RESULT),
        opcode(Opcode.IF_NEZ),
        opcode(Opcode.INVOKE_STATIC),
        opcode(Opcode.MOVE_RESULT),
        opcode(Opcode.IF_NEZ),
    ),
)

/**
 * gc.c0.d(Context)Z — reads ispremium_lifetime_v5 from SharedPreferences.
 * Patching as secondary layer to guarantee lifetime unlock.
 */
val IsLifetimeV5Fingerprint = Fingerprint(
    definingClass = "Lgc/c0;",
    name = "d",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("ispremium_lifetime_v5"),
)
