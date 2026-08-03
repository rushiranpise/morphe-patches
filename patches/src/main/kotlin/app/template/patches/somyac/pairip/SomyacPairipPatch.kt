package app.template.patches.somyac.pairip

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * Somyac Pairip v2 bypass — shared by Mini Web Browser and Translator.
 *
 * Both apps are Flutter companion apps (phone side) that use Pairip v2:
 * the LicenseActivity/LicenseClient variant with no VMRunner, no libpairipcore.so,
 * and no asset blobs. Only LicenseClient.licensePubKey and .packageName differ
 * between the two APKs; everything else is byte-for-byte identical.
 *
 * Entry: LicenseContentProvider.onCreate() → LicenseClient.initializeLicenseCheck()
 *        → connectToLicensingService() → processResponse()
 *
 * Not exposed as a top-level patch — composed via dependsOn() in per-app patches.
 */
internal val somyacPairipBytecodePatch = bytecodePatch {
    execute {
        // 1. Kill ContentProvider entry point: return true (provider OK) immediately.
        LicenseContentProviderOnCreateFingerprint.method.addInstructions(
            0, "const/4 v0, 0x1\nreturn v0"
        )

        // 2. Noop the public static checkLicense entry point (belt-and-suspenders).
        CheckLicenseFingerprint.method.returnEarly()

        // 3. Force processResponse(int responseCode, Bundle) to LICENSED (code 0).
        ProcessLicenseResponseFingerprint.apply {
            method.addInstruction(0, "const/4 p1, 0x0")

            // 4. Zero repeatedCheckEnabled so no timer-triggered re-check fires.
            //    Index is +1 due to our injected const/4 shifting all subsequent indices.
            RepeatedCheckFingerprint.matchOrNull(originalMethod)?.apply {
                val sgetIdx = instructionMatches.first().index + 1
                val sgetInstr = method.implementation!!.instructions
                    .toList()[sgetIdx] as? OneRegisterInstruction
                    ?: return@apply
                method.replaceInstruction(sgetIdx, "const/4 v${sgetInstr.registerA}, 0x0")
            }
        }

        // 5. Short-circuit JWS signature validation.
        ValidateLicenseResponseFingerprint.method.returnEarly()
    }
}
