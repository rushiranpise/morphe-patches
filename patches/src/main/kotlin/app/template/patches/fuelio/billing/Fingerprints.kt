package app.template.patches.fuelio.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPremiumFingerprint ──────────────────────────────────────────────────────
//
// Targets: ProFeatureManager.b()Z  [classes3.dex]
//
// Synchronous isPremium() gate. Reads MutableStateFlow<Boolean> `d` via
// getValue() + Boolean.booleanValue(). Gates all in-app feature access.
//
// definingClass non-obfuscated — stable across versions.
// Sole public final ()Z method on this class.
//
// Smali instruction order:
//   invoke-interface  MutableStateFlow;->getValue()      ← filter[0]
//   invoke-virtual    Boolean;->booleanValue()Z          ← filter[1]
//
object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/billing/ProFeatureManager;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lkotlinx/coroutines/flow/MutableStateFlow;",
            name = "getValue",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
)

// ── HasPremiumEmitFingerprint ─────────────────────────────────────────────────
//
// Targets: SubscriptionDataRepository$special$$inlined$map$2$2.emit()
//          [classes3.dex]
//
// hasPremium flow collector. Feeds BuyState.b.
//
// Filter anchors (ArrayList.contains is unambiguous — one occurrence,
// unlike Purchase.a() which also appears for a Timber log call):
//   [0] ArrayList;->contains()    line 98
//   [1] Boolean;->valueOf(Z)      line 103
//
object HasPremiumEmitFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/billing/SubscriptionDataRepository\$special\$\$inlined\$map\$2\$2;",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;", "Lkotlin/coroutines/Continuation;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf",
        ),
    ),
)

// ── HasRenewablePremiumEmitFingerprint ────────────────────────────────────────
//
// Targets: SubscriptionDataRepository$special$$inlined$map$1$2.emit()
//          [classes3.dex]
//
// hasRenewablePremium flow collector. Feeds BuyState.a.
// Distinguished from map$2$2 by the extra JSONObject.optBoolean call.
//
// Filter anchors:
//   [0] ArrayList;->contains()      line 98
//   [1] JSONObject;->optBoolean()   line 103
//   [2] Boolean;->valueOf(Z)        line 108
//
object HasRenewablePremiumEmitFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/billing/SubscriptionDataRepository\$special\$\$inlined\$map\$1\$2;",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;", "Lkotlin/coroutines/Continuation;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
        methodCall(
            definingClass = "Lorg/json/JSONObject;",
            name = "optBoolean",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "valueOf",
        ),
    ),
)

// ── DestinationScreenFingerprint ──────────────────────────────────────────────
//
// Targets: BuyViewModel$1$1.invokeSuspend()  [classes3.dex]
//
// The flow collector lambda that reads BuyState.a / BuyState.b and posts
// a DestinationScreen enum value to BuyViewModel.g (MutableLiveData).
// BuyFragment observes g and navigates accordingly:
//   PREMIUM_RENEWABLE_PROFILE → profile screen (PRO user)
//   PREMIUM_PROFILE           → profile screen (PRO, no autorenew)
//   SUBSCRIPTIONS_OPTIONS_SCREEN → paywall (what we see)
//
// Patching this method to always post PREMIUM_RENEWABLE_PROFILE eliminates
// the paywall screen entirely — BuyFragment navigates away immediately.
//
// definingClass non-obfuscated — stable.
// returnType Object (suspend fun). No parameters (coroutine state machine).
// Filter anchors — two consecutive LiveData.j() calls on the same object,
// one for PREMIUM_RENEWABLE_PROFILE, one for SUBSCRIPTIONS_OPTIONS_SCREEN:
//   [0] LiveData;->j()   ← first post (PREMIUM_RENEWABLE_PROFILE branch)
//   [1] LiveData;->j()   ← second post (PREMIUM_PROFILE branch)
//
// We only need to match to confirm we're in the right method; then
// clearBody + inject sget PREMIUM_RENEWABLE_PROFILE + invoke j + return.
//
object DestinationScreenFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/ui/paywall/BuyViewModel\$1\$1;",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroidx/lifecycle/LiveData;",
            name = "j",
        ),
        methodCall(
            definingClass = "Landroidx/lifecycle/LiveData;",
            name = "j",
        ),
    ),
)

// ── AwaitAccessFingerprint ────────────────────────────────────────────────────
//
// Targets: ProFeatureManager.a(ContinuationImpl)Object  [classes3.dex]
//
// Async suspend gate — awaitAccess(). Called by coroutine-based feature checks
// (StationsOnRoute, ReportResult, etc). Suspends on CompletableDeferred.f
// (BillingClient ready signal), then reads MutableStateFlow.d.getValue()
// and returns the Boolean object. Callers do .booleanValue() + if-nez branch;
// false → navigate to ActionGlobalBuypro (paywall).
//
// Patch: clearBody + return Boolean.TRUE immediately, bypassing the
// CompletableDeferred await and the StateFlow read entirely.
//
// Unique anchors in smali instruction order:
//   [0] Deferred;->m()        ← await() call on CompletableDeferred.f
//   [1] MutableStateFlow;->getValue()  ← reads premium state after await
//
// definingClass non-obfuscated — stable across versions.
//
object AwaitAccessFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/billing/ProFeatureManager;",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lkotlin/coroutines/jvm/internal/ContinuationImpl;"),
    filters = listOf(
        methodCall(
            definingClass = "Lkotlinx/coroutines/Deferred;",
            name = "m",
        ),
        methodCall(
            definingClass = "Lkotlinx/coroutines/flow/MutableStateFlow;",
            name = "getValue",
        ),
    ),
)

// ── FuelioApplicationOnCreateFingerprint ──────────────────────────────────────
//
// Targets: FuelioApplication.onCreate()V  [classes3.dex]
//
// Fuelio's Application subclass. Injection point for FuelioHelper.init()
// which installs the IPackageManager proxy before Google Maps SDK reads
// the signing cert and com.google.android.maps.v2.API_KEY metadata.
//
// Must run before any Maps initialisation (which happens in onCreate).
// definingClass and name are non-obfuscated — stable across versions.
//
object FuelioApplicationOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/FuelioApplication;",
    name = "onCreate",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ── PromoEnabledFingerprint ───────────────────────────────────────────────────
//
// Targets: FirebaseRemoteConfigRepository.c()Z  [classes3.dex]
//
// Returns true when Firebase Remote Config "promo30_enabled" == true AND
// build is eligible. Guards the "Limited Promo / 30% OFF" banner in the
// dashboard composable. Patch to return false to suppress the promo banner
// for PRO users who already have premium unlocked.
//
// Both c() and d() ("promo30_enabled_home") share the same structure.
// Use string anchor "promo30_enabled" (without "_home") to target c() only.
//
// Smali: FirebaseRemoteConfigRepository.smali, method c()Z
//   const-string "promo30_enabled"  ← filter[0]
//   invoke-virtual FirebaseRemoteConfig;->getBoolean()Z
//
object PromoEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/ui/promo/FirebaseRemoteConfigRepository;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        app.morphe.patcher.string("promo30_enabled"),
        methodCall(
            definingClass = "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;",
            name = "getBoolean",
        ),
    ),
    custom = { method, _ ->
        // c() uses "promo30_enabled", d() uses "promo30_enabled_home"
        // The string filter above matches "promo30_enabled" in both — but
        // "promo30_enabled" is a prefix of "promo30_enabled_home", so
        // string() will also match the "_home" variant. Use custom to
        // restrict to c() only by checking method name.
        method.name == "c"
    },
)

// ── PromoHomeEnabledFingerprint ───────────────────────────────────────────────
//
// Targets: FirebaseRemoteConfigRepository.d()Z  [classes3.dex]
//
// Same as c() but reads "promo30_enabled_home" — the home-screen variant.
// Also guards the dashboard promo banner (checked alongside c()).
//
object PromoHomeEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/ui/promo/FirebaseRemoteConfigRepository;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        app.morphe.patcher.string("promo30_enabled_home"),
        methodCall(
            definingClass = "Lcom/google/firebase/remoteconfig/FirebaseRemoteConfig;",
            name = "getBoolean",
        ),
    ),
)
