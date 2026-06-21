package app.template.patches.socialgamebox

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOCIALGAMEBOX_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Social Gamebox premium features.",
    default = true
) {
    compatibleWith(SOCIALGAMEBOX_COMPATIBILITY)

    execute {
        CustomerInfoMapFingerprint.method.apply {
            val returnIndex = instructions.indexOfLast {
                it.opcode.name.startsWith("return-object")
            }
            addInstructions(
                returnIndex,
                """
                new-instance v1, Ljava/util/HashMap;
                invoke-direct {v1, v0}, Ljava/util/HashMap;-><init>(Ljava/util/Map;)V

                new-instance v2, Ljava/util/HashMap;
                invoke-direct {v2}, Ljava/util/HashMap;-><init>()V

                const-string v3, "identifier"
                const-string v4, "premium"
                invoke-interface {v2, v3, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "isActive"
                const/4 v5, 0x1
                invoke-static {v5}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v5
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "willRenew"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "periodType"
                const-string v5, "NORMAL"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "latestPurchaseDate"
                const-string v5, "2026-06-18T00:00:00.000Z"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "latestPurchaseDateMillis"
                const-wide v6, 0x19ec73f4000L
                invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                move-result-object v6
                invoke-interface {v2, v3, v6}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "originalPurchaseDate"
                const-string v5, "2026-06-18T00:00:00.000Z"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "originalPurchaseDateMillis"
                invoke-interface {v2, v3, v6}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "expirationDate"
                const-string v5, "2036-06-18T00:00:00.000Z"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "expirationDateMillis"
                const-wide v8, 0x1f01d542000L
                invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                move-result-object v8
                invoke-interface {v2, v3, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "store"
                const-string v5, "PLAY_STORE"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "productIdentifier"
                const-string v5, "premium"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "productPlanIdentifier"
                const/4 v5, 0x0
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "isSandbox"
                const/4 v5, 0x0
                invoke-static {v5}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v5
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "unsubscribeDetectedAt"
                const/4 v5, 0x0
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "unsubscribeDetectedAtMillis"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "billingIssueDetectedAt"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "billingIssueDetectedAtMillis"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "ownershipType"
                const-string v5, "PURCHASED"
                invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                new-instance v3, Ljava/util/HashMap;
                invoke-direct {v3}, Ljava/util/HashMap;-><init>()V

                new-instance v5, Ljava/util/HashMap;
                invoke-direct {v5}, Ljava/util/HashMap;-><init>()V
                invoke-interface {v5, v4, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                new-instance v10, Ljava/util/HashMap;
                invoke-direct {v10}, Ljava/util/HashMap;-><init>()V
                invoke-interface {v10, v4, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v2, "active"
                invoke-interface {v3, v2, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v2, "all"
                invoke-interface {v3, v2, v10}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v2, "verification"
                const-string v4, "VERIFIED"
                invoke-interface {v3, v2, v4}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v2, "entitlements"
                invoke-interface {v1, v2, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                new-instance v2, Ljava/util/ArrayList;
                invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V
                const-string v3, "premium"
                invoke-interface {v2, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z

                const-string v3, "activeSubscriptions"
                invoke-interface {v1, v3, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                const-string v3, "allPurchasedProductIdentifiers"
                invoke-interface {v1, v3, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

                move-object v0, v1
                """.trimIndent()
            )
        }

        ProcessResponseFingerprint.method.addInstructions(0, "return-void")
    }
}
