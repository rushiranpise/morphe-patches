package app.template.patches.citymapper

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CITYMAPPER_COMPATIBILITY

@Suppress("unused")
val citymapperUnlockClubPatch = bytecodePatch(
    name = "Unlock Club",
    description = "Unlocks Citymapper Club Membership Note: Need to manually Purchase inside APP!.",
    default = true,
) {
    compatibleWith(CITYMAPPER_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        CitymapperApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/CitymapperHelper;->init()V",
        )

        FeatureFlagIsEnabledFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lst3;->USE_FAKE_SUBSCRIPTION:Lst3;
                if-ne p0, v0, :ignore
                const/4 v0, 0x1
                return v0
                :ignore
                """.trimIndent()
        )

    }
}
