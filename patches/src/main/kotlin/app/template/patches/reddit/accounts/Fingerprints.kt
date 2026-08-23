package app.template.patches.reddit.accounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Reddit accounts fingerprints — verified against 2026.32.0 smali (classes9)
//
// Show Comment Karma
// ──────────────────
// Reddit's profile screen already calls Account.getCommentKarma() and displays
// it in UserAccountPresenter$attach$1$1$2 (classes3). The issue is that
// Account.getCommentKarma() and Account.getLinkKarma() are used together but
// commentKarma is not shown on the nav drawer header alongside linkKarma.
//
// The stable injection target is Account.getCommentKarma()I and the
// MyAccount variants — forcing their visibility is done by ensuring the
// presenter always receives a non-zero pathway. Since Reddit already shows
// commentKarma in the profile, this patch is about *surfacing* it:
// we target the field accessor methods on both Account and MyAccount.
//
// Verified smali:
//   Account.getCommentKarma()I  — .method public final, .registers 1
//     iget p0, p0, Lcom/reddit/domain/model/Account;->commentKarma:I
//   Account.getLinkKarma()I     — .method public final, .registers 1
//   Account.getTotalKarma()I    — .method public final, .registers 1
//   MyAccount.getCommentKarma()I — .method public final, .registers 1
//   MyAccount.getLinkKarma()I    — .method public final, .registers 1
//
// Username Hider
// ──────────────
// Account.getUsername()Ljava/lang/String; — .method public (NOT final)
//   iget-object p0, p0, Lcom/reddit/domain/model/Account;->username:Ljava/lang/String;
// MyAccount.getUsername()Ljava/lang/String; — .method public (NOT final)
//   same pattern
// Account.getPrefixedUsername() — .method public (NOT final)
//   iget-object p0, p0, Lcom/reddit/domain/model/Account;->prefixedUsername:Ljava/lang/String;
// ─────────────────────────────────────────────────────────────────────────────

// ── Show Comment Karma ────────────────────────────────────────────────────────

// Account.getCommentKarma()I — .method public final
internal object AccountGetCommentKarmaFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "getCommentKarma",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET,
            smali = "Lcom/reddit/domain/model/Account;->commentKarma:I",
        ),
    ),
)

// MyAccount.getCommentKarma()I — .method public final
internal object MyAccountGetCommentKarmaFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getCommentKarma",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET,
            smali = "Lcom/reddit/domain/model/MyAccount;->commentKarma:I",
        ),
    ),
)

// ── Username Hider ────────────────────────────────────────────────────────────

// Account.getUsername()Ljava/lang/String; — .method public (NOT final)
internal object AccountGetUsernameFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "getUsername",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            smali = "Lcom/reddit/domain/model/Account;->username:Ljava/lang/String;",
        ),
    ),
)

// MyAccount.getUsername()Ljava/lang/String; — .method public (NOT final)
internal object MyAccountGetUsernameFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getUsername",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            smali = "Lcom/reddit/domain/model/MyAccount;->username:Ljava/lang/String;",
        ),
    ),
)

// Account.getPrefixedUsername() — used in nav drawer (u/name format)
internal object AccountGetPrefixedUsernameFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "getPrefixedUsername",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            smali = "Lcom/reddit/domain/model/Account;->prefixedUsername:Ljava/lang/String;",
        ),
    ),
)
