package app.template.patches.bluetoothvolumemanager

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// UpgradeRepoGplay$Info.<init>(BillingData, Throwable, int)V   [classes.dex]
//
// The Info data class constructor sets isUpgraded:Z based on whether any purchase
// matches OurSku.PRO_SKUS:
//
//   this.isUpgraded = (upgrades.isNotEmpty() || gracePeriod)
//
// Smali verified (.registers 16, single constructor):
//   ...loop over purchases, match OurSku.Companion.PRO_SKUS...
//   :L18  move v1, v2          ← isNotEmpty=false AND gracePeriod=false: v1=0
//   :L19  iput-boolean v1, p0, ->isUpgraded:Z   ← THE write (line 428)
//   ...rest of constructor (upgradedAt, etc.)...
//   return-void
//
// Fix: inject const/4 v1, 0x1 immediately BEFORE the iput-boolean at the
// instructionMatches[0].index, forcing v1=true. The iput then writes true.
// No register expansion needed (method has 16 registers).
//
// Fingerprint: definingClass (stable non-obfuscated) + constructor name + iput-boolean
// on isUpgraded:Z field. Only one constructor exists on this class.
val InfoConstructorFingerprint = Fingerprint(
    definingClass = "Leu/darken/bluemusic/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Leu/darken/bluemusic/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "I",
    ),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IPUT_BOOLEAN,
            definingClass = "Leu/darken/bluemusic/upgrade/core/UpgradeRepoGplay\$Info;",
            name = "isUpgraded",
        ),
    ),
)
