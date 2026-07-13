package app.template.patches.cpuz

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.CPUZ_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val cpuzUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
    default = true,
) {
    compatibleWith(CPUZ_COMPATIBILITY)

    execute {
        // Force N:Z (isPremium) = true in <init> before billing can set it to false.
        // N:Z at smali index 18 in constructor; inject at 19.
        classDefBy("Lcom/cpuid/cpu_z/MainActivity;")
            .methods
            .first { it.name == "<init>" }
            .toMutable()
            .addInstructions(
                19,
                """
                const/4 v1, 0x1
                iput-boolean v1, p0, Lcom/cpuid/cpu_z/MainActivity;->N:Z
                """,
            )

        // Stub billing callback — prevents N:Z reset from server.
        CPUZPurchaseCallbackFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub ContentProvider ad SDK auto-init.
        CPUZMobileAdsInitProviderAttachInfoFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub w() — the real ad loader. N:Z does NOT gate this; l04.a() does.
        // Must stub directly to prevent ads from showing regardless of purchase state.
        CPUZAdLoaderFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub wc.d() — ad resume/refresh trigger.
        CPUZAdResumeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
