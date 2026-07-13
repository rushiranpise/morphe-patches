package app.template.patches.thetransit

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.THETRANSIT_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val transitUnlockRoyalePatch = bytecodePatch(
    name = "Unlock Royale",
    description = "Unlocks Transit Royale Membership.",
    default = true
) {
    compatibleWith(THETRANSIT_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        // Layer 1: isActive() getter -> always return true
        RoyaleIsActiveFingerprint
            .match(classDefBy(RoyaleIsActiveFingerprint.definingClass!!))
            .method
            .apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // Layer 2: force activate_royale_subscription feature flag to true
        FetchSubscriptionStatusFingerprint
            .match(classDefBy(FetchSubscriptionStatusFingerprint.definingClass!!))
            .method
            .apply {
                val idx = instructions.indexOfFirst {
                    it.opcode == Opcode.INVOKE_STATIC &&
                        (it as ReferenceInstruction).reference.toString()
                            .contains("isFeatureActivated(Ljava/lang/String;)Z")
                }
                if (idx < 0) throw PatchException("Transit Royale feature flag check not found.")

                addInstructions(idx + 2, "const/4 v2, 0x1")
            }

        // Layer 3: inject TransitHelper.init() in TransitApp.onCreate()
        // to spoof original signing cert + Maps API key for re-signed APKs
        TransitAppOnCreateFingerprint
            .match(classDefBy(TransitAppOnCreateFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                "invoke-static {}, Lapp/template/extension/extension/TransitHelper;->init()V"
            )
    }
}
