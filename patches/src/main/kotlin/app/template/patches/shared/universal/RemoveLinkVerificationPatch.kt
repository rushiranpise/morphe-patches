package app.template.patches.shared

import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.getNode
import org.w3c.dom.Element

/**
 * Removes `autoVerify` from all intents. This fixes 'open link with' that are impossible
 * to manually enable on some devices.
 */
@Suppress("unused")
val removeLinkVerification = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.getNode("manifest") as Element
            val intentFilters = manifest.getElementsByTagName("intent-filter")

            for (i in 0 until intentFilters.length) {
                val element = intentFilters.item(i) as Element
                val attribute = "android:autoVerify"
                if (element.hasAttribute(attribute)) {
                    element.removeAttribute(attribute)
                }
            }
        }
    }
}
