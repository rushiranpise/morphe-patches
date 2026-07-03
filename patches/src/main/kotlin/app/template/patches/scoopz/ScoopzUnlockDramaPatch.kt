package app.template.patches.scoopz

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SCOOPZ_COMPATIBILITY

/**
 * Unlocks drama mini-series episode gating in Scoopz — Local & Breaking News
 * (com.localaiapp.scoops v3.27.0).
 *
 * ## Monetization model
 *
 * Scoopz v3.27.0 does NOT use a subscription or IAP billing system.
 * Drama mini-series use an IAA (in-app advertising) model:
 *   - `DramaMetadata.free_episodes_count` — episodes freely playable
 *   - `GlobalDataCache.iaaDramaUnlockMap` — per-dramaId map of ad-unlocked indices
 *   - Episodes beyond free_episodes_count require watching an ad to unlock
 *
 * ## Lock flow
 *
 * 1. User taps an episode card in the drama series list
 * 2. `onboarding/a.x(chaptId)V` is invoked
 * 3. Queries `iaaDramaUnlockMap.getOrDefault(dramaId, 0)` → unlock_count
 * 4. If `chaptId > unlock_count` → calls `q()` (pause player) and emits to
 *    StateFlow `v`, which triggers `DramaEpisodeBottomSheetDialogFragment`
 *    (the ad-watch prompt / lock screen)
 * 5. After watching ad → `updateIaaDramaUnlockIdx(dramaId, newIdx)` updates the map
 *
 * ## Layers
 *
 * **Layer 1 — episode play gate (onboarding/a.x)**
 * Replace body with `return-void` so the lock branch (`chaptId > unlock_count`)
 * is never reached. Every episode tap always proceeds to play.
 *
 * **Layer 2 — IAA unlock map writer (GlobalDataCache.updateIaaDramaUnlockIdx)**
 * Replace body to always store `Integer.MAX_VALUE` for the dramaId, so any
 * subsequent map query returns the maximum possible unlock index. Covers the
 * ad-SDK reporting path as belt-and-suspenders.
 */
@Suppress("unused")
val scoopzUnlockDramaPatch = bytecodePatch(
    name = "Unlock Drama Episodes",
    description = "Bypasses the IAA (ad-watch-to-unlock) episode gate for drama mini-series.",
    default = true,
) {
    compatibleWith(SCOOPZ_COMPATIBILITY)

    execute {
        // ── Layer 1: episode play gate ────────────────────────────────────────
        // onboarding/a.x(int chaptId)V — bypass chaptId > unlock_count lock branch.
        EpisodePlayGateFingerprint
            .match(classDefBy(EpisodePlayGateFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }

        // ── Layer 2: IAA unlock map writer ────────────────────────────────────
        // GlobalDataCache.updateIaaDramaUnlockIdx(dramaId, idx)V
        // Store Integer.MAX_VALUE so the map always reports full unlock.
        UpdateUnlockIdxFingerprint
            .match(classDefBy(UpdateUnlockIdxFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    if-eqz p1, :skip
                    iget-object v0, p0, Lcom/particlemedia/data/GlobalDataCache;->iaaDramaUnlockMap:Ljava/util/HashMap;
                    const v1, 0x7fffffff
                    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v1
                    invoke-virtual {v0, p1, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                    :skip
                    return-void
                    """.trimIndent()
                )
            }
    }
}
