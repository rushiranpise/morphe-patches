package app.template.patches.camscanner

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CAMSCANNER_COMPATIBILITY

@Suppress("unused")
val yearlyUnlockPatch = bytecodePatch(
    name = "Yearly Unlock",
    description = "Unlocks premium features without login. Note: Login Won't Work",
) {
    compatibleWith(CAMSCANNER_COMPATIBILITY)

    execute {
        IsPremiumFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        GetStatusCodeFingerprint.method.addInstructions(0, """
            const-wide/16 v0, 0x1
            return-wide v0
        """)

        IsVipUserFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        IsPirateAppFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        // Flutter pigeon isVip DB path
        IsVipContextFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        IsKeySyncFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // Suppress relogin broadcast (fake UID causes server 401)
        SendReLoginBroadcastFingerprint.method.addInstructions(0, "return-void")

        // Suppress ReLoginDialogActivity launch
        LaunchReLoginDialogFingerprint.method.addInstructions(0, "return-void")
    }
}
