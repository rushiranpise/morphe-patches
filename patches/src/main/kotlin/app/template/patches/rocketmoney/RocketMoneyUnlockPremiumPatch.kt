package app.template.patches.rocketmoney

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ROCKETMONEY_COMPATIBILITY

/**
 * Unlocks Rocket Money Premium in com.rocketmoney.app.
 *
 * Rocket Money is a React Native (Expo) app with a GraphQL backend (Apollo).
 * Premium status flows from the server -> Apollo cache -> JS bundle -> native
 * bridge. A single-layer JVM patch is insufficient; we apply two layers:
 *
 * ## Architecture overview
 *
 * ```
 * Server (GraphQL)
 *     └─> OkHttp response (JSON body)
 *             └─> [LAYER 1] RocketMoneyGraphQLSpoofInterceptor rewrites
 *                           isPremium / premiumTier / subscriptionStatus
 *             └─> Apollo cache
 *                     └─> JS bundle reads premium fields
 *                             └─> React Native UI unlocked
 *
 * Native Android gating (splash / onboarding / budgeting screens)
 *     └─> UserSession.isPremium()              [LAYER 2a]
 *     └─> UserSession.getSubscriptionStatus()   [LAYER 2b]
 *     └─> PremiumGateHelper.isPremiumUser()     [LAYER 2c]
 * ```
 *
 * ## Layer 1 — GraphQL response spoofing (OkHttp interceptor injection)
 *
 * We inject a new-instance of RocketMoneyGraphQLSpoofInterceptor into the
 * OkHttpClient builder used by Apollo, as the last network interceptor.
 * The interceptor rewrites `currentUser.isPremium`, `premiumTier`, and
 * `subscriptionStatus` in every GraphQL response body before Apollo
 * deserialises it into its in-memory normalized cache.
 *
 * This ensures the JS bundle always receives premium=true regardless of the
 * actual account subscription state.
 *
 * ## Layer 2 — JVM domain model patches
 *
 * Patches three native Android methods that gate premium UI independently
 * of the React Native bridge:
 *   a) UserSession.isPremium()            → always true
 *   b) UserSession.getSubscriptionStatus() → always "ACTIVE"
 *   c) PremiumGateHelper.isPremiumUser()   → always true
 *
 * Features unlocked:
 *   Subscription tracker  — UserSession.isPremium + GraphQL spoof
 *   Budget categories     — PremiumGateHelper.isPremiumUser
 *   Bill negotiation      — GraphQL spoof (server-gated)
 *   Credit score details  — GraphQL spoof (server-gated)
 *   Net worth tracking    — GraphQL spoof (server-gated)
 *   Smart savings         — GraphQL spoof (server-gated)
 *   Premium insights      — GraphQL spoof (server-gated)
 */
@Suppress("unused")
val rocketMoneyUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Rocket Money Premium features via GraphQL response spoofing and JVM gate patching.",
    default = true
) {
    compatibleWith(ROCKETMONEY_COMPATIBILITY)

    execute {

        // ── Layer 1: Inject GraphQL spoof interceptor into OkHttpClient ──────
        //
        // We try the primary NetworkModule fingerprint first, then fall back
        // to the ApolloModule fingerprint if the first one misses.
        //
        // Injection point: immediately before the OkHttpClient.Builder.build()
        // invocation (the last instruction before the return).
        //
        // Injected Dalvik:
        //   new-instance vN, Lapp/template/patches/rocketmoney/RocketMoneyGraphQLSpoofInterceptor;
        //   invoke-direct {vN}, Lapp/template/patches/rocketmoney/RocketMoneyGraphQLSpoofInterceptor;-><init>()V
        //   invoke-virtual {v0, vN}, Lokhttp3/OkHttpClient$Builder;->addNetworkInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient$Builder;
        val interceptorClass = RocketMoneyGraphQLSpoofInterceptor.CLASS_DESCRIPTOR
        val interceptorInit = "$interceptorClass-><init>()V"
        val addInterceptor =
            "Lokhttp3/OkHttpClient\$Builder;->addNetworkInterceptor(Lokhttp3/Interceptor;)Lokhttp3/OkHttpClient\$Builder;"

        val injectSmali = """
            new-instance v8, $interceptorClass
            invoke-direct {v8}, $interceptorInit
            invoke-virtual {v0, v8}, $addInterceptor
        """.trimIndent()

        // Primary fingerprint
        runCatching {
            RocketMoneyOkHttpBuilderFingerprint
                .match(classDefBy(RocketMoneyOkHttpBuilderFingerprint.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    val buildIdx = instructions.indexOfLast {
                        it.toString().contains("->build()")
                    }
                    if (buildIdx >= 0) addInstructions(buildIdx, injectSmali)
                }
        }.onFailure {
            // Fallback: ApolloModule
            runCatching {
                RocketMoneyApolloBuilderFingerprint
                    .match(classDefBy(RocketMoneyApolloBuilderFingerprint.definingClass!!))
                    .method
                    .apply {
                        if (implementation == null) return@apply
                        val buildIdx = instructions.indexOfLast {
                            it.toString().contains("->build()")
                        }
                        if (buildIdx >= 0) addInstructions(buildIdx, injectSmali)
                    }
            }
        }

        // ── Layer 2a: Force UserSession.isPremium() → true ───────────────────
        runCatching {
            RocketMoneyIsPremiumFingerprint
                .match(classDefBy(RocketMoneyIsPremiumFingerprint.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        // ── Layer 2b: Force UserSession.getSubscriptionStatus() → "ACTIVE" ───
        runCatching {
            RocketMoneySubscriptionStatusFingerprint
                .match(classDefBy(RocketMoneySubscriptionStatusFingerprint.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(
                        0,
                        "const-string v0, \"ACTIVE\"\nreturn-object v0"
                    )
                }
        }

        // ── Layer 2c: Force PremiumGateHelper.isPremiumUser() → true ─────────
        runCatching {
            RocketMoneyPremiumGateFingerprint
                .match(classDefBy(RocketMoneyPremiumGateFingerprint.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }
    }
}
