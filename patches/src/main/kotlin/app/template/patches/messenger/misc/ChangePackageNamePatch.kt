package app.template.patches.messenger.misc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import org.w3c.dom.Element
import org.w3c.dom.Node

private const val ORIGINAL_PACKAGE = "com.facebook.orca"
private const val DEFAULT_PACKAGE   = "app.morphe.messenger.orca"

// ─── CsR.<init> fingerprint ──────────────────────────────────────────────────
// CsR calls Context.getPackageName() and compares the result against a packed-switch
// table mapping to "com.facebook.orca", "com.facebook.orca.debug", "com.facebook.katana".
// Under a renamed clone, nothing matches → first{} throws NoSuchElementException.
//
// Fix: insert const-string v7, "com.facebook.orca" at index 13 (after move-result-object v7).
// Overwrites the register with the original package name before the switch loop runs.
//
// Verified: classes6/X/CsR.smali — constructor (J)V, instruction sequence confirmed.
// Verified against com.facebook.orca 573.0.0.44.88.
private val CsRInitFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/Context;",
            name = "getPackageName",
        ),
    ),
    custom = { _, classDef ->
        classDef.superclass == "Ljava/lang/Object;" &&
            classDef.fields.any { it.type == "Landroid/content/SharedPreferences;" } &&
            classDef.fields.any { it.type == "Landroid/content/Context;" }
    },
)

// Fixes the encrypted-backup auto-restore crash that occurs when the app runs
// under a renamed package. Applied automatically by ChangePackageNamePatch.
private val messengerFixAutoRestoreCrashPatch = bytecodePatch(default = false) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    execute {
        CsRInitFingerprint.method.addInstruction(
            13,
            """const-string v7, "com.facebook.orca"""",
        )
    }
}

// Parallel-install patch for Messenger (com.facebook.orca).
//
// Renames the manifest package, rewrites provider authorities, removes
// <permission> declarations and matching <uses-permission> entries so there is
// no duplicate-permission conflict when Facebook (com.facebook.katana) or the
// original Messenger is installed side-by-side.
//
// Depends on messengerFixAutoRestoreCrashPatch so the encrypted-backup
// package-name lookup is spoofed before it can crash.
//
// Verified against com.facebook.orca 573.0.0.44.88.
@Suppress("unused")
val messengerChangePackageNamePatch = resourcePatch(
    name = "Change package name",
    description = "Installs Messenger beside the original by renaming the manifest package and provider authorities, removing duplicate permission declarations.",
    default = false,
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(
        messengerSignaturePatch,
        messengerFixAutoRestoreCrashPatch,
    )

    val packageName by stringOption(
        key = "messengerPackageName",
        default = DEFAULT_PACKAGE,
        title = "Package name",
        description = "Package name for the cloned Messenger app.",
        required = true,
    ) { it?.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")) == true }

    val appName by stringOption(
        key = "messengerAppName",
        default = "Messenger Morphe",
        title = "App name",
        description = "Launcher label for the cloned Messenger.",
        required = true,
    ) { !it.isNullOrBlank() }

    execute {
        val newPkg = packageName ?: DEFAULT_PACKAGE

        document("AndroidManifest.xml").use { doc ->
            val manifest = doc.documentElement

            // 1. Rename top-level package attribute
            manifest.setAttribute("package", newPkg)

            // 2. Remove ALL <permission> declarations
            val toRemove = mutableListOf<Node>()
            val permissionNodes = doc.getElementsByTagName("permission")
            for (i in 0 until permissionNodes.length) {
                toRemove.add(permissionNodes.item(i))
            }
            toRemove.forEach { it.parentNode?.removeChild(it) }

            // 3. Remove <uses-permission> for any permission we declared
            //    (covers com.facebook.orca.* AND com.facebook.receiver.* etc.)
            val declaredPermissionNames = mutableSetOf<String>()
            for (i in 0 until permissionNodes.length) {
                val el = permissionNodes.item(i) as? Element ?: continue
                val name = el.getAttribute("android:name")
                if (name.isNotBlank()) declaredPermissionNames.add(name)
            }

            val usesPermNodes = doc.getElementsByTagName("uses-permission")
            val usesPermToRemove = mutableListOf<Node>()
            for (i in 0 until usesPermNodes.length) {
                val el = usesPermNodes.item(i) as? Element ?: continue
                val name = el.getAttribute("android:name")
                if (name in declaredPermissionNames || name.startsWith(ORIGINAL_PACKAGE))
                    usesPermToRemove.add(el)
            }
            usesPermToRemove.forEach { it.parentNode?.removeChild(it) }

            // 4. Rename provider authorities
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

            // 5. Inject app name label
            (doc.getElementsByTagName("application").item(0) as? Element)
                ?.setAttribute("android:label", "@string/messenger_morphe_app_name")
        }

        // 6. Inject string resource
        document("res/values/strings.xml").use { doc ->
            val resources = doc.documentElement
            val strings = doc.getElementsByTagName("string")
            val existing = (0 until strings.length)
                .mapNotNull { strings.item(it) as? Element }
                .firstOrNull { it.getAttribute("name") == "messenger_morphe_app_name" }
            val node = existing ?: doc.createElement("string").also {
                it.setAttribute("name", "messenger_morphe_app_name")
                resources.appendChild(it)
            }
            node.textContent = appName ?: "Messenger Morphe"
        }
    }
}
