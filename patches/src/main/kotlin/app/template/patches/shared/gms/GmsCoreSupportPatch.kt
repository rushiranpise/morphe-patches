package app.template.patches.shared.gms

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.cert.autoSha1
import app.template.patches.shared.cert.extractApkCertificatePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element
import org.w3c.dom.Node

// Default vendor — MicroG RE / standard GmsCore. Pass a different value for other forks
// (e.g. "app.morphe" for Morphe GmsCore).
const val DEFAULT_GMS_CORE_VENDOR = "app.revanced.android.gms"

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/GmsCoreSupportPatch;"

// Patches getGmsCoreVendorGroupId() in the extension to return the user's chosen vendor group.
private val GmsCoreSupportFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getGmsCoreVendorGroupId"
    },
)

// Patches getOriginalPackageName() in the extension to return the original app package name.
private val OriginalPackageNameFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getOriginalPackageName"
    },
)

// Matches the main Activity's onCreate(Bundle) to inject checkGmsCore().
// Takes Bundle param — distinguishes Activity.onCreate from Application.onCreate.
// activityClassSuffix is e.g. "/MainActivity;" or "/EntryActivity;" — we match by suffix
// so it works even if the package is obfuscated. Falls back to any Activity.onCreate if not set.
private var activityClassSuffix: String? = null

private val ActivityOnCreateFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    custom = { method: Method, classDef: ClassDef ->
        method.name == "onCreate" &&
            (activityClassSuffix?.let { classDef.type.endsWith(it) }
                ?: classDef.superclass?.endsWith("Activity;") == true)
    },
)

// Matches isGooglePlayServicesAvailable(Context, int) → return 0 (CONNECTION_SUCCESS).
private val IsGooglePlayServicesAvailableFingerprint = Fingerprint(
    name = "GmsCore / MicroG support",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "I",
    parameters = listOf("Landroid/content/Context;", "I"),
)

private val GMS_PERMISSIONS = setOf(
    "com.google.android.c2dm.permission.RECEIVE",
    "com.google.android.c2dm.permission.SEND",
    "com.google.android.gms.auth.api.phone.permission.SEND",
    "com.google.android.gms.permission.AD_ID",
    "com.google.android.gms.permission.AD_ID_NOTIFICATION",
    "com.google.android.gms.permission.CAR_FUEL",
    "com.google.android.gms.permission.CAR_INFORMATION",
    "com.google.android.gms.permission.CAR_MILEAGE",
    "com.google.android.gms.permission.CAR_SPEED",
    "com.google.android.gms.permission.CAR_VENDOR_EXTENSION",
    "com.google.android.googleapps.permission.GOOGLE_AUTH",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.cp",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.local",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.mail",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.writely",
    "com.google.android.gtalkservice.permission.GTALK_SERVICE",
    "com.google.android.providers.gsf.permission.READ_GSERVICES",
)

private val GMS_ACTIONS = setOf(
    "com.google.android.c2dm.intent.RECEIVE",
    "com.google.android.c2dm.intent.REGISTER",
    "com.google.android.c2dm.intent.REGISTRATION",
    "com.google.android.c2dm.intent.UNREGISTER",
    "com.google.android.contextmanager.service.ContextManagerService.START",
    "com.google.android.gcm.intent.SEND",
    "com.google.android.gms.accounts.ACCOUNT_SERVICE",
    "com.google.android.gms.accountsettings.ACCOUNT_PREFERENCES_SETTINGS",
    "com.google.android.gms.accountsettings.action.BROWSE_SETTINGS",
    "com.google.android.gms.accountsettings.action.VIEW_SETTINGS",
    "com.google.android.gms.accountsettings.MY_ACCOUNT",
    "com.google.android.gms.accountsettings.PRIVACY_SETTINGS",
    "com.google.android.gms.accountsettings.SECURITY_SETTINGS",
    "com.google.android.gms.ads.identifier.service.EVENT_ATTESTATION",
    "com.google.android.gms.analytics.service.START",
    "com.google.android.gms.auth.account.authapi.START",
    "com.google.android.gms.auth.account.authenticator.auto.service.START",
    "com.google.android.gms.auth.account.authenticator.tv.service.START",
    "com.google.android.gms.auth.account.data.service.START",
    "com.google.android.gms.auth.api.credentials.service.START",
    "com.google.android.gms.auth.api.identity.service.authorization.START",
    "com.google.android.gms.auth.api.identity.service.credentialsaving.START",
    "com.google.android.gms.auth.api.identity.service.signin.START",
    "com.google.android.gms.auth.api.phone.service.InternalService.START",
    "com.google.android.gms.auth.api.signin.service.START",
    "com.google.android.gms.auth.be.appcert.AppCertService",
    "com.google.android.gms.auth.blockstore.service.START",
    "com.google.android.gms.auth.config.service.START",
    "com.google.android.gms.auth.cryptauth.cryptauthservice.START",
    "com.google.android.gms.auth.GOOGLE_SIGN_IN",
    "com.google.android.gms.auth.login.LOGIN",
    "com.google.android.gms.auth.service.START",
    "com.google.android.gms.checkin.BIND_TO_SERVICE",
    "com.google.android.gms.clearcut.service.START",
    "com.google.android.gms.common.account.CHOOSE_ACCOUNT",
    "com.google.android.gms.common.download.START",
    "com.google.android.gms.common.service.START",
    "com.google.android.gms.config.START",
    "com.google.android.gms.drive.ApiService.START",
    "com.google.android.gms.droidguard.service.START",
    "com.google.android.gms.fido.fido2.privileged.START",
    "com.google.android.gms.fido.fido2.regular.START",
    "com.google.android.gms.fonts.service.START",
    "com.google.android.gms.games.service.START",
    "com.google.android.gms.gass.START",
    "com.google.android.gms.googlehelp.HELP",
    "com.google.android.gms.googlehelp.service.GoogleHelpService.START",
    "com.google.android.gms.identity.service.BIND",
    "com.google.android.gms.instantapps.START",
    "com.google.android.gms.location.reporting.service.START",
    "com.google.android.gms.locationsharing.api.START",
    "com.google.android.gms.measurement.START",
    "com.google.android.gms.nearby.connection.service.START",
    "com.google.android.gms.nearby.messages.service.NearbyMessagesService.START",
    "com.google.android.gms.notifications.service.START",
    "com.google.android.gms.people.service.START",
    "com.google.android.gms.phenotype.service.START",
    "com.google.android.gms.safetynet.service.START",
    "com.google.android.gms.signin.service.START",
    "com.google.android.gms.tapandpay.service.BIND",
    "com.google.android.gms.update.START_API_SERVICE",
    "com.google.android.gms.update.START_SERVICE",
    "com.google.android.gms.wallet.service.BIND",
    "com.google.android.gms.wearable.BIND",
    "com.google.android.gms.wearable.DATA_CHANGED",
    "com.google.android.gsf.action.GET_GLS",
    "com.google.firebase.auth.api.gms.service.START",
    "com.google.firebase.dynamiclinks.service.START",
    "com.google.iid.TOKEN_REQUEST",
)

private val GMS_AUTHORITIES = setOf(
    "com.google.android.gms.auth.accounts",
    "com.google.android.gms.chimera",
    "com.google.android.gms.fonts",
    "com.google.android.gms.phenotype",
    "com.google.android.gsf.gservices",
    "com.google.settings",
)

/**
 * Resource-only half of GmsCore support. Injects spoofing metadata into AndroidManifest so
 * MicroG RE (or any GmsCore fork) can present itself as Google Play Services to this app.
 *
 * Works for any app — not just Google apps. If the app just needs signature spoofing and
 * doesn't have GMS string constants in its DEX, use this alone instead of [gmsCoreSupportPatch].
 *
 * @param originalPackageName Original (Google) package name this app should impersonate.
 * @param spoofedPackageSignature SHA-256 of Google's release signing cert for [originalPackageName].
 * @param patchedPackageName New package name in the patched build (defaults to [originalPackageName]).
 * @param gmsCoreVendor Vendor group ID of the GmsCore build installed on the device.
 *   - `"app.revanced"` (default) — MicroG RE / standard GmsCore
 *   - `"app.morphe"` — Morphe GmsCore fork
 *   - Any other fork's group ID
 */
fun gmsCoreSupportResourcePatch(
    originalPackageName: String,
    spoofedPackageSignature: String,
    patchedPackageName: String = originalPackageName,
    gmsCoreVendor: String = DEFAULT_GMS_CORE_VENDOR,
) = resourcePatch {
    execute {
        document("AndroidManifest.xml").use { doc ->
            val manifestNode = doc.getElementsByTagName("manifest").item(0)
            val applicationNode = doc.getElementsByTagName("application").item(0)

            // FAKE_PACKAGE_SIGNATURE permission — MicroG RE reads this to enable spoofing.
            val existingPerms = doc.getElementsByTagName("uses-permission")
            val permAlreadyAdded = (0 until existingPerms.length).any { i ->
                existingPerms.item(i).attributes
                    ?.getNamedItem("android:name")
                    ?.nodeValue == "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE"
            }
            if (!permAlreadyAdded) {
                val permNode = doc.createElement("uses-permission")
                permNode.setAttribute("android:name", "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE")
                manifestNode.appendChild(permNode)
            }

            // <queries> entry so the app can resolve the GmsCore package.
            val manifestFile = get("AndroidManifest.xml")
            var manifestContent = manifestFile.readText()
            if ("</queries>" in manifestContent && "$gmsCoreVendor.android.gms" !in manifestContent) {
                manifestContent = manifestContent.replace(
                    "</queries>",
                    "<package android:name=\"$gmsCoreVendor.android.gms\"/></queries>",
                )
            }

            // Manifest string rewrites: c2dm, authorities, per-package permissions.
            mapOf(
                "com.google.android.c2dm" to "$gmsCoreVendor.android.c2dm",
                "android:authorities=\"$originalPackageName" to "android:authorities=\"$patchedPackageName",
                "$originalPackageName.permission.C2D_MESSAGE" to "$patchedPackageName.permission.C2D_MESSAGE",
                "$originalPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" to
                    "$patchedPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
            ).forEach { (from, to) ->
                manifestContent = manifestContent.replace(from, to)
            }
            manifestFile.writeText(manifestContent)

            // meta-data entries read by GmsCore to spoof the original identity.
            fun Node.addMetaIfAbsent(name: String, value: String) {
                val exists = (0 until childNodes.length).any { i ->
                    childNodes.item(i).nodeName == "meta-data" &&
                        childNodes.item(i).attributes?.getNamedItem("android:name")?.nodeValue == name
                }
                if (!exists) {
                    val meta = (ownerDocument ?: doc).createElement("meta-data") as Element
                    meta.setAttribute("android:name", name)
                    meta.setAttribute("android:value", value)
                    appendChild(meta)
                }
            }

            applicationNode.addMetaIfAbsent(
                "$gmsCoreVendor.android.gms.SPOOFED_PACKAGE_NAME",
                originalPackageName,
            )
            applicationNode.addMetaIfAbsent(
                "$gmsCoreVendor.android.gms.SPOOFED_PACKAGE_SIGNATURE",
                spoofedPackageSignature,
            )
            // Points the app at the correct GmsCore package (vendor-aware).
            applicationNode.addMetaIfAbsent(
                "$gmsCoreVendor.MICROG_PACKAGE_NAME",
                "$gmsCoreVendor.android.gms",
            )
        }
    }
}

/**
 * Universal GmsCore / MicroG support patch — shows in Morphe for **any** app, no wrapper needed.
 *
 * Works like `changePackageNamePatch`: no `compatibleWith()` restriction, no per-app declaration.
 * The user picks it from the patch list and fills in two options.
 *
 * The factory [gmsCoreSupportResourcePatch] still exists for app-specific patches that
 * want to hardcode values in code instead of relying on user options.
 *
 * Options exposed to the user in Morphe:
 * - **spoofedPackageSignature** — SHA-256 of Google's release cert for this app.
 * - **gmsCoreVendor** — which GmsCore fork is installed on the device.
 *
 * `originalPackageName` is read directly from AndroidManifest at patch time — no code needed.
 */
val gmsCorePatch = bytecodePatch(
    name = "GmsCore support (MicroG)",
    description = """
        Routes Google Play Services calls through MicroG instead of real GPS.

        Works for: Google apps (YouTube, Maps, News, Photos) and third-party apps using classic Google Sign-In (Android 13 and below).

        Does not work for: Android 14+ Credential Manager sign-in (most modern third-party apps), Play Integrity / SafetyNet checks, or apps with custom auth.

        Requires MicroG RE installed. Apply with Original app certificate patch.
    """.trimIndent(),
    default = false,
) {
    extendWith("extensions/extension.mpe")

    // Optional — if left blank, SPOOFED_PACKAGE_SIGNATURE meta-data is not injected.
val gmsCoreVendorOption by stringOption(
        key = "gmsCoreVendor",
        default = "app.revanced.android.gms",
        values = mapOf(
            "app.revanced.android.gms (default)" to "app.revanced.android.gms",
            "app.morphe.android.gms" to "app.morphe.android.gms",
            "app.morphe.manager" to "app.morphe.manager",
            "app.revanced.manager" to "app.revanced.manager",
            "app.rvx.manager" to "app.rvx.manager",
            "com.google.android.gms" to "com.google.android.gms",
            "org.microg.gms" to "org.microg.gms",
        ),
        title = "MicroG package name",
        description = "Which MicroG / GmsCore app is installed on your device. " +
            "Check your device's app list for \"MicroG\" or \"GmsCore\" if unsure — " +
            "most users should leave this as the default.",
        required = true,
    )

    // Optional — specify the main activity class suffix for checkGmsCore injection.
    // e.g. "/MainActivity;" or "/EntryActivity;"
    // Leave blank to auto-detect any Activity.onCreate(Bundle).
    val mainActivityName by stringOption(
        key = "mainActivityName",
        default = null,
        title = "Main activity class (optional)",
        description = "The app's main Activity class name ending with a semicolon, e.g. /MainActivity; " +
            "Used to inject the MicroG check at startup. Leave blank for auto-detection.",
        required = false,
    ) { it == null || it.endsWith(";") }

    // Optional — blank keeps the original package name unchanged.
    val customPackageName by stringOption(
        key = "packageName",
        default = null,
        title = "Custom package name (optional)",
        description = "Rename the app to this package name. Leave blank to keep the original. " +
            "Must be a valid package name (e.g. com.example.myapp).",
        required = false,
    ) { it == null || it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+$")) }

    // Read original package name from the manifest before execute runs.
    lateinit var originalPackageName: String

    dependsOn(extractApkCertificatePatch,
        resourcePatch {
            execute {
                document("AndroidManifest.xml").use { doc ->
                    originalPackageName = (doc.getElementsByTagName("manifest").item(0) as Element)
                        .getAttribute("package")
                }

                val vendor = gmsCoreVendorOption ?: DEFAULT_GMS_CORE_VENDOR
                val sig = autoSha1
                // Resolve package name: user custom > <original>.revanced
                val resolvedPackageName = customPackageName?.takeIf { it.isNotBlank() }
                    ?: originalPackageName

                document("AndroidManifest.xml").use { doc ->
                    val manifestNode = doc.getElementsByTagName("manifest").item(0)
                    val applicationNode = doc.getElementsByTagName("application").item(0)

                    // FAKE_PACKAGE_SIGNATURE permission for MicroG RE.
                    val existingPerms = doc.getElementsByTagName("uses-permission")
                    val permAdded = (0 until existingPerms.length).any { i ->
                        existingPerms.item(i).attributes
                            ?.getNamedItem("android:name")
                            ?.nodeValue == "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE"
                    }
                    if (!permAdded) {
                        val perm = doc.createElement("uses-permission")
                        perm.setAttribute("android:name", "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE")
                        manifestNode.appendChild(perm)
                    }

                    fun Node.addMetaIfAbsent(name: String, value: String) {
                        val exists = (0 until childNodes.length).any { i ->
                            childNodes.item(i).nodeName == "meta-data" &&
                                childNodes.item(i).attributes?.getNamedItem("android:name")?.nodeValue == name
                        }
                        if (!exists) {
                            val meta = (ownerDocument ?: doc).createElement("meta-data") as Element
                            meta.setAttribute("android:name", name)
                            meta.setAttribute("android:value", value)
                            appendChild(meta)
                        }
                    }

                    applicationNode.addMetaIfAbsent("$vendor.SPOOFED_PACKAGE_NAME", originalPackageName)
                    // Only inject if user provided a signature — not required for all apps.
                    if (sig != null) applicationNode.addMetaIfAbsent("$vendor.SPOOFED_PACKAGE_SIGNATURE", sig)
                    applicationNode.addMetaIfAbsent("$vendor.MICROG_PACKAGE_NAME", vendor)
                }

                // Manifest text rewrites.
                val manifestFile = get("AndroidManifest.xml")
                var text = manifestFile.readText()
                if ("</queries>" in text && vendor !in text)
                    text = text.replace("</queries>", "<package android:name=\"$vendor\"/></queries>")
                // c2dm rewrite only makes sense for app.revanced/app.morphe style vendors
                if (!vendor.contains("microg") && !vendor.startsWith("com.google"))
                    text = text.replace("com.google.android.c2dm", "${vendor.substringBeforeLast(".")}.c2dm")
                // Rewrite package-specific manifest strings to the resolved package name.
                text = text.replace("android:authorities=\"$originalPackageName", "android:authorities=\"$resolvedPackageName")
                text = text.replace("$originalPackageName.permission.C2D_MESSAGE", "$resolvedPackageName.permission.C2D_MESSAGE")
                text = text.replace(
                    "$originalPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    "$resolvedPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                )
                manifestFile.writeText(text)
            }
        }
    )

    execute {
        val vendor = gmsCoreVendorOption ?: DEFAULT_GMS_CORE_VENDOR
        // Derive the vendor group prefix for com.google.* rewrites
        // e.g. "app.revanced.android.gms" → "app.revanced"
        val vendorGroup = when {
            vendor.endsWith(".android.gms") -> vendor.removeSuffix(".android.gms")
            vendor.endsWith(".manager") -> vendor.removeSuffix(".manager")
            else -> vendor
        }

        val rewriteStrings = buildMap<String, String> {
            put("com.google", vendorGroup)
            put("com.google.android.gms", vendor)
            put("subscribedfeeds", "$vendorGroup.subscribedfeeds")
            (GMS_PERMISSIONS + GMS_ACTIONS + GMS_AUTHORITIES).forEach { s ->
                put(s, s.replace("com.google", vendorGroup))
            }
        }

        fun transform(str: String): String? {
            rewriteStrings[str]?.let { return it }
            if (str.startsWith("content://")) {
                GMS_AUTHORITIES.forEach { auth ->
                    if (str.startsWith("content://$auth"))
                        return str.replace("content://$auth", "content://${auth.replace("com.google", vendorGroup)}")
                }
                if (str.startsWith("content://subscribedfeeds"))
                    return str.replace("content://subscribedfeeds", "content://$vendorGroup.subscribedfeeds")
            }
            return null
        }

        getAllClassesWithStrings().forEach { classDef ->
            val hasMatch = classDef.methods.any { method ->
                method.implementation?.instructions?.any { instr ->
                    val s = ((instr as? Instruction21c)?.reference as? StringReference)?.string
                    s != null && transform(s) != null
                } == true
            }
            if (!hasMatch) return@forEach

            val mutableClass = mutableClassDefBy(classDef)
            for (mutableMethod in mutableClass.methods) {
                val impl = mutableMethod.implementation ?: continue
                val replacements = mutableListOf<Pair<Int, BuilderInstruction21c>>()
                impl.instructions.forEachIndexed { index, instr ->
                    val s = ((instr as? Instruction21c)?.reference as? StringReference)?.string
                        ?: return@forEachIndexed
                    val transformed = transform(s) ?: return@forEachIndexed
                    replacements += index to BuilderInstruction21c(
                        Opcode.CONST_STRING,
                        instr.registerA,
                        ImmutableStringReference(transformed),
                    )
                }
                replacements.forEach { (idx, replacement) ->
                    mutableMethod.replaceInstruction(idx, replacement)
                }
            }
        }

        // Set activity class suffix for fingerprint (must be before fingerprint matching runs,
        // but fingerprints are evaluated lazily on first access so we set this in execute).
        activityClassSuffix = mainActivityName

        ServiceCheckFingerprint.methodOrNull?.addInstruction(0, "return-void")

        // Return CONNECTION_SUCCESS (0) for GPS availability checks.
        GooglePlayUtilityFingerprint.methodOrNull?.let { method ->
            method.addInstruction(0, "return v0")
            method.addInstruction(0, "const/4 v0, 0x0")
        }
        IsGooglePlayServicesAvailableFingerprint.methodOrNull?.let { method ->
            method.addInstruction(0, "return v0")
            method.addInstruction(0, "const/4 v0, 0x0")
        }

        // Patch extension: getGmsCoreVendorGroupId() → return "<vendor group>"
        GmsCoreSupportFingerprint.methodOrNull?.apply {
            addInstruction(0, "return-object v0")
            addInstruction(0, "const-string v0, \"$vendorGroup\"")
            println("GmsCore extension: getGmsCoreVendorGroupId() patched → \"$vendorGroup\"")
        } ?: println("GmsCore extension: getGmsCoreVendorGroupId() NOT FOUND — extension may not be merged.")

        // Patch extension: getOriginalPackageName() → return "<original package name>"
        OriginalPackageNameFingerprint.methodOrNull?.apply {
            addInstruction(0, "return-object v0")
            addInstruction(0, "const-string v0, \"$originalPackageName\"")
            println("GmsCore extension: getOriginalPackageName() patched → \"$originalPackageName\"")
        } ?: println("GmsCore extension: getOriginalPackageName() NOT FOUND — extension may not be merged.")

        // Inject checkGmsCore(context) into Application.onCreate()
        val onCreate = ActivityOnCreateFingerprint.methodOrNull
        if (onCreate != null) {
            onCreate.addInstruction(
                0,
                "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS->checkGmsCore(Landroid/content/Context;)V",
            )
            println("GmsCore extension: checkGmsCore() injected into Activity.onCreate().")
        } else {
            println("GmsCore extension: Activity.onCreate() NOT FOUND — checkGmsCore() not injected.")
        }
    }
}
