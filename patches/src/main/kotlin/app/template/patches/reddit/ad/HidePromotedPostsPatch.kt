package app.template.patches.reddit.ad

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.firebase.spoofFirebaseCertHashPatch
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Hide Promoted Posts
//
// Two-layer approach (sourced from Reddidnt / oenderg):
//
//  Layer 1 — composable render method (visual suppression):
//    c.d(brv, Function0, ye10, mc20, uph, ekw, brv, zkb, I)V
//    string "promoted_post_unit", .registers 32, public final
//    class in com.reddit.ads.impl.feeds.composables.*
//    → return-void blanks the rendered slot entirely
//
//  Layer 2 — model layer (data suppression, belt+suspenders):
//    Link.getPromoted()Z         → false  (.method public)
//    Link.isBlankAd()Z           → false  (.method public)
//    Announcement.getPromoted()Z → false  (.method public)
//
// VerifyError rule: NEVER inject return-void into a constructor.
// Constructors must call super.<init>() before returning — the Android
// verifier rejects classes that skip the superclass constructor call.
// ─────────────────────────────────────────────────────────────────────────────

// ── Model-layer fingerprints ──────────────────────────────────────────────────

// Link.getPromoted()Z — .method public getPromoted()Z, iget-boolean promoted:Z
private object LinkGetPromotedFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Link;",
    name = "getPromoted",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            smali = "Lcom/reddit/domain/model/Link;->promoted:Z",
        ),
    ),
)

// Link.isBlankAd()Z — .method public isBlankAd()Z, iget-boolean isBlankAd:Z
private object LinkIsBlankAdFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Link;",
    name = "isBlankAd",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            smali = "Lcom/reddit/domain/model/Link;->isBlankAd:Z",
        ),
    ),
)

// Announcement.getPromoted()Z — .method public getPromoted()Z, iget-boolean promoted:Z
private object AnnouncementGetPromotedFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Announcement;",
    name = "getPromoted",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            smali = "Lcom/reddit/domain/model/Announcement;->promoted:Z",
        ),
    ),
)

// ── Patch ─────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHidePromotedPostsPatch = bytecodePatch(
    name = "Hide Promoted Posts",
    description = "Blanks the promoted post render method and forces promoted/ad flags to false at the model layer.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    dependsOn(
        spoofSignatureVerificationPatch,
        spoofFirebaseCertHashPatch,
        spoofInstallSourcePatch,
    )

    execute {

        // ── Layer 1: blank the composable render method ────────────────────
        runCatching {
            PromotedPostComposableFingerprint.method.addInstructions(0, "return-void")
        }

        // ── Layer 2: force promoted flags false at the model layer ─────────
        listOf(
            LinkGetPromotedFingerprint,
            LinkIsBlankAdFingerprint,
            AnnouncementGetPromotedFingerprint,
        ).forEach { fp ->
            runCatching { fp.method.returnEarly(false) }
        }
    }
}
