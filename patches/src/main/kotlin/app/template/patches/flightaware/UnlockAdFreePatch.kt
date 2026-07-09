package app.template.patches.flightaware

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FLIGHTAWARE_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val flightAwareUnlockAdFreePatch = bytecodePatch(
    name = "Unlock Ad-Free",
    description = "Forces FlightAware ad-free subscribed state.",
    default = true
) {
    compatibleWith(FLIGHTAWARE_COMPATIBILITY)

    execute {
        BillingInitFingerprint
            .match(classDefBy(BillingInitFingerprint.definingClass!!))
            .method
            .apply {
                val adFreeStateIndex = implementation!!.instructions.indexOfFirst {
                    it.toString().contains("->_adFreeState:Lkotlinx/coroutines/flow/StateFlowImpl;")
                }
                if (adFreeStateIndex >= 0) {
                    addInstructions(
                        adFreeStateIndex + 1,
                        """
                            sget-object p1, Lcom/flightaware/android/liveFlightTracker/billing/AdFreeState;->AD_FREE_SUBSCRIBED:Lcom/flightaware/android/liveFlightTracker/billing/AdFreeState;
                            invoke-virtual {p1}, Ljava/lang/Enum;->ordinal()I
                            move-result v0
                            sget-object v1, Lcom/flightaware/android/liveFlightTracker/App;->sPrefs:Landroid/content/SharedPreferences;
                            invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                            move-result-object v1
                            const-string v2, "pref_ad_free_state"
                            invoke-interface {v1, v2, v0}, Landroid/content/SharedPreferences${'$'}Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences${'$'}Editor;
                            move-result-object v0
                            invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                            iget-object v0, p0, Lcom/flightaware/android/liveFlightTracker/billing/MyBillingClient;->_adFreeState:Lkotlinx/coroutines/flow/StateFlowImpl;
                            const/4 v1, 0x0
                            invoke-virtual {v0, v1, p1}, Lkotlinx/coroutines/flow/StateFlowImpl;->updateState(Ljava/lang/Object;Ljava/lang/Object;)Z
                        """.trimIndent(),
                    )
                }
            }

        ProcessPurchasesFingerprint
            .match(classDefBy(ProcessPurchasesFingerprint.definingClass!!))
            .method
            .apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        sget-object p1, Lcom/flightaware/android/liveFlightTracker/billing/AdFreeState;->AD_FREE_SUBSCRIBED:Lcom/flightaware/android/liveFlightTracker/billing/AdFreeState;
                        invoke-virtual {p1}, Ljava/lang/Enum;->ordinal()I
                        move-result v0
                        sget-object v1, Lcom/flightaware/android/liveFlightTracker/App;->sPrefs:Landroid/content/SharedPreferences;
                        invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v1
                        const-string v2, "pref_ad_free_state"
                        invoke-interface {v1, v2, v0}, Landroid/content/SharedPreferences${'$'}Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                        iget-object v0, p0, Lcom/flightaware/android/liveFlightTracker/billing/MyBillingClient;->_adFreeState:Lkotlinx/coroutines/flow/StateFlowImpl;
                        const/4 v1, 0x0
                        invoke-virtual {v0, v1, p1}, Lkotlinx/coroutines/flow/StateFlowImpl;->updateState(Ljava/lang/Object;Ljava/lang/Object;)Z
                        return-void
                    """.trimIndent(),
                )
            }

        SettingsPreferenceClickFingerprint
            .match(classDefBy(SettingsPreferenceClickFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                    iget-object v0, p1, Landroidx/preference/Preference;->mKey:Ljava/lang/String;
                    if-eqz v0, :manage_subscription_continue
                    const-string v1, "pref_manage_subscription"
                    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                    move-result v0
                    if-eqz v0, :manage_subscription_continue
                    const/4 v0, 0x1
                    return v0
                    :manage_subscription_continue
                """.trimIndent(),
            )
    }
}
