package app.template.patches.yatri

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.YATRI_COMPATIBILITY

@Suppress("unused")
val yatriUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks Yatri VIP by forcing active plan status and spoofing active plan DB query."
) {
    compatibleWith(YATRI_COMPATIBILITY)

    execute {
        // Layer 1: DataStore plan status → always true (ads, VIP logo, feature gates)
        UserActivePlanStatusFingerprint
            .match(classDefBy(UserActivePlanStatusFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object p0
                    """.trimIndent()
                )
            }

        // Layer 2: DB active plan query → return dummy model with future end_date
        // Uses no-arg constructor + setters to stay within .locals 7 budget
        ActivePlanDaoFingerprint
            .match(classDefBy(ActivePlanDaoFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    new-instance v0, Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;
                    invoke-direct {v0}, Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;-><init>()V
                    const-string v1, "success"
                    invoke-virtual {v0, v1}, Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;->setStatus(Ljava/lang/String;)V
                    const-string v1, "2026-01-01"
                    invoke-virtual {v0, v1}, Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;->setStart_date(Ljava/lang/String;)V
                    const-string v1, "2099-12-31"
                    invoke-virtual {v0, v1}, Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;->setEnd_date(Ljava/lang/String;)V
                    return-object v0
                    """.trimIndent()
                )
            }
    }
}
