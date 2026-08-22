package app.template.patches.octopilauncher.premium

import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OCTOPILAUNCHER_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.getReference
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Octopi Launcher Premium Patch
//
// Architecture:
//   Octopi Launcher gates all pro UI behind a single runtime boolean getter:
//   yq5.e()Z — the isPro getter on the PurchaseViewModel.
//
//   e() reads yq5.s (zo4/LiveData<Boolean>) and unboxes it:
//     iget-object p0, p0, Lyq5;->s:Lzo4;
//     invoke-virtual {p0}, Lzo4;->getValue()Ljava/lang/Object;
//     check-cast p0, Ljava/lang/Boolean;
//     invoke-virtual {p0}, Ljava/lang/Boolean;->booleanValue()Z
//     return p0
//
//   This method is called by 17 UI composables and ViewModel classes across
//   both DEX shards. Returning true unconditionally bypasses every pro feature
//   gate without touching billing, Room DB, or coroutine state.
//
// Patch strategy:
//   1. ProSnackCoroutineFingerprint locates ln2.v() via the "pro_snack" SKU
//      string — the only globally unique stable anchor in the billing chain.
//   2. In execute{}, scan ln2.v() instructions for the IGET_OBJECT opcode
//      referencing `Lyq5;->s:Lzo4;` — this reveals the obfuscated yq5 class name.
//   3. Use mutableClassDefBy(yq5Type) to get the mutable class, then locate
//      the e()Z method (PUBLIC FINAL, no params, returns Z) and returnEarly(true).
//
// This avoids hardcoding the obfuscated `Lyq5;` name in a fingerprint (which
// would break on any r8 reshuffle) while remaining version-stable through the
// "pro_snack" SKU string which is tied to the Google Play product ID.
//
// No Pairip, no signature check, no SSL pinning, no root detection to bypass.

@Suppress("unused")
val octopiLauncherPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Octopi Launcher Pro by returning true from the isPro LiveData getter, bypassing all pro feature gates without modifying billing or database logic.",
    default = true,
) {
    compatibleWith(OCTOPILAUNCHER_COMPATIBILITY)

    execute {

        // ── Step 1: Resolve obfuscated yq5 class via ln2.v() field reference ──
        //
        // ln2.v() contains: iget-object v0, v2, Lyq5;->s:Lzo4;
        // We scan for the IGET_OBJECT whose field type is Lzo4; (MutableLiveData)
        // and whose name is "s". This gives us the definingClass = "Lyq5;" at runtime.
        val yq5Type = ProSnackCoroutineFingerprint.method.instructions
            .firstNotNullOfOrNull { instruction ->
                if (instruction.opcode != Opcode.IGET_OBJECT) return@firstNotNullOfOrNull null
                instruction.getReference<FieldReference>()
                    ?.takeIf { ref -> ref.name == "s" && ref.type == "Lzo4;" }
                    ?.definingClass
            }
            ?: throw PatchException(
                "Octopi Launcher: could not resolve yq5 class — Lyq5;->s:Lzo4; " +
                "iget-object not found in ProSnackCoroutineFingerprint method."
            )

        // ── Step 2: Locate yq5.e()Z and force-return true ─────────────────────
        //
        // e() is: PUBLIC FINAL, returnType Z, no parameters.
        // It is the sole isPro accessor for the entire UI layer.
        val yq5Class = mutableClassDefBy(yq5Type)
        val isProMethod = yq5Class.methods.firstOrNull { method ->
            method.name == "e" &&
            method.returnType == "Z" &&
            method.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "Octopi Launcher: could not find e()Z on $yq5Type — " +
            "method signature may have changed."
        )

        yq5Class.findMutableMethodOf(isProMethod).returnEarly(true)
    }
}
