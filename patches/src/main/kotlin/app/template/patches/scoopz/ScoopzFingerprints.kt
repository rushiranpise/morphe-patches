package app.template.patches.scoopz

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * BloomVideoFragment / drama player episode gating — onboarding/a.x(int)V.
 *
 * Called when the user taps an episode in the drama series list.
 * Queries GlobalDataCache.iaaDramaUnlockMap for the highest episode index
 * unlocked via in-app ads (default 0). If the tapped episode's chaptId exceeds
 * the unlocked count, it pauses the player and emits to StateFlow v, which
 * triggers the DramaEpisodeBottomSheetDialogFragment lock screen requiring
 * the user to watch an ad to unlock.
 *
 * The critical branch is:
 *   if (chaptId <= unlock_count) → play (goto :cond_4 / return-void)
 *   else → pause + show lock sheet
 *
 * Fingerprinted by definingClass + method name "x" + parameter (I) + return void.
 * Replacing the body with return-void makes every episode always branch to play,
 * bypassing the unlock gate entirely.
 */
internal val EpisodePlayGateFingerprint = Fingerprint(
    definingClass = "Lcom/particlemedia/video/stream/onboarding/a;",
    name = "x",
    parameters = listOf("I"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/**
 * GlobalDataCache.updateIaaDramaUnlockIdx(String, int)V — IAA unlock index writer.
 *
 * Called after an ad is successfully watched to record how many episodes are now
 * unlocked for a given dramaId. The map is queried via getOrDefault(dramaId, 0)
 * each time an episode is tapped — if the stored value is less than the episode's
 * chaptId the lock screen fires.
 *
 * Fingerprinted by the stable public method name in a non-obfuscated utility class.
 * Replacing the body to always store Integer.MAX_VALUE ensures any dramaId added
 * to the map (e.g. after the ad SDK reports a view) becomes permanently fully
 * unlocked.
 *
 * Note: this is belt-and-suspenders alongside EpisodePlayGateFingerprint —
 * the gate patch covers playback; this covers the ad-SDK reporting path.
 */
internal val UpdateUnlockIdxFingerprint = Fingerprint(
    definingClass = "Lcom/particlemedia/data/GlobalDataCache;",
    name = "updateIaaDramaUnlockIdx",
    parameters = listOf("Ljava/lang/String;", "I"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)
