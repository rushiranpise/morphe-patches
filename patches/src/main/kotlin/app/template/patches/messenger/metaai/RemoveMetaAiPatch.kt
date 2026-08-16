package app.template.patches.messenger.metaai

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.messenger.misc.messengerSignaturePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY

// Removes Meta AI from the Messenger UI across four surfaces.
//
// Each target is matched with methodOrNull so a missing surface (removed or
// reorganised by Meta in a newer version) is silently skipped rather than
// crashing the patch run. The patch succeeds as long as at least one surface
// is removed.
//
// Surfaces (verified com.facebook.orca 573.0.0.44.88):
//  1. AiFabComponent render()       — floating AI compose button (classes4)
//  2. AiCreationFolderItem gate     — "AI Creation" nav drawer item (classes7)
//  3. AiHomeFolderItem gate         — "AI Home" nav drawer item (classes7)
//  4. SearchAiagentImplementations  — AI suggestions row in search (classes3)
//
// Fingerprint fix vs v573:
//  Previously used obfuscated parameter types (LX/2C0;, LX/1bi;) which change
//  every update. Now anchored solely on stable non-obfuscated string constants
//  (component name, kill-switch FQCN) which survive R8 obfuscation.
@Suppress("unused")
val messengerRemoveMetaAiPatch = bytecodePatch(
    name = "Remove Meta AI",
    description = "Removes the Meta AI floating button, drawer items, and search suggestions.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        // 1. AI FAB — return null (render nothing)
        MetaAiFabRenderFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """.trimIndent(),
        )

        // 2. AI Creation drawer item gate — return false
        MetaAiCreationFolderItemFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )

        // 3. AI Home drawer item gate — return false
        MetaAiHomeFolderItemFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )

        // 4. Search AI kill-switch gate — return false
        MetaAiSearchFingerprint.methodOrNull?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
