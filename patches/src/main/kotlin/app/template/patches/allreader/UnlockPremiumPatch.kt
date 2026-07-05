package app.template.patches.allreader

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ALLREADER_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium.",
    default = true,
) {
    compatibleWith(ALLREADER_COMPATIBILITY)

    execute {
        // K8.j.u() → hide ad card (GONE=8) and return — removes native ad containers
        LoadNativeAdFingerprint.match(classDefBy(LoadNativeAdFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                "iget-object v0, p0, LK8/j;->c:Ljava/lang/Object;\n" +
                "check-cast v0, Landroid/view/View;\n" +
                "const/16 v1, 0x8\n" +
                "invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V\n" +
                "return-void"
            )
        }

        // isPremiumUser() → true
        IsPremiumUserFingerprint.match(classDefBy(IsPremiumUserFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // purchase SharedPref gate → true (skip paywall)
        IsPurchasedFingerprint.match(classDefBy(IsPurchasedFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // firstLaunch → false (skip LanguageActivity onboarding)
        IsFirstLaunchFingerprint.match(classDefBy(IsFirstLaunchFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // showInterAd (static): fire callback p1 then return
        ShowInterAdFingerprint.match(classDefBy(ShowInterAdFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "invoke-interface {p1}, Lc7/a;->a()Ljava/lang/Object;\nreturn-void")
        }

        // showPreloadTimeInter (instance): fire callback p2 then return
        ShowPreloadTimeInterFingerprint.match(classDefBy(ShowPreloadTimeInterFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "invoke-interface {p2}, Lc7/a;->a()Ljava/lang/Object;\nreturn-void")
        }
    }
}

