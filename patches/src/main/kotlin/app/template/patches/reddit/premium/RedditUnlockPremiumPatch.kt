package app.template.patches.reddit.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.reddit.ad.CommentAdComposableFingerprint
import app.template.patches.reddit.ad.CommentsAdStateToStringFingerprint
import app.template.patches.reddit.ad.CommentsViewModelAdLoaderFingerprint
import app.template.patches.reddit.ad.PromotedPostComposableFingerprint
import app.template.patches.reddit.misc.version.is_2026_04_0_or_greater
import app.template.patches.reddit.misc.version.versionCheckPatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.findFieldFromToString
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction

// ─────────────────────────────────────────────────────────────────────────────
// Unlock Premium + Hide Ads
//
// Merged: ad-hiding IS a premium feature (getHideAds() is Reddit's own flag).
// Having two separate patches for this is redundant — one patch does it all.
//
// What this patches:
//
//  Premium flags (client-side model layer):
//    MyAccount.getHasPremium()Z               → true
//    MyAccount.isPremiumSubscriber()Z          → true
//    MyAccount.getHasPremiumAvatarTreatment()Z → true
//    MyAccount.getHideAds()Z                  → true  (premium ad-hide flag)
//    Account.getHasPremium()Z                 → true
//    Account.isPremiumSubscriber()Z           → true
//    Account.getHideAds()Z                    → true
//    User.getHasPremium()Z                    → true  (protobuf)
//
//  Ad composables (render layer):
//    c.d("promoted_post_unit")V               → return-void
//    yz9.b("blank_ad_container")V             → return-void
//    CommentsViewModel ad-loader              → return-void
//    CommentsAdState.adsLoadCompleted (2026.04+) → force true
//
//  Spoof patches (auto-cert from APK, no pre-seeding):
//    spoofSignatureVerificationPatch
//    spoofFirebaseCertHashPatch
//    spoofInstallSourcePatch
//
// What remains server-side only (cannot be patched):
//    Reddit Coins/Awards, r/lounge access, custom profile banner,
//    profile traffic analytics, custom app icons
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Reddit Premium features and removes all ads client-side.",
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

        // ── Premium model flags ────────────────────────────────────────────
        listOf(
            MyAccountHasPremiumFingerprint,
            MyAccountIsPremiumSubscriberFingerprint,
            MyAccountHasPremiumAvatarTreatmentFingerprint,
            MyAccountHideAdsFingerprint,
            AccountHasPremiumFingerprint,
            AccountIsPremiumSubscriberFingerprint,
            AccountHideAdsFingerprint,
            UserHasPremiumFingerprint,
            // PremiumPreferences — new comment highlighting, avatar accessories, link previews
            PremiumPrefsNewCommentsHighlightFingerprint,
            PremiumPrefsAvatarTreatmentFingerprint,
            PremiumPrefsLinkPreviewsFingerprint,
        ).forEach { fp ->
            runCatching { fp.method.returnEarly(true) }
        }

        // ── LinkInsights status → ONBOARDING_COMPLETE (unlocks Analytics UI) ──
        // Server still controls actual data; this only opens the UI entry point.
        runCatching {
            val onboardingCompleteMatch = MyAccountLinkInsightsStatusFingerprint
            onboardingCompleteMatch.method.addInstructions(
                0,
                """
                    sget-object p0, Lcom/reddit/domain/model/LinkInsightsOnboardingStatus;->ONBOARDING_COMPLETE:Lcom/reddit/domain/model/LinkInsightsOnboardingStatus;
                    return-object p0
                """.trimIndent()
            )
        }

        // ── Feed promoted post composable ──────────────────────────────────
        runCatching {
            PromotedPostComposableFingerprint.method.addInstructions(0, "return-void")
        }

        // ── Comment ad composable ──────────────────────────────────────────
        runCatching {
            CommentAdComposableFingerprint.method.addInstructions(0, "return-void")
        }

        // ── CommentsViewModel ad-loader ────────────────────────────────────
        runCatching {
            CommentsViewModelAdLoaderFingerprint.method.addInstructions(0, "return-void")
        }

        // ── adsLoadCompleted flag (2026.04+) ───────────────────────────────
        if (is_2026_04_0_or_greater) {
            runCatching {
                val adsLoadCompletedField = CommentsAdStateToStringFingerprint.method
                    .findFieldFromToString(", adsLoadCompleted=")

                val dynFp = Fingerprint(
                    definingClass = CommentsAdStateToStringFingerprint.originalClassDef.type,
                    name = "<init>",
                    returnType = "V",
                    filters = listOf(
                        fieldAccess(
                            opcode = Opcode.IPUT_BOOLEAN,
                            reference = adsLoadCompletedField,
                        ),
                    ),
                )
                val matchIdx = dynFp.instructionMatches.last().index
                val reg = dynFp.method.getInstruction<TwoRegisterInstruction>(matchIdx).registerA
                dynFp.method.addInstructions(matchIdx, "const/4 v$reg, 0x1")
            }
        }

    }
}
