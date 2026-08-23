package app.template.patches.reddit.accounts

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Username Hider
//
// Inspired by RES "Username Hider" which hides the real username while
// logged in, useful for screen-sharing/streaming.
//
// Approach: override getUsername() and getPrefixedUsername() on both
// Account and MyAccount to return the string "u/hidden" and "hidden"
// respectively. This affects:
//   - Nav drawer header (NavDrawerHelper$updateNavUi$2 calls getUsername())
//   - Profile page header
//   - Any composable that calls getUsername() on the logged-in account
//
// Does NOT affect:
//   - Server-side identification (you are still logged in as yourself)
//   - Post authorship (post author is a separate field, not this method)
//   - DMs / chat
//
// Smali-verified (classes9):
//   Account.getUsername()Ljava/lang/String; — public (NOT final), .registers 1
//   Account.getPrefixedUsername()Ljava/lang/String; — public (NOT final), .registers 1
//   MyAccount.getUsername()Ljava/lang/String; — public (NOT final), .registers 1
//   MyAccount.getPrefixedUsername() — NOT present as separate method;
//     MyAccount is a data class that likely inherits or inlines it.
// ─────────────────────────────────────────────────────────────────────────────

private const val HIDDEN_USERNAME = "hidden"
private const val HIDDEN_PREFIXED = "u/hidden"

@Suppress("unused")
val redditHideUsernamePatch = bytecodePatch(
    name = "Username Hider",
    description = "Replaces the displayed username with 'u/hidden' for screen-sharing privacy.",
    default = false,  // opt-in only — most users want to see their username
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // Account.getUsername() → return "hidden"
        runCatching {
            AccountGetUsernameFingerprint.method.addInstructions(
                0,
                """
                    const-string p0, "$HIDDEN_USERNAME"
                    return-object p0
                """.trimIndent(),
            )
        }

        // Account.getPrefixedUsername() → return "u/hidden"
        runCatching {
            AccountGetPrefixedUsernameFingerprint.method.addInstructions(
                0,
                """
                    const-string p0, "$HIDDEN_PREFIXED"
                    return-object p0
                """.trimIndent(),
            )
        }

        // MyAccount.getUsername() → return "hidden"
        runCatching {
            MyAccountGetUsernameFingerprint.method.addInstructions(
                0,
                """
                    const-string p0, "$HIDDEN_USERNAME"
                    return-object p0
                """.trimIndent(),
            )
        }
    }
}
