package app.template.patches.shared.universal

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

/**
 * Universal "Remove internet permission" patch.
 *
 * Adapted from Morning-Entree-Patches (Entree3k/Morning-Entree-Patches),
 * originally ported from adobo (dev.jkcarino.adobo...network.RemoveInternetPermissionPatch).
 *
 * Strips `<uses-permission android:name="android.permission.INTERNET"/>` from the manifest.
 * With no INTERNET permission the OS blocks every socket the app opens, so bundled
 * ad/analytics/telemetry SDKs cannot phone home.
 *
 * ⚠ Also disables any legitimate online features the app has. Only enable for apps
 * you want fully offline, or when combined with a network proxy/hosts patch.
 */
@Suppress("unused")
val removeInternetPermissionPatch = resourcePatch(
    name = "Remove internet permission",
    description = "Removes the INTERNET permission so the app cannot access the network at all. " +
        "Blocks all trackers, analytics and ads from phoning home, but also disables any " +
        "legitimate online features. Only enable for apps you want fully offline.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getElementsByTagName("manifest").item(0)
            val permissions = manifest.childNodes
            val toRemove = mutableListOf<org.w3c.dom.Node>()

            for (i in 0 until permissions.length) {
                val node = permissions.item(i) as? Element ?: continue
                if (node.tagName == "uses-permission" &&
                    node.getAttribute("android:name") == "android.permission.INTERNET"
                ) {
                    toRemove.add(node)
                }
            }

            toRemove.forEach { manifest.removeChild(it) }
        }
    }
}
