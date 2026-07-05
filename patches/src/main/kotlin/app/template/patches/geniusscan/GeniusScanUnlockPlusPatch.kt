package app.template.patches.geniusscan

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GENIUSSCAN_COMPATIBILITY

@Suppress("unused")
val geniusScanUnlockPlusPatch = bytecodePatch(
    name = "Unlock Ultra",
    description = "Unlocks Ultra features in app.",
    default = true,
) {
    compatibleWith(GENIUSSCAN_COMPATIBILITY)

    execute {
        // Always return true for any per-feature plan check
        SubscriptionFeatureCheckFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )

        // Force the resolved plan to Ultra so the UI displays "Ultra" instead of "Basic"
        // Constructs: new tk/q(tk/b.ULTRA, tk/d.a, null) and returns it immediately
        SubscriptionPlanResolverFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Ltk/q;
                sget-object v1, Ltk/b;->ULTRA:Ltk/b;
                sget-object v2, Ltk/d;->a:Ltk/d;
                const/4 v3, 0x0
                invoke-direct {v0, v1, v2, v3}, Ltk/q;-><init>(Ltk/b;Lef/nf;Ljava/lang/String;)V
                return-object v0
            """,
        )

        // Suppress upgrade paywall screen
        UpgradeCompareActivityFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, Landroid/app/Activity;->finish()V
                return-void
            """,
        )
    }
}
