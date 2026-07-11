package app.template.patches.pocketbard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.POCKET_BARD_COMPATIBILITY
import app.template.patches.shared.clearBody

private const val LICENSE_CHECK_STATE = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"
private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"

@Suppress("unused")
val pocketBardUnlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlock Premium features in app",
    default = true,
) {
    compatibleWith(POCKET_BARD_COMPATIBILITY)

    execute {

        // 1. PairIP bypass — LicenseClient.initializeLicenseCheck()
        //    Sets licenseCheckState = LOCAL_CHECK_OK before ordinal check.
        //    Ordinal 2 hits no branch (not 0/1/4) → falls through to return-void.
        InitializeLicenseCheckFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, $LICENSE_CHECK_STATE->LOCAL_CHECK_OK:$LICENSE_CHECK_STATE
                sput-object v0, $LICENSE_CLIENT->licenseCheckState:$LICENSE_CHECK_STATE
            """,
        )

        // 2. Subscription gate — UserInfo.getHasValidSubscription()
        //    Root cause: hasValidSubscription is NOT persisted in DataStore.
        //    It's recomputed each time as: expirationDate != null && expirationDate > Clock.now().
        //    Without a real subscription, expirationDate is null → false → "free".
        //    Patching this single getter covers all callers unconditionally.
        HasValidSubscriptionFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }
    }
}
