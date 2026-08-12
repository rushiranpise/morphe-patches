package app.template.patches.messenger.metaai

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Removes Meta AI from the Messenger UI.
//
// Targets four surfaces, all verified in com.facebook.orca 573.0.0.44.88:
//
//  1. AiFabComponent render() — classes4/X/7Ey
//     The floating "AI" compose button rendered on the chat list.
//     Returning null at index 0 renders nothing. The return type is LX/1Kh;
//     (a nullable component type), so null is a valid sentinel meaning "render nothing".
//
//  2. F6D.A00()Z — lazy-loader for AiCreationFolderItem — classes7/X/F6D
//     Gate that decides whether to add the "AI Creation" item to the navigation drawer.
//     Returning false prevents the item from ever being constructed or added.
//
//  3. F6D.A01()Z — lazy-loader for AiHomeFolderItem — classes7/X/F6D
//     Same pattern for the "AI Home" drawer item.
//     Returning false hides the item.
//
//  4. 5sR.A00(1bi, AtomicInteger, I)Z — search AI kill-switch reader — classes3/X/5sR
//     Reads MobileConfig to decide whether AI suggestions appear in search.
//     Returning false disables the AI suggestions row.
//
// No extension required — all patches are pure bytecode overrides.
@Suppress("unused")
val messengerRemoveMetaAiPatch = bytecodePatch(
    name = "Remove Meta AI",
    description = "Removes the Meta AI floating button, drawer items, and search suggestions.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        // 1. AI FAB — return null (const/4 v0, 0x0 / return-object v0)
        MetaAiFabRenderFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return-object v0
            """.trimIndent(),
        )

        // 2. AI Creation drawer item gate — return false
        MetaAiCreationFolderItemFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )

        // 3. AI Home drawer item gate — return false
        MetaAiHomeFolderItemFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )

        // 4. Search AI kill-switch gate — return false
        MetaAiSearchFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}
