package app.template.patches.bluetoothvolumemanager

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// UpgradeRepoGplay$Info.<init>(BillingData, Throwable, int)V   [classes.dex]
//
// The Info data class constructor computes isUpgraded:Z from the purchase list:
//
//   this.isUpgraded = (upgrades.isNotEmpty() || gracePeriod)
//
// Smali verified (3.4.3-rc0, .registers 16, single constructor):
//   ...loop over purchases, match OurSku.Companion.PRO_SKUS...
//   :L18  move v1, v2          ← isNotEmpty=false AND gracePeriod=false: v1=0
//   :L19  iput-boolean v1, p0, ->isUpgraded:Z   ← THE write (smali line 428)
//   ...rest of constructor...
//   return-void
//
// Fix: read the value register from the matched iput-boolean (registerA) and inject
// const/4 vREG, 0x1 immediately BEFORE it, forcing isUpgraded=true regardless of
// the billing result. The iput then writes true into the object.
//
// Anchor: stable app-owned definingClass + constructor name + accessFlags
// + full parameter list + iput-boolean filter on the isUpgraded field.
// Only one constructor exists on this class — match is always unique.
internal val InfoConstructorFingerprint = Fingerprint(
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

// UStringsKt.isPro(UpgradeRepo, ContinuationImpl)Object   [classes.dex]
//
// Kotlin coroutine extension function, relocated to kotlin.text.UStringsKt by R8.
// After awaiting the first emission from UpgradeRepoGplay.upgradeInfo, reads
// Info.isUpgraded and boxes it as Boolean:
//
//   iget-boolean p0, p1, ...UpgradeRepoGplay$Info;->isUpgraded:Z
//   invoke-static { p0 }, Ljava/lang/Boolean;->valueOf(Z)
//   return-object p0
//
// Fix: return Boolean.TRUE at method entry — short-circuits the entire coroutine
// state machine, bypassing the upgradeInfo flow read completely.
//
// Note: BlueMusic uses ContinuationImpl (not plain Continuation) as the second
// parameter because R8 specialised the coroutine dispatch for this call-site.
// Stable anchor: non-obfuscated app-owned UpgradeRepo type in the parameter list.
internal val IsProSuspendFingerprint = Fingerprint(
    definingClass = "Lkotlin/text/UStringsKt;",
    name = "isPro",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "Leu/darken/bluemusic/common/upgrade/UpgradeRepo;",
        "Lkotlin/coroutines/jvm/internal/ContinuationImpl;",
    ),
)
