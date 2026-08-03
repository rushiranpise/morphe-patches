package app.template.patches.theathletic.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ---------------------------------------------------------------------------
// Subscription gate call chain (fully traced, 13.144.0):
//
//   b2$f0.invokeSuspend (coroutine launched by b2.x4(), itself called from b2.o4())
//     → UserManager.t()
//       → SubAuth e.g()
//         → dw/a.i()  (interface)
//           → ax/a.i()
//             → ax/b.c()         reads field g:Z
//               ← ax/b.e()      sets g = UserData.hasActiveEntitlement(entitlement)
//                                    └─ calls hasLinkedActiveEntitlement() first
//
//   Paywall fires when: j()=false AND t()=false AND W()=false
//   i.e. logged-in account with no active entitlement → PaywallState(showPaywall=true)
//
// Root fix: patch hasActiveEntitlement() + hasLinkedActiveEntitlement() → true.
// Both live in the stable, non-obfuscated NYT library class — safe across updates.
//
// Obfuscated names that changed from 13.141.0 → 13.144.0:
//   PaywallState class:           m2  → q2
//   CONTENT_LOADED enum class:    v2  → e2
//   ArticleWebViewViewModel:      x1  → b2
//   Paywall coroutine method:     t4  → x4
// Fingerprints updated accordingly. Targets 1+2 (NYT library) are unchanged.
//
// ---------------------------------------------------------------------------
// Ad suppression (13.144.0):
//
//   AthleticApplication checks f30/a.o0() (ADS_ENABLED Firebase RC flag), then:
//   1. ads/component/h.b(Context)V → coroutine h$a.invokeSuspend()
//        → MobileAds.initialize() → all GAM banner/native/interstitial ad slots
//   2. nytplatform/ads/malice/b.a()V → Malice (MOAT viewability) coroutine
//        → impression-measurement SDK init
//
//   Surface flags (ADS_ON_ARTICLE, ADS_ON_LIVE_BLOG, etc.) then gate per-screen
//   ad slot loading via f30/a interface → l70/g → Firebase Remote Config.
//
//   Patch: returnEarly() on both coroutine entry points. Since MobileAds.initialize()
//   is never called, no ad slot on any surface can register or fill.
// ---------------------------------------------------------------------------

// ── Paywall fingerprints ────────────────────────────────────────────────────

// Target 1: UserData.hasActiveEntitlement(UserSubscriptionEntitlement) → true
//
// Class:  Lcom/nytimes/android/subauth/core/database/userdata/UserData;
//         (NON-OBFUSCATED — NYT SubAuth library)
// DEX:    classes10
object HasActiveEntitlementFingerprint : Fingerprint(
    definingClass = "Lcom/nytimes/android/subauth/core/database/userdata/UserData;",
    name = "hasActiveEntitlement",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/nytimes/android/subauth/core/database/userdata/subscription/UserSubscriptionEntitlement;"),
)

// Target 2: UserData.hasLinkedActiveEntitlement(UserSubscriptionEntitlement) → true
//
// Belt-and-suspenders: called first inside hasActiveEntitlement().
object HasLinkedActiveEntitlementFingerprint : Fingerprint(
    definingClass = "Lcom/nytimes/android/subauth/core/database/userdata/UserData;",
    name = "hasLinkedActiveEntitlement",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/nytimes/android/subauth/core/database/userdata/subscription/UserSubscriptionEntitlement;"),
)

// Target 3: PaywallState.<init>(showPaywall: Z, isForced: Z) → force showPaywall=false
//
// Class:   Lcom/theathletic/article/ui/webview/q2;  (= PaywallState, was m2 in 13.141.0)
// Anchored via stable toString() literal from Kotlin data class compiler.
// DEX:     classes11
private val PaywallStateClassFingerprint = Fingerprint(
    strings = listOf("PaywallState(showPaywall="),
)

object PaywallStateConstructorFingerprint : Fingerprint(
    classFingerprint = PaywallStateClassFingerprint,
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Z", "Z"),
)

// Target 4: ArticleWebViewViewModel.o4() — paywall coroutine launch on page load
//
// Class:   Lcom/theathletic/article/ui/webview/b2;  (= ArticleWebViewViewModel, was x1)
// Method:  o4()V  (was k4()V in 13.141.0)
// DEX:     classes11
//
// Filter order verified against b2.smali:
//   sget-object …, Lcom/theathletic/article/ui/e2;->CONTENT_LOADED  ← [0]
//   invoke-static …, AnalyticsExtensionsKt;->F(…)V                   ← [1]
//   (invoke-direct {p0}, b2;->x4()V sits between the two filters)
//
// CONTENT_LOADED enum class: v2 → e2 in 13.144.0.
object ArticleLoadPaywallTriggerFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lcom/theathletic/article/ui/e2;",
            name = "CONTENT_LOADED",
        ),
        methodCall(
            definingClass = "Lcom/theathletic/analytics/newarch/AnalyticsExtensionsKt;",
            name = "F",
        ),
    ),
)

// ── Ad suppression fingerprints ─────────────────────────────────────────────

// Target 5: Article initial ad config coroutine — initAdConfig$1.invokeSuspend(Object)Object
//
// Class:   Lcom/theathletic/article/ui/webview/b2$i;
//          (= ArticleWebViewViewModel$initAdConfig$1)
// Method:  invokeSuspend(Object)Object
// DEX:     classes11
//
// Launched when an article is opened; initialises AdScrollBehaviorImpl and registers
// the initial ad slot with GAM. This is the root of ALL article ads — scroll-triggered
// ads (Target 6) are registered here first. Suppressing this coroutine prevents both
// the initial article ad and all subsequent scroll-triggered ads from being set up,
// making Target 6 a belt-and-suspenders companion rather than the primary fix.
//
// Fingerprint: methodCall(article/component/a → a(String,String,I,I,lg0/c)Object) is
// the AdComponent.initAdSlot() interface call — the only invokeSuspend in the article
// package that calls this stable interface. The non-obfuscated package path
// Lcom/theathletic/article/component/a; (NYT article component lib) is stable.
// No obfuscated class/method names in the fingerprint.
internal val ArticleInitAdConfigFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE_RANGE,
            definingClass = "Lcom/theathletic/article/component/a;",
            name = "a",
        ),
    ),
)

// Target 6: Article WebView ad loader — AdScrollBehaviorImpl.onScrolled(Int)V
//
// Class:   Lcom/theathletic/ads/ui/articles/a;  (= AdScrollBehaviorImpl)
// Method:  a(I)V  — the only public (I)V on the class
// DEX:     classes10
//
// Called on article scroll events; calls WebView.loadUrl() to inject an ad into
// the article body. Independent of MobileAds SDK — uses the NYT ad bridge instead.
// This is why article ads survive the MobileAds init suppression (target 7).
//
// Fingerprint: methodCall(WebView.loadUrl) is present only in this method among
// all public (I)V methods in classes10. Additionally anchored by the ordered pair:
//   [AtomicBoolean.get(), WebView.loadUrl()] — the state-guard check before the URL load.
// No obfuscated names used. Both android.webkit.WebView and java.util.concurrent
// are stable Android framework/JDK classes, never obfuscated.
internal val ArticleWebViewAdLoaderFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("I"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/concurrent/atomic/AtomicBoolean;",
            name = "get",
        ),
        methodCall(
            definingClass = "Landroid/webkit/WebView;",
            name = "loadUrl",
        ),
    ),
)

// Target 6: Feed ad slot composable — DropzoneUi.b(DropzoneUiModel, Composer, Int)V
//
// Class:   Lcom/theathletic/feed/ui/items/c0;  (= DropzoneUiKt)
// Method:  b(d0, m, I)V  — the public static Compose entry point
// DEX:     classes13
//
// Renders the grey "ADVERTISEMENT" placeholder card in the home/discover/team/league
// feeds. Returning void suppresses the entire slot container, eliminating both
// the blank space and the ADVERTISEMENT label rather than just leaving an empty gap.
//
// Anchored by string("com.theathletic.feed.ui.items.DropzoneUi (DropzoneUi.kt:17)")
// — a Compose framework debug tag emitted verbatim by the Kotlin Compose compiler.
// This string is unique to the DropzoneUi composable and stable across obfuscation.
internal val FeedAdSlotFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("com.theathletic.feed.ui.items.DropzoneUi (DropzoneUi.kt:17)"),
)

// Target 7: AdMob / GAM init coroutine — h$a.invokeSuspend(Object)Object
//
// Class:   ads/component/h$a (obfuscated inner lambda of the AdManager)
// DEX:     classes10
//
// Anchored via methodCall(MobileAds) — the stable Google Mobile Ads SDK class
// is never obfuscated by R8. This is the only invokeSuspend(Object)Object method
// in the entire app that calls any MobileAds method, confirmed by cross-referencing
// all three MobileAds callers (s00/a and ads/ui/c call MobileAds.c/a but have no
// invokeSuspend; only h$a.invokeSuspend calls MobileAds.b).
//
// Nopping invokeSuspend prevents MobileAds.initialize() from running, so no
// ad slot on any surface (article, liveblog, hub, feeds) can register or fill.
internal val AdsInitCoroutineFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/google/android/gms/ads/MobileAds;"),
    ),
)

// Target 8: Malice (MOAT viewability) init — nytplatform/ads/malice/b.a()V
//
// Class:   Lcom/theathletic/nytplatform/ads/malice/b;  (NON-OBFUSCATED — NYT platform lib)
// DEX:     classes15
//
// The class path is stable (NYT platform library, never R8-obfuscated).
// a()V is the only public ()V method on the class — uniquely identified by
// definingClass + returnType + accessFlags + parameters alone. No string filter
// needed or possible: a()V contains no const-string instructions (strings like
// "alsRepository" are in <init>, not a()).
// Nopping a()V suppresses impression-measurement pings to the MOAT SDK.
internal val MaliceAdsInitFingerprint = Fingerprint(
    definingClass = "Lcom/theathletic/nytplatform/ads/malice/b;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)
