package app.template.patches.capod

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// UpgradeRepoGplay$Info.isPro()Z   [classes.dex]
//
// CAPod exposes a direct isPro() method on the Info data class (unlike BVM/SD Maid
// which use an isUpgraded boolean field). The method checks billingData -> getProSku()
// (a PurchasedSku object, null if not purchased) and falls back to gracePeriod.
//
// Smali verified (.registers 3, PUBLIC FINAL):
//   iget-object v0, p0, ->billingData
//   if-eqz v0, :L0
//   invoke-static {v0}, UpgradeRepoGplay$Companion.access$getProSku(BillingData)PurchasedSku
//   move-result-object v0
//   [null check → gracePeriod fallback → return Z]
//
// Fingerprint: stable non-obfuscated class path + methodCall to access$getProSku.
// This is the only Z-returning no-param method in this class calling getProSku.
val IsProFingerprint = Fingerprint(
    definingClass = "Leu/darken/capod/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Leu/darken/capod/common/upgrade/core/UpgradeRepoGplay\$Companion;",
            name = "access\$getProSku",
        ),
    ),
)
