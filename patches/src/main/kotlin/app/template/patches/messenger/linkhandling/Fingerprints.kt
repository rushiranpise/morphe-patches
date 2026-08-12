package app.template.patches.messenger.linkhandling

import app.morphe.patcher.Fingerprint

// ─── In-app browser decision gate (classes9/X/KU2) ───────────────────────────
// Matches the method that decides whether a URL should open in the in-app browser.
// Returns true  → use in-app browser.
// Returns false → skip it (open externally); one of its skip reasons is logged as
//                 "user_prefers_external" under the "iab_skipped_reason" analytics key.
//
// Method signature (verified v573):
//   classes9/X/KU2 → method public A0H(Landroid/net/Uri;Lcom/facebook/auth/usersession/FbUserSession;)Z
//
// Both string anchors are present in A0H:
//   "iab_skipped_reason" — analytics event key logged on every skip decision
//   "user_prefers_external" — the specific reason value for the user setting
//
// Returning false at index 0 forces the in-app browser to always be skipped,
// equivalent to having "Open links in external browser" enabled in settings.
//
// Verified against com.facebook.orca 573.0.0.44.88 (classes9/X/KU2.smali).
internal val ShouldOpenInAppBrowserFingerprint = Fingerprint(
    parameters = listOf("Landroid/net/Uri;", "Lcom/facebook/auth/usersession/FbUserSession;"),
    returnType = "Z",
    strings = listOf("iab_skipped_reason", "user_prefers_external"),
)
