package app.template.patches.facebook.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import org.w3c.dom.Element
import org.w3c.dom.Node

private const val ORIGINAL_PACKAGE = "com.facebook.katana"

// hWL.<init> calls Context.getPackageName() and matches against a packed-switch
// table of FB package names. Under a renamed clone, nothing matches → NoSuchElementException.
// Insert const-string v6, "com.facebook.katana" at index 13 to always resolve correctly.
// Verified: classes10/X/hWL.smali, com.facebook.katana 569.0.0.42.72.
private val hWLInitFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(definingClass = "Landroid/content/Context;", name = "getPackageName"),
    ),
    custom = { _, classDef ->
        classDef.superclass == "Ljava/lang/Object;" &&
            classDef.fields.any { it.type == "Landroid/content/SharedPreferences;" } &&
            classDef.fields.any { it.type == "Landroid/content/Context;" }
    },
)

// No name/description — hidden from the Morphe Manager UI.
// Applied automatically by facebookChangePackageNamePatch.
private val facebookFixAutoRestoreCrashPatch = bytecodePatch(default = false) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    dependsOn(facebookSignaturePatch)

    execute {
        hWLInitFingerprint.method.addInstruction(
            13,
            """const-string v6, "com.facebook.katana"""",
        )
    }
}
private const val DEFAULT_PACKAGE   = "app.morphe.facebook.katana"

// Parallel-install patch for Facebook.
//
// Root cause of INSTALL_FAILED_DUPLICATE_PERMISSION:
//   Facebook declares 7 signature-protected <permission> tags (e.g. com.facebook.katana.provider.ACCESS).
//   If the patched APK renames them, the system sees two packages trying to DECLARE the same
//   permission string — one under the original name, one under the new name — and rejects both.
//   Additionally, if we rename the REFERENCES to those permissions (android:permission=,
//   <uses-permission>) away from "com.facebook.katana.*", the system can't match them back to
//   the original app's declarations.
//
// Fix:
//   • <permission> declarations — REMOVE entirely from the patched APK.
//     The original com.facebook.katana owns these; the clone does not need to redeclare them.
//     Since protectionLevel="signature" the clone can't use them anyway (different signing key).
//
//   • android:permission / android:readPermission / android:writePermission on components
//     — LEAVE as "com.facebook.katana.*" (keep pointing at the original app's declarations).
//     These are enforcement attributes; they remain meaningful only when both apps are installed.
//
//   • <uses-permission android:name="com.facebook.katana.*">
//     — REMOVE from the patched APK (signature permissions are only grantable to apps with the
//       same signing key; they will never be granted to the clone regardless).
//
//   • <manifest package="..."> — rename to newPkg (required for parallel install).
//
//   • <provider android:authorities="..."> — rename so provider URIs don't collide with the
//     original. Authority strings are the only thing that must be unique across installs.
//
//   • Launcher label — rename via string resource injection.
//
// What is NOT renamed:
//   • DEX class names (activity/service/receiver/provider android:name pointing to .java classes)
//   • android:permission refs (stays "com.facebook.katana.*")
//   • <uses-permission> for "com.facebook.*" non-katana permissions (kept as-is)
//   • Any non-package-name authority that doesn't start with ORIGINAL_PACKAGE
@Suppress("unused")
val facebookChangePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Installs Facebook beside the original by renaming the manifest package and provider authorities, removing duplicate permission declarations.",
    default = false,
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    // Auto-applies the hWL.<init> crash fix so messaging threads don't crash
    // when the app runs under a renamed package name.
    dependsOn(facebookFixAutoRestoreCrashPatch)

    val packageName by stringOption(
        key = "facebookPackageName",
        default = DEFAULT_PACKAGE,
        title = "Package name",
        description = "Package name for the cloned Facebook app.",
        required = true,
    ) { it?.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")) == true }

    val appName by stringOption(
        key = "facebookAppName",
        default = "Facebook Morphe",
        title = "App name",
        description = "Launcher label shown in the home screen for the cloned Facebook.",
        required = true,
    ) { !it.isNullOrBlank() }

    execute {
        val newPkg = packageName ?: DEFAULT_PACKAGE

        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.documentElement

            // ── 1. Rename top-level package attribute ────────────────────────
            manifest.setAttribute("package", newPkg)

            // ── 2. Remove ALL <permission> declarations ───────────────────────
            // Every <permission> in Facebook's manifest is a signature-protected permission
            // OWNED by com.facebook.katana. Re-declaring ANY of them under a new package
            // name causes INSTALL_FAILED_DUPLICATE_PERMISSION regardless of the name prefix
            // (e.g. com.facebook.permission.prod.FB_APP_COMMUNICATION also triggers it).
            // The safest rule: remove every <permission> element unconditionally — the
            // original app retains ownership of all of them.
            val toRemove = mutableListOf<Node>()
            val permissionNodes = doc.getElementsByTagName("permission")
            for (i in 0 until permissionNodes.length) {
                toRemove.add(permissionNodes.item(i))
            }
            toRemove.forEach { it.parentNode?.removeChild(it) }

            // ── 3. Remove <uses-permission> for signature-protected katana perms ─
            // Signature permissions are only grantable to apps signed with the same key.
            // The clone uses a different key, so these grants will never succeed.
            // Keeping them causes a lint/install warning on some Android versions.
            val usesPermNodes = doc.getElementsByTagName("uses-permission")
            val usesPermToRemove = mutableListOf<Node>()
            for (i in 0 until usesPermNodes.length) {
                val el = usesPermNodes.item(i) as? Element ?: continue
                val name = el.getAttribute("android:name")
                if (name.startsWith(ORIGINAL_PACKAGE)) {
                    usesPermToRemove.add(el)
                }
            }
            usesPermToRemove.forEach { it.parentNode?.removeChild(it) }

            // ── 4. Rename provider authorities ───────────────────────────────
            // Each <provider android:authorities="..."> must be unique across installed apps.
            // Rename all authorities that start with the original package.
            // Handle semicolon-separated multi-authority entries.
            // NOTE: Do NOT rename android:permission on providers — those point back to
            // com.facebook.katana.provider.ACCESS which is owned by the original app.
            val providers = doc.getElementsByTagName("provider")
            for (i in 0 until providers.length) {
                val provider = providers.item(i) as? Element ?: continue
                val auth = provider.getAttribute("android:authorities")
                if (auth.isBlank()) continue
                val rewritten = auth.split(";").joinToString(";") { part ->
                    val trimmed = part.trim()
                    if (trimmed.startsWith(ORIGINAL_PACKAGE))
                        " " + trimmed.replace(ORIGINAL_PACKAGE, newPkg)
                    else
                        " $trimmed"
                }.trimStart()
                provider.setAttribute("android:authorities", rewritten)
            }

            // ── 5. Rename the application label ──────────────────────────────
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:label", "@string/facebook_morphe_app_name")
        }

        // ── 6. Inject app name string resource ───────────────────────────────
        document("res/values/strings.xml").use { doc ->
            val resources = doc.documentElement
            val strings = doc.getElementsByTagName("string")
            val existing = (0 until strings.length)
                .mapNotNull { strings.item(it) as? Element }
                .firstOrNull { it.getAttribute("name") == "facebook_morphe_app_name" }
            val node = existing ?: doc.createElement("string").also {
                it.setAttribute("name", "facebook_morphe_app_name")
                resources.appendChild(it)
            }
            node.textContent = appName ?: "Facebook Morphe"
        }
    }
}
