package app.template.patches.protonmail.plan

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROTONMAIL_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.getReference
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val protonMailUnlimitedPatch = bytecodePatch(
    name = "Unlock Unlimited Plan",
    description = "Unlocks Proton Mail Unlimited plan features by always returning UpsellingVisibility.Hidden.",
) {
    compatibleWith(PROTONMAIL_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
    )

    execute {
        var patched = false

        classDefForEach { classDef ->
            if (patched) return@classDefForEach

            val method = classDef.methods.firstOrNull { method ->
                method.parameterTypes.size == 2 &&
                method.parameterTypes[0] == "Ljava/util/List;" &&
                method.returnType == "Ljava/lang/Object;" &&
                method.implementation?.instructions?.let { insns ->
                    val strings = insns
                        .mapNotNull { it.getReference<StringReference>()?.string }
                        .toSet()
                    "summer26" in strings && "bundle2022" in strings
                } == true
            } ?: return@classDefForEach

            mutableClassDefBy(classDef.type)
                .findMutableMethodOf(method)
                .addInstructions(
                    0,
                    "sget-object p0, Lch/protonmail/android/mailupselling/presentation/model/j;->INSTANCE:Lch/protonmail/android/mailupselling/presentation/model/j;\nreturn-object p0",
                )

            patched = true
        }

        if (!patched) throw PatchException(
            "Could not find UpsellingVisibility mapper (h.a) with summer26 + bundle2022."
        )
    }
}
