package app.template.patches.reddit.content

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Reddit content fingerprints — verified against 2026.32.0 smali (classes9)
//
// All methods below are in Lcom/reddit/domain/model/ and use a 1-register
// pattern (.registers 1, p0 = this, iget → return).
//
// Link model
// ──────────
//   getOver18()Z            — public final, iget-boolean over18:Z
//   getHideScore()Z         — public final, iget-boolean hideScore:Z
//   getHidden()Z            — public final, iget-boolean hidden:Z
//   getSaved()Z             — public final, iget-boolean saved:Z
//   getAwards()Ljava/util/List; — public final, iget-object awards:List
//   isCrosspostable$domain_model()Z — public final, iget-boolean isCrosspostable:Z
//
// Comment model
// ─────────────
//   getScoreHidden()Z       — public final, iget-boolean scoreHidden:Z
//   isAwardedRedditGold()Z  — public final, iget-boolean isAwardedRedditGold:Z
// ─────────────────────────────────────────────────────────────────────────────

private const val LINK  = "Lcom/reddit/domain/model/Link;"
private const val COMMENT = "Lcom/reddit/domain/model/Comment;"

// ── Hide NSFW Posts ─────────────────────────────────────────────────────────

// Link.getOver18()Z — public final
internal object LinkGetOver18Fingerprint : Fingerprint(
    definingClass = LINK,
    name = "getOver18",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$LINK->over18:Z"),
    ),
)

// ── Hide Score / Hide Score on Posts ────────────────────────────────────────

// Link.getHideScore()Z — public final
internal object LinkGetHideScoreFingerprint : Fingerprint(
    definingClass = LINK,
    name = "getHideScore",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$LINK->hideScore:Z"),
    ),
)

// Comment.getScoreHidden()Z — public final
internal object CommentGetScoreHiddenFingerprint : Fingerprint(
    definingClass = COMMENT,
    name = "getScoreHidden",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$COMMENT->scoreHidden:Z"),
    ),
)

// ── Hide Repost / Crosspost Button ──────────────────────────────────────────

// Link.isCrosspostable$domain_model()Z — public final
// Note: method name contains $ (Kotlin-mangled), matched via exact definingClass + name
internal object LinkIsCrosspostableFingerprint : Fingerprint(
    definingClass = LINK,
    name = "isCrosspostable\$domain_model",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$LINK->isCrosspostable:Z"),
    ),
)

// ── Hide Awards ──────────────────────────────────────────────────────────────

// Link.getAwards()Ljava/util/List; — public final
internal object LinkGetAwardsFingerprint : Fingerprint(
    definingClass = LINK,
    name = "getAwards",
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_OBJECT, smali = "$LINK->awards:Ljava/util/List;"),
    ),
)

// Comment.isAwardedRedditGold()Z — public final (used in comment rendering)
internal object CommentIsAwardedRedditGoldFingerprint : Fingerprint(
    definingClass = COMMENT,
    name = "isAwardedRedditGold",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$COMMENT->isAwardedRedditGold:Z"),
    ),
)

// ── Hide Seen / Already-Visited Posts ────────────────────────────────────────

// Link.getHidden()Z — public final
// Reddit's "hide post" marks Link.hidden = true; returning true here hides it client-side.
internal object LinkGetHiddenFingerprint : Fingerprint(
    definingClass = LINK,
    name = "getHidden",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(opcode = Opcode.IGET_BOOLEAN, smali = "$LINK->hidden:Z"),
    ),
)
