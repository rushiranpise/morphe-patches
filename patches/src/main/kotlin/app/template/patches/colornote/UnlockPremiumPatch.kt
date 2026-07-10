package app.template.patches.colornote

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COLORNOTE_COMPATIBILITY
import org.w3c.dom.Element

private val adPermissions = setOf(
    "com.google.android.gms.permission.AD_ID",
    "android.permission.ACCESS_ADSERVICES_AD_ID",
)

private val stripAdIdPatch = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            val permissionNodes = document.getElementsByTagName("uses-permission")
            for (index in permissionNodes.length - 1 downTo 0) {
                val node = permissionNodes.item(index) as Element
                if (node.getAttribute("android:name") in adPermissions) {
                    manifest.removeChild(node)
                }
            }
        }
    }
}

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks ColorNote premium and removes advertising ID permissions.",
    default = true,
) {
    compatibleWith(COLORNOTE_COMPATIBILITY)
    dependsOn(stripAdIdPatch)

    execute {
        IsPremiumFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )
    }
}
