package app.template.patches.myperm.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Targets UpgradeRepoGplay$Info.<init>(Z, BillingData, Throwable, Z)V
// This is the real (non-synthetic) constructor that computes:
//
//   isPro = !upgrades.isEmpty() || gracePeriod
//
// Smali (classes.dex, versionCode 20202000, .registers 24):
//   Line 879: invoke-interface {v1}, Ljava/util/Collection;->isEmpty()Z
//   Line 880: move-result v1
//   Line 885: if-eqz v1, :cond_198     [upgrades not empty → jump to set true]
//   Line 891: iget-boolean v1, v0, ->gracePeriod:Z
//   Line 895: if-eqz v1, :cond_196     [no grace → jump to set false]
//   Line 906: :cond_196 / const/4 v6, 0x0
//   Line 910: :cond_198/:goto_198 / const/4 v6, 0x1
//   Line 914: :goto_199
//   Line 915: iput-boolean v6, v0, ->isPro:Z   ← THE write
//
// Patch: inject const/4 v6, 0x1 immediately before the iput-boolean isPro,
// overriding whatever v6 was computed to. The iput then writes true.
//
// Fingerprint strategy:
//   1. Collection.isEmpty() — only call on a collection in this constructor
//   2. iput-boolean isPro    — stable non-obfuscated field name
// Access flags: PUBLIC CONSTRUCTOR (not STATIC, not SYNTHETIC, not FINAL).
// DEX: classes — smali verified against versionCode 20202000.
internal val InfoConstructorFingerprint = Fingerprint(
    definingClass = "Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay\$Info;",
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Z",
        "Leu/darken/myperm/common/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "Z",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/Collection;",
            name = "isEmpty",
        ),
        fieldAccess(
            opcode = Opcode.IPUT_BOOLEAN,
            definingClass = "Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay\$Info;",
            name = "isPro",
        ),
    ),
)

// Targets UpgradeRepoExtensionsKt.isProForUi-8Mi8wO0(UpgradeRepoGplay, J, ContinuationImpl)Object
// Kotlin suspend extension function — awaits upgradeInfo flow and reads Info.isPro.
// Already "fails open" (catches exceptions and allows), but we short-circuit entirely
// to avoid any billing flow consumption.
//
// Smali (classes.dex, versionCode 20202000):
//   .method public static final isProForUi-8Mi8wO0(
//       Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay;
//       J
//       Lkotlin/coroutines/jvm/internal/ContinuationImpl;
//   )Ljava/lang/Object;
//
// Fingerprint: stable non-obfuscated class + mangled name + parameters.
// The name-mangling suffix (-8Mi8wO0) is stable for this version;
// if it ever changes, remove the `name` field and rely on definingClass + parameters.
// DEX: classes — smali verified against versionCode 20202000.
internal val IsProForUiFingerprint = Fingerprint(
    definingClass = "Leu/darken/myperm/common/upgrade/UpgradeRepoExtensionsKt;",
    name = "isProForUi-8Mi8wO0",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Leu/darken/myperm/common/upgrade/core/UpgradeRepoGplay;",
        "J",
        "Lkotlin/coroutines/jvm/internal/ContinuationImpl;",
    ),
)
