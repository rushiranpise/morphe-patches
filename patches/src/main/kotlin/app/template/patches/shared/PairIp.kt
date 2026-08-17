package app.template.patches.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.Base64

private const val SIGNATURE_CHECK = "Lcom/pairip/SignatureCheck;"
private const val VM_RUNNER = "Lcom/pairip/VMRunner;"
private const val STARTUP_LAUNCHER = "Lcom/pairip/StartupLauncher;"

fun BytecodePatchContext.gutPairIpVm(vararg extraNoOps: Pair<String, String>) {
    mutableClassDefBy(SIGNATURE_CHECK).methods.first { it.name == "verifyIntegrity" }.returnEarly()
    val vmRunner = mutableClassDefBy(VM_RUNNER)
    vmRunner.methods.first { it.name == "<clinit>" }.returnEarly()
    vmRunner.methods.first { it.name == "invoke" }.returnEarly()
    mutableClassDefBy(STARTUP_LAUNCHER).methods.first { it.name == "launch" }.returnEarly()
    for ((cls, method) in extraNoOps)
        mutableClassDefBy(cls).methods.first { it.name == method }.returnEarly()
}

fun BytecodePatchContext.restorePairIpHolders(resourceDir: String) {
    fun resource(name: String) =
        object {}.javaClass.getResourceAsStream("/$resourceDir/$name")?.bufferedReader()?.readText()
            ?: error("Missing: /$resourceDir/$name")

    fun bakeClinit(type: String, registerCount: Int, body: String) {
        val holder = mutableClassDefByOrNull(type) ?: error("Holder $type not found")
        if (holder.methods.any { it.name == "<clinit>" }) error("$type already has <clinit>")
        val clinit = ImmutableMethod(
            type, "<clinit>", emptyList(), "V",
            AccessFlags.STATIC.value or AccessFlags.CONSTRUCTOR.value,
            null, null, ImmutableMethodImplementation(registerCount, emptyList(), null, null),
        ).toMutable()
        holder.methods.add(clinit)
        clinit.addInstructions(0, body)
    }

    var type: String? = null
    val body = StringBuilder()
    fun flushStrings() {
        val t = type ?: return
        body.append("return-void")
        bakeClinit(t, 1, body.toString())
        body.setLength(0)
    }
    resource("depairip_strings.tsv").lineSequence().filter { it.isNotBlank() }.forEach { line ->
        if (line.startsWith("@")) { flushStrings(); type = line.substring(1) }
        else {
            val tab = line.indexOf('\t')
            body.append("const-string v0, \"${line.substring(tab + 1)}\"\nsput-object v0, $type->${line.substring(0, tab)}:Ljava/lang/String;\n")
        }
    }
    flushStrings()

    resource("depairip_methods.tsv").lineSequence().filter { it.isNotBlank() }.forEach { line ->
        val (t, rc, b64) = line.split('\t', limit = 3)
        bakeClinit(t, rc.toInt(), String(Base64.getDecoder().decode(b64)))
    }
}

// ── killPairIpFull ────────────────────────────────────────────────────────────

/**
 * Full DEX-layer Pairip kill. No-ops:
 *   VMRunner.<clinit>()                    — stops System.loadLibrary("pairipcore")
 *   VMRunner.invoke()                      — belt-and-suspenders
 *   SignatureCheck.verifyIntegrity()       — DEX cert check
 *   SignatureCheck.verifySignatureMatches() — returns true
 *   StartupLauncher.launch()               — IAP VM invoker
 *   LicenseClient.initializeLicenseCheck() — Play LVL, forced LOCAL_CHECK_OK
 *   LicenseClient.connectToLicensingService()
 *   LicenseClient.processResponse()        — NOT_LICENSED path
 *   LicenseClient.startPaywallActivity()   — paywall + System.exit failsafe
 *   All VMRunner.invoke() call sites outside com.pairip.*
 *
 * Also calls [initPairipStringHolders] to inject "" <clinit>s into string
 * holder classes to prevent NPEs when the native lib is not running.
 */
fun BytecodePatchContext.killPairIpFull(
    vararg extraNoOps: Pair<String, String>,
) {
    val sig   = "Lcom/pairip/SignatureCheck;"
    val vm    = "Lcom/pairip/VMRunner;"
    val sl    = "Lcom/pairip/StartupLauncher;"
    val lic   = "Lcom/pairip/licensecheck/LicenseClient;"
    val state = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

    mutableClassDefByOrNull(vm)?.methods?.firstOrNull { it.name == "<clinit>" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }
    mutableClassDefByOrNull(vm)?.methods?.firstOrNull { it.name == "invoke" }
        ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x0\nreturn-object v0") }

    mutableClassDefByOrNull(sig)?.methods?.firstOrNull { it.name == "verifyIntegrity" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }
    mutableClassDefByOrNull(sig)?.methods?.firstOrNull { it.name == "verifySignatureMatches" }
        ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

    mutableClassDefByOrNull(sl)?.methods?.firstOrNull { it.name == "launch" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == "initializeLicenseCheck" }
        ?.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, $state->LOCAL_CHECK_OK:$state
                sput-object v0, $lic->licenseCheckState:$state
                return-void
            """.trimIndent())
        }
    listOf("connectToLicensingService", "lambda\$retryOrThrow\$0", "processResponse", "startPaywallActivity",
           "validateResponse", "performLocalInstallerCheck")
        .forEach { name ->
            mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == name }
                ?.apply { clearBody(); addInstructions(0, "return-void") }
        }

    // performLocalInstallerCheck lives on LicenseClient — returnEarly(true)
    mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == "performLocalInstallerCheck" }
        ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

    // validateResponse lives on ResponseValidator
    val rv = "Lcom/pairip/licensecheck/ResponseValidator;"
    mutableClassDefByOrNull(rv)?.methods?.firstOrNull { it.name == "validateResponse" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    // Disable repeated background checks via LicenseClient.<clinit>
    mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == "<clinit>" }
        ?.addInstructions(0, """
            const/4 v0, 0x0
            sput-boolean v0, $lic->repeatedCheckEnabled:Z
        """.trimIndent())

    // Clear all VMRunner.invoke() call sites outside com.pairip.*
    classDefForEach { classDef ->
        if (classDef.type.startsWith("Lcom/pairip/")) return@classDefForEach
        val callers = classDef.methods.filter { method ->
            method.implementation?.instructions?.any { insn ->
                insn.opcode.name.startsWith("INVOKE") &&
                    (insn as? ReferenceInstruction)?.reference.let { ref ->
                        ref is MethodReference &&
                            ref.definingClass == vm &&
                            ref.name == "invoke"
                    }
            } == true
        }
        if (callers.isEmpty()) return@classDefForEach
        val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
        for (method in callers) {
            mutableClass.methods.firstOrNull {
                it.name == method.name && it.returnType == method.returnType
            }?.returnEarly()
        }
    }

    for ((cls, method) in extraNoOps)
        mutableClassDefByOrNull(cls)?.methods?.firstOrNull { it.name == method }
            ?.apply { clearBody(); addInstructions(0, "return-void") }
}

// ── Standalone shared patch ───────────────────────────────────────────────────

/**
 * Standalone shared patch — other patches can `dependsOn(disablePairIPLicenseCheckPatch)`.
 *
 * Combines [killPairIpFull] (full VM + license kill) with the rivanced additions:
 *   - ResponseValidator.validateResponse()         → return-void
 *   - LicenseClient.performLocalInstallerCheck()  → return true
 *   - LicenseClient.<clinit> repeatedCheckEnabled → false
 *
 * Gracefully skips any method that does not exist (not all apps include all PairIP classes).
 */
@Suppress("unused")
val disablePairIPLicenseCheckPatch = bytecodePatch(
    name = "Disable PairIP license check",
    description = "Disables PairIP license verification, VM checks, and repeated background checks.",
    default = false,
) {
    execute {
        killPairIpFull()
    }
}

// ── Manifest companion ────────────────────────────────────────────────────────

/**
 * Shared manifest-level PairIP bypass.
 *
 * Generalised from our cubesolver BypassPairIPManifestPatch. Three operations:
 *
 * 1. **Replace Application class** (optional) — PairIP often registers
 *    `com.pairip.application.Application` as `android:name`. Its
 *    `attachBaseContext` calls `verifyIntegrity` + `checkLicense` before any
 *    app code runs. Pass the real app class to skip this.
 *
 * 2. **Remove LicenseActivity** — prevents Play Store redirect on check failure.
 *
 * 3. **Remove CHECK_LICENSE permission** — `com.android.vending.CHECK_LICENSE`
 *    declared by all PairIP apps; removing it denies it at OS level.
 *
 * Usage:
 * ```kotlin
 * dependsOn(pairIPManifestPatch())                         // items 2+3 only
 * dependsOn(pairIPManifestPatch("com.example.app.MyApp"))  // also replaces app class
 * ```
 */
fun pairIPManifestPatch(replacementAppClass: String? = null) = resourcePatch(
    name = "PairIP manifest bypass",
    description = "Removes LicenseActivity and CHECK_LICENSE permission from AndroidManifest. " +
        "Optionally replaces the PairIP Application class with the real one.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { doc ->
            if (replacementAppClass != null) {
                val app = doc.getElementsByTagName("application").item(0) as? org.w3c.dom.Element
                app?.setAttribute("android:name", replacementAppClass)
            }

            val activities = doc.getElementsByTagName("activity")
            for (i in activities.length - 1 downTo 0) {
                val el = activities.item(i) as? org.w3c.dom.Element ?: continue
                if (el.getAttribute("android:name").contains("LicenseActivity"))
                    el.parentNode.removeChild(el)
            }

            val permissions = doc.getElementsByTagName("uses-permission")
            for (i in permissions.length - 1 downTo 0) {
                val el = permissions.item(i) as? org.w3c.dom.Element ?: continue
                if (el.getAttribute("android:name").contains("CHECK_LICENSE"))
                    el.parentNode.removeChild(el)
            }
        }
    }
}
