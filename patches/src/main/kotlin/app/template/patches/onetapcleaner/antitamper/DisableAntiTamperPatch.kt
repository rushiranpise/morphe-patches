package app.template.patches.onetapcleaner.antitamper

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

// Internal: applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disableAntiTamperPatch = bytecodePatch(
    description = "No-ops the app's central self-kill method (is.ザ: killProcess + System.exit(10)) " +
        "called by multiple anti-tamper checks that fire on patched builds: " +
        "(1) fkp.onActivityCreated — kills if Debug.isDebuggerConnected() at even ms timestamps; " +
        "(2) fkp.onActivityDestroyed — kills if ApplicationInfo.flags & FLAG_DEBUGGABLE (0x2), " +
        "which Morphe patcher sets in the manifest; " +
        "(3) aok/hlr — kills on Finsky BILLING_UNAVAILABLE / cert-mismatch billing response. " +
        "Pinned by the unique '/cmdline' string in is.smali (proc name reader), combined with " +
        "the sole ()V static method that calls both killProcess and System.exit.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // is.smali is pinned by "/cmdline" which is unique to this utility class
        // (reads /proc/<pid>/cmdline for process name detection).
        val isClass = classDefByStrings("/cmdline")
            .firstOrNull()
            ?: throw PatchException(
                "1Tap Cleaner: anti-tamper utility class (is) not found — obfuscation changed.",
            )
        val mutableIs = mutableClassDefBy(isClass)

        // is.ザ()V = the sole public static no-arg void method that calls both
        // Process.killProcess and System.exit — the central kill dispatcher.
        val killMethod = mutableIs.methods.firstOrNull {
            it.name == "ザ" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "1Tap Cleaner: is.ザ()V not found — obfuscation changed.",
        )

        // clearBody required — method has a try-catch block around killProcess + System.exit.
        killMethod.clearBody()
        killMethod.addInstructions(0, "return-void")
    }
}
