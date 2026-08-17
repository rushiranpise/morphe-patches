package app.template.patches.reddit.layout.sidebar

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Hide Sidebar Components
//
// Suppresses specific sidebar sections by blanking their coroutine loaders.
//
// Smali-verified targets (2026.32.0, all in classes3):
//
//  About section:
//    CommunityDrawerPresenter$loadAboutDrawerItems$2.invokeSuspend
//    .registers 3 — p0=this, p1=continuationResult, v0=local
//    Body: calls c.Z()V then returns kotlin.Unit
//    Fix: return-object p1 at index 0 (coroutine-safe exit)
//
//  Resources section:
//    CommunityDrawerPresenter$loadResourcesDrawerItems$2.invokeSuspend
//    .registers 3 — same structure as above
//    Fix: return-object p1
//
//  Reddit Pro section:
//    CommunityDrawerPresenter$loadRedditPro$1.invokeSuspend
//    .registers 8
//    Fix: return-object p1
//
// Games on Reddit and Recently Visited: these are initialised in the presenter
// constructor and toggled via separate DevvitCommunityDrawerPreferences observers.
// Their removal requires a different approach (observer suppression) — deferred.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditHideSidebarComponentsPatch = bytecodePatch(
    name = "Hide Sidebar Components",
    description = "Hides About, Resources, and Reddit Pro sections from the community sidebar.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // All three lambdas have .registers 3 or 8.
        // return-object p1 is a coroutine-safe early exit:
        // it returns the incoming continuation result, which the caller
        // (kotlinx.coroutines dispatcher) interprets as a completed Unit result.
        listOf(
            LoadAboutDrawerItemsFingerprint,
            LoadResourcesDrawerItemsFingerprint,
            LoadRedditProFingerprint,
        ).forEach { fp ->
            runCatching {
                fp.method.addInstructions(0, "return-object p1")
            }
        }
    }
}
