package app.template.patches.waze

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.Fingerprint
import app.template.patches.shared.Constants.WAZE_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

import org.w3c.dom.Element
import app.morphe.patcher.patch.resourcePatch

@Suppress("unused")
private val wazeAudioKitManifestPatch = resourcePatch(
    name = "AudioKit Manifest Package Query",
    description = "Adds package visibility queries for the spoofed audio app so Waze can connect to it on Android 11+.",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    
    val packageName by stringOption(
        key = "packageName",
        default = "app.morphe.android.apps.youtube.music",
        title = "Target Package Name",
        description = "The package name of the YouTube Music app to spoof.",
    )

    execute {
        document("AndroidManifest.xml").use { document ->
            val manifest = document.documentElement
            
            // Check if <queries> element exists
            var queriesElement = document.getElementsByTagName("queries").item(0) as? Element
            if (queriesElement == null) {
                queriesElement = document.createElement("queries")
                manifest.appendChild(queriesElement)
            }
            
            // Add <package android:name="..." />
            val targetPkgName = if (packageName.isNullOrBlank()) "app.morphe.android.apps.youtube.music" else packageName!!
            
            val packages = queriesElement.getElementsByTagName("package")
            var found = false
            for (i in 0 until packages.length) {
                val node = packages.item(i) as Element
                if (node.getAttribute("android:name") == targetPkgName) {
                    found = true
                    break
                }
            }
            
            if (!found) {
                val packageElement = document.createElement("package")
                packageElement.setAttribute("android:name", targetPkgName)
                queriesElement.appendChild(packageElement)
            }
        }
    }
}

@Suppress("unused")
val wazeAudioKitSpoofPatch = bytecodePatch(
    name = "AudioKit Integration",
    description = "Allows third-party audio apps (like Morphe YouTube Music) to be recognized as official partners by Waze AudioKit.",
    default = true
) {
    compatibleWith(WAZE_COMPATIBILITY)
    dependsOn(wazeAudioKitManifestPatch)
    
    val packageName by stringOption(
        key = "packageName",
        default = "app.morphe.android.apps.youtube.music",
        title = "Target Package Name",
        description = "The package name of the YouTube Music app to spoof.",
    )

    // Hardcoded YouTube Music signature (default official certificate) so the user isn't prompted
    val certificateData = "MIIDuzCCAqOgAwIBAgIJAKEkO2fQIFpxMA0GCSqGSIb3DQEBBQUAMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRYwFAYDVQQHDA1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKDAtHb29nbGUgSW5jLjEQMA4GA1UECwwHQW5kcm9pZDEQMA4GA1UEAwwHeXRtdXNpYzAeFw0xNDEwMTAxOTE4MTBaFw00MjAyMjUxOTE4MTBaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIDApDYWxpZm9ybmlhMRYwFAYDVQQHDA1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKDAtHb29nbGUgSW5jLjEQMA4GA1UECwwHQW5kcm9pZDEQMA4GA1UEAwwHeXRtdXNpYzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAN7ZdAMBV+hqhqAbyQEUKGlsQzy471NGE1LpYeq9rQVbZyBrEjcEahgotjB1Dv30cq5J8vBjgkH1MsF4yKHjfqtKTZH0SfxecgFVixinzuiqiE7Y2NhqVy/Dq9BT4NaD0Eae1y3bKoux3ApgnxwcNXR0TD7/vKcdxe0jF9PwBAMTdmESXP/oXWwYyxKH0hVjxPdVUUsns3TfBd3Zie//DoSdthYyHZs9pU4ZPARa/SM4VLR1+wnOynl3+ODhcxsnvM9J3csBAjIoVSKH2WmoQuZKYz/ef3ran6o5shljJ5RaW3uUsQlmgwFhdsndTL8kFgTtZ6ihp33FQMdjPTkMkWMCAwEAAaNQME4wHQYDVR0OBBYEFJIbStuhYWO9Xi9hkfJdPn0gz0dBMB8GA1UdIwQYMBaAFJIbStuhYWO9Xi9hkfJdPn0gz0dBMAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAK/aUW/U2lkTQJDLafac6HkNWWBtSyGTZO2P/mduXudzLBZgTKooKl7FT3EkCFvi/vMsakGCwDf/xIg1Glv0PVdx29IzTQzHKRBjpy4eBeD4r0RodvamX/O5w510hDihU5JrO8UsiNWtA6mB0vVSUcFQUSMw5tlqeW3OZ0v+gq2kmJKQNBwDoHX9i1tlIPdVKlzXzIMa9rux3U5wNDDTgSdkkaf3gXIB9yr4Z+YPS/WuEAzXCzgEBiayDO/SAObuefIu75BHrfEkUIlqsGT764bx2HhrVxI2x3EZcuqvkbfslErKb8mcaBNtapkgFrsd81z/cJ9X3zeq4xYfiaEUEm4="

    extendWith("extensions/extension.mpe")

    execute {
        // Find the `init` method of our spoofer in the MPE
        val spooferInitFingerprint = object : Fingerprint(
            returnType = "V",
            custom = { method, _ ->
                method.name == "init" && method.definingClass == "Lapp/template/extension/extension/WazeAudioKitSpoofer;"
            }
        ) {}
        
        val spooferInitMethod = spooferInitFingerprint.method ?: throw IllegalStateException("WazeAudioKitSpooferInit not found")

        // Replace the placeholder strings in the spoofer's init method
        spooferInitMethod.apply {
            val instructions = implementation!!.instructions.toList()
            val packageNameIndex = instructions.indexOfFirst {
                it is ReferenceInstruction && it.reference is StringReference && (it.reference as StringReference).string == "TARGET_PACKAGE_NAME"
            }
            if (packageNameIndex == -1) throw IllegalStateException("TARGET_PACKAGE_NAME not found")
            val packageNameRegister = getInstruction<OneRegisterInstruction>(packageNameIndex).registerA
            val targetPkgName = if (packageName.isNullOrBlank()) "app.morphe.android.apps.youtube.music" else packageName!!
            replaceInstruction(packageNameIndex, "const-string v$packageNameRegister, \"$targetPkgName\"")

            val certIndex = instructions.indexOfFirst {
                it is ReferenceInstruction && it.reference is StringReference && (it.reference as StringReference).string == "TARGET_CERTIFICATE_BASE64"
            }
            if (certIndex == -1) throw IllegalStateException("TARGET_CERTIFICATE_BASE64 not found")
            val certRegister = getInstruction<OneRegisterInstruction>(certIndex).registerA
            val certDataStr = if (certificateData.isNullOrBlank()) "YOUR_YTM_CERT_BASE64_HERE" else certificateData!!
            replaceInstruction(certIndex, "const-string v$certRegister, \"$certDataStr\"")
        }

        // Now inject the call to our spoofer into WazeMobileApplication's onCreate
        val wazeAppFingerprint = object : Fingerprint(
            returnType = "V",
            custom = { method, _ ->
                method.name == "onCreate" && method.definingClass == "Lcom/waze/mobile/WazeMobileApplication;"
            }
        ) {}

        (wazeAppFingerprint.method ?: throw IllegalStateException("WazeMobileApplicationOnCreate not found")).apply {
            addInstructions(0, "invoke-static {}, Lapp/template/extension/extension/WazeAudioKitSpoofer;->init()V")
        }

        // Hook MediaBrowserCompat subscribe to spoof root ID for YouTube Music compatibility
        try {
            val mediaBrowserSubscribeFingerprint = object : Fingerprint(
                returnType = "V",
                custom = { method, _ ->
                    method.parameters.size == 2 &&
                    method.parameters[0].type == "Ljava/lang/String;" &&
                    method.parameters[1].type.startsWith("Landroid/support/v4/media/") &&
                    method.definingClass.startsWith("Landroid/support/v4/media/") &&
                    method.implementation?.instructions?.any { instr ->
                        instr is com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction &&
                        instr.reference is com.android.tools.smali.dexlib2.iface.reference.MethodReference &&
                        (instr.reference as com.android.tools.smali.dexlib2.iface.reference.MethodReference).name == "subscribe" &&
                        (instr.reference as com.android.tools.smali.dexlib2.iface.reference.MethodReference).definingClass == "Landroid/media/browse/MediaBrowser;"
                    } == true
                }
            ) {}

            val subscribeMethod = mediaBrowserSubscribeFingerprint.method
            if (subscribeMethod != null) {
                subscribeMethod.addInstructions(0, 
                    "invoke-static {p1}, Lapp/template/extension/extension/WazeAudioKitSpoofer;->spoofRoot(Ljava/lang/String;)Ljava/lang/String;\n" +
                    "move-result-object p1\n"
                )
                println("WazeAudioKitSpoofPatch: Successfully hooked MediaBrowserCompat subscribe!")
            } else {
                println("WazeAudioKitSpoofPatch: Could not find MediaBrowserCompat subscribe!")
            }
        } catch (e: Exception) {
            println("WazeAudioKitSpoofPatch: Failed to hook MediaBrowserCompat subscribe: ${e.message}")
        }
    }
}
