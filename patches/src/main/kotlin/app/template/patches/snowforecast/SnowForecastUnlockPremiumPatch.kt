package app.template.patches.snowforecast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SNOWFORECAST_COMPATIBILITY

@Suppress("unused")
val snowForecastUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium features."
) {
    compatibleWith(SNOWFORECAST_COMPATIBILITY)

    execute {
        val premiumEntitlementInstructions = """
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
            const-string v5, "2026-07-02T00:00:00.000Z"
            invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "latestPurchaseDateMillis"
            const-wide v6, 0x197cb20fc00L
            invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
            move-result-object v6
            invoke-interface {v2, v3, v6}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "originalPurchaseDate"
            const-string v5, "2026-07-02T00:00:00.000Z"
            invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "originalPurchaseDateMillis"
            invoke-interface {v2, v3, v6}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "expirationDate"
            const-string v5, "2100-01-01T00:00:00.000Z"
            invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "expirationDateMillis"
            const-wide v8, 0x6bfc23ac000L
            invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
            move-result-object v8
            invoke-interface {v2, v3, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "store"
            const-string v5, "PLAY_STORE"
            invoke-interface {v2, v3, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "productIdentifier"
            const-string v5, "com.snow_forecast.application.premium.12_months"
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
            const-string v6, "pro"
            invoke-interface {v5, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v7, "subscription"
            invoke-interface {v5, v7, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "snow_membership_v1"
            invoke-interface {v5, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "membership"
            invoke-interface {v5, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "SnowPro"
            invoke-interface {v5, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "com.snow_forecast.application.premium.12_months"
            invoke-interface {v5, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            new-instance v10, Ljava/util/HashMap;
            invoke-direct {v10}, Ljava/util/HashMap;-><init>()V
            invoke-interface {v10, v4, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "pro"
            invoke-interface {v10, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            invoke-interface {v10, v7, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "snow_membership_v1"
            invoke-interface {v10, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "membership"
            invoke-interface {v10, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "SnowPro"
            invoke-interface {v10, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v6, "com.snow_forecast.application.premium.12_months"
            invoke-interface {v10, v6, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v2, "active"
            invoke-interface {v3, v2, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v2, "all"
            invoke-interface {v3, v2, v10}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v2, "verification"
            const-string v5, "VERIFIED"
            invoke-interface {v3, v2, v5}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v2, "entitlements"
            invoke-interface {v1, v2, v3}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            new-instance v2, Ljava/util/ArrayList;
            invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V
            invoke-interface {v2, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "com.snow_forecast.application.premium.weekly_auto_renew"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1:monthly-autorenewing"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1:snow-membership-v1-1-month"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "com.snow_forecast.application.premium.1_month"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1:6months-autorenewing"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1:snow-membership-v1-6-months"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "com.snow_forecast.application.premium.6_months"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1:snow-membership-v1-yearly"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "com.snow_forecast.application.premium.12_months"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "snow_membership_v1"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "membership"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "SnowPro"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v6, "pro"
            invoke-interface {v2, v6}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v7, "subscription"
            invoke-interface {v2, v7}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            const-string v3, "activeSubscriptions"
            invoke-interface {v1, v3, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string v3, "allPurchasedProductIdentifiers"
            invoke-interface {v1, v3, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            move-object v0, v1
        """.trimIndent()
        val premiumProductSetInstructions = """
            new-instance v0, Ljava/util/HashSet;
            invoke-direct {v0}, Ljava/util/HashSet;-><init>()V
            const-string v1, "com.snow_forecast.application.premium.weekly_auto_renew"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "snow_membership_v1:monthly-autorenewing"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "snow_membership_v1:snow-membership-v1-1-month"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "com.snow_forecast.application.premium.1_month"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "snow_membership_v1:6months-autorenewing"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "snow_membership_v1:snow-membership-v1-6-months"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "com.snow_forecast.application.premium.6_months"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "snow_membership_v1:snow-membership-v1-yearly"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "com.snow_forecast.application.premium.12_months"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            return-object v0
        """.trimIndent()

        CustomerInfoMapFingerprint
            .match(classDefBy(CustomerInfoMapFingerprint.definingClass!!))
            .method
            .apply {
                val returnIndex = instructions.indexOfLast { it.opcode.name.startsWith("return-object") }
                addInstructions(returnIndex, premiumEntitlementInstructions)
            }

        val getActiveMethod = EntitlementInfosGetActiveFingerprint
            .match(classDefBy(EntitlementInfosGetActiveFingerprint.definingClass!!))
            .method
        getActiveMethod.removeInstructions(0, getActiveMethod.instructions.count())
        getActiveMethod.addInstructions(
            0,
            """
            iget-object v0, p0, Lcom/revenuecat/purchases/EntitlementInfos;->all:Ljava/util/Map;
            return-object v0
            """.trimIndent()
        )

        val isActiveMethod = EntitlementInfoIsActiveFingerprint
            .match(classDefBy(EntitlementInfoIsActiveFingerprint.definingClass!!))
            .method
        isActiveMethod.removeInstructions(0, isActiveMethod.instructions.count())
        isActiveMethod.addInstructions(
            0,
            """
            const/4 v0, 0x1
            return v0
            """.trimIndent()
        )

        listOf(
            CustomerInfoGetActiveSubscriptionsFingerprint,
            CustomerInfoGetAllPurchasedProductIdsFingerprint
        ).forEach { fingerprint ->
            fingerprint
                .match(classDefBy(fingerprint.definingClass!!))
                .method
                .apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, premiumProductSetInstructions)
                }
        }
    }
}
