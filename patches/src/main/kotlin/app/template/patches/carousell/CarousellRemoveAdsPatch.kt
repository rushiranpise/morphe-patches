package app.template.patches.carousell

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.CAROUSELL_COMPATIBILITY
import app.template.patches.shared.clearBody
import org.w3c.dom.Element

private val adPermissions = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
    "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
    "android.permission.ACCESS_ADSERVICES_TOPICS",
)

private val adComponents = setOf(
    "com.google.android.gms.ads.MobileAdsInitProvider",
    "com.google.android.gms.ads.AdService",
    "com.google.android.gms.ads.AdActivity",
    "com.google.android.gms.ads.OutOfContextTestingActivity",
    "com.google.android.gms.ads.NotificationHandlerActivity",
)

private val carousellStripAdManifestPatch = resourcePatch(
    name = "Remove ADS",
    description = "Removes ad permissions and disables bundled ad SDK components in the manifest.",
    default = true
) {
    compatibleWith(CAROUSELL_COMPATIBILITY)

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val application = document.getElementsByTagName("application").item(0) as Element

            val permissionNodes = document.getElementsByTagName("uses-permission")
            for (i in permissionNodes.length - 1 downTo 0) {
                val node = permissionNodes.item(i) as Element
                if (node.getAttribute("android:name") in adPermissions) {
                    manifest.removeChild(node)
                }
            }

            listOf("activity", "provider", "service").forEach { tag ->
                val nodes = application.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    if (node.getAttribute("android:name") in adComponents) {
                        node.setAttribute("android:enabled", "false")
                    }
                }
            }
        }
    }
}

@Suppress("unused")
val carousellRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables interstitial/banner ad loaders, hides promoted listings and profiles from search feeds.",
    default = true
) {
    compatibleWith(CAROUSELL_COMPATIBILITY)
    dependsOn(carousellStripAdManifestPatch)

    execute {
        // Force getExcludePromotedListing() → true so search API excludes promoted listings
        ExcludePromotedListingFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // Force getExcludePromotedProfile() → true so search API excludes promoted profiles
        ExcludePromotedProfileFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """
            )
        }

        // Nop interstitial ad loader — return Observable.empty() instead of loading ads
        AdLoadManagerLoadInterstitialFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-static {}, Lio/reactivex/r;->empty()Lio/reactivex/r;
                    move-result-object v0
                    return-object v0
                """
            )
        }

        // Nop banner/native ad loader — return Observable.empty()
        AdLoadManagerLoadBannerFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-static {}, Lio/reactivex/r;->empty()Lio/reactivex/r;
                    move-result-object v0
                    return-object v0
                """
            )
        }

        // Return empty list from getPromotedListingCards() so inline promoted cards are never rendered
        GetAdResponsePromotedCardsFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
                    move-result-object v0
                    return-object v0
                """
            )
        }
    }
}
