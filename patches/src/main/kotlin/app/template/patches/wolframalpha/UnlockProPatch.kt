package app.template.patches.wolframalpha

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val wolframAlphaUnlockProPatch = bytecodePatch(
    name = "Unlock Professional",
    description = "Unlocks Professional features inapp.",
) {
    compatibleWith(Constants.WOLFRAMALPHA_COMPATIBILITY)

    execute {
        listOf(
            IsProfessionalFingerprint,
            IsProEnabledFingerprint,
            IsActiveSubscriptionFingerprint,
            IsAndroidSubscriptionFingerprint,
            IsWolframSubscriptionFingerprint,
            SubscriptionSuccessFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        SetSubscriptionDataFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    iput-boolean v0, p0, Lcom/wolfram/android/alphapro/WolframAlphaProApplication;->X1:Z
                    return-void
                """,
            )
        }

        OnboardingCreateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-super {p0, p1}, Lf/l;->onCreate(Landroid/os/Bundle;)V
                    const-string v0, "WolframAlphaProFragment"
                    invoke-virtual {p0, v0}, Lcom/wolfram/android/alphapro/activity/ProUserOnBoardingActivity;->w(Ljava/lang/String;)V
                    return-void
                """,
            )
        }

        SubscriptionStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"SUBSCRIPTION_STATE_ACTIVE\"\nreturn-object v0")
        }

        SubscriptionProductLevelFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Professional\"\nreturn-object v0")
        }

        SubscriptionUseTypeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Professional\"\nreturn-object v0")
        }

        SubscriptionPaymentTermsFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Android\"\nreturn-object v0")
        }

        SubscriptionPeriodUnitFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Year\"\nreturn-object v0")
        }

        SubscriptionPeriodValueFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        AccountPlanPrimaryFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"Professional\"\nreturn-object v0")
        }

        AccountPlanSecondaryFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"1-Year Subscription\"\nreturn-object v0")
        }
    }
}
