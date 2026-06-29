package app.template.patches.onetapcleaner.license

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.iface.Method

// Internal (no name): applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disablePairIPLicenseCheckPatch = bytecodePatch(
    description = "Removes the PairIP license check. Entry point: Application.attachBaseContext " +
        "calls checkLicense(Context) which new-instances LicenseClient and calls " +
        "initializeLicenseCheck(). That method has a try-catch block, so clearBody() is " +
        "required before addInstructions — prepending without clearing breaks the catch table.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // PairIP keeps LicenseClient unobfuscated; pin by ILicensingService const-string opcode.
        val licenseClass = classDefByStrings("com.android.vending.licensing.ILicensingService")
            .firstOrNull()
            ?: throw PatchException(
                "1Tap Cleaner: PairIP LicenseClient not found.",
            )
        val mutableLicenseClass = mutableClassDefBy(licenseClass)

        fun noOp(methodName: String, predicate: (Method) -> Boolean) {
            val method = mutableLicenseClass.methods.firstOrNull {
                it.name == methodName && predicate(it)
            } ?: throw PatchException(
                "1Tap Cleaner: PairIP LicenseClient.$methodName() not found.",
            )
            method.clearBody()
            method.addInstructions(0, "return-void")
        }

        // initializeLicenseCheck() has .locals 2 and a try-catch block — must clearBody() first.
        noOp("initializeLicenseCheck") { it.returnType == "V" && it.parameterTypes.isEmpty() }
        // scheduleAppShutdown() — failsafe.
        noOp("scheduleAppShutdown") { it.returnType == "V" && it.parameterTypes.isEmpty() }
    }
}
