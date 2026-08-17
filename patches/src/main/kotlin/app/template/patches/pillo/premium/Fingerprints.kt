package app.template.patches.pillo.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── SetIsPremiumStateFingerprint ──────────────────────────────────────────────
//
// Targets SubscriptionStateProvider.setIsPremiumState(Z) — the private method
// that receives the resolved premium boolean from Adapty and stores it into the
// mIsPremium MutableStateFlow (which all feature gates observe).
//
// Why NOT AdaptyProfile$AccessLevel.isActive() (prior attempt):
//   isActive() is only reached when ImmutableMap.get("premium") returns a
//   non-null AccessLevel object. For a free user with no subscription, the map
//   returns null and the null-check branch skips isActive() entirely, passing
//   false directly to setIsPremiumState(false). So patching isActive() has
//   zero effect for the target user.
//
// Patch strategy — force p1 = true at method entry:
//   setIsPremiumState(Z) stores p1 into the coroutine lambda constructor:
//     new-instance v1, ...setIsPremiumState$1
//     invoke-direct {v1, p0, p1, v2}, ...setIsPremiumState$1;-><init>(SSP, Z, Continuation)
//   Then the lambda emits p1 into mIsPremium StateFlow and persists it to Preferences.
//   Injecting `const/4 p1, 0x1` at index 0 overrides the incoming false with true
//   before it reaches the lambda, making every call behave as if premium is active.
//
// Fingerprint stability:
//   definingClass: SubscriptionStateProvider is a Hilt-injected singleton —
//     Hilt DI requires stable class references; R8 cannot rename it.
//   returnType "Lkotlinx/coroutines/Job;" + parameters ["Z"] + PRIVATE FINAL:
//     exact method signature, distinguishes from setIsAdfreeState.
//   filter: <init> of inner class SubscriptionStateProvider$setIsPremiumState$1 —
//     inner class name is preserved by R8 because Kotlin DebugMetadata annotation
//     embeds the enclosing method name as a string at runtime (verified in smali).
//
// Smali evidence (0.6.18 / classes6 / SubscriptionStateProvider.smali:641):
//   .method private final setIsPremiumState(Z)Lkotlinx/coroutines/Job;
//     .registers 8
//     iget-object v0, p0, ...->mainScope:Lkotlinx/coroutines/CoroutineScope;
//     new-instance v1, L.../SubscriptionStateProvider$setIsPremiumState$1;
//     const/4 v2, 0
//     invoke-direct {v1, p0, p1, v2}, L...setIsPremiumState$1;-><init>(L...;ZLkotlin/coroutines/Continuation;)V
//     ...BuildersKt.launch$default...
//   .end method
//
object SetIsPremiumStateFingerprint : Fingerprint(
    definingClass = "Lxyz/rtrvr/pillo/subscription/SubscriptionStateProvider;",
    returnType = "Lkotlinx/coroutines/Job;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(
            definingClass = "Lxyz/rtrvr/pillo/subscription/SubscriptionStateProvider\$setIsPremiumState\$1;",
            name = "<init>",
        ),
    ),
)

// ── SetIsAdfreeStateFingerprint ───────────────────────────────────────────────
//
// Mirrors SetIsPremiumStateFingerprint for the "adfree" access level.
// Controls the mIsAdfree StateFlow — consulted by AdmobExposeChecker and
// AdWarnProvider to decide whether to display ads.
//
// Smali evidence (0.6.18 / classes6 / SubscriptionStateProvider.smali:624):
//   .method private final setIsAdfreeState(Z)Lkotlinx/coroutines/Job;
//     identical structure; inner class is $setIsAdfreeState$1 (distinguishing filter)
//
object SetIsAdfreeStateFingerprint : Fingerprint(
    definingClass = "Lxyz/rtrvr/pillo/subscription/SubscriptionStateProvider;",
    returnType = "Lkotlinx/coroutines/Job;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(
            definingClass = "Lxyz/rtrvr/pillo/subscription/SubscriptionStateProvider\$setIsAdfreeState\$1;",
            name = "<init>",
        ),
    ),
)
