package app.template.patches.reddit.layout.screenshotpopup

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Screenshot popup fingerprints — verified against 2026.32.0 smali
//
// The screenshot-share banner is driven by two coroutine lambdas:
//
//  1. RedditScreenshotTriggerSharingListener$ScreenshotBanner$1$1
//     (classes3) — inner lambda inside ScreenshotBanner coroutine.
//     Contains the `$shouldShowBanner$delegate` field + Boolean.TRUE + setValue.
//     .field final synthetic $shouldShowBanner$delegate:Lqlw;   ← obfuscated type
//     Pattern in invokeSuspend:
//       iget-object p1, p0, ...->$shouldShowBanner$delegate:Lqlw;
//       sget-object v0,  Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
//       invoke-interface { p1, v0 }, Lqlw;->setValue(Ljava/lang/Object;)V
//
//  2. ScreenshotTakenBannerKt$ScreenshotTakenBanner$1$1
//     (classes3) — same structure.
//
// Fingerprint strategy: use the stable Boolean.TRUE sget + setValue interface
// call as filters. The definingClass is stable (non-obfuscated outer class name).
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Trigger-side lambda: sets shouldShowBanner=TRUE when a screenshot is detected.
 * Exact smali: iget-object → sget Boolean.TRUE → invoke-interface setValue
 */
internal object ScreenshotTriggerBannerSetValueFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/sharing/screenshot/RedditScreenshotTriggerSharingListener\$ScreenshotBanner\$1\$1;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            name = "\$shouldShowBanner\$delegate",
        ),
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            smali = "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        ),
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE,
            name = "setValue",
        ),
    ),
)

/**
 * UI-side lambda: sets shouldShowBanner=TRUE in the Compose layer.
 * Same structure but in the composables package.
 */
internal object ScreenshotTakenBannerSetValueFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/sharing/screenshot/composables/ScreenshotTakenBannerKt\$ScreenshotTakenBanner\$1\$1;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            name = "\$shouldShowBanner\$delegate",
        ),
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            smali = "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        ),
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE,
            name = "setValue",
        ),
    ),
)
