package app.template.patches.amboss

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

private const val LOAD_RESULT = "Lde/miamed/amboss/shared/contract/permission/LoadResult;"
private const val LOAD_RESULT_COMPANION = "Lde/miamed/amboss/shared/contract/permission/LoadResult\$Companion;"
private const val LOAD_RESULT_SUCCESS = "Lde/miamed/amboss/shared/contract/permission/LoadResult\$Success;"
private const val PERM_REPO = "Lde/miamed/amboss/knowledge/permission/PermissionRepositoryImpl;"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all medical content.",
) {
    compatibleWith(Constants.AMBOSS_COMPATIBILITY)

    execute {
        // PRIMARY: LoadResult.getPermissionErrorFail() → null
        //
        // Every content gate (ArticlePresenter, ArticleContentFragmentViewModel,
        // ArticleRepositoryImpl, ImageGalleryRepositoryImpl, SettingsPresenter,
        // OfflineAccessPresenter, ContentPresenterActivityViewModel, TargetOpenerRepositoryImpl)
        // checks result.getPermissionErrorFail() != null to decide whether to show the paywall.
        //
        // Method: .locals 0 (instance method, only register is p0 = this)
        // Safe pattern for 0-locals instance method: use p0, NOT v0 (v0 doesn't exist).
        //
        // Using mutableClassDefByOrNull for explicit class resolution across all dex files.
        val getPermErrFail = mutableClassDefByOrNull(LOAD_RESULT)
            ?.methods
            ?.firstOrNull { it.name == "getPermissionErrorFail" && it.parameters.isEmpty() }
            ?: throw PatchException("LoadResult.getPermissionErrorFail not found")

        getPermErrFail.apply {
            clearBody()
            addInstructions(
                0,
                // p0 = this (LoadResult instance, also register 0 = the only register)
                // const/4 p0, 0x0 → null (integer 0 is the null reference)
                // return-object p0 → return null as PermissionError
                "const/4 p0, 0x0\nreturn-object p0",
            )
        }

        // SECONDARY: doPermissionChecksForTarget(PermissionTarget) → LoadResult.success(null)
        //
        // Reached when a PermissionTarget exists in the local DB (user has synced online).
        // Checks: errorCode != null → failure, isUsageCountExceeded → failure, expiry → failure.
        // Returns success(null) to bypass all three checks.
        //
        // Method: .locals 6 — v0 and v1 are valid registers.
        val doCheckTarget = mutableClassDefByOrNull(PERM_REPO)
            ?.methods
            ?.firstOrNull { it.name == "doPermissionChecksForTarget" }
            ?: throw PatchException("PermissionRepositoryImpl.doPermissionChecksForTarget not found")

        doCheckTarget.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    sget-object v1, $LOAD_RESULT->Companion:$LOAD_RESULT_COMPANION
                    invoke-virtual {v1, v0}, $LOAD_RESULT_COMPANION->success(Ljava/lang/Object;)$LOAD_RESULT_SUCCESS
                    move-result-object v0
                    return-object v0
                """,
            )
        }
    }
}
