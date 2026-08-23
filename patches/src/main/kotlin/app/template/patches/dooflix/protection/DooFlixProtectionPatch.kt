package app.template.patches.dooflix.protection

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_DOOFLIX
import app.template.patches.shared.returnEarly

@Suppress("unused")
val dooFlixProtectionPatch = bytecodePatch(
    name = "Bypass Protections",
    description = "Bypasses DNS blocker gate and tamper class detection."
) {
    compatibleWith(COMPATIBILITY_DOOFLIX)

    execute {
        // DNS blocker gate — must return false (not blocked)
        DnsBlockerGateFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        )

        // Tamper class checker — nop the entire method
        TamperClassCheckerFingerprint.method.returnEarly()
    }
}
