package app.template.patches.calory.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALORY_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Calory premium system overview:
// - RevenueCat manages subscriptions; entitlements are "calory-pro" and "remove-ads"
// - On CustomerInfo update, sk.n.d() writes booleans to SharedPreferences
// - Feature gates call rk.h$a.c() / rk.h$a.d() which read those SharedPrefs booleans
// - Pairip provides an additional Play Store license check layer
//
// Patch strategy:
// 1. EntitlementHandler: inject "write true to both prefs" at method start, then return
// 2. PairipCheckLicense: returnEarly() — no-op the license check
// 3. IsPurchased getter: returnEarly(true) — all premium gates open
// 4. IsRemoveAds getter: returnEarly(true) — all ad-removal gates open

@Suppress("unused")
val caloryPremiumPatch = bytecodePatch(
    name = "Calory Premium",
    description = "Unlocks Calory Pro and removes ads."
) {
    compatibleWith(CALORY_COMPATIBILITY)

    execute {
        // Force both premium SharedPrefs keys to true at the start of the
        // entitlement handler. The injected smali writes true to both keys
        // before any RevenueCat CustomerInfo logic runs.
        // Registers: p0=CustomerInfo (unused after injection), v0-v2 available.
        EntitlementHandlerFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, Lcom/funnmedia/calory/utils/CaloryApplication${'$'}a;->a()Lcom/funnmedia/calory/utils/CaloryApplication;
                move-result-object v0
                iget-object v0, v0, Lcom/funnmedia/calory/utils/CaloryApplication;->c:Landroid/content/SharedPreferences;
                invoke-interface { v0 }, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                move-result-object v0
                const/4 v1, 0x1
                const-string v2, "isRevenueCatPurchased"
                invoke-interface { v0, v2, v1 }, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                const-string v2, "isRevenueCatRemoveAdsPurchased"
                invoke-interface { v0, v2, v1 }, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                invoke-interface { v0 }, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                return-void
            """
        )

        // No-op the Pairip license check — prevents the blocking paywall screen.
        PairipCheckLicenseFingerprint.method.returnEarly()

        // Always return true from both premium/ads-removed getters.
        IsPurchasedFingerprint.method.returnEarly(true)
        IsRemoveAdsPurchasedFingerprint.method.returnEarly(true)
    }
}
