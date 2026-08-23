package app.template.patches.reddit.layout.navoptions

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.reddit.layout.navigation.BottomNavScreenM5Fingerprint
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Hide Navigation Options
//
// Extends the existing "Hide Navigation Buttons" approach to cover more tabs.
// All tabs are members of the BottomNavTab enum (classes10):
//   Home, Communities, Answers, Post, Chat, Inbox,
//   Games, UnifiedInbox, Profile, MyCommunities
//
// Same target: BottomNavScreen.m5(Ljava/util/List;Lzkb;I)V
//   .registers 9 — p1 = List<BottomNavTab> (mutable ArrayList)
//   We call List.remove(Object) for each tab to suppress.
//   v0 is safe to clobber — overwritten by const 0xed58adf immediately.
//
// Three standalone patches:
//   1. Hide Chat tab      — removes the Chat bottom nav button
//   2. Hide Answers tab   — removes the AI Answers tab (Reddit's AI feature)
//   3. Hide Games tab     — removes the Games tab
//
// Each is a separate bytecodePatch so users can toggle them independently.
// They all target the same method; multiple List.remove() calls in sequence
// are safe — ArrayList.remove(Object) is a no-op if the element isn't present.
//
// Smali-verified (classes10):
//   BottomNavTab.Chat   — enum field .field public static final enum Chat
//   BottomNavTab.Answers — enum field .field public static final enum Answers
//   BottomNavTab.Games  — enum field .field public static final enum Games
//   BottomNavScreen.m5(Ljava/util/List;Lzkb;I)V — same as existing nav patch
// ─────────────────────────────────────────────────────────────────────────────

private const val BOTTOM_NAV_TAB = "Lcom/reddit/launch/bottomnav/BottomNavTab;"

@Suppress("unused")
val redditHideChatTabPatch = bytecodePatch(
    name = "Hide Chat Tab",
    description = "Removes the Chat button from the bottom navigation bar.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            BottomNavScreenM5Fingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, $BOTTOM_NAV_TAB->Chat:$BOTTOM_NAV_TAB
                    invoke-interface { p1, v0 }, Ljava/util/List;->remove(Ljava/lang/Object;)Z
                """.trimIndent(),
            )
        }
    }
}

@Suppress("unused")
val redditHideAnswersTabPatch = bytecodePatch(
    name = "Hide Answers Tab",
    description = "Removes the AI Answers tab from the bottom navigation bar.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            BottomNavScreenM5Fingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, $BOTTOM_NAV_TAB->Answers:$BOTTOM_NAV_TAB
                    invoke-interface { p1, v0 }, Ljava/util/List;->remove(Ljava/lang/Object;)Z
                """.trimIndent(),
            )
        }
    }
}

@Suppress("unused")
val redditHideGamesTabPatch = bytecodePatch(
    name = "Hide Games Tab",
    description = "Removes the Games tab from the bottom navigation bar.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            BottomNavScreenM5Fingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, $BOTTOM_NAV_TAB->Games:$BOTTOM_NAV_TAB
                    invoke-interface { p1, v0 }, Ljava/util/List;->remove(Ljava/lang/Object;)Z
                """.trimIndent(),
            )
        }
    }
}
