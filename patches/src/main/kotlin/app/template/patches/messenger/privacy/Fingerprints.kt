package app.template.patches.messenger.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── Screenshot detector observer registration — classes4/X/9Sa.A00()V ───────
// 9Sa is the base class of ThreadScreenshotDetector. Its private A00()V method
// registers a ScreenshotContentObserver against MediaStore.Images.EXTERNAL_CONTENT_URI.
// This is the root of both screenshot detection features:
//
//  1. FLAG_SECURE enforcement — triggered via the registered observer path that
//     calls Window.addFlags(0x2000) through the Cp0 lambda chain
//  2. "Recording detected" notification — fired from ScreenshotContentObserver.onChange
//
// Returning void at index 0 prevents the ContentObserver from ever being registered,
// silently disabling both features at their shared root.
//
// Stable anchors:
//   • string "Required value was null." — Kotlin not-null check inside A00
//   • fieldAccess SGET on MediaStore$Images$Media;->EXTERNAL_CONTENT_URI (Android SDK)
//   • fieldAccess IGET-OBJECT on ScreenshotContentObserver field (non-obf class type)
//   • PRIVATE, returns V, no parameters
//
// Verified: classes4/X/9Sa.smali → method private final A00()V — v573.
internal val ScreenshotObserverRegistrationFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        string("Required value was null."),
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Landroid/provider/MediaStore\$Images\$Media;",
            name = "EXTERNAL_CONTENT_URI",
        ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Lcom/facebook/screenshot/ScreenshotContentObserver;",
        ),
    ),
)

// ─── Screenshot ContentObserver callback — ScreenshotContentObserver.onChange ─
// onChange(ZLandroid/net/Uri;)V fires on the main thread every time the
// MediaStore Images content URI changes (i.e. a screenshot or screen recording
// is saved). It reads the new file metadata and dispatches a "screenshot taken"
// event to all registered listeners, which then show the in-app detection banner.
//
// Returning void at index 0 silences the callback — no event is dispatched,
// no banner appears, and no "recording detected" notification is sent.
//
// Stable anchors:
//   • string "date_added" — MediaStore column queried at the top of onChange
//   • fieldAccess SGET-OBJECT on MediaStore$Images$Media;->EXTERNAL_CONTENT_URI
//   • definingClass = non-obfuscated ScreenshotContentObserver
//   • PUBLIC, parameters = [Z, Landroid/net/Uri;], returns V
//
// Verified: classes4/com/facebook/screenshot/ScreenshotContentObserver.smali
//   → method public onChange(ZLandroid/net/Uri;)V — v573.
internal val ScreenshotOnChangeFingerprint = Fingerprint(
    definingClass = "Lcom/facebook/screenshot/ScreenshotContentObserver;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Z", "Landroid/net/Uri;"),
    filters = listOf(
        string("date_added"),
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Landroid/provider/MediaStore\$Images\$Media;",
            name = "EXTERNAL_CONTENT_URI",
        ),
    ),
)
