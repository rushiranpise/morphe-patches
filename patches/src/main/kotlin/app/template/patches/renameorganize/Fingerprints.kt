package app.template.patches.renameorganize

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * f5.c.n0(Context)Z — master isPremium gate.
 * Checks ispremium_lifetime → f5.c.c static flag → ispremium → ispremium_lifetime_v2.
 * Patching to always return true grants full premium access.
 */
val IsPremiumFingerprint = Fingerprint(
    definingClass = "Lf5/c;",
    name = "n0",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("ispremium_lifetime", "ispremium", "ispremium_lifetime_v2"),
)
