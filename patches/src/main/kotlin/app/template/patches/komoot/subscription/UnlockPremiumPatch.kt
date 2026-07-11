package app.template.patches.komoot.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.KOMOOT_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features and all map packs.",
    default = true,
) {
    compatibleWith(KOMOOT_COMPATIBILITY)

    execute {
        // UserV7.k0()Z — primary premium check across tour data, map overlays and feature gates.
        UserIsPremiumFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }

        // AppConfigV3.n() — server-config premium flag read during app initialisation.
        AppConfigPremiumFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object v0
                """,
            )
        }

        // bqr.g()Z — RegionsData.ownsWorldPack; fed into gpj.t (ownsWorldPackSnapshot)
        // so every region is treated as purchased. .locals 0 → use p0.
        RegionsDataOwnsWorldPackFingerprint.method.replaceInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """,
        )

        // cin.n()Z — OwnedRegion.isOwned per-region gate; .locals 0 → use p0.
        OwnedRegionIsOwnedFingerprint.method.replaceInstructions(
            0,
            """
                const/4 p0, 0x1
                return p0
            """,
        )
    }
}
