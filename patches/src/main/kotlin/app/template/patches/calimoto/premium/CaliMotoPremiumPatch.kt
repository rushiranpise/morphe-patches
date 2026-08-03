package app.template.patches.calimoto.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALIMOTO_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

/**
 * Unlocks Calimoto by forcing the app's single top-level membership getter to
 * always return LIFETIME.
 *
 * See Fingerprints.kt for how MembershipGetterFingerprint locates the target
 * method and its LIFETIME enum constant without hardcoding any of the
 * (obfuscated, rename-prone) class or field names involved.
 */
@Suppress("unused")
val calimotoPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Calimoto LIFETIME membership by spoofing the membership getter.",
) {
    compatibleWith(CALIMOTO_COMPATIBILITY)

    execute {
        // Reuse the method's own "allMaps flag → return LIFETIME" sget-object
        // instruction verbatim (matched as filter[1]), so the injected
        // early-return below never needs to name the Membership enum type or
        // its LIFETIME constant directly — both are obfuscated and already
        // observed to change name every release.
        val lifetimeField = MembershipGetterFingerprint.instructionMatches[1]
            .getInstruction<ReferenceInstruction>()
            .reference as FieldReference

        MembershipGetterFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, ${lifetimeField.definingClass}->${lifetimeField.name}:${lifetimeField.type}
                return-object v0
            """.trimIndent(),
        )
    }
}
