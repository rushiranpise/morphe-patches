package app.template.patches.googlephotos

import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE_NAME = "com.google.android.apps.photos"
private const val DEFAULT_PACKAGE_NAME = "app.morphe.android.apps.photos"
private const val LEGACY_MARS_AUTHORITY = "app.revanced.android.apps.photos.api.mars"
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
        val newMarsAuthority = marsAuthorityFor(newPackageName)

        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            manifest.setAttribute("package", newPackageName)

            // Rename permission strings and activity-alias names.
            // Component class names (activity, service, receiver, provider, application)
            // are DEX-class references and are intentionally left unchanged — see
            // replaceNameAttributes() for the full rationale.
            replaceNameAttributes(document.getElementsByTagName("*"), newPackageName)
            replaceComponentPermissions(document.getElementsByTagName("*"), newPackageName)
            replaceProviderAuthorities(document.getElementsByTagName("provider"), newPackageName, newMarsAuthority)
            replaceMarsHosts(document.getElementsByTagName("data"), newMarsAuthority)

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

private fun marsAuthorityFor(packageName: String) = "$packageName.api.mars"

// Replace ORIGINAL_PACKAGE_NAME with newPackageName in a string, but only when the
// value starts with ORIGINAL_PACKAGE_NAME exactly followed by end-of-string or a
// non-alpha-numeric boundary character.  This prevents a double-replace if
// newPackageName itself starts with ORIGINAL_PACKAGE_NAME (e.g. "…photos.xl"):
//   "…photos.SomeClass"  →  "…photos.xl.SomeClass"  ✓
//   "…photos.xl.Class"   →  unchanged (already replaced by a prior pass)        ✓
private fun String.safeReplace(newPackageName: String): String {
    if (!startsWith(ORIGINAL_PACKAGE_NAME)) return this
    val after = drop(ORIGINAL_PACKAGE_NAME.length)
    // Guard: if the character immediately after ORIGINAL is alphanumeric or a dot
    // that continues into the suffix of newPackageName, this value was already
    // replaced.  Specifically, if newPackageName starts with ORIGINAL + "." and the
    // current value already has that extra suffix, skip it.
    val extraSuffix = newPackageName.removePrefix(ORIGINAL_PACKAGE_NAME)
    if (extraSuffix.isNotEmpty() && after.startsWith(extraSuffix)) return this
    return newPackageName + after
}

private fun replaceNameAttributes(nodes: org.w3c.dom.NodeList, newPackageName: String) {
    // android:name means different things on different elements:
    //
    //   <permission>, <uses-permission>   → permission identifier string   → RENAME
    //   <activity-alias>                  → alias component label          → RENAME
    //   <activity>, <service>, <receiver>,
    //   <provider>, <application>         → DEX class name                → DO NOT RENAME
    //
    // DEX class names are never changed by this patch — only the manifest package
    // attribute changes.  Renaming a DEX-class android:name produces a
    // ClassNotFoundException at startup (seen with Application and ContentProvider).
    //
    // android:parentActivityName, android:targetActivity, android:manageSpaceActivity
    // are also DEX class references and must NOT be renamed for the same reason.
    val dexClassElements = setOf("activity", "service", "receiver", "provider", "application")

    for (i in 0 until nodes.length) {
        val element = nodes.item(i) as? Element ?: continue
        val value = element.getAttribute("android:name")
        if (value.startsWith(ORIGINAL_PACKAGE_NAME) && element.tagName !in dexClassElements) {
            element.setAttribute("android:name", value.safeReplace(newPackageName))
        }
    }
}

private fun replaceComponentPermissions(nodes: org.w3c.dom.NodeList, newPackageName: String) {
    for (i in 0 until nodes.length) {
        val element = nodes.item(i) as? Element ?: continue
        val permission = element.getAttribute("android:permission")
        if (permission.startsWith(ORIGINAL_PACKAGE_NAME)) {
            element.setAttribute("android:permission", permission.safeReplace(newPackageName))
        }
    }
}

private fun replaceProviderAuthorities(
    nodes: org.w3c.dom.NodeList,
    newPackageName: String,
    newMarsAuthority: String,
) {
    for (i in 0 until nodes.length) {
        val provider = nodes.item(i) as? Element ?: continue
        val authorities = provider.getAttribute("android:authorities")
        if (authorities.isBlank()) continue

        val rewritten = authorities.split(";").joinToString(";") { authority ->
            when {
                authority.startsWith(ORIGINAL_PACKAGE_NAME) ->
                    authority.safeReplace(newPackageName)
                authority == "com.google.android.libraries.photos.api.mars" ->
                    newMarsAuthority
                authority == LEGACY_MARS_AUTHORITY ->
                    newMarsAuthority
                else -> authority
            }
        }
        provider.setAttribute("android:authorities", rewritten)
    }
}

private fun replaceMarsHosts(nodes: org.w3c.dom.NodeList, newMarsAuthority: String) {
    for (i in 0 until nodes.length) {
        val data = nodes.item(i) as? Element ?: continue
        val host = data.getAttribute("android:host")
        if (host == "com.google.android.libraries.photos.api.mars" || host == LEGACY_MARS_AUTHORITY) {
            data.setAttribute("android:host", newMarsAuthority)
        }
    }
}
