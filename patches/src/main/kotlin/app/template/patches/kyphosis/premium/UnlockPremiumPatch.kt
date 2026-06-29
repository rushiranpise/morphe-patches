package app.template.patches.kyphosis.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium content.",
) {
    compatibleWith(Constants.KYPHOSIS_COMPATIBILITY)

    execute {
        // App.isPremium():Z → true (main user subscription gate, reads sp_premium SharedPref)
        AppIsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // LoginResponse.isPaid():Z → true (user paid flag from login API)
        LoginResponseIsPaidFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // AssetCJ.isPaid():Z → false (content gate: false = free, true = triggers paywall)
        AssetCJIsPaidFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // NewsItem.isPaid():Z → false (news/popular items content gate)
        NewsItemIsPaidFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}
