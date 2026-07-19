package app.template.patches.googlephotos

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE_NAME = "com.google.android.apps.photos"
private const val DEFAULT_PACKAGE_NAME = "app.morphe.android.apps.photos"
private const val MARS_AUTHORITY = "app.revanced.android.apps.photos.api.mars"
private const val APP_NAME_STRING = "morphe_google_photos_app_name"

// Based on RI-Vanced Universal "Change package name", with Photos-specific provider fixes.
@Suppress("unused")
val googlePhotosChangePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Installs Google Photos beside the system Photos app by changing package, permissions, providers, and app name.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)

    val packageName by stringOption(
        key = "googlePhotosPackageName",
        default = DEFAULT_PACKAGE_NAME,
        title = "Package name",
        description = "Package name for the cloned Google Photos app.",
        required = true,
    ) { it?.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$")) == true }

    val appName by stringOption(
        key = "googlePhotosAppName",
        default = "Photos Morphe",
        title = "App name",
        description = "Launcher name for the cloned Google Photos app.",
        required = true,
    ) { !it.isNullOrBlank() }

    execute {
        val newPackageName = packageName ?: DEFAULT_PACKAGE_NAME

        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            manifest.setAttribute("package", newPackageName)

            replaceNameAttributes(document.getElementsByTagName("permission"), newPackageName)
            replaceNameAttributes(document.getElementsByTagName("uses-permission"), newPackageName)
            replaceComponentPermissions(document.getElementsByTagName("*"), newPackageName)
            replaceProviderAuthorities(document.getElementsByTagName("provider"), newPackageName)
            replaceMarsHosts(document.getElementsByTagName("data"))

            (document.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:label", "@string/$APP_NAME_STRING")
        }

        document("res/values/strings.xml").use { document ->
            val resources = document.documentElement
            val strings = document.getElementsByTagName("string")
            val existing = (0 until strings.length)
                .mapNotNull { strings.item(it) as? Element }
                .firstOrNull { it.getAttribute("name") == APP_NAME_STRING }
            val target = existing ?: document.createElement("string").also {
                it.setAttribute("name", APP_NAME_STRING)
                resources.appendChild(it)
            }
            target.textContent = appName ?: "Photos Morphe"
        }
    }
}

private fun replaceNameAttributes(nodes: org.w3c.dom.NodeList, newPackageName: String) {
    for (i in 0 until nodes.length) {
        val element = nodes.item(i) as? Element ?: continue
        val name = element.getAttribute("android:name")
        if (name.startsWith(ORIGINAL_PACKAGE_NAME)) {
            element.setAttribute("android:name", name.replace(ORIGINAL_PACKAGE_NAME, newPackageName))
        }
    }
}

private fun replaceComponentPermissions(nodes: org.w3c.dom.NodeList, newPackageName: String) {
    for (i in 0 until nodes.length) {
        val element = nodes.item(i) as? Element ?: continue
        val permission = element.getAttribute("android:permission")
        if (permission.startsWith(ORIGINAL_PACKAGE_NAME)) {
            element.setAttribute("android:permission", permission.replace(ORIGINAL_PACKAGE_NAME, newPackageName))
        }
    }
}

private fun replaceProviderAuthorities(nodes: org.w3c.dom.NodeList, newPackageName: String) {
    for (i in 0 until nodes.length) {
        val provider = nodes.item(i) as? Element ?: continue
        val authorities = provider.getAttribute("android:authorities")
        if (authorities.isBlank()) continue

        val rewritten = authorities.split(";").joinToString(";") { authority ->
            when {
                authority.startsWith(ORIGINAL_PACKAGE_NAME) ->
                    authority.replace(ORIGINAL_PACKAGE_NAME, newPackageName)
                authority == "com.google.android.libraries.photos.api.mars" ->
                    MARS_AUTHORITY
                else -> authority
            }
        }
        provider.setAttribute("android:authorities", rewritten)
    }
}

private fun replaceMarsHosts(nodes: org.w3c.dom.NodeList) {
    for (i in 0 until nodes.length) {
        val data = nodes.item(i) as? Element ?: continue
        if (data.getAttribute("android:host") == "com.google.android.libraries.photos.api.mars") {
            data.setAttribute("android:host", MARS_AUTHORITY)
        }
    }
}
