package app.template.patches.telegram.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TELEGRAM_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_PLUS_COMPATIBILITY
import app.template.patches.shared.Constants.TELEGRAM_WEB_COMPATIBILITY
import app.template.patches.telegram.signature.telegramSpoofDependency

/**
 * The former ChatPullingDownDrawable implementation is not present in any of
 * the supplied 12.10.x DEX sets. The old three fingerprints therefore must
 * not be required: doing so is exactly what caused the 12.10.4 patch failure.
 *
 * This patch intentionally becomes a compatibility-safe no-op until a stable
 * semantic channel-switch hook can be identified without disabling Telegram's
 * generic RecyclerView edge-pull behavior.
 */
@Suppress("unused")
val telegramDisableChannelSwitchingPatch = bytecodePatch(
    name = "Disable channel switching",
    description = "Disables the pull-down gesture that switches to the next unread channel when a stable target exists.",
    default = true,
) {
    compatibleWith(
        TELEGRAM_COMPATIBILITY,
        TELEGRAM_PLUS_COMPATIBILITY,
        TELEGRAM_WEB_COMPATIBILITY,
    )
    dependsOn(telegramSpoofDependency())

    execute {
        // No unsafe 12.10.x replacement is applied here.
        // The obsolete ChatPullingDownDrawable hooks are absent from all three
        // inspected APKs, so leaving this patch as a no-op prevents a false
        // positive hook from breaking the rest of the patch set.
    }
}