package app.template.patches.universaltv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.UNIVERSALTV_COMPATIBILITY

/**
 * Forces s8/f.e(Context)Z to always return true, bypassing all three
 * purchase gates (SharedPreferences isPremium, inapp SKU, and both
 * subscription SKUs) in a single no-try/catch method replacement.
 */
@Suppress("unused")
val universalTvUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features In the App.",
    default = true
) {
    compatibleWith(UNIVERSALTV_COMPATIBILITY)

    execute {
        IsPremiumFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
    }
}

/**
 * No-ops s8/f.f(Application)V to prevent the AppOpenAd preloader
 * from starting. The method has an early-return guard on field s8/f.s:Z
 * so prepending return-void is safe regardless of state.
 */
@Suppress("unused")
val universalTvDisableAdsPatch = bytecodePatch(
    name = "Disable Ads",
    description = "Prevents the AppOpen ad preloader from initialising.",
    default = true
) {
    compatibleWith(UNIVERSALTV_COMPATIBILITY)

    execute {
        AppOpenAdPreloaderFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}

/**
 * No-ops s8/f.a()V to suppress the CHECK_PREMIUM LocalBroadcast.
 * The entire method body is inside a try/catch, so we prepend rather
 * than replace to avoid disturbing the exception table.
 */
@Suppress("unused")
val universalTvSuppressPaywallPatch = bytecodePatch(
    name = "Suppress Paywall",
    description = "Suppresses the in-app paywall.",
    default = true
) {
    compatibleWith(UNIVERSALTV_COMPATIBILITY)

    execute {
        CheckPremiumBroadcastFingerprint
            .match()
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}