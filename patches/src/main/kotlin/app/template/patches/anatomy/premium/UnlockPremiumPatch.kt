package app.template.patches.anatomy.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

private val CONTENT_RESPONSE_CLASSES_Z = listOf(
    "Lair/com/musclemotion/data/remote/response/anatomy/AnatomyNodeToItemResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/BoneJointPreviewResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/BoneOverviewResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/SubJointOverviewResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/SubMuscleOverviewResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/SubMusclePreviewResponse;",
    "Lair/com/musclemotion/data/remote/response/book/BookChapterResponse;",
    "Lair/com/musclemotion/data/remote/response/collection/CollectionFolderFileResponse;",
    "Lair/com/musclemotion/data/remote/response/custom_exercise/CustomExerciseFullResponse;",
    "Lair/com/musclemotion/data/remote/response/custom_exercise/CustomExerciseResponse;",
    "Lair/com/musclemotion/data/remote/response/exercises/EnrichmentVideoResponse;",
    "Lair/com/musclemotion/data/remote/response/exercises/ExerciseFullResponse;",
    "Lair/com/musclemotion/data/remote/response/exercises/ExerciseResponse;",
    "Lair/com/musclemotion/data/remote/response/home/NewPopularItemResponse;",
    "Lair/com/musclemotion/data/remote/response/home/RecommendedItemResponse;",
    "Lair/com/musclemotion/data/remote/response/most_viewed/TheoryMostViewedVideoResponse;",
    "Lair/com/musclemotion/data/remote/response/plan/PlanResponse;",
    "Lair/com/musclemotion/data/remote/response/search/SearchAnatomyResponse;",
    "Lair/com/musclemotion/data/remote/response/search/SearchCustomExerciseResponse;",
    "Lair/com/musclemotion/data/remote/response/search/SearchExerciseResponse;",
    "Lair/com/musclemotion/data/remote/response/search/SearchSkeletalResponse;",
    "Lair/com/musclemotion/data/remote/response/search/SearchTheoryResponse;",
    "Lair/com/musclemotion/data/remote/response/theory/TheoryExerciseResponse;",
    "Lair/com/musclemotion/data/remote/response/theory/TheoryItemPreviewResponse;",
    "Lair/com/musclemotion/data/remote/response/theory/TheoryItemResponse;",
    "Lair/com/musclemotion/data/remote/response/theory/TheorySubItemResponse;",
)

private val CONTENT_ENTITY_CLASSES_Z = listOf(
    "Lair/com/musclemotion/data/local/entity/anatomy/AnatomyModelEntity;",
    "Lair/com/musclemotion/data/local/entity/anatomy/BoneJointPreviewEntity;",
    "Lair/com/musclemotion/data/local/entity/anatomy/BoneOverviewEntity;",
    "Lair/com/musclemotion/data/local/entity/anatomy/SubJointOverviewEntity;",
    "Lair/com/musclemotion/data/local/entity/anatomy/SubMuscleEntity;",
    "Lair/com/musclemotion/data/local/entity/anatomy/SubMusclePreviewEntity;",
    "Lair/com/musclemotion/data/local/entity/book/BookChapterEntity;",
    "Lair/com/musclemotion/data/local/entity/collection/CollectionFolderFileEntity;",
    "Lair/com/musclemotion/data/local/entity/custom_exercises/CustomExerciseEntity;",
    "Lair/com/musclemotion/data/local/entity/custom_exercises/CustomExerciseFullEntity;",
    "Lair/com/musclemotion/data/local/entity/exercises/EnrichmentVideoEntity;",
    "Lair/com/musclemotion/data/local/entity/exercises/ExerciseEntity;",
    "Lair/com/musclemotion/data/local/entity/exercises/ExerciseFullEntity;",
    "Lair/com/musclemotion/data/local/entity/exercises/ExerciseVideoEntity;",
    "Lair/com/musclemotion/data/local/entity/home/NewPopularItemEntity;",
    "Lair/com/musclemotion/data/local/entity/home/RecentlyViewedItemEntity;",
    "Lair/com/musclemotion/data/local/entity/home/RecommendedItemEntity;",
    "Lair/com/musclemotion/data/local/entity/most_viewed/MostViewedVideoEntity;",
    "Lair/com/musclemotion/data/local/entity/plan/PlanEntity;",
    "Lair/com/musclemotion/data/local/entity/plan/WorkoutItemEntity;",
    "Lair/com/musclemotion/data/local/entity/theory/TheoryExerciseEntity;",
    "Lair/com/musclemotion/data/local/entity/theory/TheoryItemEntity;",
    "Lair/com/musclemotion/data/local/entity/theory/TheoryItemPreviewEntity;",
    "Lair/com/musclemotion/data/local/entity/theory/TheorySubItemEntity;",
)

private val CONTENT_CLASSES_BOOLEAN = listOf(
    "Lair/com/musclemotion/data/remote/response/exercises/VideoResponse;",
    "Lair/com/musclemotion/data/remote/response/anatomy/SubMusclePartResponse;",
    "Lair/com/musclemotion/data/remote/response/plan/WorkoutItemResponse;",
    "Lair/com/musclemotion/data/remote/response/home/RecentlyViewedItemResponse;",
    "Lair/com/musclemotion/data/local/entity/anatomy/SubMusclePartEntity;",
)

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium content in Anatomy by Muscle Motion.",
) {
    compatibleWith(Constants.ANATOMY_COMPATIBILITY)

    execute {
        fun forceIsPaidFalse(descriptor: String) {
            mutableClassDefByOrNull(descriptor)
                ?.methods
                ?.filter { it.name == "isPaid" && it.returnType == "Z" }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
        }

        fun forceIsPaidBooleanFalse(descriptor: String) {
            mutableClassDefByOrNull(descriptor)
                ?.methods
                ?.filter { it.name == "isPaid" && it.returnType == "Ljava/lang/Boolean;" }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(
                        0,
                        """
                            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                            return-object v0
                        """,
                    )
                }
        }

        CONTENT_RESPONSE_CLASSES_Z.forEach { forceIsPaidFalse(it) }
        CONTENT_ENTITY_CLASSES_Z.forEach { forceIsPaidFalse(it) }
        CONTENT_CLASSES_BOOLEAN.forEach { forceIsPaidBooleanFalse(it) }

        LoginResponseIsPaidFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        IsPaidStatusEmptyFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // l/m0.d():Z → true
        // Drives: settings pricing row visibility + profile "purchased" badge.
        // Reads sp_premium SharedPreference; patching removes the upgrade row from settings.
        UserPrefsIsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // PaidStatus.getType():String → "pro"
        // Shown as the subscription plan label in the profile "Current Plan" section.
        PaidStatusGetTypeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"pro\"\nreturn-object v0")
        }

        // PaidStatus.getBilling():String → "Yearly"
        // Shown as the billing period in the profile subscription info group.
        PaidStatusGetBillingFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Yearly\"\nreturn-object v0")
        }
    }
}
