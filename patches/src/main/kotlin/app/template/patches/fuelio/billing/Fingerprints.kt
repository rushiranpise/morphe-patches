package app.template.patches.fuelio.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Architecture note (v10.3.2 → v10.3.3 → v10.3.4) ─────────────────────────
//
// v10.3.3: ProFeatureManager removed → Lel5;. map$1$2/map$2$2 merged → Lve0;.h().
//   BuyViewModel$1$1 gone → Lr7;. LiveData La74;->i(). Enum Lhc0;.
//   FirebaseRemoteConfigRepository methods renamed from c()/d() to stable names.
//   DEX count 3 → 2.
//
// v10.3.4: ProFeatureManager renamed el5 → fl5. StateFlow type Lst6 → Ltt6.
//   LiveData type La74 → Lb74 (->i() method name unchanged).
//   All other types/structure identical to v10.3.3.
//
// ── IsPremiumFingerprint ──────────────────────────────────────────────────────
//
// Targets: fl5.b()Z  [classes.dex]
//
// Synchronous isPremium() gate. Reads MutableStateFlow<Boolean> (field d:Ltt6;)
// via Ltt6;->getValue() + Boolean.booleanValue(). Gates all in-app feature access.
//
// v10.3.2: definingClass = Lcom/kajda/fuelio/billing/ProFeatureManager; (non-obfuscated)
// v10.3.3: class obfuscated to Lel5;, StateFlow type Lst6;
// v10.3.4: class renamed to Lfl5;, StateFlow type renamed to Ltt6;
//   No definingClass — use stable filter anchors only.
//
// Smali (fl5.smali, v10.3.4):
//   .method public final b()Z
//     iget-object p0, p0, Lfl5;->d:Ltt6;
//     invoke-virtual {p0}, Ltt6;->getValue()Ljava/lang/Object;
//     check-cast p0, Ljava/lang/Boolean;
//     invoke-virtual {p0}, Boolean;->booleanValue()Z    ← filter[1]
//     return p0
//
object IsPremiumFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Ltt6;",
            name = "getValue",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
    custom = { method, _ ->
        // Restrict to fl5.b() — many classes use getValue+booleanValue.
        // fl5 is uniquely identified by having exactly ONE getValue call
        // (not a coroutine — no packed-switch, no state machine overhead).
        (method.implementation?.instructions?.count() ?: Int.MAX_VALUE) < 20
    },
)

// ── SubscriptionEmitFingerprint ───────────────────────────────────────────────
//
// Targets: ve0.h(Object, Li41;)Object  [classes.dex]
//
// Merged coroutine state machine that handles BOTH subscription flow emissions
// in a single packed-switch method (was two separate classes in v10.3.2):
//
//   Branch A — hasPremium:
//     ArrayList.contains(skuId) → Boolean.valueOf(result)    ← filter[0,1]
//
//   Branch B — hasRenewablePremium:
//     ArrayList.contains(skuId) → JSONObject.optBoolean("autoRenewing")  ← filter[2,3]
//     → Boolean.valueOf(result)
//
// Patching this single method (clearBody → return Boolean.TRUE) covers both
// hasPremium and hasRenewablePremium in one shot.
//
// Stable anchors: "fuelio_subscription" (store key, version-stable) + contains + optBoolean.
//
// Smali (ve0.smali, v10.3.3):
//   .method public final h(Ljava/lang/Object;Li41;)Ljava/lang/Object;
//     const-string v3, "fuelio_subscription"           ← filter[0]
//     ...
//     invoke-virtual ArrayList;->contains(Object)Z     ← filter[1]
//     ...
//     invoke-virtual JSONObject;->optBoolean(String)Z  ← filter[2]
//
object SubscriptionEmitFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;", "Li41;"),
    filters = listOf(
        string("fuelio_subscription"),
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
        methodCall(
            definingClass = "Lorg/json/JSONObject;",
            name = "optBoolean",
        ),
    ),
)

// ── DestinationScreenFingerprint ──────────────────────────────────────────────
//
// Targets: r7.p(Object)Object  [classes.dex]
//
// Coroutine state machine that reads BuyState (Lgc0;) and posts a DestinationScreen
// enum value (Lhc0;) to a MutableLiveData-equivalent (La74;) via ->i().
//
// Dispatch logic at pswitch_8b4 (r7.smali ~line 4366):
//   check-cast v0, Lgc0;                          BuyState cast (opcode, not invoke)
//   iget-object v2, v0, Lgc0;->a                  read hasRenewablePremium
//   invoke-static Llo3;->f() → if-eqz :cond_8dc   branch if false
//   sget-object v0, Lhc0;->s:Lhc0;               PREMIUM_RENEWABLE_PROFILE ← filter[0]
//   invoke-virtual {v1,v0}, La74;->i(Object)V     postValue                 ← filter[1]
//   :cond_8dc  iget-object gc0.b (hasPremium) ...
//   sget-object Lhc0;->t / La74;->i()
//   :cond_8f5
//   sget-object Lhc0;->q / La74;->i()
//
// WHY PREVIOUS FILTER FAILED:
//   filter[0] used methodCall(Lgc0;-><init>) — r7.p() never CALLS gc0.<init>.
//   It only check-casts to Lgc0; which is a DEX opcode, not an invoke instruction.
//   methodCall filters only match invoke-* opcodes. check-cast is unreachable.
//
// CORRECT anchors (verified unique across entire classes.dex):
//   [0] fieldAccess(SGET_OBJECT, Lhc0;, "s") — hc0.s (PREMIUM_RENEWABLE_PROFILE sget)
//       appears ONLY in r7.smali across the entire DEX.
//   [1] methodCall(La74;->i(Object)V)         — postValue, immediately after.
//
// Smali evidence (r7.smali, v10.3.4, verified sget@1134, if-eqz@1128, offset=6 unchanged):
//   sget-object v0, Lhc0;->s:Lhc0;           ← filter[0] fieldAccess SGET_OBJECT
//   invoke-virtual {v1,v0}, Lb74;->i(Obj)V   ← filter[1] methodCall (La74→Lb74 in v10.3.4)
//
object DestinationScreenFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lhc0;",
            name = "s",
        ),
        methodCall(
            definingClass = "Lb74;",
            name = "i",
            returnType = "V",
            parameters = listOf("Ljava/lang/Object;"),
        ),
    ),
)

// ── AwaitAccessFingerprint ────────────────────────────────────────────────────
//
// Targets: el5.a(Lk41;)Object  [classes.dex]
//
// Async suspend gate. Awaits a coroutine Deferred (via Lrq3;->z()), then reads
// MutableStateFlow.d.getValue() and returns the Boolean object. Callers do
// .booleanValue() + if-nez → false routes to ActionGlobalBuypro paywall.
//
// v10.3.2: definingClass = ProFeatureManager; param = ContinuationImpl
// v10.3.3: el5.a(Lk41;)Object — Lk41; is the new continuation type
// v10.3.4: fl5.a(Lk41;)Object — same param, StateFlow type Lst6→Ltt6
//
// Patch: clearBody + return Boolean.TRUE immediately.
// Stable anchors: identified by param Lk41; + getValue on StateFlow type.
//
// Smali (fl5.smali, v10.3.4):
//   .method public final a(Lk41;)Ljava/lang/Object;
//     ... (coroutine state machine preamble)
//     invoke-virtual {p1, v0}, Lrq3;->z(Lk41;)Ljava/lang/Object;  ← Deferred.await
//     ...
//     iget-object p0, p0, Lfl5;->d:Ltt6;
//     invoke-virtual {p0}, Ltt6;->getValue()Ljava/lang/Object;     ← StateFlow read
//     return-object p0
//
object AwaitAccessFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lk41;"),
    filters = listOf(
        methodCall(
            definingClass = "Ltt6;",
            name = "getValue",
        ),
    ),
)

// ── FuelioApplicationOnCreateFingerprint ──────────────────────────────────────
//
// Targets: FuelioApplication.onCreate()V  [classes.dex]
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
// Targets: FirebaseRemoteConfigRepository.isPromo30Enabled()Z  [classes.dex]
//
// v10.3.2: obfuscated to c()Z — identified via string("promo30_enabled") + custom method.name=="c"
// v10.3.3: de-obfuscated to isPromo30Enabled()Z — use stable name directly.
//
// Patch to return false: suppresses the "Limited Promo / 30% OFF" dashboard banner
// for users who already have premium unlocked.
//
object PromoEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/ui/promo/FirebaseRemoteConfigRepository;",
    name = "isPromo30Enabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ── PromoHomeEnabledFingerprint ───────────────────────────────────────────────
//
// Targets: FirebaseRemoteConfigRepository.isPromo30EnabledHome()Z  [classes.dex]
//
// v10.3.2: obfuscated to d()Z
// v10.3.3: de-obfuscated to isPromo30EnabledHome()Z — use stable name directly.
//
// Same rationale as PromoEnabledFingerprint — suppresses the home-screen promo banner.
//
object PromoHomeEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/kajda/fuelio/ui/promo/FirebaseRemoteConfigRepository;",
    name = "isPromo30EnabledHome",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
