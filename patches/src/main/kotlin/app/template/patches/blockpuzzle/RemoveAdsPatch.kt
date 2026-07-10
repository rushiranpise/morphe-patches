package app.template.patches.blockpuzzle

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.BLOCKPUZZLE_COMPATIBILITY
import org.w3c.dom.Element

private val adPermissions = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
    "android.permission.AD_SERVICES_CONFIG",
)

private val stripAdManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            listOf("uses-permission", "uses-permission-sdk-23").forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (index in nodes.length - 1 downTo 0) {
                    val node = nodes.item(index) as Element
                    if (node.getAttribute("android:name") in adPermissions) {
                        manifest.removeChild(node)
                    }
                }
            }

            val application = document.getElementsByTagName("application").item(0) as Element
            listOf("provider", "service", "activity").forEach { tag ->
                val nodes = application.getElementsByTagName(tag)
                for (index in 0 until nodes.length) {
                    val node = nodes.item(index) as Element
                    val name = node.getAttribute("android:name")
                    if (name.startsWith("com.google.android.gms.ads.")) {
                        node.setAttribute("android:enabled", "false")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables Block Puzzle AdMob initialization, banner, and interstitial ads.",
    default = true,
) {
    compatibleWith(BLOCKPUZZLE_COMPATIBILITY)
    dependsOn(stripAdManifestPatch)

    execute {
        listOf(
            InitAdmobFingerprint,
            LoadInterstitialFingerprint,
            ShowBannerFingerprint,
            ShowInterstitialFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.addInstructions(0, "return-void")
        }
    }
}
