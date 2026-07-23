package app.template.patches.trackerdetect.sounddelay

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TRACKERDETECT_COMPATIBILITY

/**
 * Removes the 10-minute play-sound wait in Tracker Detect.
 *
 * ## Background
 * Apple's Tracker Detect enforces a 10-minute delay before the "Play Sound" button
 * becomes available on a detected tracker. The delay is computed in Z1/Z.<clinit>:
 *
 *   const/16 v0, 0xa       // 10 (minutes)
 *   int-to-long v0, v0
 *   const-wide/16 v2, 0x3c // 60 (seconds per minute)
 *   mul-long/2addr v0, v2  // = 600 seconds
 *   sput-wide v0, LZ1/Z;->b:J
 *
 * The item detail composable reads Z1/Z.b, subtracts elapsed time, and either shows
 * the Play Sound button (elapsed >= delay) or a "X minutes remaining" label (elapsed < delay).
 *
 * ## Patch
 * Replace `const/16 v0, 0xa` (value 10) with `const/4 v0, 0x0` (value 0).
 * The multiplication then yields 0 × 60 = 0, so elapsed time (always >= 0) satisfies
 * the `cmp-long >= 0` check immediately — Play Sound is available as soon as a tracker
 * is detected, with no waiting required.
 *
 * const/4 is a single-word instruction; const/16 is two words. They encode differently
 * but both fit in register v0 and the register width (4 registers declared) is unchanged.
 * We use replaceInstruction at the literal(0xa) match index to keep the instruction count
 * stable and avoid disturbing the sput-object of Z1/Z.a that precedes it.
 */
@Suppress("unused")
val trackerDetectSoundDelayPatch = bytecodePatch(
    name = "Remove Sound Delay",
    description = "Removes the 10-minute waiting period before playing a sound on a detected tracker.",
    default = true
) {
    compatibleWith(TRACKERDETECT_COMPATIBILITY)

    execute {
        // instructionMatches[0] → const/16 v0, 0xa  (the minute count)
        val match = PlaySoundDelayClinitFingerprint.match()
        val idx = match.instructionMatches[0].index

        // Replace const/16 v0, 0xa  with  const/4 v0, 0x0
        // Encoding changes from 2-unit const/16 to 1-unit const/4.
        // replaceInstruction handles the size difference in the instruction list.
        match.method.replaceInstruction(idx, "const/4 v0, 0x0")
    }
}
