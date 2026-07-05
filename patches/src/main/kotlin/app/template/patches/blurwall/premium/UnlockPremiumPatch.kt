package app.template.patches.blurwall.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.blurwall.liccheck.SharedPrefsGetterFingerprint
import app.template.patches.blurwall.pairip.PerformLocalInstallerCheckFingerprint
import app.template.patches.shared.Constants.BLURWALL_COMPATIBILITY

// BlurWall v2.9.2 — RevenueCat onReceived bypass.
//
// onReceived() checks activeSubscriptions/nonSubscriptionTransactions and writes
// the result to SharedPreferences key "start_blur" via an obfuscated unicode method.
// Instead of calling the unicode method (which may fail in patcher), we return-void
// early so no FALSE is ever written. The liccheck patch (DisableLicCheckPatch) then
// intercepts the SharedPreferences read and returns TRUE for "start_blur".

private val disableLicCheckPatch = bytecodePatch(
    name = "Disable License Check",
    description = "Disables BlurWall's SharedPreferences license gate.",
    default = false,
) {
    compatibleWith(BLURWALL_COMPATIBILITY)

    execute {
        SharedPrefsGetterFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "start_blur"
                invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v0
                if-eqz v0, :skip_start_blur
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                :skip_start_blur
                nop
            """,
        )
    }
}

private val bypassPairIpLicenseCheckPatch = bytecodePatch(
    name = "Bypass PairIP License Check",
    description = "Bypasses BlurWall's PairIP license check.",
    default = false,
) {
    compatibleWith(BLURWALL_COMPATIBILITY)

    execute {
        PerformLocalInstallerCheckFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
    }
}

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock Premium Features in app.",
) {
    compatibleWith(BLURWALL_COMPATIBILITY)
    dependsOn(disableLicCheckPatch, bypassPairIpLicenseCheckPatch)

    execute {
        OnReceivedFingerprint.method.addInstructions(
            0,
            """
                return-void
            """,
        )
    }
}
