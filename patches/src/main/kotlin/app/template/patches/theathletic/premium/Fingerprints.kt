package app.template.patches.theathletic.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ---------------------------------------------------------------------------
// Subscription gate call chain (fully traced):
//
//   x1$q.invokeSuspend
//     → UserManager.t()
//       → SubAuth e.g()
//         → dw/a.i()  (interface)
//           → ax/a.i()
//             → ax/b.c()        reads field g:Z
//               ← ax/b.e()     sets g = UserData.hasActiveEntitlement(entitlement)
//                                   └─ calls hasLinkedActiveEntitlement() first
//
//   x1$q shows paywall when: j()=false AND t()=false AND W()=false
//   i.e. logged-in account with no active entitlement → paywall fires
//
// Root fix: patch hasActiveEntitlement() + hasLinkedActiveEntitlement() → true.
// Both are non-obfuscated NYT library classes — stable across app updates.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Target 1: UserData.hasActiveEntitlement(UserSubscriptionEntitlement) → true
//
// Class:  Lcom/nytimes/android/subauth/core/database/userdata/UserData;
//         (NON-OBFUSCATED — NYT library class)
// Method: hasActiveEntitlement(UserSubscriptionEntitlement)Z
//         (NON-OBFUSCATED — Kotlin function name preserved)
// DEX:    classes10
// Flags:  public final
//
// This is the primary subscription check. ax/b.e() calls this to set field g:Z,
// which flows up through ax/a.i() → e.g() → UserManager.t() → x1$q paywall gate.
// Returning true always makes every account look entitled regardless of server data.
//
// Smali (classes10/com/nytimes/android/.../UserData.smali):
//   .method public final hasActiveEntitlement(Lcom/.../UserSubscriptionEntitlement;)Z
//       const-string v0, "entitlement"
//       invoke-static {p1, v0}, Intrinsics;->checkNotNullParameter(...)V
//       invoke-virtual {p0,p1}, UserData;->hasLinkedActiveEntitlement(...)Z
//       ...
// ---------------------------------------------------------------------------
object HasActiveEntitlementFingerprint : Fingerprint(
    definingClass = "Lcom/nytimes/android/subauth/core/database/userdata/UserData;",
    name = "hasActiveEntitlement",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/nytimes/android/subauth/core/database/userdata/subscription/UserSubscriptionEntitlement;"),
)

// ---------------------------------------------------------------------------
// Target 2: UserData.hasLinkedActiveEntitlement(UserSubscriptionEntitlement) → true
//
// Same class, same flags. Called by hasActiveEntitlement() as its first check.
// Patching both is belt-and-suspenders: if hasLinkedActiveEntitlement returns true,
// hasActiveEntitlement returns early with true before reaching Google Play checks.
//
// Smali (classes10/com/nytimes/android/.../UserData.smali):
//   .method public final hasLinkedActiveEntitlement(Lcom/.../UserSubscriptionEntitlement;)Z
//       iterates subscriptions list, calls UserSubscription.hasActiveEntitlement()
// ---------------------------------------------------------------------------
object HasLinkedActiveEntitlementFingerprint : Fingerprint(
    definingClass = "Lcom/nytimes/android/subauth/core/database/userdata/UserData;",
    name = "hasLinkedActiveEntitlement",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/nytimes/android/subauth/core/database/userdata/subscription/UserSubscriptionEntitlement;"),
)

// ---------------------------------------------------------------------------
// Target 3: PaywallState.<init>(showPaywall: Z, isForced: Z) → force showPaywall=false
//
// Class:   Lcom/theathletic/article/ui/webview/m2;  (= PaywallState, obfuscated)
// Method:  <init>(ZZ)V
// DEX:     classes11
// Flags:   public constructor
//
// Belt-and-suspenders: x1$q.f(h1) hardcodes const/4 v0, 1 directly before calling
// the constructor. With p1 zeroed at entry, no PaywallState can have showPaywall=true
// regardless of what the caller passes.
//
// Fingerprint: classFingerprint anchors m2 via stable toString() literal.
// <init>(ZZ)V is unique — only other constructor is the no-arg <init>()V.
// ---------------------------------------------------------------------------
private val PaywallStateClassFingerprint = Fingerprint(
    strings = listOf("PaywallState(showPaywall="),
)

object PaywallStateConstructorFingerprint : Fingerprint(
    classFingerprint = PaywallStateClassFingerprint,
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Z", "Z"),
)

// ---------------------------------------------------------------------------
// Target 4: ArticleWebViewViewModel.k4() — paywall coroutine launch on page load
//
// Class:   Lcom/theathletic/article/ui/webview/x1;  (= ArticleWebViewViewModel)
// Method:  k4()V
// DEX:     classes11
// Flags:   public final
//
// Suppresses the t4() coroutine launch on article page load. Belt-and-suspenders
// in case entitlement patches don't cover all timing windows.
//
// Filter order verified (x1.smali):
//   line 15136: sget CONTENT_LOADED          ← filter[0]
//   line 15174: invoke-direct t4()V          ← nop target
//   line 15212: invoke-static Analytics.F()  ← filter[1]
// ---------------------------------------------------------------------------
object ArticleLoadPaywallTriggerFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lcom/theathletic/article/ui/v2;",
            name = "CONTENT_LOADED",
        ),
        methodCall(
            definingClass = "Lcom/theathletic/analytics/newarch/AnalyticsExtensionsKt;",
            name = "F",
        ),
    ),
)
