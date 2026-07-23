package app.template.patches.life360

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.LIFE360_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// Smali helper: build a LinkedHashMap with one entry.
// p_key = register holding the key, p_val = register holding the value.
// Result lands in v0. Uses v0, v1, v2 scratch.
private fun linkedHashMapOf(keyReg: String, valReg: String): String = """
    new-instance v0, Ljava/util/LinkedHashMap;
    invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V
    invoke-virtual {v0, $keyReg, $valReg}, Ljava/util/LinkedHashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
""".trimIndent()

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
            FeaturesAccessIsEnabledForActiveCircleFingerprint,
            FeaturesAccessIsEnabledForAnyCircleFingerprint,
            FeatureSetIsOnVictimOnlyCollisionDispatchAvailableFingerprint,
            FeatureSetIsOnVictimOnlyPremiumSosAvailableFingerprint,
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
            FeatureSetResolveGpsTrackerForCircleFingerprint to "0x63",
            FeatureSetResolveLocationHistoryForCircleFingerprint to "0x7fffffff",
            FeatureSetResolvePlaceAlertsForCircleFingerprint to "0x7fffffff",
        ).forEach { (fingerprint, value) ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const v0, $value
                        return v0
                    """.trimIndent(),
                )
            }
        }

        listOf(
            DefaultResolveLocationHistoryForCircleValueFingerprint,
            DefaultResolvePlaceAlertsForCircleValueFingerprint,
            PremiumCircleResolvePlaceAlertsForCircleValueFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const v0, 0x7fffffff
                        invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                        move-result-object v0
                        return-object v0
                    """.trimIndent(),
                )
            }
        }

        // FIX: All Map-returning methods MUST return LinkedHashMap, not Collections.singletonMap().
        // Callers in DefaultMembershipUtil cast the result to LinkedHashMap explicitly:
        //   return (LinkedHashMap) featureSetEntitlementManager.resolveLocationHistoryPerSkus(premium)
        // Collections.singletonMap() returns SingletonMap → ClassCastException → app crash.

        FeatureSetResolveLocationHistoryPerSkusFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    const v2, 0x7fffffff
                    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v2
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        DefaultResolveLocationHistoryPerSkusValueFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    const v2, 0x7fffffff
                    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v2
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        FeatureSetIsAvailableForPerSkusFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        DefaultResolvePlaceAlertsPerSkusValueFingerprint.method.apply {
            ensureRegisters(5)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    new-instance v2, Lcom/life360/android/core/models/AvailablePlaceAlerts${'$'}LimitedAlerts;
                    const v3, 0x7fffffff
                    invoke-direct {v2, v3}, Lcom/life360/android/core/models/AvailablePlaceAlerts${'$'}LimitedAlerts;-><init>(I)V
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        FeatureSetResolvePlaceAlertsPerSkusFingerprint.method.apply {
            ensureRegisters(5)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    new-instance v2, Lcom/life360/android/core/models/AvailablePlaceAlerts${'$'}LimitedAlerts;
                    const v3, 0x7fffffff
                    invoke-direct {v2, v3}, Lcom/life360/android/core/models/AvailablePlaceAlerts${'$'}LimitedAlerts;-><init>(I)V
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        listOf(
            FeatureSetResolveIdTheftReimbursementForCircleFingerprint,
            FeatureSetResolveStolenPhoneReimbursementForCircleFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                ensureRegisters(4)
                clearBody()
                addInstructions(
                    0,
                    """
                        new-instance v0, Lcom/life360/android/core/models/ReimbursementValue;
                        const v1, 0xc350
                        sget-object v2, Lcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;->USD:Lcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;
                        invoke-direct {v0, v1, v2}, Lcom/life360/android/core/models/ReimbursementValue;-><init>(ILcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;)V
                        return-object v0
                    """.trimIndent(),
                )
            }
        }

        FeatureSetResolveRoadsideAssistanceForCircleFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/android/core/models/RoadsideAssistanceValue${'$'}UnlimitedDistance;->INSTANCE:Lcom/life360/android/core/models/RoadsideAssistanceValue${'$'}UnlimitedDistance;
                    return-object v0
                """.trimIndent(),
            )
        }

        FeatureSetResolveSosEmergencyDispatchForCircleFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Lcom/life360/android/core/models/PremiumFeature${'$'}SOS${'$'}EmergencyDispatch;
                    const/4 v1, 0x0
                    invoke-direct {v0, v1}, Lcom/life360/android/core/models/PremiumFeature${'$'}SOS${'$'}EmergencyDispatch;-><init>(Z)V
                    return-object v0
                """.trimIndent(),
            )
        }

        FeatureSetResolveTileDevicePackageForCircleFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/android/core/models/PremiumFeature${'$'}TileDevicePackage${'$'}StarterPack;->INSTANCE:Lcom/life360/android/core/models/PremiumFeature${'$'}TileDevicePackage${'$'}StarterPack;
                    return-object v0
                """.trimIndent(),
            )
        }

        listOf(
            FeatureSetResolveIdTheftReimbursementPerSkusFingerprint,
            FeatureSetResolveStolenPhoneReimbursementPerSkusFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                ensureRegisters(6)
                clearBody()
                addInstructions(
                    0,
                    """
                        sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                        new-instance v2, Lcom/life360/android/core/models/ReimbursementValue;
                        const v3, 0xc350
                        sget-object v4, Lcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;->USD:Lcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;
                        invoke-direct {v2, v3, v4}, Lcom/life360/android/core/models/ReimbursementValue;-><init>(ILcom/life360/android/core/models/PremiumFeature${'$'}MembershipCurrency;)V
                        ${linkedHashMapOf("v1", "v2")}
                        return-object v0
                    """.trimIndent(),
                )
            }
        }

        FeatureSetResolveRoadsideAssistancePerSkusFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    sget-object v2, Lcom/life360/android/core/models/RoadsideAssistanceValue${'$'}UnlimitedDistance;->INSTANCE:Lcom/life360/android/core/models/RoadsideAssistanceValue${'$'}UnlimitedDistance;
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
        }

        FeatureSetSkuTileClassicFulfillmentsFingerprint.method.apply {
            ensureRegisters(4)
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v1, Lcom/life360/android/core/models/Sku;->PLATINUM:Lcom/life360/android/core/models/Sku;
                    sget-object v2, Lcom/life360/android/core/models/PremiumFeature${'$'}TileDevicePackage${'$'}StarterPack;->INSTANCE:Lcom/life360/android/core/models/PremiumFeature${'$'}TileDevicePackage${'$'}StarterPack;
                    ${linkedHashMapOf("v1", "v2")}
                    return-object v0
                """.trimIndent(),
            )
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

        // FeaturesAccess integer gates — server delivers numeric limits (e.g. place_alerts = 3 on free).
        // Return MAX_VALUE so every numeric limit check passes (free tier: typically 3 alerts, 0 history days).
        listOf(
            FeaturesAccessGetIntForCircleFingerprint,
            FeaturesAccessGetIntFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const v0, 0x7fffffff
                        return v0
                    """.trimIndent(),
                )
            }
        }

        // FeaturesAccess boolean gates — server delivers boolean feature flags per circle.
        // Return true for all boolean feature checks.
        FeaturesAccessIsEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        // CircleFeatures.isPremium() — local DB model read by map/switcher UI.
        // Normally checks for non-null skuId; patch to always return true.
        CircleFeatureIsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }

        // Premium.membershipTierExperience() — return TRIPLE_TIER so Silver/Gold/Platinum
        // upgrade screens show the full 3-tier plan picker, not the truncated 2-tier view.
        PremiumMembershipTierExperienceFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/life360/inapppurchase/MembershipTierExperience;->TRIPLE_TIER:Lcom/life360/inapppurchase/MembershipTierExperience;
                    return-object v0
                """.trimIndent(),
            )
        }

        // FeaturesAccessImpl.getLocationUpdateFreq() — return 10_000L ms (Platinum = 10 sec interval).
        // Free=300s, Silver=120s, Gold=30s, Platinum=10s. Value is used by LocationWorker
        // and HeartbeatReceiver to determine when to push next location update.
        FeaturesAccessGetLocationUpdateFreqFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-wide/32 v0, 0x2710
                    return-wide v0
                """.trimIndent(),
            )
        }
    }
}
