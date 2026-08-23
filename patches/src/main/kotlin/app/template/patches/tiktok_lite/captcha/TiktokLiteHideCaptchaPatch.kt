package app.template.patches.tiktok_lite.captcha

/*
 * Ported from hxreborn/hxreborn-tiktok-patches (GPL-3.0)
 * https://github.com/hxreborn/hxreborn-tiktok-patches
 * Original: HideCaptchaPopupsPatch by icysymmetra / hxreborn
 *
 * Adaptation for Lite:
 * - popCaptchaV2: 3 params (Activity, String, LX/8i1) -- no Fragment param vs full TikTok
 * - popCaptcha: 3 params (Activity, I, LX/8i1)
 * - oecverify (BdTuring) classes absent in Lite -- those fingerprints skipped
 * - No extension/settings system in Lite -- simple return-void at index 0
 */

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY

// SecApiImpl.popCaptchaV2(Activity, String, LX/8i1)V
// Confirmed in Lite: .method public final popCaptchaV2(Landroid/app/Activity;Ljava/lang/String;LX/8i1;)V
// String "popCaptchaV2 - riskInfo =" identifies it stably.
private object CaptchaPopupV2Fingerprint : Fingerprint(
    definingClass = "/sec/SecApiImpl;",
    name = "popCaptchaV2",
    returnType = "V",
    strings = listOf("popCaptchaV2 - riskInfo ="),
)

// SecApiImpl.popCaptcha(Activity, I, LX/8i1)V
// Confirmed in Lite: .method public final popCaptcha(Landroid/app/Activity;ILX/8i1;)V
// String "popCaptcha - errorcode = " identifies it stably.
private object CaptchaPopupLegacyFingerprint : Fingerprint(
    definingClass = "/sec/SecApiImpl;",
    name = "popCaptcha",
    returnType = "V",
    strings = listOf("popCaptcha - errorcode = "),
)

@Suppress("unused")
val tiktokLiteHideCaptchaPatch = bytecodePatch(
    name = "Hide CAPTCHA Popups",
    description = "Suppresses browsing CAPTCHA dialogs from SecApiImpl.",
    default = true,
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // Block V2 captcha -- used for most in-app risk checks.
        CaptchaPopupV2Fingerprint.method.addInstructions(
            0, "return-void",
        )

        // Block legacy captcha -- older flow still present in Lite.
        CaptchaPopupLegacyFingerprint.method.addInstructions(
            0, "return-void",
        )
    }
}
