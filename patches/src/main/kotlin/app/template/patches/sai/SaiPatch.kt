package app.template.patches.sai

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.SAI_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.returnEarly
import org.w3c.dom.Element

private val saiStripAdsAndLicenseManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val permissions = document.getElementsByTagName("uses-permission")
            for (index in permissions.length - 1 downTo 0) {
                val node = permissions.item(index) as Element
                if (node.getAttribute("android:name") in setOf(
                        "com.android.vending.CHECK_LICENSE",
                        "android.permission.ACCESS_ADSERVICES_AD_ID",
                        "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
                        "android.permission.ACCESS_ADSERVICES_TOPICS",
                        "com.google.android.gms.permission.AD_ID",
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
                    "com.pairip.licensecheck.LicenseActivity",
                ),
            )
        }
    }
}

@Suppress("unused")
val saiUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks SAI PRO gates and removes ads.",
    default = true,
) {
    compatibleWith(SAI_COMPATIBILITY)
    dependsOn(saiStripAdsAndLicenseManifestPatch)

    execute {
        killPairIpFull()

        HasActiveSubscriptionPreferenceFingerprint.method.returnEarly(true)

        SetActiveSubscriptionPreferenceFingerprint.method.addInstructions(
            0,
            "const/4 p2, 0x1",
        )

        ActiveSubscriptionFlowEmitFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    iget-object v0, p0, Lb89;->a:Ldc3;
                    sget-object v1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    invoke-interface {v0, v1, p2}, Ldc3;->emit(Ljava/lang/Object;Lvs1;)Ljava/lang/Object;
                    move-result-object v0
                    return-object v0
                """.trimIndent(),
            )
        }
    }
}
