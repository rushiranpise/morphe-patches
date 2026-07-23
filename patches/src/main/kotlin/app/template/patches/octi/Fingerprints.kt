package app.template.patches.octi

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// UpgradeRepoGplay$Info.isPro()Z   [classes.dex]
//
// Octi exposes a direct isPro() method on the Info data class that simply
// reads the isPro:Z boolean field and returns it.
//
// Smali verified (.registers 2, PUBLIC FINAL):
//   iget-boolean v0, p0, Leu/darken/octi/common/upgrade/core/UpgradeRepoGplay$Info;->isPro:Z
//   return v0
//
// This is the deepest check — every consumer (UI, feature gates) calls
// upgradeInfo.first().isPro() which routes here.
//
// Fingerprint: stable non-obfuscated class path + IGET_BOOLEAN on the isPro field.
val IsProFingerprint = Fingerprint(
    definingClass = "Leu/darken/octi/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Leu/darken/octi/common/upgrade/core/UpgradeRepoGplay\$Info;",
            name = "isPro",
        ),
    ),
)
