package app.template.patches.theathletic.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.THE_ATHLETIC_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.Instruction

// The Athletic 13.144.0 — combined premium + ad suppression patch.
//
// ── Subscription gate (paywall bypass) ──────────────────────────────────────
//
//   b2.o4() (ArticleWebViewViewModel, called on article page load)
//     → launches b2.x4() coroutine (b2$f0.invokeSuspend)
//       checks: j() = isAnonymous  → true: emit different event, no paywall
//       checks: t() = isEntitled   → false: check W() feature flag
//         checks: W()              → false: construct PaywallState(showPaywall=true)
//
//   t() = UserManager → SubAuth e.g() → dw/a.i() → ax/a.i() → ax/b.c()
//   ax/b.c() reads field g:Z, populated by:
//     ax/b.e() = UserData.hasActiveEntitlement(entitlement)
//               → hasLinkedActiveEntitlement() (subscription list check)
//               → Play Billing entitlement check
//
//   Four-layer strategy:
//     1+2. UserData.hasActiveEntitlement() + hasLinkedActiveEntitlement() → true  [PRIMARY]
//          Non-obfuscated NYT library — stable across all versions.
//     3.   PaywallState.<init>(ZZ)V — zero p1 (showPaywall) at entry              [BELT]
//     4.   b2.o4() — nop the invoke-direct to x4() (paywall coroutine)            [SUSPENDERS]
//
// ── Ad suppression ──────────────────────────────────────────────────────────
//
//   Two independent ad systems require four separate patch points:
//
//   GAM/AdMob (SDK-based ads):
//     7. ads/component/h$a.invokeSuspend() → MobileAds.initialize()
//          → all native/interstitial ads on article, liveblog, feed surfaces
//     8. nytplatform/ads/malice/b.a()V → Malice (MOAT) viewability coroutine
//
//   NYT ad bridge (WebView-based ads, independent of MobileAds SDK):
//     5. ads/ui/articles/a.a(I)V (AdScrollBehaviorImpl.onScrolled)
//          → WebView.loadUrl() → article body inline ad injection
//     6. feed/ui/items/c0.b(d0, m, I)V (DropzoneUi composable)
//          → ADVERTISEMENT container card in home/discover/team/league feeds

@Suppress("unused")
val theAthleticPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Bypasses The Athletic paywall and removes all ads.",
) {
    compatibleWith(THE_ATHLETIC_COMPATIBILITY)

    execute {
        // ── Paywall bypass ───────────────────────────────────────────────────

        // Targets 1 + 2: UserData entitlement checks → always true.
        HasActiveEntitlementFingerprint.method.returnEarly(true)
        HasLinkedActiveEntitlementFingerprint.method.returnEarly(true)

        // Target 3: Force showPaywall=false in PaywallState constructor (belt).
        // q2.<init>(ZZ)V: p1=showPaywall, p2=isForced.
        PaywallStateConstructorFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x0",
        )

        // Target 4: Nop the invoke-direct to x4() in b2.o4() (suspenders).
        // Walk backwards from AnalyticsExtensionsKt.F() (filter[1]) to find
        // the nearest preceding INVOKE_DIRECT — that is x4().
        val triggerMethod = ArticleLoadPaywallTriggerFingerprint.method
        val analyticsIdx = ArticleLoadPaywallTriggerFingerprint.instructionMatches[1].index

        var x4Idx = analyticsIdx - 1
        while (x4Idx >= 0) {
            if (triggerMethod.getInstruction<Instruction>(x4Idx).opcode == Opcode.INVOKE_DIRECT) break
            x4Idx--
        }
        require(x4Idx >= 0) { "Could not find paywall coroutine invoke-direct in o4()" }
        triggerMethod.replaceInstruction(x4Idx, "nop")

        // ── Ad suppression ────────────────────────────────────────────────────

        // Target 5: Article ad init coroutine (initAdConfig$1.invokeSuspend).
        // Launched on article open; initializes AdScrollBehaviorImpl and registers
        // the initial GAM ad slot. Suppressing this coroutine prevents ALL article ads —
        // both the initial above-the-fold ad and all subsequent scroll-triggered ads.
        // invokeSuspend returns Object — must use returnEarly(null), not returnEarly().
        ArticleInitAdConfigFingerprint.method.returnEarly(null)

        // Target 6: Article WebView scroll-triggered ad loader (AdScrollBehaviorImpl).
        // Belt-and-suspenders companion to Target 5 — prevents any residual scroll-based
        // ad loads if the init coroutine somehow still executes.
        ArticleWebViewAdLoaderFingerprint.method.returnEarly()

        // Target 7: Feed ad slot composable (DropzoneUi).
        // Renders the "ADVERTISEMENT" container card in home/discover/team/league feeds.
        // Returning void suppresses the entire slot view, removing both the blank space
        // and the ADVERTISEMENT label. Anchored by the stable Compose debug tag string.
        FeedAdSlotFingerprint.method.returnEarly()

        // Target 8: AdMob / GAM init coroutine.
        // h$a.invokeSuspend() — the only invokeSuspend(Object)Object calling MobileAds.
        // Returning null (Object return type) prevents SDK init; no ad slots can register.
        AdsInitCoroutineFingerprint.method.returnEarly(null)

        // Target 8: Malice (MOAT viewability) init.
        // nytplatform/ads/malice/b.a()V — non-obfuscated NYT platform class.
        // Returning early suppresses impression-measurement pings.
        MaliceAdsInitFingerprint.method.returnEarly()
    }
}
