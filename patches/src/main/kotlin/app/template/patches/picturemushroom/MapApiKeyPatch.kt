package app.template.patches.picturemushroom

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private const val NETMONSTER_MAPS_KEY = "AIzaSyBzpcP0fFQDiYePiLmI5EPBTa35kkm_19k"

val pictureMushroomMapApiKeyPatch = resourcePatch {
    execute {
        document("res/values/strings.xml").use { document ->
            val strings = document.getElementsByTagName("string")
            for (i in 0 until strings.length) {
                val node = strings.item(i) as? Element ?: continue
                if (node.getAttribute("name") == "google_maps_key") {
                    node.textContent = NETMONSTER_MAPS_KEY
                    break
                }
            }
        }
    }
}
