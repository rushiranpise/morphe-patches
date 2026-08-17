package app.template.patches.tiktok_lite.reuse

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY

@Suppress("unused")
val tiktokLiteReusePatch = bytecodePatch(
    name = "Enable Duet and Stitch",
    description = "Unlocks Duet and Stitch on all videos regardless of creator permission settings.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // getDuetSetting() -> 0 (allowed). Source: Toki hookReuseGetter("getDuetSetting").
        AwemeDuetSettingFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // getStitchSetting() -> 0 (allowed). Source: Toki hookReuseGetter("getStitchSetting").
        AwemeStitchSettingFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
