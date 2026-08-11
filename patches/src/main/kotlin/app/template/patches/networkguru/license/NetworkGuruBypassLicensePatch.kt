package app.template.patches.networkguru.license

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NETWORKGURU_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

// Network Guru v2.0 — two subscription gates:
//
// Gate 1: Pairip LicenseClient — startup integrity check (unchanged from v1.9-beta5).
//   processResponse → clearBody + set licenseCheckState=FULL_CHECK_OK
//   handleError     → clearBody + return-void
//
// Gate 2: w70.k(String) — Play Billing ownership check (new in v2.0).
//   Called from the IntroActivity subscription coroutine (pr case 1).
//   Iterates w70.c (owned Purchase list), reads purchaseState from JSON,
//   returns true only if a matching active purchase exists.
//   → returnEarly(true) bypasses the prompt entirely.
//
// Gate 3 (secondary): t44.m(Boolean) — billing result StateFlow.
//   Writes to t44.f35687m; read by billing coroutine callbacks.
//   → force p1=TRUE to prevent background queries from overriding subscribed state.
//
// Note on t44.l / SubscriptionDisplayStateFlowInitFingerprint (removed):
//   t44.l (field `l`) is a separate StateFlow for ads/UI display state.
//   Patching it was not sufficient and introduced complexity. The direct
//   Play Billing ownership gate (w70.k) is the correct and complete fix.

private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LICENSE_STATE  = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

@Suppress("unused")
val networkGuruBypassLicensePatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlock Pro features.",
    default = true,
) {
    compatibleWith(NETWORKGURU_COMPATIBILITY)

    execute {
        // ── Gate 1: Pairip LicenseClient ──────────────────────────────────────
        val licenseClass = mutableClassDefBy(LICENSE_CLIENT)

        licenseClass.methods.first { it.name == "processResponse" }.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, $LICENSE_STATE->FULL_CHECK_OK:$LICENSE_STATE
                    sput-object v0, $LICENSE_CLIENT->licenseCheckState:$LICENSE_STATE
                    return-void
                """.trimIndent(),
            )
        }

        licenseClass.methods.first { it.name == "handleError" }.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // ── Gate 2: w70.k(String) — Play Billing ownership check ──────────────
        // The direct subscription ownership check called by the IntroActivity
        // coroutine. returnEarly(true) makes every product ID look purchased.
        SubscriptionOwnershipCheckFingerprint.method.returnEarly(true)

        // ── Gate 3: t44.m(Boolean) — billing result StateFlow ─────────────────
        // Prevent background billing callbacks from resetting the subscribed flag.
        SubscriptionStateFlowSetterFingerprint.method.addInstructions(
            0,
            "sget-object p1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
        )
    }
}
