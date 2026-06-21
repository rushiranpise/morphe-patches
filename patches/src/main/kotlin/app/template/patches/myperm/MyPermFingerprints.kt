package app.template.patches.myperm

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * UpgradeRepoGplay$Info.isPro() — returns true if user has a valid Pro SKU purchase
 * or is in a grace period. Patching this to always return true bypasses the billing check.
 * Fingerprinted by: definingClass + name + return type + accessFlags.
 */
val UpgradeRepoGplayIsProFingerprint = Fingerprint(
    definingClass = "Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
