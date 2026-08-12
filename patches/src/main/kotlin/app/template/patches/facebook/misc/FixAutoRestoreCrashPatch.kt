package app.template.patches.facebook.misc

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY

// Internal patch — applied automatically as a dependency of ChangePackageNamePatch.
// Not exposed as a standalone user option.
//
// Fixes the encrypted-backup auto-restore crash that occurs when messaging threads
// are opened after installing Facebook under a renamed package.
//
// Root cause (identical to Messenger's CsR crash, fixed by messengerFixAutoRestoreCrashPatch):
//   LX/hWL.<init>() (classes10) calls Context.getPackageName(), then iterates a
//   packed-switch table of integer IDs that map to:
//     • "com.facebook.katana"     (Facebook release)
//     • "com.facebook.orca"       (Messenger release)
//     • "com.facebook.wakizashi"  (Facebook Lite)
//     • "com.facebook.orca.debug" (Messenger debug)
//   When installed under a renamed package (e.g. app.morphe.facebook.katana),
//   getPackageName() returns the clone name → no arm matches → loop exhausts:
//     throw new NoSuchElementException(...)   ← crash at hWL.<init>:124
//   This manifests as a FATAL EXCEPTION on CombinedTP19 thread when opening messages.
//
// Stack trace (from logcat):
//   java.util.NoSuchElementException: Array contains no element matching the predicate.
//     at X.hWL.<init>(:124)
//     at X.WvX.A07(:489)
//     ...
//     at com.facebook.messaginginblue.e2ee.cloudbackup.handlers.autorestore.MibEbAutoRestoreHandler.A00
//
// Fix (one instruction — same approach as Messenger CsR fix):
//   getPackageName() result lands in v6 at instruction index 12 (0-based).
//   Insert const-string v6, "com.facebook.katana" at index 13.
//   This overwrites v6 with the original package name before the switch loop runs,
//   so the lookup always finds the "com.facebook.katana" arm regardless of install name.
//
// Instruction sequence (verified classes10/X/hWL.smali, 569.0.0.42.72):
//   [0]  invoke-direct  {p0}, Object;-><init>()V
//   [1]  invoke-static  {}, 0YP;->A00()Application
//   [2]  move-result-object v5
//   [3]  iput-object    v5, p0, hWL;->A03:Context
//   [4]  const          v0, 0x3463a
//   [5]  invoke-static  {v0}, 40b;->A0F(I)
//   [6]  move-result-object v0
//   [7]  invoke-interface {v0}, 0yO;->get()Object
//   [8]  move-result-object v0
//   [9]  check-cast     v0, YU7
//   [10] iget-object    v0, v0, YU7;->A00:Context
//   [11] invoke-virtual {v0}, Context;->getPackageName()String   ← anchor
//   [12] move-result-object v6
//   INSERT → const-string v6, "com.facebook.katana"              ← at index 13
@Suppress("unused")
internal val facebookFixAutoRestoreCrashPatch = bytecodePatch(
    name = "Fix auto-restore crash (internal)",
    description = "Spoofs the package name in hWL.<init> so the E2EE backup lookup always resolves to com.facebook.katana. Required by Change package name.",
    default = false,
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    execute {
        // Insert const-string v6, "com.facebook.katana" at index 13 (after move-result-object v6)
        hWLInitFingerprint.method.addInstruction(
            13,
            """const-string v6, "com.facebook.katana"""",
        )
    }
}
