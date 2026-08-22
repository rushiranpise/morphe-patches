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
    description = "Increases download chunk size to 1 MB and max concurrent requests to 12.",
) {
    compatibleWith(TELEGRAM_COMPATIBILITY, TELEGRAM_WEB_COMPATIBILITY, TELEGRAM_PLUS_COMPATIBILITY)
    dependsOn(telegramSpoofDependency())

    execute {
        // Replace the entire method body with maximised values.
        // Values match Telegram-Speed-Hook (AraafRoyall) which is tested working:
        //   downloadChunkSizeBig  = 1 MB  (0x100000) — fewer round-trips on fast connections
        //   maxDownloadRequests   = 12    — 12 parallel MTProto download slots
        //   maxDownloadRequestsBig = 12   — same for large file mode
        //   maxCdnParts           = 2000  — 2000 × 1 MB = 2 GB effective CDN file limit
        // const/high16 0x100000 = 0x10 << 16 = 1048576 (1 MB)
        FileLoadOperationUpdateParamsFingerprint.method.addInstructions(0, """
            const/high16 v0, 0x100000
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->downloadChunkSizeBig:I
            const/16 v0, 0xC
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxDownloadRequests:I
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxDownloadRequestsBig:I
            const/16 v0, 0x7D0
            iput v0, p0, Lorg/telegram/messenger/FileLoadOperation;->maxCdnParts:I
            return-void
        """)
    }
}
