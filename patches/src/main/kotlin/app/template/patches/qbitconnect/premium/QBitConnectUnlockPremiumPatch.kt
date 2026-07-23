package app.template.patches.qbitconnect.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.QBITCONNECT_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions

/**
 * Unlocks the "Remove Ads" purchase in qBitConnect.
 *
 * ## Purchase model
 *
 * qBitConnect is a Flutter app (libapp.so / Dart AOT). All purchase logic lives in
 * PurchaseService (package:qbitconnect/src/services/prefs.dart). The service tracks a
 * single one-time purchase: "remove_ads" (Google Play product ID).
 *
 * Premium state is read at startup from flutter_secure_storage, which stores values in
 * a SharedPreferences file named "FlutterSecureStorage" (MODE_PRIVATE). Keys are stored
 * with the prefix "This is the prefix for a secure storage_" prepended. Values are plain
 * UTF-8 strings (encryptedSharedPreferences defaults to false).
 *
 * ## Injection strategy
 *
 * Application.attachBaseContext(Context) is the earliest Java lifecycle hook before
 * Flutter initialises. Original body (smali verified, .registers 2):
 *
 *   invoke-static  {p1}, LicenseClient.checkLicense(Context)V   → no-op'd by pairip patch
 *   invoke-super   {p0, p1}, Application.attachBaseContext(Context)V
 *   return-void
 *
 * Injecting AFTER return-void is impossible; inserting BEFORE it into a 2-register
 * method triggers ART VerifyError ("register index out of range") because addInstructions
 * does not grow .registers automatically for this method.
 *
 * Fix: clearBody() to wipe all instructions, ensureRegisters(5) to declare 3 locals
 * (v0, v1, v2) on top of the 2 parameter slots (p0, p1), then addInstructions(0, ...)
 * to write the complete replacement body including the super call.
 *
 * ## Register layout after ensureRegisters(5)
 *   v0, v1, v2  — scratch locals
 *   p0 (= v3)   — this (Application)
 *   p1 (= v4)   — context (passed by Android runtime)
 */
@Suppress("unused")
val qBitConnectUnlockPremiumPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Unlocks the Remove Ads purchase in qBitConnect by injecting IAP ownership flags into SharedPreferences before Flutter reads them.",
) {
    compatibleWith(QBITCONNECT_COMPATIBILITY)
    execute {

        // 1. Block the entry point — checkLicense() is static so match via classDefBy
        CheckLicenseFingerprint
            .match(classDefBy(CheckLicenseFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }

        // 2. Neutralise RSA signature verification
        ValidateResponseFingerprint
            .match(classDefBy(ValidateResponseFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }

        // 3. Suppress error dialog + app exit
        HandleErrorFingerprint
            .match(classDefBy(HandleErrorFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }

        // 4. Suppress paywall screen launch
        StartPaywallActivityFingerprint
            .match(classDefBy(StartPaywallActivityFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "return-void")
            }

        ApplicationAttachBaseContextFingerprint
            .match(classDefBy(ApplicationAttachBaseContextFingerprint.definingClass!!))
            .method
            .apply {
                clearBody()
                ensureRegisters(5) // v0 v1 v2 | p0(=v3) p1(=v4)

                addInstructions(
                    0,
                    """
                    invoke-super {p0, p1}, Landroid/app/Application;->attachBaseContext(Landroid/content/Context;)V
                    const-string v0, "FlutterSecureStorage"
                    const/4 v1, 0x0
                    invoke-virtual {p1, v0, v1}, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;
                    move-result-object v0
                    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${"$"}Editor;
                    move-result-object v0
                    const-string v1, "This is the prefix for a secure storage_iap_remove_ads_granted"
                    const-string v2, "true"
                    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences${"$"}Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences${"$"}Editor;
                    move-result-object v0
                    const-string v1, "This is the prefix for a secure storage_iap_remove_ads_owned_android"
                    const-string v2, "true"
                    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences${"$"}Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences${"$"}Editor;
                    move-result-object v0
                    const-string v1, "This is the prefix for a secure storage_iap_remove_ads_reconciled"
                    const-string v2, "true"
                    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences${"$"}Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences${"$"}Editor;
                    move-result-object v0
                    invoke-interface {v0}, Landroid/content/SharedPreferences${"$"}Editor;->apply()V
                    return-void
                    """.trimIndent(),
                )
            }
    }
}
