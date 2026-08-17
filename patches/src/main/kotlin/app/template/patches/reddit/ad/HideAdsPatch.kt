package app.template.patches.reddit.ad

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.reddit.misc.version.is_2026_04_0_or_greater
import app.template.patches.reddit.misc.version.versionCheckPatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.findFieldFromToString
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

// ─────────────────────────────────────────────────────────────────────────────
// Hide Ads (standalone)
//
// Standalone ad removal for users who don't want the full premium unlock.
// If "Unlock Premium" is also selected, Morphe deduplicates the
// shared dependsOn patches and skips methods already patched.
//
// Targets:
//   c.d("promoted_post_unit")V               → return-void (feed ads)
//   yz9.b("blank_ad_container")V             → return-void (comment ads)
//   CommentsViewModel ad-loader              → return-void
//   CommentsAdState.adsLoadCompleted (2026.04+) → force true
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideAdsPatch = bytecodePatch(
    name = "Hide Ads",
    description = "Removes promoted posts and comment ads without requiring Reddit Premium unlock.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    dependsOn(
        versionCheckPatch,
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
        spoofInstallSourcePatch,
    )

    execute {
        runCatching {
            PromotedPostComposableFingerprint.method.addInstructions(0, "return-void")
        }
        runCatching {
            CommentAdComposableFingerprint.method.addInstructions(0, "return-void")
        }
        runCatching {
            CommentsViewModelAdLoaderFingerprint.method.addInstructions(0, "return-void")
        }
        if (is_2026_04_0_or_greater) {
            runCatching {
                val adsLoadCompletedField = CommentsAdStateToStringFingerprint.method
                    .findFieldFromToString(", adsLoadCompleted=")
                val dynFp = Fingerprint(
                    definingClass = CommentsAdStateToStringFingerprint.originalClassDef.type,
                    name = "<init>",
                    returnType = "V",
                    filters = listOf(
                        fieldAccess(opcode = Opcode.IPUT_BOOLEAN, reference = adsLoadCompletedField),
                    ),
                )
                val matchIdx = dynFp.instructionMatches.last().index
                val reg = dynFp.method.getInstruction<TwoRegisterInstruction>(matchIdx).registerA
                dynFp.method.addInstructions(matchIdx, "const/4 v$reg, 0x1")
            }
        }
    }
}
