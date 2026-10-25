package app.template.patches.batterypods.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BATTERYPODS_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val batteryPodsPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks BatteryPods Pro: removes advertisements and enables premium " +
        "features including widget themes and device customization options.",
    default = true,
) {
    compatibleWith(BATTERYPODS_COMPATIBILITY)
    execute {
        PairIPValidateResponseFingerprint.method.returnEarly()
        PairIPProcessResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")
        PairIPGetRepeatedCheckMetadataFingerprint.method.returnEarly(null)
        BatteryPodsIsPurchasedFingerprint.method.returnEarly(true)

        // 8.32: the app checks PURCHASED_ITEM_NO_ADS on Application startup,
        // BEFORE MainActivity is created. Write the preference immediately
        // at instruction index 1, immediately after Application.onCreate()'s invoke-super to avoid the cold-start
        // stale ad state introduced by the original MainActivity-only injection.
        // Registers: 8 total, 1 parameter; v0-v2 are available local registers.
        BatteryPodsApplicationOnCreateFingerprint.method.addInstructions(
            1,
            """
                invoke-static {p0}, Landroidx/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;
                move-result-object v0
                invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                const-string v1, "PURCHASED_ITEM_NO_ADS"
                const/4 v2, 0x1
                invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
            """.trimIndent(),
        )

        // Called by the app for startup/billing state updates. Preserve its
        // existing UI handling but prevent it from turning the no-ads flag off.
        BatteryPodsBillingStatusUpdateFingerprint.method.addInstruction(
            0,
            "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )

        // In 8.32 Lf6.b():int returns 1 if PURCHASED_ITEM_NO_ADS=true.
        // Its callers include the popup widget ad logic, so enforce that
        // already-existing no-ads state without patching Bluetooth routines.
        BatteryPodsAdEligibilityFingerprint.method.returnEarly(1)

        // Keep the working 8.32 MainActivity.onCreate injection point used by
        // the tested patch. This supplements the Application startup write.
        // The method has 12 registers and 2 argument registers, leaving
        // v0-v9 for locals (so v0-v2 are valid).
        BatteryPodsMainActivityOnCreateFingerprint.method.addInstructions(
            3,
            """
                invoke-static {p0}, Landroidx/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;
                move-result-object v0
                invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                const-string v1, "PURCHASED_ITEM_NO_ADS"
                const/4 v2, 0x1
                invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
            """.trimIndent(),
        )
    }
}
