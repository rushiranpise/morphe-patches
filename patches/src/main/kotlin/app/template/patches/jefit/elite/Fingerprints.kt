package app.template.patches.jefit.elite

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// JefitAccountV2.isElite()Z — primary elite subscription gate.
// Returns true if accountType >= 2 (Elite=2, Trainer=3).
object IsEliteFingerprint : Fingerprint(
    definingClass = "Lje/fit/account/v2/JefitAccountV2;",
    name = "isElite",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// JefitAccountV2.isPro()Z — legacy pro subscription gate (accountType == 1).
object IsProFingerprint : Fingerprint(
    definingClass = "Lje/fit/account/v2/JefitAccountV2;",
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// JefitAccountV2.getAccountType()I — raw accountType int getter used directly by
// modern ViewModels/UseCases (e.g. DaysManagementViewModel, CheckEliteAccountUseCase).
// Must return 2 (Elite) so if-lt / if-ne comparisons allow feature access.
object GetAccountTypeFingerprint : Fingerprint(
    definingClass = "Lje/fit/account/v2/JefitAccountV2;",
    name = "getAccountType",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Function.accountType()I — legacy gateway used by presenters and ExerciseChartRepository.
// Creates a new JefitAccount, reads accountType from SharedPrefs ("accounttype" key).
// Returning 2 covers: PopupSystemRouteHandler, DayItemListPresenter, RoutineDetailsPresenter,
// ExerciseListAdapter, MuscleRecoveryViewHolder, and any other legacy caller.
object FunctionAccountTypeFingerprint : Fingerprint(
    definingClass = "Lje/fit/Function;",
    name = "accountType",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ExerciseChartRepository.isEliteUser()Z — chart-specific gate, delegates to Function.accountType().
object IsEliteUserFingerprint : Fingerprint(
    definingClass = "Lje/fit/charts/ExerciseChartRepository;",
    name = "isEliteUser",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
)
