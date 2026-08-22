package app.template.patches.tiktok_lite.login

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TIKTOK_LITE_COMPATIBILITY

@Suppress("unused")
val tiktokLiteDisableLoginPatch = bytecodePatch(
    name = "Disable Login Requirement",
    description = "Removes TikTok Lite mandatory login gate so the app can be browsed without an account.",
) {
    compatibleWith(TIKTOK_LITE_COMPATIBILITY)

    execute {
        // Block the root feature flag: X/5v2.L()Z -> return false.
        // This makes MandatoryLoginService.LBL()Z short-circuit to false immediately,
        // killing the canSkipForcedLoginPanel path before it reaches LocalTestApi.
        ForcedLoginFeatureFlagFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // Block per-flow login gate: L(String)Z -> always false.
        MandatoryLoginEnableFlowFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // Block deferred-login check: LB(String)Z -> always false.
        MandatoryLoginShouldShowFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )

        // Block secondary login path: LCC(String, String)Z -> always false.
        MandatoryLoginSecondaryFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """,
        )
    }
}
