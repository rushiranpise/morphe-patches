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
        // Field renamed H→B in 16.0.20228 — updated accordingly.
        firstRunN0Fingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, Lcom/microsoft/office/firstrun/d${'$'}s;->FINAL:Lcom/microsoft/office/firstrun/d${'$'}s;
                iput-object v0, p0, Lcom/microsoft/office/firstrun/d;->B:Lcom/microsoft/office/firstrun/d${'$'}s;
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
                invoke-interface {p3, v0}, Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;->onTaskComplete(Lcom/microsoft/office/officehub/objectmodel/TaskResult;)V
                return-void
            """)
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
