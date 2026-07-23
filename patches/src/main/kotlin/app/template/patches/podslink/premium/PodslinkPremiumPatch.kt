package app.template.patches.podslink.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PODSLINK_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.shared.clearBody

private const val LIC = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LIC_STATE = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

private const val ACCOUNT_INFO = "Lnet/podslink/entity/net/AccountInfo;"

@Suppress("unused")
val podslinkPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks lifetime pro by bypassing all entitlement checks and poisoning the account cache."
) {
    compatibleWith(PODSLINK_COMPATIBILITY)

    execute {

        // 1. Pre-set licenseCheckState to LOCAL_CHECK_OK before any check runs.
        //    initializeLicenseCheck() branches on the current state ordinal —
        //    LOCAL_CHECK_OK causes an early return with no network call.
        LicenseClientInitFingerprint.method.apply {
            clearBody()
            addInstructions(
                0, """
                    sget-object v0, $LIC_STATE->LOCAL_CHECK_OK:$LIC_STATE
                    sput-object v0, $LIC->licenseCheckState:$LIC_STATE
                    return-void
                """.trimIndent()
            )
        }

        // 2. No-op the static entry point so the check never starts.
        LicenseCheckFingerprint.method.returnEarly()

        // 3. No-op processResponse to prevent NOT_LICENSED path from triggering exit/paywall.
        ProcessResponseFingerprint.method.returnEarly()

        // 4. No-op startPaywallActivity as a final failsafe against System.exit().
        StartPaywallFingerprint.method.returnEarly()

        // 5. No-op validateResponse so JWS signature verification always passes.
        ValidateResponseFingerprint.method.returnEarly()

        // Fix 1: AccountInfo.isActive() → always true.
        // Covers all 12+ direct call sites (activities, fragments, adapters, services, widgets).
        AccountInfoIsActiveFingerprint.method.returnEarly(true)

        // Fix 2: AccountManager.cacheAccountInfo() — inject setActive(true) before serialization.
        //
        // Root cause of "still free": every billing refresh ends by calling cacheAccountInfo(),
        // which serializes AccountInfo (with active=false) to SharedPreferences JSON.
        // On next cold start getAccountInfoFromCache() deserializes it, returning active=false,
        // which then propagates through every isActive() call site before Fix 1 can intercept.
        //
        // Solution: force setActive(true) on the AccountInfo param (p1) before Gson.toJson()
        // runs, so the JSON written to SP always contains "active":true.
        // p1 = the AccountInfo argument (register 1 in this non-static method, .registers 5).
        CacheAccountInfoFingerprint.method.addInstructions(
            0, """
                const/4 v0, 0x1
                invoke-virtual {p1, v0}, $ACCOUNT_INFO->setActive(Z)V
            """.trimIndent()
        )
    }
}
