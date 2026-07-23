package app.template.patches.onetapcleaner.licwnd

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.AccessFlags

// Internal: applied automatically as a dependency of Unlock Pro.
//
// 1Tap Cleaner's "upgrade to pro" button handlers call one of two static methods
// to show the purchase Activity:
//   LicWnd.蕃(Context)V         — starts LicWnd via Intent (~12 callers)
//   LicWnd.虃(Context, frt, Z)V — parameterised billing flow (2 callers)
//
// These handlers gate on hts.鐽 (billing/app initialized flag, always true at runtime
// after MainApp.onCreate). Since 鐽 is always true, every "upgrade" button tap launches
// the purchase dialog even though PRO is already unlocked via hts.鷖.
//
// Fix: no-op both show-methods at source. Suppresses purchase Activity from all 14
// call sites without touching any individual handler.
@Suppress("unused")
val disableLicWndPatch = bytecodePatch(
    description = "No-ops LicWnd.蕃(Context)V and LicWnd.虃(Context,frt,Z)V — the two static " +
        "entry points that launch the in-app purchase Activity — so 'upgrade to PRO' " +
        "button taps silently do nothing on a patched build.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        val licWndClass = mutableClassDefBy("Lcom/a0soft/gphone/acc/wnd/LicWnd;")

        // LicWnd.蕃(Context)V — sole static ()V method taking exactly one Context parameter.
        val showSimple = licWndClass.methods.firstOrNull { method ->
            AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "V" &&
                method.parameterTypes == listOf("Landroid/content/Context;")
        } ?: throw PatchException(
            "1Tap Cleaner: LicWnd.蕃(Context)V not found — class was renamed or removed.",
        )
        showSimple.clearBody()
        showSimple.addInstructions(0, "return-void")

        // LicWnd.虃(Context, frt, Z)V — sole static method with 3 params, first = Context.
        // Has try-catch blocks; clearBody() required.
        val showParametrised = licWndClass.methods.firstOrNull { method ->
            AccessFlags.STATIC.isSet(method.accessFlags) &&
                method.returnType == "V" &&
                method.parameterTypes.size == 3 &&
                method.parameterTypes[0] == "Landroid/content/Context;"
        } ?: throw PatchException(
            "1Tap Cleaner: LicWnd.虃(Context,frt,Z)V not found — class was renamed or removed.",
        )
        showParametrised.clearBody()
        showParametrised.addInstructions(0, "return-void")
    }
}
