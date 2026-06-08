package app.template.patches.pialytic

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PIALYTIC_COMPATIBILITY

/**
 * Dismiss Activity pattern - same as Citizen patches.
 * Call super.onCreate(), then finish() immediately to prevent Activity from showing.
 */
private fun dismissActivity(@Suppress("UNUSED_PARAMETER") cls: String) =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
    "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
    "return-void"

@Suppress("unused")
val pialyticUnlockAllPatch = bytecodePatch(
    name = "Unlock All Features",
    description = "Bypasses PairIP DRM license check, removes all paywalls, and unlocks all premium features including cloud sync and remote access.",
    default = true
) {
    compatibleWith(PIALYTIC_COMPATIBILITY)

    execute {
        // ── Layer 1: LicenseActivity.onCreate() ─────────────────────────────────────
        // Primary entry point - dismiss Activity immediately to block paywall/error UI
        runCatching {
            LicenseActivityOnCreateFingerprint
                .match(classDefBy(LicenseActivityOnCreateFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, dismissActivity(LicenseActivityOnCreateFingerprint.definingClass!!))
                }
        }

        // ── Layer 2: LicenseActivity Helper Methods ─────────────────────────────────
        // Defense-in-depth: no-op all methods that show paywall/error UI or close app
        listOf(
            ShowPaywallAndCloseAppFingerprint,
            ShowErrorDialogFingerprint,
            CloseAppFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
            }
        }

        // ── Layer 3: LicenseClient Backend Methods ──────────────────────────────────
        // No-op all license check logic at the core verification layer
        listOf(
            LicenseClientInitFingerprint,
            LicenseClientCheckInternalFingerprint,
            LicenseClientHandleErrorFingerprint,
            LicenseClientProcessResponseFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
            }
        }

        // ── Layer 4: BillingService Initialization ──────────────────────────────────
        // Prevent Google Play billing from initializing
        runCatching {
            BillingServiceInitFingerprint
                .match(classDefBy(BillingServiceInitFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
        }

        // ── Layer 5: BillingService Query Methods ───────────────────────────────────
        // No-op all purchase query and verification methods
        listOf(
            BillingServiceQueryPurchasesFingerprint,
            BillingServiceStartPurchaseFingerprint
        ).forEach { fp ->
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
            }
        }

        // BillingServiceQueryProductDetailsAsync returns CompletableFuture - return null
        runCatching {
            BillingServiceQueryProductDetailsFingerprint
                .match(classDefBy(BillingServiceQueryProductDetailsFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                }
        }

        // ── Layer 6: Premium Feature Gates ──────────────────────────────────────────
        // Validator.isProVersion() - Core premium check that gates ALL premium features
        // Returns true to unlock: cloud sync, PDF generation, unlimited projects, etc.
        runCatching {
            ValidatorIsProVersionFingerprint
                .match(classDefBy(ValidatorIsProVersionFingerprint.definingClass!!))
                .method.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }
    }
}

