package app.template.patches.amoledpix

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.AMOLEDPIX_COMPATIBILITY
import app.template.patches.shared.clearBody
import org.w3c.dom.Element

private val stripPairipManifestPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val permissions = document.getElementsByTagName("uses-permission")
            for (index in permissions.length - 1 downTo 0) {
                val node = permissions.item(index) as Element
                if (node.getAttribute("android:name") == "com.android.vending.CHECK_LICENSE") {
                    manifest.removeChild(node)
                }
            }

            val application = document.getElementsByTagName("application").item(0) as Element
            if (application.getAttribute("android:name") == "com.pairip.application.Application") {
                application.setAttribute("android:name", "com.androholic.amoledpix.App")
            }
        }
    }
}

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks AmoledPix premium features and disables ads.",
    default = true,
) {
    compatibleWith(AMOLEDPIX_COMPATIBILITY)
    dependsOn(stripPairipManifestPatch)

    execute {
        listOf(
            AdGatekeeperIsPremiumUserFingerprint,
            PrefManagerIsPremiumFingerprint,
            PrefManagerIsPremiumLinkedFingerprint,
            PrefManagerIsPremiumRestoreAvailableFingerprint,
            PrefManagerShouldPreserveLegacyPremiumAccessFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x1
                        return v0
                    """.trimIndent(),
                )
            }
        }

        listOf(
            AdGatekeeperShouldShowBannerFingerprint,
            AdGatekeeperShouldShowInterstitialFingerprint,
            AdGatekeeperShouldShowNativeFeedFingerprint,
            AdGatekeeperShouldShowPagerNativeFingerprint,
            AdGatekeeperShouldShowRewardedFingerprint,
            PrefManagerIsPremiumMigrationPendingFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        return v0
                    """.trimIndent(),
                )
            }
        }

        LicenseProviderOnCreateFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """.trimIndent(),
            )
        }

        InitializeLicenseCheckFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        CheckLicenseFingerprint.method.addInstructions(0, "return-void")
    }
}
