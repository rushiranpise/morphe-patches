package app.template.patches.spotangels

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPOTANGELS_COMPATIBILITY

@Suppress("unused")
val spotAngelsUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Plus",
    description = "Unlocks all SpotAngels Plus features in app",
    default = true,
) {
    compatibleWith(SPOTANGELS_COMPATIBILITY)

    execute {
        // Force RevenueCat EntitlementInfo.isActive() → true
        // Ensures the "active" map in EntitlementInfos is populated even for expired/cancelled subs
        EntitlementIsActiveFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent()
        )

        // Patch getFALLBACK() to return a Subscription using the first available Entitlement
        // from the server Reference map, instead of null when User has no activeEntitlement
        SubscriptionGetFallbackFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, Lgk8;->h()Lcom/spotangels/android/model/business/Reference;
                move-result-object v0
                invoke-virtual {v0}, Lcom/spotangels/android/model/business/Reference;->getEntitlements()Ljava/util/Map;
                move-result-object v0
                invoke-interface {v0}, Ljava/util/Map;->values()Ljava/util/Collection;
                move-result-object v0
                check-cast v0, Ljava/lang/Iterable;
                invoke-static {v0}, Lyd1;->x0(Ljava/lang/Iterable;)Ljava/lang/Object;
                move-result-object v0
                if-eqz v0, :skip_fallback
                check-cast v0, Lcom/spotangels/android/util/PurchasesUtils${'$'}Entitlement;
                new-instance v1, Lcom/spotangels/android/util/PurchasesUtils${'$'}Subscription;
                const/4 v2, 0x0
                invoke-direct {v1, v0, v2, v2, v2}, Lcom/spotangels/android/util/PurchasesUtils${'$'}Subscription;-><init>(Lcom/spotangels/android/util/PurchasesUtils${'$'}Entitlement;Ljava/lang/Boolean;Ljava/util/Calendar;Landroid/net/Uri;)V
                return-object v1
                :skip_fallback
                nop
            """.trimIndent()
        )

        // Entitlement boolean feature-gate getters → force true (premium)
        listOf(
            EntitlementGetAvailabilityFingerprint,
            EntitlementGetSatelliteFingerprint,
            EntitlementGetSpotRegulationsFingerprint,
            EntitlementGetMapFiltersFingerprint,
            EntitlementGetMultipleCarsFingerprint,
            EntitlementGetCarSharingFingerprint,
            EntitlementGetDarkModeFingerprint,
            EntitlementGetCalendarFingerprint,
            EntitlementGetCalendarSyncFingerprint,
            EntitlementGetCalendarRecommendationsFingerprint,
            EntitlementGetOpenSpotAlertsFingerprint,
            EntitlementGetAutomatedOpenSpotAlertsFingerprint,
            EntitlementGetParkingRemindersFingerprint,
            EntitlementGetCustomNotificationsFingerprint,
            EntitlementGetSmsNotificationsFingerprint,
            EntitlementGetEmailNotificationsFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // getFreeSubscription() → force false (treat as paid tier)
        EntitlementGetFreeSubscriptionFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent()
        )
    }
}