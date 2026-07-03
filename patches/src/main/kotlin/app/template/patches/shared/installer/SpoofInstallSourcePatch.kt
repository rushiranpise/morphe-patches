package app.template.patches.shared.installer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.Instruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val PACKAGE_MANAGER    = "Landroid/content/pm/PackageManager;"
private const val INSTALL_SOURCE_INFO = "Landroid/content/pm/InstallSourceInfo;"
private const val SESSION_INFO        = "Landroid/content/pm/PackageInstaller\$SessionInfo;"

private const val EXTENSION_CLASS =
    "Lapp/template/extension/extension/InstallSourceHelper;"

private fun Instruction.methodReferenceOrNull(): MethodReference? =
    (this as? ReferenceInstruction)?.reference as? MethodReference

private fun MethodReference.isInstallerGetter() =
    (definingClass == PACKAGE_MANAGER &&
        name == "getInstallerPackageName" &&
        parameterTypes.size == 1 &&
        parameterTypes[0].toString() == "Ljava/lang/String;" &&
        returnType == "Ljava/lang/String;") ||
    (definingClass == INSTALL_SOURCE_INFO &&
        name in setOf(
            "getInitiatingPackageName",
            "getInstallingPackageName",
            "getOriginatingPackageName",
            "getUpdateOwnerPackageName",
        ) &&
        parameterTypes.isEmpty() &&
        returnType == "Ljava/lang/String;") ||
    // PackageInstaller.SessionInfo — API 21+/31+
    (definingClass == SESSION_INFO &&
        name in setOf(
            "getInstallerPackageName",
            "getInstallInitiatingPackageName",
            "getInstallOriginatingPackageName",
        ) &&
        parameterTypes.isEmpty() &&
        returnType == "Ljava/lang/String;")

private fun Instruction.isInstallSourceTarget() =
    opcode in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE) &&
        methodReferenceOrNull()?.isInstallerGetter() == true

private fun Method.hasInstallSourceTarget() =
    instructionsOrNull?.any { it.isInstallSourceTarget() } == true

/**
 * Universal "Spoof install source" patch — shows in Morphe for **any** app, no wrapper needed.
 *
 * Two-layer approach:
 *
 * **Layer 1 — DEX const-string replacement** (xob0t + kiraio-moe/Lain-Patches dev branch):
 * Walks every class and replaces the `move-result-object` after each install-source getter
 * call with a `const-string` returning the chosen installer. Covers:
 *   - `PackageManager.getInstallerPackageName()` [API 5+]
 *   - `InstallSourceInfo.get{Initiating,Installing,Originating,UpdateOwner}PackageName()` [API 30+]
 *   - `PackageInstaller.SessionInfo.getInstaller/Initiating/OriginatingPackageName()` [API 21+/31+]
 *
 * **Layer 2 — Binder-level IPackageManager proxy** (ported from WhatsAppHelper extension):
 * Injects `InstallSourceHelper.init(installer)` into `Application.onCreate()` via the
 * extension. This installs a Java Proxy over `ActivityThread.sPackageManager` — the singleton
 * binder stub that backs *every* PackageManager call in the process — so even native code or
 * freshly-obtained PackageManager instances return the spoofed installer. Equivalent to what
 * CalyxOS/GrapheneOS do at the system_server level, but in userspace via reflection.
 *
 * If no Application.onCreate fingerprint is found, Layer 2 is silently skipped and Layer 1
 * still provides broad coverage.
 */

private val ApplicationOnCreateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
    custom = { method, classDef ->
        method.name == "onCreate" &&
            classDef.superclass == "Landroid/app/Application;"
    },
)

@Suppress("unused")
val spoofInstallSourcePatch = bytecodePatch(
    name = "Spoof install source",
    description = """
        Makes the app think it was installed from a specific store (default: Google Play).

        Useful when an app blocks features or shows errors because it detects it was not installed from the Play Store.

        Only affects what the app itself sees - does not change the real system install record.
    """.trimIndent(),
    default = false,
) {
    val installerPackageName by stringOption(
        key = "installerPackageName",
        default = "com.android.vending",
        values = mapOf(
            "Google Play Store" to "com.android.vending",
            "Samsung Galaxy Store" to "com.sec.android.app.samsungapps",
            "Huawei AppGallery" to "com.huawei.appmarket",
            "Amazon Appstore" to "com.amazon.venezia",
            "F-Droid" to "org.fdroid.fdroid",
        ),
        title = "Store to impersonate",
        description = "Most apps only check for the Google Play Store, so the default is usually correct. " +
            "Pick from the list, or type any package name directly if your app checks for a different store.",
        required = true,
    ) { it == null || it.matches(Regex("^[a-z]\\w*(\\.[a-z]\\w*)+\$")) }

    extendWith("extensions/extension.mpe")

    execute {
        val targetInstaller = installerPackageName ?: "com.android.vending"

        // Layer 1: DEX const-string replacement ────────────────────────────
        var patchedCount = 0

        classDefForEach { classDef ->
            if (classDef.methods.none { it.hasInstallSourceTarget() }) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                if (!method.hasInstallSourceTarget()) return@forEach

                val instructionList = method.instructionsOrNull?.toList() ?: return@forEach

                instructionList.forEachIndexed { index, instruction ->
                    if (!instruction.isInstallSourceTarget()) return@forEachIndexed

                    val moveResult = instructionList.getOrNull(index + 1) as? OneRegisterInstruction
                        ?: return@forEachIndexed
                    if (moveResult.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed

                    method.replaceInstruction(
                        index + 1,
                        "const-string v${moveResult.registerA}, \"$targetInstaller\"",
                    )
                    patchedCount++
                }
            }
        }

        // Layer 2: Binder-level IPackageManager proxy via extension ─────────
        // Find Application.onCreate() — present in every Android app.
        val onCreateMethod = ApplicationOnCreateFingerprint.methodOrNull

        if (onCreateMethod != null) {
            onCreateMethod.addInstructions(0, "const-string v0, \"$targetInstaller\"\ninvoke-static {v0}, $EXTENSION_CLASS->init(Ljava/lang/String;)V")
            println("Spoof install source: binder proxy injected into Application.onCreate().")
        } else {
            println("Spoof install source: Application.onCreate() not found — Layer 2 skipped.")
        }

        if (patchedCount == 0 && onCreateMethod == null) {
            println("Spoof install source: no call sites and no Application.onCreate() — patch has no effect.")
        } else {
            println("Spoof install source: $patchedCount DEX call site(s) patched → \"$targetInstaller\".")
        }
    }
}
