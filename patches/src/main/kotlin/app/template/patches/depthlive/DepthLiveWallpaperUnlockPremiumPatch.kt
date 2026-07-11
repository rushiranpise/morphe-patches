package app.template.patches.depthlive

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DEPTH_LIVE_WALLPAPER_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private fun dismissActivity() =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
    "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
    "return-void"

@Suppress("unused")
val depthLiveWallpaperUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium wallpapers.",
    default = true,
) {
    compatibleWith(DEPTH_LIVE_WALLPAPER_COMPATIBILITY)

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
            val containsMatch = PremiumSetterFingerprint.instructionMatches[0]
            val containsIndex = containsMatch.index
            val moveResultIndex = containsIndex + 1
            val moveResultInstr = method.getInstruction<OneRegisterInstruction>(moveResultIndex)
            val register = moveResultInstr.registerA
            method.addInstructions(moveResultIndex + 1, "const/4 v$register, 0x1")
        }
    }
}
