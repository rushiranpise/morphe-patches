package app.template.patches.monet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MONET_COMPATIBILITY
import app.template.patches.shared.indexOfFirstInstructionReversed
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Monet Launcher (com.klevico.monet) — premium model (1.0.84+)
//
// Google Play Billing, single SKU: "premium_unlock" (one-time inapp purchase)
//
// ── Billing architecture ─────────────────────────────────────────────────────
//
//   BillingManager (R8; La/iu; in 1.0.84) owns a billing state holder
//   (R8; La/fk4; in 1.0.84) in its first non-parameter obfuscated object field.
//
//   State holder fields (R8-renamed each build):
//     b:Z   = is_premium_cached  (first Z param of state holder constructor)
//     c:Z   = has_license_blob   (second Z param)
//     a()Z  = isPremium formula: c || (b && (d || !e))
//     f     = MutableStateFlow<Boolean> — seeded from a() at construction
//     b(Z)V = synchronized: write c:Z=arg, recompute a(), emit result into f
//
//   NOTE: the re-emit method b(Z)V writes c (has_license_blob), not b.
//   b (is_premium_cached) must be set directly via iput-boolean.
//
// ── Why all obfuscated names are resolved dynamically ────────────────────────
//
//   R8 renames every short identifier (La/iu;, La/fk4;, field "b", method "b")
//   on every build. Hardcoding any of them causes NoClassDefFoundError /
//   NoSuchFieldError on the next app update. Instead:
//
//   BillingManager type  → BillingManagerConstructorFingerprint.classDef.type
//   State holder field   → first IPUT_OBJECT in ctor writing an obfuscated
//                          non-parameter type into `this`
//   State holder type    → FieldReference.type of that IPUT_OBJECT
//   is_premium_cached fld→ first IPUT_BOOLEAN in state holder <init>(ZZ)V
//                          (p1 = first Z arg = is_premium_cached)
//   Re-emit method name  → sole non-constructor method on state holder
//                          with parameters ["Z"] and return type "V"
//
// ── Layer 1 — billing write-back (BillingCallbackFingerprint) ────────────────
//
//   Anchors: developer log string + Android SDK calls (never obfuscated).
//   Patch: const/4 p1, 0x1 at index 0 — every billing refresh grants premium.
//
// ── Layer 2 — BillingManager constructor (BillingManagerConstructorFingerprint)
//
//   Anchors: "billing_prefs", "is_premium_cached", "license_blob_v1" — stable
//   SharedPrefs key strings, never obfuscated.
//   Patch: before return-void, re-fetch state holder, set isPremiumCachedField=true,
//   call reEmitMethod(true) to set has_license_blob and emit isPremium=true into
//   the MutableStateFlow so all ViewModels see premium immediately on cold-start.
//
//   Injection index: indexOfFirstInstructionReversed(RETURN_VOID), not
//   instructions.count()-1. The constructor tail is:
//     return-void              ← correct injection point (sole return-void)
//     :catchall_129  move-exception p0 … throw p0
//     :catchall_12c  move-exception p0 … throw p0
//   count()-1 lands on the last throw where p0 = Throwable → VerifyError.

@Suppress("unused")
val monetUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Monet Launcher premium features by forcing the billing cache to always report premium as active.",
    default = true,
) {
    compatibleWith(MONET_COMPATIBILITY)

    execute {
        // ── Layer 1 ──────────────────────────────────────────────────────────
        BillingCallbackFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )

        // ── Layer 2 ──────────────────────────────────────────────────────────
        val ctor = BillingManagerConstructorFingerprint.method
        val billingManagerType = BillingManagerConstructorFingerprint.classDef.type
        val ctorParamTypes = ctor.parameterTypes.toSet()

        // ── 2a. Resolve state holder field ───────────────────────────────────
        // Find the IPUT_OBJECT in the constructor that stores the newly-constructed
        // billing state holder into `this`. Identified by:
        //   - definingClass == billingManagerType  (write to `this`)
        //   - field type starts with "La/"         (short R8 obfuscated class)
        //   - field type not in ctor param types   (excludes the stored dependency
        //     parameter, e.g. La/re0; stored at iu.b immediately after super.<init>)
        val stateHolderField = ctor.instructionsOrNull
            ?.firstNotNullOfOrNull { insn ->
                if (insn.opcode != Opcode.IPUT_OBJECT) return@firstNotNullOfOrNull null
                val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
                    ?: return@firstNotNullOfOrNull null
                if (ref.definingClass != billingManagerType) return@firstNotNullOfOrNull null
                if (!ref.type.startsWith("La/")) return@firstNotNullOfOrNull null
                if (ref.type in ctorParamTypes) return@firstNotNullOfOrNull null
                ref
            }
            ?: throw PatchException(
                "BillingManager constructor: state holder IPUT_OBJECT not found on $billingManagerType. " +
                    "Billing architecture may have changed.",
            )

        val stateHolderType = stateHolderField.type       // La/fk4; in 1.0.84
        val stateHolderFieldName = stateHolderField.name  // c in 1.0.84

        // ── 2b. Resolve is_premium_cached field on state holder ───────────────
        // In state holder <init>(ZZ)V the first IPUT_BOOLEAN writes p1 (first Z arg)
        // which is is_premium_cached. This is the field we set to true directly.
        val stateHolderClass = classDefBy(stateHolderType)
        val stateHolderCtor = stateHolderClass.methods
            .first { it.name == "<init>" && it.parameterTypes.toList() == listOf("Z", "Z") }
        val isPremiumCachedField = stateHolderCtor.instructionsOrNull
            ?.firstNotNullOfOrNull { insn ->
                if (insn.opcode != Opcode.IPUT_BOOLEAN) return@firstNotNullOfOrNull null
                (insn as? ReferenceInstruction)?.reference as? FieldReference
            }
            ?.name
            ?: throw PatchException(
                "State holder <init>(ZZ)V: first IPUT_BOOLEAN not found in $stateHolderType. " +
                    "Field layout may have changed.",
            )
        // isPremiumCachedField == "b" in 1.0.84

        // ── 2c. Resolve re-emit method on state holder ───────────────────────
        // The re-emit method is the sole non-constructor method on the state holder
        // that takes exactly one Z parameter and returns V.
        // It: sets has_license_blob (c:Z) = arg, recomputes isPremium, emits into StateFlow.
        val reEmitMethod = stateHolderClass.methods
            .first { method ->
                method.name != "<init>" &&
                    method.returnType == "V" &&
                    method.parameterTypes.toList() == listOf("Z")
            }
            .name
            ?: throw PatchException(
                "State holder $stateHolderType: re-emit method (Z)V not found. " +
                    "Method may have been renamed or signature changed.",
            )
        // reEmitMethod == "b" in 1.0.84

        // ── 2d. Inject before return-void ────────────────────────────────────
        // Scan backwards to skip the catch-block throws after return-void.
        val returnVoidIndex = ctor.indexOfFirstInstructionReversed(Opcode.RETURN_VOID)

        // v0/v1 are dead at return-void: all locals stored into object fields above.
        // p0 = `this` (billingManagerType).
        ctor.addInstructions(
            returnVoidIndex,
            """
                iget-object v0, p0, $billingManagerType->$stateHolderFieldName:$stateHolderType
                const/4 v1, 0x1
                iput-boolean v1, v0, $stateHolderType->$isPremiumCachedField:Z
                invoke-virtual {v0, v1}, $stateHolderType->$reEmitMethod(Z)V
            """.trimIndent(),
        )
    }
}
