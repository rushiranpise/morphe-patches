package app.template.patches.messenger.media

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.messenger.misc.messengerSignaturePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY

// Disables automatic media transcoding / compression when sending photos and videos.
//
// Messenger re-encodes media before sending to reduce file size. This patch bypasses
// the transcoding pipeline so images and videos are sent at their original quality.
//
// Target: classes9/X/MXY → method public BdH(LX/3ky;)OperationResult
// MXY.BdH is the service operation that drives the transcoding workflow.
// Returning OperationResult.A00 (the default no-op singleton) at index 0 causes
// the operation to exit before any transcoding occurs. The media upload proceeds
// normally via the unmodified fallback path — original files are sent as-is.
//
// Note: very large videos may still be rejected server-side if they exceed
// Messenger's upload size limit (~25 MB). This patch removes client-side compression
// only; the server limit is unaffected.
//
// Ported from MessengerPro (Mino260806) MediaTranscoderHook — adapted from Xposed
// runtime hook to static bytecode patch. The Xposed hook nulled param.args to skip
// transcoding; our approach returns early from the operation method with the same effect.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes9/X/MXY.smali).
@Suppress("unused")
val messengerDisableMediaTranscodingPatch = bytecodePatch(
    name = "Disable media transcoding",
    description = "Sends photos and videos at original quality without re-encoding.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        DisableMediaTranscodingFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lcom/facebook/fbservice/service/OperationResult;->A00:Lcom/facebook/fbservice/service/OperationResult;
                return-object v0
            """.trimIndent(),
        )
    }
}
