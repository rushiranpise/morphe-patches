package app.template.patches.reddit.layout.screenshotpopup

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

// ─────────────────────────────────────────────────────────────────────────────
// Disable Screenshot Popup
//
// Both coroutine lambdas set shouldShowBanner via:
//   sget-object vN, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
//   invoke-interface { p1, vN }, Lqlw;->setValue(Ljava/lang/Object;)V
//
// We overwrite the register containing TRUE with FALSE immediately after the
// sget, so setValue always receives FALSE and the banner is never displayed.
//
// instructionMatches[1] = SGET_OBJECT that loads Boolean.TRUE
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditDisableScreenshotPopupPatch = bytecodePatch(
    name = "Disable Screenshot Popup",
    description = "Suppresses the share prompt that appears after taking a screenshot.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        listOf(
            ScreenshotTriggerBannerSetValueFingerprint,
            ScreenshotTakenBannerSetValueFingerprint,
        ).forEach { fingerprint ->
            runCatching {
                val sgetMatch = fingerprint.instructionMatches[1]   // Boolean.TRUE sget
                val sgetIndex = sgetMatch.index
                val reg = sgetMatch.getInstruction<OneRegisterInstruction>().registerA

                // Overwrite TRUE with FALSE so setValue(false) → banner hidden
                fingerprint.method.addInstructions(
                    sgetIndex + 1,
                    "sget-object v$reg, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;",
                )
            }
        }
    }
}
