package app.template.patches.word.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val wordUnlock365FamilyPatch = bytecodePatch {
    execute {
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
        storageQuotaCheckFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        isPremiumPlanUpsellEnabledFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        isEnterpriseViewOLSCheckEnabledFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        licenseStatusIsPremiumFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        subscriptionDataIsTrialFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
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
        subscriptionPaywallGateFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        subscriptionStatusYFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasFamilyPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPersonalPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPremiumPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}

@JvmSynthetic
internal fun wordUnlock365FamilyDependency() = wordUnlock365FamilyPatch
