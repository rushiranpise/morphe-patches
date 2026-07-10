package app.template.patches.jefit.elite

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockElitePatch = bytecodePatch(
    name = "Unlock Elite",
    description = "Unlocks JEFIT Elite features in app.",
) {
    compatibleWith(Constants.JEFIT_COMPATIBILITY)

    execute {
        val retTrue = "const/4 v0, 0x1\nreturn v0"
        val retElite = "const/4 v0, 0x2\nreturn v0"

        // JefitAccountV2.isElite()Z → true
        IsEliteFingerprint.method.apply {
            clearBody()
            addInstructions(0, retTrue)
        }

        // JefitAccountV2.isPro()Z → true
        IsProFingerprint.method.apply {
            clearBody()
            addInstructions(0, retTrue)
        }

        // JefitAccountV2.getAccountType()I → 2 (Elite)
        // Used directly by modern ViewModels: DaysManagementViewModel, handleCopyWorkoutDayClick,
        // handleBannerCopyClick, CheckEliteAccountUseCase, etc.
        GetAccountTypeFingerprint.method.apply {
            clearBody()
            addInstructions(0, retElite)
        }

        // Function.accountType()I → 2 (Elite)
        // Used by legacy presenters: DayItemListPresenter, RoutineDetailsPresenter,
        // ExerciseListAdapter, MuscleRecoveryViewHolder, PopupSystemRouteHandler, etc.
        FunctionAccountTypeFingerprint.method.apply {
            clearBody()
            addInstructions(0, retElite)
        }

        // ExerciseChartRepository.isEliteUser()Z → true
        IsEliteUserFingerprint.method.apply {
            clearBody()
            addInstructions(0, retTrue)
        }
    }
}
