package app.template.patches.bluetoothvolumemanager

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

/**
 * Unlocks the Pro upgrade in Bluetooth Volume Manager.
 *
 * ## History of failed attempts
 *
 * 1. `isPro$1.invokeSuspend()` — wrong target: only the resume callback, body
 *    replacement had no effect since the actual state machine had already read
 *    Info.isUpgraded and returned the real billing result.
 *
 * 2. `UStringsKt.isPro()` (relocated state machine) — fingerprint had wrong
 *    parameter type (ContinuationImpl vs Continuation) causing silent no-match,
 *    and clearBody/removeInstructions approach fragile for large coroutine methods.
 *
 * ## Correct target: UpgradeRepoGplay$Info.<init> — isUpgraded field write
 *
 * The `Info` constructor computes `isUpgraded` from the purchase list:
 *
 * ```
 * this.isUpgraded = (upgrades.isNotEmpty() || gracePeriod)
 * ```
 *
 * Smali: register v1 holds the boolean result just before:
 * ```smali
 * :L18  move v1, v2              ← v1 = 0 (false) if no upgrades + no grace
 * :L19  iput-boolean v1, p0, ->isUpgraded:Z
 * ```
 *
 * We inject `const/4 v1, 0x1` immediately before the iput-boolean at the
 * instructionMatches[0].index. This forces isUpgraded=true regardless of
 * what the billing check returned. The constructor has .registers 16 —
 * no register expansion needed.
 *
 * The register used (v1 from the iput's registerA) is read from the matched
 * instruction at runtime, making the patch robust to minor smali reorderings.
 *
 * ## SKUs
 *   - IAP: "upgrade.premium.rewrite.v3" (current), "upgrade.premium" (legacy)
 *   - Sub: "upgrade.pro"
 */
@Suppress("unused")
val bluetoothVolumeManagerPremiumPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks the Pro upgrade in Bluetooth Volume Manager by forcing isUpgraded=true in the billing Info constructor.",
) {
    compatibleWith(BLUETOOTH_VOLUME_MANAGER_COMPATIBILITY)

    execute {
        val match = InfoConstructorFingerprint.instructionMatches[0]
        val iputIndex = match.index
        // iput-boolean vA, vB, field — vA is the value register (registerA)
        val valueReg = match.getInstruction<TwoRegisterInstruction>().registerA

        InfoConstructorFingerprint.method.addInstructions(
            iputIndex,
            "const/4 v$valueReg, 0x1",
        )
    }
}
