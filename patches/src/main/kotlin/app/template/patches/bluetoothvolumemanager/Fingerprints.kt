package app.template.patches.bluetoothvolumemanager

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private const val INFO = "Leu/darken/bluemusic/upgrade/core/UpgradeRepoGplay\$Info;"

// UpgradeRepoGplay$Info.<init>(Z, BillingData, Throwable, Z)V   [classes.dex]
//
// 3.5.0 refactor: the upgrade state is now stored as `isPro:Z` (computed from the
// purchase list at construction time) rather than the old `isUpgraded:Z` field.
// The synthetic delegating constructor signature changed from (BillingData, Throwable, int)
// to (BillingData, Throwable, Z, I) — the primary constructor now takes (Z, BillingData, Throwable, Z).
//
// Smali verified (3.5.0-rc0, .registers 24, single primary constructor):
//   ...purchase loop over OurSku.Companion.PRO_SKUS...
//   :L28  const/4 v6, 0x1        ← has upgrades OR gracePeriod=true
//   :L29  iput-boolean v6, v0, ->isPro:Z   ← line 725
//   ...rest of constructor...
//   return-void
//
// Fix: read the value register from the matched iput-boolean (registerA) and inject
// `const/4 vREG, 0x1` immediately before it, forcing isPro=true at construction.
//
// Anchors: non-obfuscated definingClass + name + exact parameter list
// + fieldAccess(IPUT_BOOLEAN, definingClass=Info, name="isPro").
// Only one primary constructor on this class; only one iput-boolean on ->isPro:Z in it.
internal val InfoConstructorFingerprint = Fingerprint(
    definingClass = INFO,
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Z",
        "Leu/darken/bluemusic/upgrade/core/billing/BillingData;",
        "Ljava/lang/Throwable;",
        "Z",
    ),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IPUT_BOOLEAN,
            definingClass = INFO,
            name = "isPro",
        ),
    ),
)
