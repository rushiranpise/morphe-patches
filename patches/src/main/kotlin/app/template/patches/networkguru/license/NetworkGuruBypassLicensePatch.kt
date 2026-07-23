package app.template.patches.networkguru.license

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NETWORKGURU_COMPATIBILITY
import app.template.patches.shared.clearBody

// Network Guru has two independent subscription gates:
//
// 1. Pairip LicenseClient — startup gate.
//    Flow: LicenseContentProvider → LicenseClient.initializeLicenseCheck()
//      → processResponse(responseCode, bundle):
//          0 → validateResponse() → licenseCheckState = OK → app runs
//          2 → startPaywallActivity() → paywall overlay
//          error → handleError() → startErrorDialogActivity() → kills app
//    Bypass: set licenseCheckState=OK in processResponse, no-op handleError.
//
// 2. Google Play Billing — in-app premium UI gate.
//    Product IDs: one_year_subscription, one_month_subscription, one_week_subscription.
//    Flow: BillingClient.queryPurchasesAsync() → Q4/l checks product IDs
//      → P4/L.b(this, hasSubscription) → posts to MutableLiveData LP4/L;->k
//      → P4/H observer: if true → hide ads / show premium; if false → show ads.
//    Bypass: inject const/4 p1, 0x1 at index 0 in P4/L.b so it always posts true.
//
// Both classes are non-obfuscated (LicenseClient) or uniquely fingerprinted (P4/L.b).

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
        // ── Gate 1: Pairip LicenseClient ────────────────────────────────────────────
        val licenseClass = mutableClassDefBy(LICENSE_CLIENT)

        // processResponse(I, Bundle) — force licenseCheckState = OK and return immediately.
        licenseClass.methods.first { it.name == "processResponse" }.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, $LICENSE_STATE->OK:$LICENSE_STATE
                    sput-object v0, $LICENSE_CLIENT->licenseCheckState:$LICENSE_STATE
                    return-void
                """.trimIndent(),
            )
        }

        // handleError(LicenseCheckException) — no-op to prevent error dialogs killing the app.
        licenseClass.methods.first { it.name == "handleError" }.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // ── Gate 2: In-app billing subscription LiveData setter ──────────────────────
        // P4/L.b(P4/L, boolean) logs "SUBSCRIBED" then posts the boolean to MutableLiveData.
        // Injecting const/4 p1, 0x1 at index 0 forces it to always post true → premium UI.
        SubscriptionLiveDataSetterFingerprint.method.addInstructions(0, "const/4 p1, 0x1")
    }
}
