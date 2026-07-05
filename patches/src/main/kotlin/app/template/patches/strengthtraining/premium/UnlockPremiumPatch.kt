package app.template.patches.strengthtraining.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

// Strength Training by Muscle Motion — Premium unlock
//
// Architecture:
//   Content gate (exercise_details/d.smali invoke() handler):
//     iget-boolean v9, content, Lq6/c;->c:Z   // content.isPaid
//     if-eqz v9, :cond_3                       // isPaid==false → open content (d0)
//     ...                                       // isPaid==true  → show paywall (e0)
//
//   isPaid propagation chain:
//     API Response.isPaid() → cached into Room DB Entity.isPaid()
//     → mapper (m/t.smali, b0/j1.smali) reads Entity.isPaid()
//     → domain model (q6/c.c, j1/a.d) carries isPaid
//     → UI gate reads domain.isPaid directly (no getter, iget-boolean)
//
//   User subscription gate:
//     PaidStatus.isPaidStatusEmpty() → true when type+billing blank → show paywall
//     PaidStatus.isPremium()         → server-set premium flag
//     LoginResponse.isPaid()         → user-level paid flag from login API
//
// Fix strategy:
//   1. Content layer: all Response + Entity isPaid():Z → false  (content appears free)
//      isPaid():Boolean → Boolean.FALSE                         (nullable variant)
//   2. User layer:    LoginResponse.isPaid():Z → true           (user appears paid)
//                     PaidStatus.isPaidStatusEmpty() → false    (not empty = subscribed)
//                     PaidStatus.isPremium() → true             (premium flag on)

// All Response class descriptors whose isPaid():Z must return false
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

// All Entity class descriptors whose isPaid():Z must return false
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

// Classes whose isPaid():Ljava/lang/Boolean; must return Boolean.FALSE
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
    description = "Unlocks all premium content in Strength Training by Muscle Motion. "+
    "Note: Create an exercise plan to access videos",
) {
    compatibleWith(Constants.STRENGTHTRAINING_COMPATIBILITY)

    execute {
        // ── Helper lambdas ─────────────────────────────────────────────────────
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

        // ── Content layer: all isPaid() → false ────────────────────────────────
        // Covers both fresh-from-network (Response) and cached (Entity) paths.
        // When isPaid==false the exercise_details gate calls d0() (open content)
        // instead of e0() (show paywall).
        CONTENT_RESPONSE_CLASSES_Z.forEach { forceIsPaidFalse(it) }
        CONTENT_ENTITY_CLASSES_Z.forEach { forceIsPaidFalse(it) }
        CONTENT_CLASSES_BOOLEAN.forEach { forceIsPaidBooleanFalse(it) }

        // ── User subscription layer ────────────────────────────────────────────

        // LoginResponse.isPaid():Z → true (user appears paid in login response)
        LoginResponseIsPaidFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // PaidStatus.isPaidStatusEmpty():Z → false
        // PricingFragment checks this to decide whether to show the paywall.
        // false = "status not empty" = user has an active subscription.
        IsPaidStatusEmptyFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // PaidStatus.isPremium():Z → true
        // Read by profile badge, analytics, and FAQ subscription check.
        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // PaidStatus.getType() → "indi"
        // getCurrentPlan() maps "indi" → "Individual" for display in profile.
        GetTypeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string v0, "indi"
                    return-object v0
                """,
            )
        }

        // PaidStatus.getBilling() → "Yearly"
        // Profile fragment displays this as the billing period.
        GetBillingFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string v0, "Yearly"
                    return-object v0
                """,
            )
        }

        // LoginResponse.getExpiryDate() → Long(4070908800000) = Jan 1 2099
        // Profile fragment shows this as the subscription expiry date.
        // Must return a boxed Long object (Ljava/lang/Long;), not a primitive.
        GetExpiryDateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-wide v0, 0x3B4AF5B5B00L
                    invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                    move-result-object v0
                    return-object v0
                """,
            )
        }

    }
}
