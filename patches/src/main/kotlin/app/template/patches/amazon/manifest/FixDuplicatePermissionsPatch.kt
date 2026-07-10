package app.template.patches.amazon.manifest

import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.AMAZON_IN_COMPATIBILITY
import app.template.patches.shared.Constants.AMAZON_SHOPPING_COMPATIBILITY
import org.w3c.dom.Element

private val PERMISSION_DECLARATION_TAGS = listOf(
    "permission",
    "permission-group",
    "permission-tree",
)

private val PERMISSION_REFERENCE_TAGS = listOf(
    "uses-permission",
    "uses-permission-sdk-23",
    "uses-permission-sdk-m",
)

private val PERMISSION_REFERENCE_ATTRIBUTES = listOf(
    "android:permission",
    "android:readPermission",
    "android:writePermission",
)

@Suppress("unused")
val amazonFixDuplicatePermissionsPatch = resourcePatch(
    name = "Fix Amazon manifest conflicts",
    description = "Updates shared Amazon permissions so other Amazon apps can coexist.",
    default = true,
) {
    compatibleWith(AMAZON_SHOPPING_COMPATIBILITY, AMAZON_IN_COMPATIBILITY)

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val packageName = manifest.getAttribute("package")
            val uniquePrefix = "$packageName.morphe"
            val permissionRenames = linkedMapOf<String, String>()

            PERMISSION_DECLARATION_TAGS.forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    val oldName = node.getAttribute("android:name")
                    if (oldName.isBlank() || oldName.startsWith("$uniquePrefix.")) continue

                    val newName = "$uniquePrefix.$oldName"
                    node.setAttribute("android:name", newName)
                    permissionRenames[oldName] = newName
                }
            }

            PERMISSION_REFERENCE_TAGS.forEach { tag ->
                val nodes = document.getElementsByTagName(tag)
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    val newName = permissionRenames[node.getAttribute("android:name")] ?: continue
                    node.setAttribute("android:name", newName)
                }
            }

            val allNodes = document.getElementsByTagName("*")
            for (i in 0 until allNodes.length) {
                val node = allNodes.item(i) as Element

                PERMISSION_REFERENCE_ATTRIBUTES.forEach { attribute ->
                    val newName = permissionRenames[node.getAttribute(attribute)] ?: return@forEach
                    node.setAttribute(attribute, newName)
                }
            }
        }
    }
}
