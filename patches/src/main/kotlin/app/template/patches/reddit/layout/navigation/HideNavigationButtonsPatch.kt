package app.template.patches.reddit.layout.navigation

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Hide Navigation Buttons
//
// Removes the "Create Post" (Post tab) from the bottom navigation bar.
//
// Target: BottomNavScreen.m5(Ljava/util/List;Lzkb;I)V
//   .registers 9 — p0=this, p1=List<BottomNavTab>, p2=zkb, p3=I
//   Existing labels: :L0..:L7
//
// Injection: addInstructions at index 0 (no labels — avoids label-table
// displacement in methods with existing forward branches).
//
// Strategy: sget the Post enum singleton, call List.remove(Object)Z on p1.
// No iterator, no branches, no labels. Uses v0 only (clobbered before its
// first use in the original method body).
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideNavigationButtonsPatch = bytecodePatch(
    name = "Hide Navigation Buttons",
    description = "Removes the Create Post button from the bottom navigation bar.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        runCatching {
            // p1 = List<BottomNavTab> — remove Post tab in-place before any rendering.
            // List.remove(Object) is safe on ArrayList (mutable) which is what Reddit uses.
            // v0 is immediately overwritten by the original body (const 248875743), safe to clobber.
            BottomNavScreenM5Fingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, Lcom/reddit/launch/bottomnav/BottomNavTab;->Post:Lcom/reddit/launch/bottomnav/BottomNavTab;
                    invoke-interface { p1, v0 }, Ljava/util/List;->remove(Ljava/lang/Object;)Z
                """.trimIndent(),
            )
        }
    }
}
