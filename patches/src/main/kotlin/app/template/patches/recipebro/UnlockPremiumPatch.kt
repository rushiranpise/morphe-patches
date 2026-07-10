package app.template.patches.recipebro

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RECIPEBRO_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock premium features in RecipeBro.",
    default = true,
) {
    compatibleWith(RECIPEBRO_COMPATIBILITY)

    execute {
        CheckLicenseFingerprint.method.addInstructions(0, "return-void")

        GetShowPremiumAfterOnboardingFingerprint.method.addInstructions(
            0, "const/4 p0, 0x0\nreturn p0",
        )

        GetBooleanFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                invoke-virtual {p1}, Ljava/lang/Enum;->ordinal()I
                move-result v0
                if-nez v0, :feature_true
                const/4 v0, 0x0
                return v0
                :feature_true
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }

        GetHardImportLimitFingerprint.method.addInstructions(
            0, "const v0, 0x7fffffff\nreturn v0",
        )

        GetSoftImportLimitFingerprint.method.addInstructions(
            0, "const v0, 0x7fffffff\nreturn v0",
        )

        // b9.a() has .locals 7 — v0-v6 are real locals, no register overlap.
        // Instruction layout: [0] getClass  [1] getActiveSubscriptions  [2] move-result-object v1
        // Inject at index 3: overwrite v1 with Collections.singleton("annual_premium").
        // isEmpty() on a singleton returns false → app sees active subscription.
        B9CustomerInfoConverterFingerprint.method.addInstructions(
            3,
            """
            const-string v0, "annual_premium"
            invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
            move-result-object v1
            """.trimIndent(),
        )
    }
}
