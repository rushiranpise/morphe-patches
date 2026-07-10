package app.template.patches.excel.login

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.clearBody

private val excelDisableLoginRequirementPatch = bytecodePatch(
) {
    execute {
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

        firstRunN0Fingerprint.method.apply {
            clearBody()
            addInstructions(0, """
                    sget-object v0, Lcom/microsoft/office/firstrun/d${'$'}s;->FINAL:Lcom/microsoft/office/firstrun/d${'$'}s;
                    iput-object v0, p0, Lcom/microsoft/office/firstrun/d;->H:Lcom/microsoft/office/firstrun/d${'$'}s;
                    invoke-static {}, Lcom/microsoft/office/apphost/OfficeActivityHolder;->GetActivity()Landroid/app/Activity;
                    move-result-object v0
                    const/4 v1, 0x1
                    invoke-static {v0, v1}, Lcom/microsoft/office/officehub/util/OHubSharedPreferences;->setFTUXShown(Landroid/content/Context;Z)V
                    return-void
                """)
        }

        // a0.D() — complete the listener with success instead of showing paywall
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
        isSSORequiredFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}

@JvmSynthetic
internal fun excelDisableLoginRequirementDependency() = excelDisableLoginRequirementPatch
