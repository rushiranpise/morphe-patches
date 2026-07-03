package app.template.patches.casetracker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CASETRACKER_COMPATIBILITY

/**
 * Unlocks all premium features in Case Tracker — Immigration (com.saldous.casetracker).
 *
 * ## Subscription model (v5.5.1)
 *
 * All purchase state is stored in a MutableStateFlow<z> (ads/x.b).
 * The data class z(a: Z, b: Z, c: Z, d: Z) maps to:
 *   z.a = isSubscribed (Pro || Plus || AdsRemoved)
 *   z.b = isPro
 *   z.c = isPlus
 *   z.d = isAdsRemoved
 *
 * The StateFlow is seeded at startup from SharedPreferences via
 * data/c.a(String, boolean)Z, then can be overwritten by a backend
 * refresh coroutine (ads/x.e).
 *
 * ## Layers
 *
 * **Layer 1 — SharedPrefs getter (data/c.a)**
 * Replacing with `return true` causes the StateFlow to be seeded with
 * z(true, true, true, true) on every cold start.
 *
 * **Layer 2 — isPro getter (ads/x.a)**
 * Short-circuits the Pro tier gate to always return true regardless of
 * the current StateFlow value.
 *
 * **Layer 3 — isSubscribed getter (ads/x.c)**
 * Short-circuits the generic "any paid tier" gate to always return true.
 *
 * **Layer 4 — refreshFromBackend coroutine (ads/x.e)**
 * Prepending return-void prevents the Revenew backend from overwriting
 * the premium state with an Unsubscribed result after network calls.
 */
@Suppress("unused")
val caseTrackerUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all premium features in Case Tracker — Immigration.",
    default = true
) {
    compatibleWith(CASETRACKER_COMPATIBILITY)

    execute {
        // ── Layer 1: SharedPrefs boolean getter ──────────────────────────────
        // data/c.a(String, boolean)Z — seeds StateFlow<z> on init.
        // Replace body: always return true.
        SharedPrefGetBooleanFingerprint
            .match(classDefBy(SharedPrefGetBooleanFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 2: isPro getter ─────────────────────────────────────────────
        // ads/x.a()Z — reads z.b (isPro) from StateFlow. Force true.
        IsProGetterFingerprint
            .match(classDefBy(IsProGetterFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 3: isSubscribed getter ──────────────────────────────────────
        // ads/x.c()Z — reads z.a (any paid tier). Force true.
        IsSubscribedGetterFingerprint
            .match(classDefBy(IsSubscribedGetterFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 4: refreshFromBackend coroutine ─────────────────────────────
        // ads/x.e(ContinuationImpl)Object — backend subscription refresh.
        // Return kotlin.Unit immediately — must use return-object (not return-void)
        // since the method signature returns Object, not void.
        RefreshFromBackendFingerprint
            .match(classDefBy(RefreshFromBackendFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    sget-object v0, Lkotlin/Unit;->a:Lkotlin/Unit;
                    return-object v0
                    """.trimIndent()
                )
            }
    }
}
