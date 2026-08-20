package app.template.patches.picsart

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PICSART_COMPATIBILITY
import app.template.patches.shared.returnBoxedBooleanEarly
import app.template.patches.shared.returnEarly
import app.template.patches.picsart.picsartDisableSignatureCheckPatch

/**
 * Unlocks PicsArt Studio premium (Gold / highest tier) client-side.
 *
 * Architecture (jadx, v30.5.5, classes6.dex):
 *
 *   SubscriptionRepoImpl.k()  → builds `myobfuscated.OM.a` (SubscriptionInfo data
 *                               class). Its FIRST field `a` (boolean) is THE
 *                               subscribed flag.
 *   isSubscribed()            → SubscriptionInfoUseCaseImpl$isSubscribed$2
 *                               invokeSuspend → Boolean.valueOf(k().a)
 *   SubscriptionState.o()     → SubscriptionRepoImpl.o() → k().a   (sync editor gate)
 *   Tier premium gate         → GetUserSubscriptionTiersUseCaseImpl.b()
 *                               → (k().a && tier==OLD_GOLD) ? false : true
 *   PremiumBadge / promo /    → read k().a directly
 *   offer resolvers
 *
 * Strategy (root + backstops):
 *   1. Patch the OM.a constructor so `this.a = true` — every SubscriptionInfo
 *      instance anywhere reports subscribed=true. Covers ALL k().a readers
 *      (badge, promos, offers, tiers, isSubscribed, o(), h()).
 *      p1 is the first constructor param (boolean z → this.a = z); injecting
 *      `const/4 p1, 0x1` at offset 0 is type-safe (boolean register ← int 1).
 *   2. Backstop async gate: isSubscribed$2.invokeSuspend → boxed TRUE.
 *   3. Backstop tier gate: GetUserSubscriptionTiersUseCaseImpl.b() → true.
 *
 * Server-side features (AI tools, cloud render) will still fail without a real
 * subscription — this unlocks the local feature set (tools, effects, no paywall
 * prompts) as requested.
 */
@Suppress("unused")
val picsartUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks PicsArt premium locally: forces the subscription state to subscribed " +
        "at the data-class root so tools/effects unlock and upgrade prompts are skipped.",
) {
    compatibleWith(PICSART_COMPATIBILITY)
    dependsOn(picsartDisableSignatureCheckPatch)

    execute {
        // ROOT: OM.a.<init> → this.a = true (subscribed flag)
        SubscriptionInfoCtorFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x1
            """.trimIndent(),
        )

        // BACKSTOP: async isSubscribed() coroutine → TRUE
        IsSubscribedCoroutineFingerprint.method.returnBoxedBooleanEarly(true)

        // BACKSTOP: tier premium gate → true
        TiersPremiumGateFingerprint.method.returnEarly(true)

        // BACKSTOP: tier-type predicates → true (PRO/MAX/LITE/ULTRA, OLD_GOLD, PLUS).
        // The server supplies a FREE tier in info.k.b, so the ctor patch alone leaves
        // these false → the "Try PRO" pill still shows and tier-gated features stay
        // locked. Forcing all three hides the upsell badge and unlocks every tier path.
        TierTypeProFingerprint.method.returnEarly(true)
        TierTypeGoldFingerprint.method.returnEarly(true)
        TierTypePlusFingerprint.method.returnEarly(true)
    }
}
