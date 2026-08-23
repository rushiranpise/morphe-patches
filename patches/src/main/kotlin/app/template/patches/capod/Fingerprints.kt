package app.template.patches.capod

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// UpgradeRepoGplay$Info.isPro()Z   [classes.dex]
//
// 5.2.3 change: isPro() is now a simple field getter — the old billingData-based
// body that called access$getProSku(BillingData) is gone. The Companion class no
// longer exists and access$getProSku was removed entirely. isPro is now a pre-computed
// field written in the constructor (same pattern as BVM 3.5.0).
//
// Smali verified (5.2.3-rc0, .registers 1, PUBLIC FINAL):
//   iget-boolean p0, p0, Leu/darken/capod/common/upgrade/core/UpgradeRepoGplay$Info;->isPro:Z
//   return p0
//
// Fingerprint: stable non-obfuscated class path + method name + return type + access flags.
// No filters needed — definingClass + name uniquely identifies this method.
// The old methodCall(access$getProSku) filter was only needed to disambiguate from
// gracePeriod fallback logic; now the method is a trivial getter, no filter required.
val IsProFingerprint = Fingerprint(
    definingClass = "Leu/darken/capod/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
