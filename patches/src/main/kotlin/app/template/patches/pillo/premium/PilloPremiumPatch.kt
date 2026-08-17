package app.template.patches.pillo.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PILLO_COMPATIBILITY

// ── Why the prior isActive() approach failed ──────────────────────────────────
//
// The previous patch targeted AdaptyProfile$AccessLevel.isActive(). This is
// unreachable for free users: the caller first does ImmutableMap.get("premium")
// and guards with `if-eqz v0, :skip_isActive`. A user with no subscription has
// no AccessLevel object in the map — get() returns null, the guard fires, and
// isActive() is never called. The false value is passed directly downstream.
//
// ── Correct model ─────────────────────────────────────────────────────────────
//
// Pillo subscription pipeline:
//
//   Adapty.setOnProfileUpdatedListener { profile ->           (or getProfile)
//     listenProfileUpdate$lambda$3(profile)
//       val premiumLevel = profile.getAccessLevels().get("premium")  ← null for free user
//       val isPremium    = premiumLevel != null && premiumLevel.isActive()
//       setIsPremiumState(isPremium)          ← false for free user
//         // coroutine lambda captures isPremium (false)
//         mIsPremium.emit(false)              ← StateFlow → all observers see false
//         preferences.setLastIsPremiumSubscriptionState(false)  ← persisted
//
// Fix: intercept setIsPremiumState(Z) and setIsAdfreeState(Z) at their entry
// points and override the incoming parameter to true before it reaches the
// lambda constructor. This makes every update — from Adapty, restore-purchase,
// or any other code path — always store and emit true.
//
// The injection `const/4 p1, 0x1` at index 0 is safe:
//   - p1 is a parameter register, not a local; overwriting it before use is valid.
//   - The first instruction that reads p1 is the invoke-direct at index 3
//     (after iget-object v0, new-instance v1, const/4 v2). Injecting at 0
//     means p1 is overwritten before any instruction reads it.
//
@Suppress("unused")
val pilloPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features and removes ads by overriding the subscription state setters in pillo.",
) {
    compatibleWith(PILLO_COMPATIBILITY)

    execute {
        // Force setIsPremiumState to always store / emit true.
        // Injecting at index 0 overrides p1 before it reaches the lambda constructor.
        SetIsPremiumStateFingerprint.method.addInstructions(0, "const/4 p1, 0x1")

        // Force setIsAdfreeState to always store / emit true.
        // Controls the ad-removal gate (mIsAdfree StateFlow).
        SetIsAdfreeStateFingerprint.method.addInstructions(0, "const/4 p1, 0x1")
    }
}
