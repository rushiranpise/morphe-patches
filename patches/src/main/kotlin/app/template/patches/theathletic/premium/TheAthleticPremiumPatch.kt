package app.template.patches.theathletic.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.THE_ATHLETIC_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode

// The Athletic 13.141.0 — native Kotlin/Java, owned by NYT.
//
// Subscription gate (fully traced):
//
//   x1$q.invokeSuspend (P3 coroutine, triggered on article open)
//     checks: j()=isAnonymous  → if TRUE: emit different event (not paywall)
//     checks: t()=isEntitled   → if FALSE: check W() feature flag
//       checks: W()            → if FALSE: call x1$q.f(h1)
//         x1$q.f(h1): constructs PaywallState(showPaywall=true) → overlay shown
//
//   t() = UserManager → SubAuth e.g() → dw/a.i() → ax/a.i() → ax/b.c()
//   ax/b.c() reads field g:Z, set by:
//     ax/b.e() = UserData.hasActiveEntitlement(UserSubscriptionEntitlement)
//       → calls hasLinkedActiveEntitlement() first (checks subscription list)
//       → falls through to Google Play entitlement check
//
// Patch strategy — four layers:
//
//   1+2. UserData.hasActiveEntitlement() + hasLinkedActiveEntitlement() → true  [PRIMARY]
//        Non-obfuscated NYT library classes. Returning true makes ax/b.g=true,
//        which flows up to UserManager.t()=true, causing x1$q to exit at :L3
//        without ever constructing the PaywallState.
//        DEX: classes10 · class: UserData · methods: hasActiveEntitlement,
//             hasLinkedActiveEntitlement
//
//   3. PaywallState.<init>(ZZ)V — force showPaywall=false  [BELT]
//      x1$q.f(h1) hardcodes const/4 v0, 1 before the constructor.
//      Zeroing p1 at entry ensures no PaywallState can have showPaywall=true.
//      DEX: classes11 · class: m2 (PaywallState)
//
//   4. k4() — nop the showArticlePaywall coroutine launch  [SUSPENDERS]
//      Suppresses the t4() launch on article page load.
//      DEX: classes11 · class: x1 (ArticleWebViewViewModel)

@Suppress("unused")
val theAthleticPremiumPatch = bytecodePatch(
    name = "The Athletic Premium",
    description = "Bypasses The Athletic article paywall by spoofing the NYT subscription entitlement check.",
) {
    compatibleWith(THE_ATHLETIC_COMPATIBILITY)

    execute {
        // ── Targets 1 + 2 ───────────────────────────────────────────────────
        // UserData entitlement checks → always return true.
        // These are the root of the subscription gate chain.
        HasActiveEntitlementFingerprint.method.returnEarly(true)
        HasLinkedActiveEntitlementFingerprint.method.returnEarly(true)

        // ── Target 3 ────────────────────────────────────────────────────────
        // Force showPaywall=false in PaywallState constructor (belt).
        PaywallStateConstructorFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x0",
        )

        // ── Target 4 ────────────────────────────────────────────────────────
        // Nop the invoke-direct t4() in k4() (suspenders).
        // Walk backwards from AnalyticsExtensionsKt.F() call (filter[1])
        // to find the preceding INVOKE_DIRECT — that is t4().
        val triggerMethod = ArticleLoadPaywallTriggerFingerprint.method
        val analyticsIdx = ArticleLoadPaywallTriggerFingerprint.instructionMatches[1].index

        var t4Idx = analyticsIdx - 1
        while (t4Idx >= 0) {
            if (triggerMethod.getInstruction<com.android.tools.smali.dexlib2.iface.instruction.Instruction>(t4Idx)
                    .opcode == Opcode.INVOKE_DIRECT
            ) break
            t4Idx--
        }
        triggerMethod.replaceInstruction(t4Idx, "nop")
    }
}
