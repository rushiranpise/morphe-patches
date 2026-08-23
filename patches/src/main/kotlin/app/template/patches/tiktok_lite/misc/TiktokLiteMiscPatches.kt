package app.template.patches.tiktok_lite.misc

/*
 * Ported from hxreborn/hxreborn-tiktok-patches (GPL-3.0)
 * https://github.com/hxreborn/hxreborn-tiktok-patches
 */

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val tiktokLiteStopVideoLoopingPatch = bytecodePatch(
    name = "Stop Video Looping",
    description = "Prevents videos from looping automatically after playback ends.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // TTVideoEngine.LILLLLLL(Z)V -- obfuscated in Lite; matched by string "setLooping:".
        // Inject return-void at index 0 to discard the loop flag.
        VideoEngineSetLoopingFingerprint.method.returnEarly()
    }
}


@Suppress("unused")
val tiktokLiteSanitizeShareUrlsPatch = bytecodePatch(
    name = "Sanitize Share URLs",
    description = "Removes tracking parameters (utm_campaign, share_link_id) from shared links.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // X/7it.L(String,String)String -- finds and removes tracking params from share URLs.
        // The method builds a URL string and inserts utm_campaign + share_link_id.
        // Override all return sites to strip those keys by returning early with empty string,
        // or more safely: find each String return and null the tracking params before return.
        // Simplest correct approach: at the method entry, return the first param (original URL)
        // before any tracking params are appended.
        ShareUrlTrackerFingerprint.method.let { method ->
            // .registers includes p0=first String, p1=second String
            // Return p0 (the base URL without tracking) at index 0.
            method.addInstructions(
                0,
                """
                    return-object p0
                """,
            )
        }
    }
}
