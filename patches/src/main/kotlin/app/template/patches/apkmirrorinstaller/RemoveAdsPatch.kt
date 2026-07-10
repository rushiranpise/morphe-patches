package app.template.patches.apkmirrorinstaller

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.APKMIRROR_INSTALLER_COMPATIBILITY
import app.template.patches.shared.clearBody
import org.w3c.dom.Element

private val stripAdManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val permissions = document.getElementsByTagName("uses-permission")
            for (index in permissions.length - 1 downTo 0) {
                val node = permissions.item(index) as Element
                if (node.getAttribute("android:name") in setOf(
                        "android.permission.ACCESS_ADSERVICES_AD_ID",
                    )
                ) {
                    manifest.removeChild(node)
                }
            }

            fun disable(tag: String, names: Set<String>) {
                val nodes = document.getElementsByTagName(tag)
                for (index in 0 until nodes.length) {
                    val node = nodes.item(index) as Element
                    if (node.getAttribute("android:name") in names) {
                        node.setAttribute("android:enabled", "false")
                    }
                }
            }

            disable("provider", setOf("com.google.android.gms.ads.MobileAdsInitProvider"))
            disable("service", setOf("com.google.android.gms.ads.AdService"))
            disable(
                "activity",
                setOf(
                    "com.google.android.gms.ads.AdActivity",
                    "com.google.android.gms.ads.OutOfContextTestingActivity",
                    "com.google.android.gms.ads.NotificationHandlerActivity",
                ),
            )
        }
    }
}

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Unlock Ultimate",
    description = "Unlocks Ultimate Ad-Free + Android TV.",
    default = true,
) {
    compatibleWith(APKMIRROR_INSTALLER_COMPATIBILITY)
    dependsOn(stripAdManifestPatch)

    execute {
        RewardedInstallHandlerFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-virtual {p1}, Lcom/apkmirror/presentation/installer/InstallerActivity;->F0()Lcom/apkmirror/presentation/installer/e;
                    move-result-object v0
                    invoke-virtual {v0}, Lcom/apkmirror/presentation/installer/e;->o()V
                    sget-object v0, Lx0/c;->a:Lx0/c;
                    invoke-virtual {v0}, Lx0/c;->h()V
                    sget-object v0, Ls9/u2;->a:Ls9/u2;
                    return-object v0
                """.trimIndent(),
            )
        }

        PremiumManagerBillingDataFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Ly0/e${'$'}c;
                    const-string v1, "yearly_3"
                    const-string v2, "${'$'}99.99"
                    const-string v3, "Yearly Tier 3"
                    const-string v4, "P1Y"
                    invoke-direct {v0, v1, v2, v3, v4}, Ly0/e${'$'}c;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
                    invoke-static {v0}, Ly0/c${'$'}c;->b(Ljava/lang/Object;)Ljava/lang/Object;
                    move-result-object v0
                    invoke-static {v0}, Ly0/c${'$'}c;->a(Ljava/lang/Object;)Ly0/c${'$'}c;
                    move-result-object v0
                    return-object v0
                """.trimIndent(),
            )
        }

        setOf(
            InitAdsFingerprint.method,
            LoadRewardedAdFingerprint.method,
            LoadAdsFingerprint.method,
        ).forEach { method ->
            method.apply {
                clearBody()
                addInstructions(0, "return-void")
            }
        }
    }
}
