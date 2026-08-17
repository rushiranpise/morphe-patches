package app.template.patches.onetapcleaner.antitamper

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// Internal: applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disableAntiTamperPatch = bytecodePatch(
    description = "No-ops the central self-kill dispatcher called by 1Tap Cleaner's anti-tamper " +
        "checks. On a patched build, three independent triggers fire this method: " +
        "(1) p6.onActivityCreated — kills if debugger is connected at even-millisecond timestamps; " +
        "(2) p6.onActivityDestroyed — kills if ApplicationInfo.FLAG_DEBUGGABLE is set, which " +
        "the Morphe patcher sets in the manifest; " +
        "(3) billing result handler — kills on BILLING_UNAVAILABLE / APK cert mismatch. " +
        "Pinned by '/cmdline' string (the proc-name reader in the same class). " +
        "Identified within the class by a call to Process.killProcess() — stable across " +
        "versions even as the class accumulates additional static void utility methods.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // Locate the utility class by the '/cmdline' string — unique to the proc-name reader.
        val utilClass = classDefByStrings("/cmdline")
            .firstOrNull()
            ?: throw PatchException(
                "1Tap Cleaner: anti-tamper utility class not found. '/cmdline' string missing.",
            )
        val mutableUtil = mutableClassDefBy(utilClass)

        // The kill dispatcher is the public static no-arg void method that contains a call to
        // Process.killProcess(). Narrowing by this call is necessary from v5.22 onwards —
        // the utility class now contains multiple public static void methods, so the previous
        // "sole no-arg static void" selector is no longer unambiguous.
        val killMethod = mutableUtil.methods.firstOrNull { method ->
            AccessFlags.STATIC.isSet(method.accessFlags) &&
                AccessFlags.PUBLIC.isSet(method.accessFlags) &&
                method.returnType == "V" &&
                method.parameterTypes.isEmpty() &&
                method.implementation?.instructions?.any { insn ->
                    insn is ReferenceInstruction &&
                        (insn.reference as? MethodReference)?.name == "killProcess"
                } == true
        } ?: throw PatchException(
            "1Tap Cleaner: kill dispatcher not found — expected a public static ()V method " +
                "calling Process.killProcess() in the '/cmdline' utility class.",
        )

        // clearBody() required — the method body has a try-catch block wrapping killProcess.
        killMethod.clearBody()
        killMethod.addInstructions(0, "return-void")
    }
}
