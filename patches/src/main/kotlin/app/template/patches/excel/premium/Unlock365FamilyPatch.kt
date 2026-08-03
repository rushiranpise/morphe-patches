package app.template.patches.excel.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val excelUnlock365FamilyPatch = bytecodePatch {
    execute {
        // Force LicensingState = ConsumerPremium everywhere.
        getLicensingStateFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, Lcom/microsoft/office/licensing/LicensingState;->ConsumerPremium:Lcom/microsoft/office/licensing/LicensingState;
                return-object v0
            """)
        }
        licenseSessionStateFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, Lcom/microsoft/office/licensing/LicensingState;->ConsumerPremium:Lcom/microsoft/office/licensing/LicensingState;
                return-object v0
            """)
        }

        // All individual plan checks must return true.
        hasFamilyPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPersonalPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPremiumPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Return empty non-null LicenseInfo so Has*Plan patches are reached.
        // (Method renamed g→h in 16.0.20228.)
        licensingFGFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                const/4 v0, 0x0
                new-array v0, v0, [Lcom/microsoft/office/licensing/OlsEntitlement;
                new-instance v1, Lcom/microsoft/office/licensing/LicenseInfo;
                invoke-direct {v1, v0}, Lcom/microsoft/office/licensing/LicenseInfo;-><init>([Lcom/microsoft/office/licensing/OlsEntitlement;)V
                return-object v1
            """)
        }

        // Boot-time paywall gate — return true to skip LaunchSubscriptionPurchaseFlow.
        subscriptionStatusYFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Suppress upsell feature gates.
        isPremiumPlanUpsellEnabledFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        isEnterpriseViewOLSCheckEnabledFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // Remove trial badge from paywall UI.
        subscriptionDataIsTrialFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // Upsell plugin — treat every license instance as premium.
        licenseStatusIsPremiumFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Show signed-in avatar in MeControl without a real account.
        accountProfileInfoHasProfileFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Skip storage quota UI (NPE guard when no real identity).
        storageQuotaCheckFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // Skip account-switcher dialog (NPE guard when GetActiveIdentity()=null).
        accountSwitcherRunnableFingerprint.method.apply {
            clearBody(); addInstructions(0, "return-void")
        }
    }
}

@JvmSynthetic
internal fun excelUnlock365FamilyDependency() = excelUnlock365FamilyPatch
