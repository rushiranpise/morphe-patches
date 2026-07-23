package app.template.patches.batterypods.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BATTERYPODS_COMPATIBILITY
import app.template.patches.shared.returnEarly

// BatteryPods has TWO distinct premium gate paths — both must be patched:
//
// PATH A — Direct SharedPreferences reads (MainActivity, AirPodsService, BatteryWidget, etc.)
//   All read "PURCHASED_ITEM_NO_ADS" directly via getBoolean(). Fixed by writing true at startup.
//
// PATH B — fb.c(Context)Z static helper
//   Used by n80 (AirPodsService floating overlay UI) to gate premium features.
//   When fb.c() returns false, n80 installs i80 click listener which launches
//   PremiumPlanActivity when user taps premium overlay features.
//   Also used by z31.b() (widget click handler).
//   This is what caused PremiumPlanActivity to show — fb.c() returning false.
//
// TWO-POINT PATCH:
//
// Point 1 — fb.c(Context)Z → returnEarly(true)
//   Covers n80 overlay path and z31.b() widget path.
//
// Point 2 — MainActivity.onCreate(Bundle)V → inject SharedPrefs write at MobileAds.a()
//   Covers all direct getBoolean("PURCHASED_ITEM_NO_ADS") read sites.
//   Injection at MobileAds.a() ensures context is fully initialized (after super.onCreate).
@Suppress("unused")
val batteryPodsPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks BatteryPods Pro: removes all advertisements and enables premium " +
        "features including all widget themes and device customization options.",
    default = true,
) {
    compatibleWith(BATTERYPODS_COMPATIBILITY)

    execute {

        // Step 1: Skip RSA signature verification.
        PairIPValidateResponseFingerprint.method.returnEarly()

        // Step 2: Force responseCode = 0 (LICENSED) before processResponse branches.
        PairIPProcessResponseFingerprint.method.addInstruction(0, "const/4 p1, 0x0")

        // Step 3: Return null from getRepeatedCheckMetadata so JWS parsing is never
        // attempted on the invalid NOT_LICENSED bundle data.
        PairIPGetRepeatedCheckMetadataFingerprint.method.returnEarly()
        
        // Point 1: fb.c(Context)Z always returns true.
        // Covers: n80 AirPodsService overlay, z31.b() widget handler.
        BatteryPodsIsPurchasedFingerprint.method.returnEarly(true)

        // Point 2: Write PURCHASED_ITEM_NO_ADS=true to SharedPreferences in onCreate.
        // Covers: MainActivity.C(Z)V (ads), AirPodsService direct reads,
        //         BatteryWidgetThemeSelectActivity, d50, wd, y4.
        // Injection at MobileAds.a() index = after super.onCreate(), context ready.
        val insertIndex = BatteryPodsMainActivityOnCreateFingerprint.instructionMatches[0].index
        BatteryPodsMainActivityOnCreateFingerprint.method.addInstructions(
            insertIndex,
            """
                invoke-static {p0}, Lt91;->b(Landroid/content/Context;)Ljava/lang/String;
                move-result-object v0
                const/4 v1, 0x0
                invoke-virtual {p0, v0, v1}, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
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
