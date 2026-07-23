package app.template.patches.onetapcleaner.antitamper

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.AccessFlags

// Internal: applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disableAntiTamperPatch = bytecodePatch(
    description = "No-ops the central self-kill dispatcher called by 1Tap Cleaner's anti-tamper " +
        "checks. On a patched build, three independent triggers fire this method: " +
        "(1) fkp.onActivityCreated — kills if debugger is connected at even-millisecond timestamps; " +
        "(2) fkp.onActivityDestroyed — kills if ApplicationInfo.FLAG_DEBUGGABLE is set, which " +
        "the Morphe patcher sets in the manifest; " +
        "(3) aok/hlr — kills on BILLING_UNAVAILABLE / APK cert mismatch from Play Billing. " +
        "The dispatcher is a no-arg static void method in the utility class pinned by '/cmdline' " +
        "(reads /proc/<pid>/cmdline for process-name detection). It is the only no-arg static " +
        "void method in that class, so the selector is unambiguous without inspecting instructions.",
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

        // The kill dispatcher is the sole public static ()V method in this class.
        // Its name is obfuscated (e.g. \u9c68) but the signature is unique within the class.
        val killMethod = mutableUtil.methods.firstOrNull { method ->
            AccessFlags.STATIC.isSet(method.accessFlags) &&
                AccessFlags.PUBLIC.isSet(method.accessFlags) &&
                method.returnType == "V" &&
                method.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "1Tap Cleaner: kill dispatcher not found in anti-tamper class. " +
                "Expected exactly one public static ()V method.",
        )

        // clearBody() required — the method body has a try-catch block wrapping killProcess.
        killMethod.clearBody()
        killMethod.addInstructions(0, "return-void")
    }
}
