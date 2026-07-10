package app.template.patches.life360

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.LIFE360_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

@Suppress("unused")
val life360UnlockPremiumPatch = bytecodePatch(
    name = "Unlock Platinum",
    description = "Unlocks Life360 Platinum feature in app.",
    default = true,
) {
    compatibleWith(LIFE360_COMPATIBILITY)
    dependsOn(
        life360CertSeedPatch,
        spoofFirebaseCertHashPatch,
        spoofSignatureVerificationPatch,
        spoofInstallSourcePatch,
    )

    execute {
        CircleFeaturesIsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 p0, 0x1
                    return p0
                """.trimIndent(),
            )
        }

        listOf(
            FeatureSetIsAvailableFingerprint,
            FeatureSetIsEnabledForAnyCircleFingerprint,
            FeatureSetIsEnabledForCircleFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        return v0
                    """.trimIndent(),
                )
            }
        }

        listOf(
            DefaultActiveMappedSkuOrFreeFingerprint,
            DefaultActiveSkuOrFreeFingerprint,
            PremiumCircleActiveMappedSkuOrFreeFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        sget-object v0, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                        return-object v0
                    """.trimIndent(),
                )
            }
        }

        DefaultIsActiveCircleFreeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    return-object v0
                """.trimIndent(),
            )
        }

        listOf(
            PurchasedSkuInfoGetSkuFingerprint to "9",
            PurchasedSkuInfoGetProductIdFingerprint to "premium_4999_yearly_1",
            PurchasedSkuInfoEntityGetProductIdFingerprint to "premium_4999_yearly_1",
        ).forEach { (fingerprint, value) ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const-string v0, "$value"
                        return-object v0
                    """.trimIndent(),
                )
            }
        }

        PurchasedSkuInfoGetPeriodFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;->ANNUAL:Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;
                    return-object v0
                """.trimIndent(),
            )
        }

        PurchasedSkuInfoGetPaymentStateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/inapppurchase/PaymentState;->PAID:Lcom/life360/inapppurchase/PaymentState;
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumSkuInfoForCircleFingerprint.method.apply {
            ensureRegisters(12)
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lcom/life360/inapppurchase/PurchasedSkuInfo;
                    const-string v1, "9"
                    const-string v2, "premium_4999_yearly_1"
                    const-string v3, "google_play"
                    sget-object v4, Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;->ANNUAL:Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;
                    move-object v5, p1
                    const-string v6, "1752019200"
                    const-string v7, "1893456000"
                    sget-object v8, Lcom/life360/inapppurchase/PaymentState;->PAID:Lcom/life360/inapppurchase/PaymentState;
                    sget-object v9, Lcom/life360/inapppurchase/TrialState;->CONVERTED:Lcom/life360/inapppurchase/TrialState;
                    const/4 v10, 0x0
                    invoke-direct/range {v0 .. v10}, Lcom/life360/inapppurchase/PurchasedSkuInfo;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/life360/model_store/base/localstore/room/premium/PurchasePeriod;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/life360/inapppurchase/PaymentState;Lcom/life360/inapppurchase/TrialState;Ljava/lang/String;)V
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumSkuForCircleFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string v0, "9"
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumPaymentStateForCircleFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/inapppurchase/PaymentState;->PAID:Lcom/life360/inapppurchase/PaymentState;
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumAvailableProductsForSkuFingerprint.method.apply {
            ensureRegisters(9)
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lcom/life360/inapppurchase/AvailableProductIds;
                    const-string v1, "premium_4999_monthly_1"
                    invoke-static {v1}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
                    move-result-object v1
                    const-string v2, "premium_4999_yearly_1"
                    invoke-static {v2}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
                    move-result-object v2
                    const/4 v3, 0x1
                    const/4 v4, 0x1
                    const-string v5, "premium_4999_monthly_1"
                    invoke-static {v5}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
                    move-result-object v5
                    const-string v6, "premium_4999_yearly_1"
                    invoke-static {v6}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
                    move-result-object v6
                    invoke-direct/range {v0 .. v6}, Lcom/life360/inapppurchase/AvailableProductIds;-><init>(Ljava/util/List;Ljava/util/List;ZZLjava/util/List;Ljava/util/List;)V
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumGetSkuForProductIdFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string v0, "9"
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumPricesForSkuFingerprint.method.apply {
            ensureRegisters(8)
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lcom/life360/inapppurchase/Prices;
                    const-wide v1, 0x4033fe147ae147aeL
                    const-wide v3, 0x4048fe147ae147aeL
                    const-string v5, "${'$'}19.99/mo"
                    const-string v6, "${'$'}49.99/yr"
                    const-string v7, "USD"
                    invoke-direct/range {v0 .. v7}, Lcom/life360/inapppurchase/Prices;-><init>(DDLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumTrialForSkuFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x7
                    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v0
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumGetAvailableSkusFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Ljava/util/LinkedHashSet;
                    invoke-direct {v0}, Ljava/util/LinkedHashSet;-><init>()V
                    const-string v1, "9"
                    invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}
