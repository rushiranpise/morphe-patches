package app.template.patches.telegram.content

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.FileLoadOperationUpdateParamsFingerprint

@Suppress("unused")
val telegramDownloadBoostPatch = bytecodePatch(
    name = "Download speed boost",
    description = "Increases download chunk size to 512 KB and max concurrent requests to 8.",
) {
    compatibleWith( TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Replace the entire method body — always use premium-tier values.
        // maxCdnParts is calculated from totalFileSize (0x7d000000 = 2 GB limit)
        // divided by downloadChunkSizeBig, matching the original formula.
        // const/high16 0x80000 = 0x80000 << 16 = 524288 (512 KB)
        FileLoadOperationUpdateParamsFingerprint.method.addInstructions(0, """
            const/high16 v0, 0x80000
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->downloadChunkSizeBig:I
            const/16 v0, 0x8
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxDownloadRequests:I
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxDownloadRequestsBig:I
            const-wide/32 v0, 0x7d000000
            iget v2, p0, Lorg/telegram/messenger/FileLoadOperation;->downloadChunkSizeBig:I
            int-to-long v2, v2
            div-long/2addr v0, v2
            long-to-int v0, v0
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxCdnParts:I
            return-void
        """)
    }
}
