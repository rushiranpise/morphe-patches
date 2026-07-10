package app.template.patches.picturemushroom

import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.BuildSecrets
import org.w3c.dom.Element

val pictureMushroomMapApiKeyPatch = resourcePatch {
    execute {
        document("res/values/strings.xml").use { document ->
            val strings = document.getElementsByTagName("string")
            for (i in 0 until strings.length) {
                val node = strings.item(i) as? Element ?: continue
                if (node.getAttribute("name") == "google_maps_key") {
                    node.textContent = BuildSecrets.SHARED_MAPS_API_KEY
                    break
                }
            }
        }
    }
}
