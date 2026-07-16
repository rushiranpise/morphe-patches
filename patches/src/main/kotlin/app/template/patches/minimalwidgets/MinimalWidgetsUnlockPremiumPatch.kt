package app.template.patches.minimalwidgets

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MINIMAL_WIDGETS_COMPATIBILITY

@Suppress("unused")
val minimalWidgetsUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock all premium widgets.",
    default = true,
) {
    compatibleWith(MINIMAL_WIDGETS_COMPATIBILITY)

    execute {

        // Noop the pairip entry point — prevents connection to Play licensing service entirely
        PairIpInitializeLicenseCheckFingerprint.method.addInstructions(0, "return-void")

        // Noop validateResponse — belt-and-suspenders; prevents LicenseCheckException
        // if processResponse() is somehow reached with a patched/invalid signature
        PairIpValidateResponseFingerprint.method.addInstructions(0, "return-void")
        // Force isUnlocked(Context) → true. Every widget lock check (PremiumWidgetUtils.isLocked,
        // WidgetsViewModel.isPremiumUnlockedFlow, ColorThemesScreen gate) reads through here.
        PremiumManagerIsUnlockedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Force setUnlocked to always write true so any Play Billing re-sync path can't revert.
        BillingManagerSetUnlockedFingerprint.method.addInstructions(
            0,
            "const/4 p2, 0x1",
        )
    }
}
