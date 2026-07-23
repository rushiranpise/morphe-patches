package app.template.patches.onetapcleaner.pro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.onetapcleaner.antitamper.disableAntiTamperPatch
import app.template.patches.onetapcleaner.gma.disableGmaPcamPatch
import app.template.patches.onetapcleaner.license.disablePairIPLicenseCheckPatch
import app.template.patches.onetapcleaner.licwnd.disableLicWndPatch
import app.template.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

// 1Tap Cleaner's pro state is a singleton class (obfuscated name, e.g. "hts") with:
//   four AtomicBoolean instance fields set false in <clinit>
//   a static self-typed singleton reference
//
// Field roles (from cross-referencing call sites and MainApp.onCreate):
//   鐽 (U+943D, 66 readers) — "billing/app initialized"; set TRUE in MainApp.onCreate
//                             unconditionally. Gates that show purchase UI check this.
//   鷖 (U+9DD6, 36 readers) — "has PRO license"; written by billing result handler (ata).
//                             Feature locks check this. Must be TRUE to unlock features.
//   囔 (U+56D4, 14 readers) — "billing has responded at least once"
//   驁 (U+9A41,  7 readers) — secondary purchase / credits
//
// On a patched build:
//   - MainApp.onCreate sets 鐽=true at startup (unconditional, hardware-level billing probe)
//   - ata.顩 exits early at the cert-hash check before writing 鷖 or 驁 → both stay false
//   - Result: 鐽=true, 鷖=false → every feature gate fails; every "upgrade" button shows LicWnd
//
// Fix: inject AtomicBoolean.set(true) for 鷖, 囔, and 驁 at the end of <clinit>.
//   Do NOT set 鐽 — it is already set by onCreate and its callers gate on it to show
//   purchase UI. Leaving 鐽 alone means the "upgrade" button handlers still fire,
//   which is why disableLicWndPatch (a dependency) no-ops LicWnd directly.
//
// Fields are found dynamically from the class definition so obfuscation renames are safe.
// The singleton class is located by predicate: 4 non-static AtomicBoolean fields +
// a static self-typed reference. No hardcoded obfuscated names anywhere.
@Suppress("unused")
val unlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks 1Tap Cleaner PRO features: history export, app-group filters, " +
        "unlimited cache targets, and ad removal.",
    default = true,
) {
    dependsOn(
        disablePairIPLicenseCheckPatch,
        disableGmaPcamPatch,
        disableAntiTamperPatch,
        disableLicWndPatch,
    )
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // Locate singleton class dynamically.
        val htsClass = classDefBy { cls ->
            val instanceAtomicFields = cls.fields.filter { field ->
                !AccessFlags.STATIC.isSet(field.accessFlags) &&
                    field.type == "Ljava/util/concurrent/atomic/AtomicBoolean;"
            }
            val hasSelfRef = cls.fields.any { field ->
                AccessFlags.STATIC.isSet(field.accessFlags) && field.type == cls.type
            }
            instanceAtomicFields.size == 4 && hasSelfRef
        }

        val mutableHts = mutableClassDefBy(htsClass)

        val clinit = mutableHts.methods.firstOrNull {
            it.name == "<clinit>" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("1Tap Cleaner: hts.<clinit> not found.")

        // Singleton field and the 3 fields that need to be true:
        // 鷖 (isPro), 囔 (billingResponded), 驁 (credits).
        // Exclude 鐽 (billingInitialized) — set by onCreate, gating purchase-UI callers.
        val singletonField = mutableHts.fields.first { field ->
            AccessFlags.STATIC.isSet(field.accessFlags) && field.type == mutableHts.type
        }

        // Identify 鐽 as the field most-read (66 readers) — exclude it from injection.
        // We do this by counting iget-object references in the smali text, but at patch
        // time we can't read smali text. Instead: the billing result handler (ata) sets
        // exactly 2 fields (驁 and 鷖). The field NOT set by ata AND NOT 鐽 is 囔.
        // Pragmatic approach: inject set(true) for ALL 4 fields — MainApp.onCreate
        // will set 鐽=true anyway (idempotent), and disableLicWndPatch covers the
        // purchase-UI side effect. This is simpler and equally correct.
        val atomicFields = mutableHts.fields.filter { field ->
            !AccessFlags.STATIC.isSet(field.accessFlags) &&
                field.type == "Ljava/util/concurrent/atomic/AtomicBoolean;"
        }

        // Insert set(true) for all 4 AtomicBoolean fields before final return-void.
        // Registers v0=singleton, v1=each AtomicBoolean, v2=const 1.
        // clinit has .registers 3 (v0,v1,v2 all valid).
        val smali = buildString {
            appendLine("sget-object v0, ${mutableHts.type}->${singletonField.name}:${singletonField.type}")
            appendLine("const/4 v2, 0x1")
            for (field in atomicFields) {
                appendLine("iget-object v1, v0, ${mutableHts.type}->${field.name}:${field.type}")
                appendLine("invoke-virtual { v1, v2 }, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V")
            }
        }

        clinit.addInstructions(clinit.implementation!!.instructions.lastIndex, smali)
    }
}
