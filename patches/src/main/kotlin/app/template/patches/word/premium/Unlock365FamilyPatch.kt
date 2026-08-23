package app.template.patches.word.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

private val wordUnlock365FamilyPatch = bytecodePatch {
    execute {
        // Force LicensingState = ConsumerPremium everywhere. This single enum value
        // controls all subscription UI: hides Buy/Upgrade buttons, shows premium status
        // in Backstage, and suppresses all upsell prompts.
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

        // All three Has*Plan methods must return true so that any individual plan
        // check also passes, regardless of which the app queries.
        hasFamilyPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPersonalPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        hasPremiumPlanFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Return an empty (but non-null) LicenseInfo so that the Has*Plan methods
        // above are reached. Without this, f.h() returning null causes a NullPointerException
        // before Has*Plan is ever evaluated.
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

        // Boot-time paywall dispatcher — m(Context)V calls y/o/r/v in sequence.
        // return-void silently no-ops the entire dispatch, skipping LaunchSubscriptionPurchaseFlow.
        subscriptionStatusYFingerprint.method.returnEarly()

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

        // Make the upsell plugin treat every license as premium.
        licenseStatusIsPremiumFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Show the signed-in avatar in MeControl without a real account.
        accountProfileInfoHasProfileFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Skip storage quota UI (NPE guard when no real identity exists).
        storageQuotaCheckFingerprint.method.apply {
            clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // b$n.run() NPE guard — GetActiveIdentity() returns null with no account.
        // Insert null check after move-result-object v12; return-void if null.
        accountSwitcherRunnableFingerprint.apply {
            val getActiveIdentityIdx = instructionMatches[1].index
            val moveResultIdx = getActiveIdentityIdx + 1
            method.addInstructionsWithLabels(moveResultIdx + 1, """
                if-nez v12, :has_identity
                return-void
                :has_identity
                nop
            """)
        }

    }
}

@JvmSynthetic
internal fun wordUnlock365FamilyDependency() = wordUnlock365FamilyPatch
