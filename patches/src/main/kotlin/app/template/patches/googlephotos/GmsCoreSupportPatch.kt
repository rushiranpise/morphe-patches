package app.template.patches.googlephotos

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import org.w3c.dom.Element

private const val ORIGINAL_PACKAGE_NAME = "com.google.android.apps.photos"
private const val DEFAULT_PATCHED_PACKAGE_NAME = "app.morphe.android.apps.photos"
private const val GMS_CORE_VENDOR_GROUP = "app.revanced"
private const val GMS_CORE_PACKAGE = "app.revanced.android.gms"
private const val LEGACY_MARS_AUTHORITY = "app.revanced.android.apps.photos.api.mars"
private const val EXTENSION_CLASS = "Lapp/template/extension/extension/GmsCoreSupportPatch;"
private var resolvedPackageName = DEFAULT_PATCHED_PACKAGE_NAME
private var resolvedMarsAuthority = marsAuthorityFor(DEFAULT_PATCHED_PACKAGE_NAME)

private val GMS_STRING_REPLACEMENTS = mapOf(
    "com.google" to GMS_CORE_VENDOR_GROUP,
    "com.google.android.gms" to GMS_CORE_PACKAGE,
    "com.google.android.c2dm.permission.RECEIVE" to "app.revanced.android.c2dm.permission.RECEIVE",
    "com.google.android.c2dm.permission.SEND" to "app.revanced.android.c2dm.permission.SEND",
    "com.google.android.gms.auth.api.phone.permission.SEND" to "app.revanced.android.gms.auth.api.phone.permission.SEND",
    "com.google.android.gms.permission.AD_ID" to "app.revanced.android.gms.permission.AD_ID",
    "com.google.android.gms.permission.AD_ID_NOTIFICATION" to "app.revanced.android.gms.permission.AD_ID_NOTIFICATION",
    "com.google.android.googleapps.permission.GOOGLE_AUTH" to "app.revanced.android.googleapps.permission.GOOGLE_AUTH",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.cp" to "app.revanced.android.googleapps.permission.GOOGLE_AUTH.cp",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.local" to "app.revanced.android.googleapps.permission.GOOGLE_AUTH.local",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.mail" to "app.revanced.android.googleapps.permission.GOOGLE_AUTH.mail",
    "com.google.android.googleapps.permission.GOOGLE_AUTH.writely" to "app.revanced.android.googleapps.permission.GOOGLE_AUTH.writely",
    "com.google.android.gtalkservice.permission.GTALK_SERVICE" to "app.revanced.android.gtalkservice.permission.GTALK_SERVICE",
    "com.google.android.providers.gsf.permission.READ_GSERVICES" to "app.revanced.android.providers.gsf.permission.READ_GSERVICES",
    "com.google.android.gms.auth.accounts" to "app.revanced.android.gms.auth.accounts",
    "com.google.android.gms.chimera" to "app.revanced.android.gms.chimera",
    "com.google.android.gms.fonts" to "app.revanced.android.gms.fonts",
    "com.google.android.gms.phenotype" to "app.revanced.android.gms.phenotype",
    "com.google.android.gsf.gservices" to "app.revanced.android.gsf.gservices",
    "com.google.settings" to "app.revanced.settings",
    "com.google.android.c2dm.intent.RECEIVE" to "app.revanced.android.c2dm.intent.RECEIVE",
    "com.google.android.c2dm.intent.REGISTER" to "app.revanced.android.c2dm.intent.REGISTER",
    "com.google.android.c2dm.intent.REGISTRATION" to "app.revanced.android.c2dm.intent.REGISTRATION",
    "com.google.android.c2dm.intent.UNREGISTER" to "app.revanced.android.c2dm.intent.UNREGISTER",
    "com.google.android.contextmanager.service.ContextManagerService.START" to
        "app.revanced.android.contextmanager.service.ContextManagerService.START",
    "com.google.android.gcm.intent.SEND" to "app.revanced.android.gcm.intent.SEND",
    "com.google.android.gms.accounts.ACCOUNT_SERVICE" to "app.revanced.android.gms.accounts.ACCOUNT_SERVICE",
    "com.google.android.gms.accountsettings.ACCOUNT_PREFERENCES_SETTINGS" to
        "app.revanced.android.gms.accountsettings.ACCOUNT_PREFERENCES_SETTINGS",
    "com.google.android.gms.accountsettings.action.BROWSE_SETTINGS" to
        "app.revanced.android.gms.accountsettings.action.BROWSE_SETTINGS",
    "com.google.android.gms.accountsettings.action.VIEW_SETTINGS" to
        "app.revanced.android.gms.accountsettings.action.VIEW_SETTINGS",
    "com.google.android.gms.accountsettings.MY_ACCOUNT" to
        "app.revanced.android.gms.accountsettings.MY_ACCOUNT",
    "com.google.android.gms.accountsettings.PRIVACY_SETTINGS" to
        "app.revanced.android.gms.accountsettings.PRIVACY_SETTINGS",
    "com.google.android.gms.accountsettings.SECURITY_SETTINGS" to
        "app.revanced.android.gms.accountsettings.SECURITY_SETTINGS",
    "com.google.android.gms.ads.identifier.service.EVENT_ATTESTATION" to
        "app.revanced.android.gms.ads.identifier.service.EVENT_ATTESTATION",
    "com.google.android.gms.analytics.service.START" to "app.revanced.android.gms.analytics.service.START",
    "com.google.android.gms.auth.account.authapi.START" to
        "app.revanced.android.gms.auth.account.authapi.START",
    "com.google.android.gms.auth.account.authenticator.auto.service.START" to
        "app.revanced.android.gms.auth.account.authenticator.auto.service.START",
    "com.google.android.gms.auth.account.authenticator.tv.service.START" to
        "app.revanced.android.gms.auth.account.authenticator.tv.service.START",
    "com.google.android.gms.auth.account.data.service.START" to
        "app.revanced.android.gms.auth.account.data.service.START",
    "com.google.android.gms.auth.api.credentials.service.START" to
        "app.revanced.android.gms.auth.api.credentials.service.START",
    "com.google.android.gms.auth.api.identity.service.authorization.START" to
        "app.revanced.android.gms.auth.api.identity.service.authorization.START",
    "com.google.android.gms.auth.api.identity.service.credentialsaving.START" to
        "app.revanced.android.gms.auth.api.identity.service.credentialsaving.START",
    "com.google.android.gms.auth.api.identity.service.signin.START" to
        "app.revanced.android.gms.auth.api.identity.service.signin.START",
    "com.google.android.gms.auth.api.phone.service.InternalService.START" to
        "app.revanced.android.gms.auth.api.phone.service.InternalService.START",
    "com.google.android.gms.auth.api.signin.service.START" to
        "app.revanced.android.gms.auth.api.signin.service.START",
    "com.google.android.gms.auth.be.appcert.AppCertService" to
        "app.revanced.android.gms.auth.be.appcert.AppCertService",
    "com.google.android.gms.auth.blockstore.service.START" to
        "app.revanced.android.gms.auth.blockstore.service.START",
    "com.google.android.gms.auth.config.service.START" to
        "app.revanced.android.gms.auth.config.service.START",
    "com.google.android.gms.auth.cryptauth.cryptauthservice.START" to
        "app.revanced.android.gms.auth.cryptauth.cryptauthservice.START",
    "com.google.android.gms.auth.GOOGLE_SIGN_IN" to "app.revanced.android.gms.auth.GOOGLE_SIGN_IN",
    "com.google.android.gms.auth.login.LOGIN" to "app.revanced.android.gms.auth.login.LOGIN",
    "com.google.android.gms.auth.service.START" to "app.revanced.android.gms.auth.service.START",
    "com.google.android.gms.checkin.BIND_TO_SERVICE" to "app.revanced.android.gms.checkin.BIND_TO_SERVICE",
    "com.google.android.gms.clearcut.service.START" to "app.revanced.android.gms.clearcut.service.START",
    "com.google.android.gms.common.account.CHOOSE_ACCOUNT" to
        "app.revanced.android.gms.common.account.CHOOSE_ACCOUNT",
    "com.google.android.gms.common.download.START" to "app.revanced.android.gms.common.download.START",
    "com.google.android.gms.common.service.START" to "app.revanced.android.gms.common.service.START",
    "com.google.android.gms.config.START" to "app.revanced.android.gms.config.START",
    "com.google.android.gms.drive.ApiService.START" to "app.revanced.android.gms.drive.ApiService.START",
    "com.google.android.gms.droidguard.service.START" to "app.revanced.android.gms.droidguard.service.START",
    "com.google.android.gms.fido.fido2.privileged.START" to
        "app.revanced.android.gms.fido.fido2.privileged.START",
    "com.google.android.gms.fido.fido2.regular.START" to
        "app.revanced.android.gms.fido.fido2.regular.START",
    "com.google.android.gms.fonts.service.START" to "app.revanced.android.gms.fonts.service.START",
    "com.google.android.gms.games.service.START" to "app.revanced.android.gms.games.service.START",
    "com.google.android.gms.gass.START" to "app.revanced.android.gms.gass.START",
    "com.google.android.gms.googlehelp.HELP" to "app.revanced.android.gms.googlehelp.HELP",
    "com.google.android.gms.googlehelp.service.GoogleHelpService.START" to
        "app.revanced.android.gms.googlehelp.service.GoogleHelpService.START",
    "com.google.android.gms.identity.service.BIND" to "app.revanced.android.gms.identity.service.BIND",
    "com.google.android.gms.instantapps.START" to "app.revanced.android.gms.instantapps.START",
    "com.google.android.gms.location.reporting.service.START" to
        "app.revanced.android.gms.location.reporting.service.START",
    "com.google.android.gms.locationsharing.api.START" to
        "app.revanced.android.gms.locationsharing.api.START",
    "com.google.android.gms.measurement.START" to "app.revanced.android.gms.measurement.START",
    "com.google.android.gms.nearby.connection.service.START" to
        "app.revanced.android.gms.nearby.connection.service.START",
    "com.google.android.gms.nearby.messages.service.NearbyMessagesService.START" to
        "app.revanced.android.gms.nearby.messages.service.NearbyMessagesService.START",
    "com.google.android.gms.notifications.service.START" to
        "app.revanced.android.gms.notifications.service.START",
    "com.google.android.gms.people.service.START" to "app.revanced.android.gms.people.service.START",
    "com.google.android.gms.phenotype.service.START" to "app.revanced.android.gms.phenotype.service.START",
    "com.google.android.gms.safetynet.service.START" to "app.revanced.android.gms.safetynet.service.START",
    "com.google.android.gms.signin.service.START" to "app.revanced.android.gms.signin.service.START",
    "com.google.android.gms.tapandpay.service.BIND" to "app.revanced.android.gms.tapandpay.service.BIND",
    "com.google.android.gms.update.START_API_SERVICE" to "app.revanced.android.gms.update.START_API_SERVICE",
    "com.google.android.gms.update.START_SERVICE" to "app.revanced.android.gms.update.START_SERVICE",
    "com.google.android.gms.wallet.service.BIND" to "app.revanced.android.gms.wallet.service.BIND",
    "com.google.android.gms.wearable.BIND" to "app.revanced.android.gms.wearable.BIND",
    "com.google.android.gms.wearable.DATA_CHANGED" to "app.revanced.android.gms.wearable.DATA_CHANGED",
    "com.google.android.gsf.action.GET_GLS" to "app.revanced.android.gsf.action.GET_GLS",
    "com.google.firebase.auth.api.gms.service.START" to "app.revanced.firebase.auth.api.gms.service.START",
    "com.google.firebase.dynamiclinks.service.START" to "app.revanced.firebase.dynamiclinks.service.START",
    "com.google.iid.TOKEN_REQUEST" to "app.revanced.iid.TOKEN_REQUEST",
    "subscribedfeeds" to "app.revanced.subscribedfeeds",
)

private val APP_PERMISSIONS = setOf(
    "$ORIGINAL_PACKAGE_NAME.permission.C2D_MESSAGE",
    "$ORIGINAL_PACKAGE_NAME.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
)

private val APP_AUTHORITIES = setOf(
    "$ORIGINAL_PACKAGE_NAME.cloudpicker",
    "$ORIGINAL_PACKAGE_NAME.contentprovider",
    "$ORIGINAL_PACKAGE_NAME.freeable_space",
    "$ORIGINAL_PACKAGE_NAME.editor.contentprovider",
    "$ORIGINAL_PACKAGE_NAME.fileprovider",
    "$ORIGINAL_PACKAGE_NAME.mars.contentprovider.local_locked_media",
    "$ORIGINAL_PACKAGE_NAME.memories",
    "$ORIGINAL_PACKAGE_NAME.partnercontentprovider",
    "$ORIGINAL_PACKAGE_NAME.photoeditor.cachedfileprovider",
    "$ORIGINAL_PACKAGE_NAME.photoeditor.localeditcontentprovider",
    "$ORIGINAL_PACKAGE_NAME.photoeditor.renderedimagecontentprovider",
    "$ORIGINAL_PACKAGE_NAME.photoprovider",
    "$ORIGINAL_PACKAGE_NAME.SharouselContentProvider",
    "$ORIGINAL_PACKAGE_NAME.mlkitinitprovider",
    "$ORIGINAL_PACKAGE_NAME.chime_sdk_file_provider",
)

private val GooglePhotosHomeActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/apps/photos/home/HomeActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)

private val GmsCoreVendorGroupFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getGmsCoreVendorGroupId"
    },
)

private val OriginalPackageNameFingerprint = Fingerprint(
    custom = { method: Method, classDef: ClassDef ->
        classDef.type == EXTENSION_CLASS && method.name == "getOriginalPackageName"
    },
)

private val GooglePlayUtilityFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "I",
    parameters = listOf("L", "I"),
    strings = listOf(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    ),
)

private val ServiceCheckFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("L", "I"),
    strings = listOf("Google Play Services not available"),
)

// Based on RookieEnough/De-Vanced Google Photos GmsCore support patch.
@Suppress("unused")
val googlePhotosGmsCoreSupportPatch = bytecodePatch(
    name = "GmsCore support",
    description = "Adds MicroG/GmsCore support metadata for Google Photos.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    dependsOn(
        resourcePatch {
            finalize {
                document("AndroidManifest.xml").use { document ->
                    val manifestPackage = (document.getElementsByTagName("manifest").item(0) as Element)
                        .getAttribute("package")
                    resolvedPackageName = if (manifestPackage == ORIGINAL_PACKAGE_NAME) {
                        DEFAULT_PATCHED_PACKAGE_NAME
                    } else {
                        manifestPackage
                    }
                    resolvedMarsAuthority = marsAuthorityFor(resolvedPackageName)
                }

                val manifest = get("AndroidManifest.xml")
                val transformations = mapOf(
                    "package=\"$ORIGINAL_PACKAGE_NAME" to "package=\"$resolvedPackageName",
                    "android:authorities=\"$ORIGINAL_PACKAGE_NAME" to "android:authorities=\"$resolvedPackageName",
                    "$ORIGINAL_PACKAGE_NAME.permission.C2D_MESSAGE" to "$resolvedPackageName.permission.C2D_MESSAGE",
                    "$ORIGINAL_PACKAGE_NAME.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" to
                        "$resolvedPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                    "com.google.android.c2dm" to "$GMS_CORE_VENDOR_GROUP.android.c2dm",
                    "com.google.android.libraries.photos.api.mars" to
                        resolvedMarsAuthority,
                    LEGACY_MARS_AUTHORITY to resolvedMarsAuthority,
                    "</queries>" to "<package android:name=\"$GMS_CORE_PACKAGE\"/></queries>",
                )
                manifest.writeText(
                    transformations.entries.fold(manifest.readText()) { acc, (from, to) ->
                        acc.replace(from, to)
                    },
                )

                document("AndroidManifest.xml").use { document ->
                    val manifestNode = document.getElementsByTagName("manifest").item(0) as Element
                    val applicationNode = document.getElementsByTagName("application").item(0) as Element

                    val queryPackages = document.getElementsByTagName("package")
                    for (index in 0 until queryPackages.length) {
                        val queryPackage = queryPackages.item(index) as? Element ?: continue
                        val name = queryPackage.getAttribute("android:name")
                        if (name == "com.google.android.gms" || name.startsWith("com.google.android.gms.")) {
                            queryPackage.setAttribute("android:name", name.replace("com.google", GMS_CORE_VENDOR_GROUP))
                        }
                    }

                    val hasFakeSignaturePermission = (0 until document.getElementsByTagName("uses-permission").length)
                        .mapNotNull { document.getElementsByTagName("uses-permission").item(it) as? Element }
                        .any { it.getAttribute("android:name") == "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE" }
                    if (!hasFakeSignaturePermission) {
                        manifestNode.appendChild(
                            document.createElement("uses-permission").apply {
                                setAttribute("android:name", "org.microg.gms.permission.FAKE_PACKAGE_SIGNATURE")
                            },
                        )
                    }

                    applicationNode.appendChild(
                        document.createElement("meta-data").apply {
                            setAttribute("android:name", "$GMS_CORE_PACKAGE.SPOOFED_PACKAGE_NAME")
                            setAttribute("android:value", ORIGINAL_PACKAGE_NAME)
                        },
                    )
                    applicationNode.appendChild(
                        document.createElement("meta-data").apply {
                            setAttribute("android:name", "$GMS_CORE_PACKAGE.SPOOFED_PACKAGE_SIGNATURE")
                            setAttribute("android:value", "24bb24c05e47e0aefa68a58a766179d9b613a600")
                        },
                    )
                    applicationNode.appendChild(
                        document.createElement("meta-data").apply {
                            setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                            setAttribute("android:value", GMS_CORE_PACKAGE)
                        },
                    )
                }
            }
        },
    )

    execute {
        OriginalPackageNameFingerprint.method.addInstructions(
            0,
            "const-string v0, \"$ORIGINAL_PACKAGE_NAME\"\nreturn-object v0",
        )

        GmsCoreVendorGroupFingerprint.method.addInstructions(
            0,
            "const-string v0, \"$GMS_CORE_VENDOR_GROUP\"\nreturn-object v0",
        )

        GooglePhotosHomeActivityOnCreateFingerprint.method.addInstruction(
            0,
            "invoke-static/range { p0 .. p0 }, $EXTENSION_CLASS->checkGmsCore(Landroid/app/Activity;)V",
        )

        ServiceCheckFingerprint.method.addInstruction(0, "return-void")

        GooglePlayUtilityFingerprint.methodOrNull?.addInstructions(
            0,
            "const/4 p1, 0x0\nreturn p1",
        )

        AccountValidityMonitorCheckFingerprint.method.addInstruction(0, "return-void")

        FrictionlessEligibilityFingerprint.method.apply {
            val instructions = implementation?.instructions
                ?: throw PatchException("Frictionless eligibility method has no implementation.")
            val clearSelectedAccountIndex = instructions.indexOfFirst { instruction ->
                ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.let { ref ->
                    ref.name == "o" && ref.returnType == "V" && ref.parameterTypes.toList() == listOf("I")
                } == true
            }
            if (clearSelectedAccountIndex < 0) throw PatchException("Could not find selected account clear call.")

            val accountHandlerClass = ((instructions[clearSelectedAccountIndex] as ReferenceInstruction)
                .reference as MethodReference).definingClass

            replaceInstruction(clearSelectedAccountIndex, "invoke-virtual {p0}, $accountHandlerClass->p()V")
        }

        MapLocationMarkerIconFingerprint.method.addInstructions(
            0,
            "const/4 v0, 0x0\nreturn-object v0",
        )

        fun transform(string: String): String? =
            GMS_STRING_REPLACEMENTS[string]
                ?: transformAppPackageString(string)
                ?: when {
                    string == "com.google.android.libraries.photos.api.mars" -> resolvedMarsAuthority
                    string == LEGACY_MARS_AUTHORITY -> resolvedMarsAuthority
                    string.startsWith("content://") -> transformContentUri(string)
                    else -> null
                }

        getAllClassesWithStrings().forEach { classDef ->
            if (classDef.type == EXTENSION_CLASS) return@forEach

            val hasMatch = classDef.methods.any { method ->
                method.implementation?.instructions?.any { instruction ->
                    val string = ((instruction as? Instruction21c)?.reference as? StringReference)?.string
                    string != null && transform(string) != null
                } == true
            }
            if (!hasMatch) return@forEach

            val mutableClass by lazy { mutableClassDefBy(classDef) }
            classDef.methods.forEach { method ->
                val mutableMethod = mutableClass.methods.firstOrNull {
                    it.name == method.name &&
                        it.returnType == method.returnType &&
                        it.parameterTypes.toList() == method.parameterTypes.toList()
                } ?: return@forEach

                val replacements = mutableMethod.implementation?.instructions
                    ?.mapIndexedNotNull { index, instruction ->
                        val string = ((instruction as? Instruction21c)?.reference as? StringReference)?.string
                            ?: return@mapIndexedNotNull null
                        val transformed = transform(string) ?: return@mapIndexedNotNull null

                        index to BuilderInstruction21c(
                            Opcode.CONST_STRING,
                            instruction.registerA,
                            ImmutableStringReference(transformed),
                        )
                    }
                    .orEmpty()

                replacements.forEach { (index, replacement) ->
                    mutableMethod.replaceInstruction(index, replacement)
                }
            }
        }
    }
}

private fun transformAppPackageString(string: String): String? =
    when (string) {
        in APP_PERMISSIONS, in APP_AUTHORITIES -> string.prefixOrReplaceAppPackage()
        else -> null
    }

private fun transformContentUri(string: String): String? {
    val authorityStart = "content://".length
    val authorityEnd = string.indexOf('/', authorityStart).let { if (it == -1) string.length else it }
    val authority = string.substring(authorityStart, authorityEnd)

    val replacement = when {
        authority in APP_AUTHORITIES -> authority.prefixOrReplaceAppPackage()
        authority == "com.google.android.libraries.photos.api.mars" -> resolvedMarsAuthority
        authority == LEGACY_MARS_AUTHORITY -> resolvedMarsAuthority
        authority == "com.google.android.gms" -> GMS_CORE_PACKAGE
        authority == "com.google.android.gsf.gservices" -> "app.revanced.android.gsf.gservices"
        authority == "com.google.settings" -> "app.revanced.settings"
        authority == "subscribedfeeds" -> "app.revanced.subscribedfeeds"
        else -> return null
    }

    return string.replaceRange(authorityStart, authorityEnd, replacement)
}

private fun String.prefixOrReplaceAppPackage(): String =
    if (startsWith(ORIGINAL_PACKAGE_NAME)) {
        replace(ORIGINAL_PACKAGE_NAME, resolvedPackageName)
    } else {
        "$resolvedPackageName.$this"
    }

private fun marsAuthorityFor(packageName: String) = "$packageName.api.mars"
