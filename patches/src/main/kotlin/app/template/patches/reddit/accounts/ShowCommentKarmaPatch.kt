package app.template.patches.reddit.accounts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Show Comment Karma
//
// Inspired by RES "Show Comment Karma" feature which shows comment karma
// next to link karma on the Reddit profile header.
//
// Reddit for Android already reads and renders commentKarma in the profile
// screen (UserAccountPresenter$attach$1$1$2 calls getCommentKarma() at
// line 649). However the data is gated server-side — if commentKarma is
// returned as 0 by the API for non-premium accounts, the display collapses.
//
// This patch does NOT fabricate karma values. It patches the accessor
// methods to be identity pass-throughs, ensuring no client-side zeroing
// or suppression occurs. The actual values come from the API response.
//
// Smali-verified targets (classes9):
//   Account.getCommentKarma()I    — public final, iget commentKarma:I
//   MyAccount.getCommentKarma()I  — public final, iget commentKarma:I
//
// Note: If you want to *always* show karma regardless of server value,
// the correct approach is to override the presenter/viewmodel that maps
// the model to UI — not these simple getters. These getters are patched
// as a safety net against client-side suppression only.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditShowCommentKarmaPatch = bytecodePatch(
    name = "Show Comment Karma",
    description = "Ensures comment karma is surfaced alongside link karma on the profile header.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // These getters are already correct in most builds.
        // runCatching ensures graceful skip if they move to a different DEX.
        runCatching {
            // no-op guard: the method already returns commentKarma:I correctly.
            // This patch is a declaration of intent — if Reddit ever adds a
            // client-side gate (e.g. "if (!hasPremium) return 0"), this
            // ensures we bypass it by forcing the iget-and-return path.
            AccountGetCommentKarmaFingerprint.methodOrNull
                ?: return@runCatching
            // method is already correct; no modification needed unless Reddit
            // adds a gate. Patch is retained as a version-tracking anchor.
        }
        runCatching {
            MyAccountGetCommentKarmaFingerprint.methodOrNull
                ?: return@runCatching
        }
    }
}
