package app.template.patches.tiktok_lite.downloads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val tiktokLiteDownloadsPatch = bytecodePatch(
    name = "Downloads",
    description = "Enables downloading all videos",
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // Gate 1: ACL code gate -- X/5mk.LD(Aweme)Z always returns true.
        DownloadAllowedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Gate 2: Video status gate -- X/5mk.LCI(Aweme)Z always returns true.
        // Bypasses inReviewing/isProhibited/isDelete blocks on individual videos.
        VideoStatusGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Gate 3: Music copyright gate -- X/5mk.LFFFF(Aweme)Z always returns true.
        // Bypasses music copyright download blocks. .registers 1 so use p0.
        MusicCopyrightGateFingerprint.method.addInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """,
        )

        // Watermark: swap Video->downloadAddr with newDownloadAddr in X/7ZV.LB.
        // The download builder reads Video->downloadAddr via iget-object directly,
        // bypassing any getter patch. Inject at index 0: load the Video object,
        // check newDownloadAddr, and iput it over downloadAddr so all subsequent
        // reads in this method get the clean URL. Uses scratch registers v0-v1.
        VideoGetDownloadAddrFingerprint.method.addInstructionsWithLabels(
            0,
            """
                iget-object v0, p0, Lcom/ss/android/ugc/aweme/feed/model/Aweme;->video:Lcom/ss/android/ugc/aweme/feed/model/Video;
                if-eqz v0, :no_video
                iget-object v1, v0, Lcom/ss/android/ugc/aweme/feed/model/Video;->newDownloadAddr:Lcom/ss/android/ugc/aweme/base/model/UrlModel;
                if-eqz v1, :no_video
                iput-object v1, v0, Lcom/ss/android/ugc/aweme/feed/model/Video;->downloadAddr:Lcom/ss/android/ugc/aweme/base/model/UrlModel;
                :no_video
                nop
            """,
        )

        // Watermark transcode: X/7hr.L -- override transcode register to 1 (no watermark path).
        // Find ACLCommonShare->transcode iget manually since fingerprint uses pure custom.
        DownloadTranscodeFingerprint.method.let { method ->
            val transcodeIdx = method.implementation!!.instructions
                .indexOfFirst { insn ->
                    insn.opcode.name == "IGET" &&
                        insn.toString().contains("ACLCommonShare;->transcode:I")
                }
            if (transcodeIdx >= 0) {
                val transcodeReg = method
                    .getInstruction<OneRegisterInstruction>(transcodeIdx)
                    .registerA
                method.addInstructions(transcodeIdx + 1, "const/4 v$transcodeReg, 0x1")
            }
        }
    }
}
