package app.template.patches.casetracker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CASETRACKER_COMPATIBILITY

/**
 * Unlocks all premium features in Case Tracker — Immigration (com.saldous.casetracker).
 *
 * This patch combines three independent layers to ensure premium access
 * survives every code path — initial load, billing callbacks, and SDK
 * subscription state updates.
 *
 * ## Subscription Tiers
 *
 * | Tier   | Field | SharedPrefs Key              | Vendor IDs                    |
 * |--------|-------|------------------------------|-------------------------------|
 * | Remove Ads | c.z0 | HAS_PURCHASED_REMOVE_ADS | MP199T0_ads, LP499T0_ads      |
 * | Pro    | c.A0  | HAS_PURCHASED_PRO_PLAN   | *pro* (contains match)        |
 * | Plus   | c.B0  | HAS_PURCHASED_PLUS_PLAN  | *plus* (contains match)       |
 *
 * ## Layer 1 — SharedPrefs getter (c.a)
 *
 * `c.a(String key, boolean default): Z` is the sole helper used by
 * DataManager’s init coroutine to populate z0, A0 and B0 from
 * SharedPreferences on every app launch.  Replacing its body with
 * `const/4 true; return` forces all three flags true from the first frame.
 *
 * ## Layer 2 — Billing callback writer (ads/w.a)
 *
 * `ads/w.a(v, String, int): V` is the static handler that writes purchase
 * results back to the DataManager statics and SharedPreferences when a
 * billing event fires (purchase, restore, or Unsubscribed).  We prepend
 * a block that sput-boolean true into all three statics and return-void
 * immediately so no billing event can ever reset the flags to false.
 *
 * ## Layer 3 — RevenewSDK subscription state observer
 *
 * `RevenewSDK.updateSubscriptionState(List): V` is called by the custom
 * RevenewSDK whenever Google Play Billing returns a new subscription list.
 * The SDK emits either `Subscribed` or `Unsubscribed` into the
 * `subscriptionObservable` StateFlow that UI layers observe for paywall
 * decisions.  A timeout-recovery coroutine also calls this method as a
 * fallback if billing is slow.  Prepending `return-void` keeps the
 * StateFlow permanently in its initial `Loading` state, so no paywall
 * is ever shown via the reactive path.
 */
@Suppress("unused")
val caseTrackerUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks all premium features and removes ads in Case Tracker — Immigration.",
    default = true
) {
    compatibleWith(CASETRACKER_COMPATIBILITY)

    execute {
        // ── Layer 1: SharedPrefs boolean getter ──────────────────────────────
        // c.a(String, boolean)Z — used by DataManager init to populate z0, A0, B0.
        // Replace body entirely: always return true.
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

        // ── Layer 2: Billing callback writer ─────────────────────────────────
        // ads/w.a(v, String, int)V — billing event handler.
        // Prepend: sput true into all three statics and return-void immediately.
        AdsWriterFingerprint
            .match(classDefBy(AdsWriterFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->z0:Z
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->A0:Z
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->B0:Z
                    return-void
                    """.trimIndent()
                )
            }

        // ── Layer 3: RevenewSDK subscription state observer ──────────────────
        // RevenewSDK.updateSubscriptionState(List)V — called on every
        // billing result and on timeout recovery.  Prepend return-void so
        // the subscriptionObservable StateFlow stays at Loading permanently
        // and no Unsubscribed paywall event is ever emitted to the UI.
        RevenewUpdateStateFingerprint
            .match(classDefBy(RevenewUpdateStateFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
    }
}