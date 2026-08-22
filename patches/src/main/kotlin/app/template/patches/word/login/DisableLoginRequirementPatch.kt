package app.template.patches.word.login

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val wordDisableLoginRequirementPatch = bytecodePatch {
    execute {
        // Complete the FTUX task chain immediately with SUCCESS (code 0) instead of
        // showing the sign-in UI. The boot chain proceeds as if sign-in finished.
        firstRunM0Fingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                new-instance v0, Lcom/microsoft/office/officehub/objectmodel/TaskResult;
                const/4 v1, 0x0
                invoke-direct {v0, v1}, Lcom/microsoft/office/officehub/objectmodel/TaskResult;-><init>(I)V
                invoke-interface {p2, v0}, Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;->onTaskComplete(Lcom/microsoft/office/officehub/objectmodel/TaskResult;)V
                return-void
            """)
        }

        // Set state=FINAL and mark FTUX shown without showing the upsell paywall.
        // State class renamed d$s→d$t in 16.0.20326; field reverted B→H accordingly.
        // n0() shows the FTUX upsell screen. We skip it by:
        //   1. Resolving the state-enum class dynamically from definingClass (avoids hardcoding "d$t")
        //   2. Setting field H (state) to FINAL via the enum's own ValueOf lookup — but simpler:
        //      just call setFTUXShown so m0() short-circuits on next entry, then return-void.
        //   The state field iput is skipped — safe because n0() is only called once per session
        //   and setFTUXShown prevents re-entry across launches.
        firstRunN0Fingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                invoke-static {}, Lcom/microsoft/office/apphost/OfficeActivityHolder;->GetActivity()Landroid/app/Activity;
                move-result-object v0
                const/4 v1, 0x1
                invoke-static {v0, v1}, Lcom/microsoft/office/officehub/util/OHubSharedPreferences;->setFTUXShown(Landroid/content/Context;Z)V
                return-void
            """)
        }

        // No-op the static FTUX upsell launcher (a0.C, renamed from a0.D in 16.0.20228).
        // Complete the listener with success instead of showing PaywallActivity.
        ftuxPaywallLauncherFingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                new-instance v0, Lcom/microsoft/office/officehub/objectmodel/TaskResult;
                const/4 v1, 0x0
                invoke-direct {v0, v1}, Lcom/microsoft/office/officehub/objectmodel/TaskResult;-><init>(I)V
                invoke-interface {p2, v0}, Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;->onTaskComplete(Lcom/microsoft/office/officehub/objectmodel/TaskResult;)V
                return-void
            """)
        }


        // Return null when sign-in name is null/empty — prevents IllegalArgumentException
        // crash on Timer-0 thread when no real account is present.
        getIdentityForSignInNameFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
        }

        // Return false — SSO not required — so the app opens directly on ProtocolActivation
        // without triggering the interactive sign-in flow.
        // Targets the public checkAndStartSSOIfRequired(Z) wrapper since isSSORequired()
        // became private in 16.0.20228.
        checkAndStartSSOIfRequiredFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}

@JvmSynthetic
internal fun wordDisableLoginRequirementDependency() = wordDisableLoginRequirementPatch
