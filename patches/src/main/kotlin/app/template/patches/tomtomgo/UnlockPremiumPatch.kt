package app.template.patches.tomtomgo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TOMTOMGO_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks car and truck premium features.",
) {
    compatibleWith(TOMTOMGO_COMPATIBILITY)

    execute {
        // Car: combiner always true
        HasActiveSubscriptionsCombinerFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Car: mapper always true
        HasActiveSubscriptionsMapperFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Truck path 1: Db/d default branch (a>=4) → TRUE
        TruckGateDefaultBranchFingerprint.method.addInstructions(0, """
            iget v0, p0, LDb/d;->a:I
            const/4 v1, 0x4
            if-lt v0, v1, :cond_original
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
            :cond_original
        """)

        // Truck path 2: showstopper gate → false
        TruckShowstopperGateFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                const/4 v0, 0x0
                return v0
            """)
        }

        // Truck path 3: banner dismissed → TRUE → NOT(TRUE)=FALSE → banner hidden
        TruckBannerDismissedFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        // Truck path 4: e9/P0.onClick(a=1) opens truck subscription paywall → skip
        TruckNavBannerSubscribeFingerprint.method.addInstructions(0, """
            iget v0, p0, Le9/P0;->a:I
            const/4 v1, 0x1
            if-ne v0, v1, :cond_original
            return-void
            :cond_original
        """)
    }
}
