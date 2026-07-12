package app.template.patches.animedepthwallpapers

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ANIME_DEPTH_WALLPAPERS_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private fun dismissActivity() =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
    "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
    "return-void"

@Suppress("unused")
val animeDepthWallpapersUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium wallpapers.",
    default = true,
) {
    compatibleWith(ANIME_DEPTH_WALLPAPERS_COMPATIBILITY)

    execute {
        // Layer 1: Category.isPremium() -> always true
        runCatching {
            IsPremiumFingerprint.method.addInstructions(
                0, "const/4 v0, 0x1\nreturn v0"
            )
        }

        // Layer 2: PairIP LicenseClient static(Context) -> no-op
        runCatching {
            LicenseClientFingerprint.method.addInstructions(0, "return-void")
        }

        // Layer 3: isPremiumOwned()Z -> always true
        runCatching {
            IsPremiumOwnedFingerprint.method.addInstructions(
                0, "const/4 v0, 0x1\nreturn v0"
            )
        }

        // Layer 4: LicenseActivity.onCreate -> finish immediately
        runCatching {
            LicenseActivityOnCreateFingerprint.method.addInstructions(0, dismissActivity())
        }

        // Layer 5: PremiumSetter — force Set.contains("premium_lifetime") result to true
        runCatching {
            val method = PremiumSetterFingerprint.method
            val containsIndex = PremiumSetterFingerprint.instructionMatches[0].index
            val moveResultIndex = containsIndex + 1
            val reg = method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            method.addInstructions(moveResultIndex + 1, "const/4 v$reg, 0x1")
        }

        // Layer 6: PlanSelector — force selectedPlanIndex=0 (Lifetime) in paywall plan chooser.
        // k74.invoke reads the plan StateFlow -> intValue() -> if-nez decides which card is selected.
        // Injecting 0 after the first intValue() move-result makes Lifetime always selected.
        runCatching {
            val method = PlanSelectorFingerprint.method
            val intValueIndex = PlanSelectorFingerprint.instructionMatches[0].index
            val moveResultIndex = intValueIndex + 1
            val reg = method.getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            method.addInstructions(moveResultIndex + 1, "const/4 v$reg, 0x0")
        }
    }
}
