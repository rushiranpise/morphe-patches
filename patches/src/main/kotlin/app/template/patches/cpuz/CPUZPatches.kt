package app.template.patches.cpuz

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.CPUZ_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val cpuzUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium features in app.",
    default = true,
) {
    compatibleWith(CPUZ_COMPATIBILITY)

    execute {
        // N:Z is set to false at instruction index 19 in <init>.
        // Inject const/4 v1, 0x1 + iput-boolean at index 20 to immediately override it.
        // Must be after super.<init>() (index 0) — cannot access instance fields before that.
        classDefBy("Lcom/cpuid/cpu_z/MainActivity;")
            .methods
            .first { it.name == "<init>" }
            .toMutable()
            .addInstructions(
                20,
                """
                const/4 v1, 0x1
                iput-boolean v1, p0, Lcom/cpuid/cpu_z/MainActivity;->N:Z
                """,
            )

        // Stub purchase callback so billing can never reset N to false
        CPUZPurchaseCallbackFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // Stub MobileAdsInitProvider.attachInfo to prevent ad SDK ContentProvider auto-init
        CPUZMobileAdsInitProviderAttachInfoFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
