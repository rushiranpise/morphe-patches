package app.template.patches.parcels.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.PARCELS_COMPATIBILITY

/**
 * Unlocks Parcels — Package Tracker (com.brightstripe.parcels v3.0.10).
 *
 * React Native app with a plain (non-Hermes) JS bundle.
 * All subscription logic is in JS — no native billing bridge to patch.
 *
 * isSubscriptionActive (f) reads AsyncStorage key 'Settings:subscribed',
 * returns true only if value === '1'. Patched to always resolve true.
 *
 * subscriptionExpired sets 'Settings:subscribed' to '0' when billing
 * detects no active purchase. Patched to no-op so billing cannot
 * deactivate the subscription.
 */
@Suppress("unused")
val parcelsUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlock premium features.",
    default = true,
) {
    compatibleWith(PARCELS_COMPATIBILITY)

    execute {
        val bundleFile = get("assets/index.android.bundle")
            ?: error("index.android.bundle not found")

        var bundle = bundleFile.readText()

        // ── Patch 1: isSubscriptionActive ────────────────────────────────────
        // Original: reads AsyncStorage 'Settings:subscribed', returns 1===parseInt(value)
        // Patched:  always resolves true
        val oldF = "function f(){var t;return s.default.async(function(n){for(;;)switch(n.prev=n.next)" +
            "{case 0:return n.prev=0,n.next=3,s.default.awrap(c.AsyncStorage.getItem('Settings:subscribed'))" +
            ";case 3:return t=n.sent,n.abrupt(\"return\",1===parseInt(t));case 7:n.prev=7,n.t0=n.catch(0)" +
            ",console.log(n.t0);case 10:case\"end\":return n.stop()}},null,null,[[0,7]])}"
        val newF = "function f(){return Promise.resolve(!0)}"
        check(bundle.contains(oldF)) { "isSubscriptionActive (f) not found — bundle may have changed" }
        bundle = bundle.replace(oldF, newF)

        // ── Patch 2: subscriptionExpired ─────────────────────────────────────
        // Original: clears 'Settings:subscribed', 'Settings:push', 'Settings:pushToken'
        // Patched:  no-op so billing cannot deactivate premium
        val oldExp = "subscriptionExpired:function(){return s.default.async(function(t){for(;;)switch(t.prev=t.next)" +
            "{case 0:return t.prev=0,t.next=3,s.default.awrap(c.AsyncStorage.setItem('Settings:subscribed','0'))" +
            ";case 3:return t.next=5,s.default.awrap(c.AsyncStorage.setItem('Settings:push','0'))" +
            ";case 5:return t.next=7,s.default.awrap(c.AsyncStorage.setItem('Settings:pushToken',''))" +
            ";case 7:t.next=12;break;case 9:t.prev=9,t.t0=t.catch(0),console.log(t.t0)" +
            ";case 12:case\"end\":return t.stop()}},null,null,[[0,9]])}"
        val newExp = "subscriptionExpired:function(){return Promise.resolve(!0)}"
        check(bundle.contains(oldExp)) { "subscriptionExpired not found — bundle may have changed" }
        bundle = bundle.replace(oldExp, newExp)

        bundleFile.writeText(bundle)
    }
}
