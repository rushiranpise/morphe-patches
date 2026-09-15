package app.template.patches.colornote

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COLORNOTE_COMPATIBILITY
import app.template.patches.shared.returnEarly
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
    description = "Unlocks ColorNote premium by forcing license validity and subscription checks to return true.",
    default = true,
) {
    compatibleWith(COLORNOTE_COMPATIBILITY)
    dependsOn(stripAdIdPatch)

    execute {
        // Primary: sm.F4.a.i()Z — root of isPremium chain.
        // returnEarly(true) = license always valid.
        IsPremiumFingerprint.method.returnEarly(true)

        // Belt-and-suspenders: sm.d5.y.i()Z — isSubscribed().
        // Covers null short-circuit path that bypasses F4.a.i().
        IsSubscribedFingerprint.method.returnEarly(true)
    }
}
