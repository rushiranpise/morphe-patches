package app.template.patches.excel.manifest

import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

private val excelRemoveSharedUserIdPatch = resourcePatch(
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.getElementsByTagName("manifest").item(0) as Element
            manifest.removeAttribute("android:sharedUserId")
            manifest.removeAttribute("android:sharedUserLabel")
        }
    }
}

@JvmSynthetic
internal fun excelRemoveSharedUserIdDependency() = excelRemoveSharedUserIdPatch
