package app.template.patches.pocketcasts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.POCKET_CASTS_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPlusPatch = bytecodePatch(
    name = "Unlock Patron",
    description = "Unlocks Pocket Casts Patron yearly feature checks.",
    default = true,
) {
    compatibleWith(POCKET_CASTS_COMPATIBILITY)

    execute {
        MembershipStatusFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Ld5e;->y:La5e;
                    return-object v0
                """.trimIndent(),
            )
        }

        MembershipHasFeatureFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        SubscriptionLifetimeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        SubscriptionStatusMapperFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lau/com/shiftyjelly/pocketcasts/models/type/Membership;
                    new-instance v1, Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;
                    sget-object v2, Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;->Patron:Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;
                    sget-object v3, Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;->Yearly:Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;
                    sget-object v4, Lapc;->Android:Lapc;
                    const-wide v5, 0x3bb2cc5c000L
                    invoke-static {v5, v6}, Lj$/time/Instant;->ofEpochMilli(J)Lj$/time/Instant;
                    move-result-object v5
                    const/4 v6, 0x1
                    const/16 v7, 0x270f
                    const/4 v8, 0x1
                    invoke-direct/range {v1 .. v8}, Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;-><init>(Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;Lau/com/shiftyjelly/pocketcasts/payment/BillingCycle;Lapc;Lj$/time/Instant;ZIZ)V
                    sget-object v2, Llm3;->y:Llm3;
                    invoke-direct {v0, v1, v5, v2}, Lau/com/shiftyjelly/pocketcasts/models/type/Membership;-><init>(Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;Lj$/time/Instant;Ljava/util/List;)V
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}
