package app.template.patches.obdandroid.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OBD_ANDROID_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val obdAndroidPremiumPatch = bytecodePatch(
    name = "OBD Android Premium Unlock",
    description = "Unlocks all premium features including advanced diagnostics, live sensor data, freeze frame, and removes the daily usage quota."
) {
    compatibleWith(OBD_ANDROID_COMPATIBILITY)

    execute {
        // 1. Patch isAppPurchased() → true so the BillingManager constructor
        //    initialises z:Z=true and A:LiveData<Boolean> with true on startup.
        IsAppPurchasedFingerprint.method.returnEarly(true)

        // 2. Patch d(Z)V — force p1=true at entry so every purchase state update
        //    (from queryPurchasesAsync, onPurchasesUpdated, server verify) treats
        //    the user as purchased. p1 is the only parameter (register 1, non-static).
        SetPurchasedStateFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1"
        )

        // 3. Patch onBillingSetupFinished() → return-void so billing error codes
        //    (-2, 3, 5) cannot directly set z:Z=false via iput-boolean, bypassing
        //    the setAppPurchased() path entirely.
        OnBillingSetupFinishedFingerprint.method.returnEarly()
    }
}
