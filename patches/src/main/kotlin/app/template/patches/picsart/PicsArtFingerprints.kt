package app.template.patches.picsart

import app.morphe.patcher.Fingerprint

/**
 * PicsArt Studio 30.5.5 — subscription & ads architecture (jadx, classes6.dex).
 *
 * Subscription: everything funnels into `SubscriptionRepoImpl.k()` which returns
 * `myobfuscated.OM.a` — a Kotlin data class whose FIRST field `a` (boolean) is the
 * subscribed flag. Every consumer reads it as `k().a`:
 *
 *   SubscriptionInfoUseCaseImpl$isSubscribed$2 → Boolean.valueOf(k().a)   (async gate)
 *   SubscriptionRepoImpl.o()                   → k().a                    (sync gate)
 *   GetUserSubscriptionTiersUseCaseImpl.b()    → (k().a && tier==OLD_GOLD) ? false : true
 *   PremiumBadgeProviderImpl / promo resolver  → k().a directly
 *
 * Patching the OM.a constructor to force `this.a = true` makes EVERY OM.a
 * instance report subscribed — covering all of the above with one fingerprint.
 *
 * The class is obfuscated (`myobfuscated.OM.a`) but uniquely identifiable:
 * it is the only class in the app whose field set includes a
 * `Lcom/picsart/payment/api/subscription/SubscriptionMarket;` reference
 * (verified: only myobfuscated/OM/a.java references it).
 *
 * NOTE: do not set `accessFlags` on these fingerprints — the matcher compares
 * them with exact equality against the method's full flag set, so any extra
 * flag (synthetic/bridge/etc.) causes a mismatch. The `custom` predicates below
 * are already precise enough.
 */
internal val SubscriptionInfoCtorFingerprint = Fingerprint(
    returnType = "V",
    custom = { method, classDef ->
        method.name == "<init>" &&
            method.parameterTypes.contains(
                "Lcom/picsart/payment/api/subscription/model/SubscriptionMarket;",
            )
    },
)

/**
 * Backstop gate — the async isSubscribed() coroutine (reads k().a). Kept as a
 * belt-and-suspenders patch alongside the constructor patch: if the constructor
 * fingerprint ever misses (field rename), isSubscribed() still reports true.
 */
internal val IsSubscribedCoroutineFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/payment/impl/subscription/domain/SubscriptionInfoUseCaseImpl\$isSubscribed\$2;" &&
            method.name == "invokeSuspend"
    },
)

/**
 * Tier premium gate — `GetUserSubscriptionTiersUseCaseImpl.b()Z` returns
 * `(k().a && k().k.b == TierType.OLD_GOLD) ? false : true`. Forcing true keeps
 * the "is premium (new tiers)" path enabled even if the tier payload is stale.
 */
internal val TiersPremiumGateFingerprint = Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/payment/impl/subscription/tiers/data/GetUserSubscriptionTiersUseCaseImpl;" &&
            method.name == "b" && method.returnType == "Z" && method.parameters.isEmpty()
    },
)

/**
 * Tier-type helpers on `com.picsart.payment.api.subscription.b` (stable class,
 * the payment API package is not obfuscated). These are the app-wide
 * "which tier does this user have" predicates, all reading the OM.a info:
 *
 *   j(info) = info.a                              (subscribed flag — covered by ctor patch)
 *   i(info) = j(info) && tier ∈ {PRO, MAX, LITE, ULTRA}
 *   f(info) = j(info) && tier == OLD_GOLD
 *   h(info) = j(info) && tier == PLUS
 *
 * The top-bar subscription badge hides only when `i(info) || f(info)` is true
 * (`ResolveCorrectSubsBadgeUseCase.shouldShowBadge`), and the tier predicate is
 * what gates feature access. The server hands a FREE tier in `info.k.b`, so the
 * ctor patch alone (subscribed=true) leaves these false → the "Try PRO" pill
 * still shows. Forcing all three to true makes every premium-tier path fire.
 */
internal val TierTypeProFingerprint = Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/payment/api/subscription/b;" &&
            method.name == "i" && method.returnType == "Z" && method.parameters.size == 1
    },
)

internal val TierTypeGoldFingerprint = Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/payment/api/subscription/b;" &&
            method.name == "f" && method.returnType == "Z" && method.parameters.size == 1
    },
)

internal val TierTypePlusFingerprint = Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/payment/api/subscription/b;" &&
            method.name == "h" && method.returnType == "Z" && method.parameters.size == 1
    },
)

/**
 * Central ads gate — `com.picsart.studio.ads.b.a()Z` (AdsManager).
 *
 *   a() = "ads disabled" master flag:
 *     if (!this.e || !e()) return true;               // ads off
 *     return AdsService.l.g() || !h();
 *
 *   g(touchPoint): if (a()) return false;             // banner not enabled
 *   i(context):    (!d().isConnected() || a() || …) ? false : true   // no interstitial
 *   j(context):    if (a() || i(context) || …) return null           // no banner load
 *
 * Patching a() → true disables banner + interstitial ads app-wide through
 * every entry point that consults the manager. Class name is stable
 * (com.picsart.studio.ads package is NOT obfuscated; only method names are
 * shortened to a/b/c/g/h/i/j — matched structurally here).
 */
internal val AdsManagerDisabledFingerprint = Fingerprint(
    returnType = "Z",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/studio/ads/b;" &&
            method.name == "a" && method.returnType == "Z" && method.parameters.isEmpty()
    },
)

/**
 * Anti-tamper signature check — `com.picsart.appstart.items.SignatureCheckInit`.
 *
 * The appstart pipeline runs `initialize(Context)` on a background executor.
 * It opens an obfuscated asset, reads `Make`/`Model` keys, and compares them
 * against `myobfuscated.p1550mT.b.a(context)` = MD5 of the app's signing cert
 * (AppSignature.kt). If neither asset value equals the cert MD5, it schedules
 * `System.exit(-1)` after a RANDOM 8–23s delay on a worker thread (seen in
 * logcat as "System.exit called, status: -1" ~12s after launch — the app runs,
 * shows UI, then dies silently).
 *
 * Re-signed builds (test key / Morphe signing) always fail this check → the
 * app must be re-signed, so this check must be neutralized. Making
 * `initialize(Context)V` a no-op prevents the exit from ever being scheduled.
 * Class name is stable (com.picsart.appstart is not obfuscated).
 *
 * NOTE: the class has TWO `initialize(Context)` methods — a synthetic bridge
 * returning `Lkotlin/Unit;` and the real one returning `V`. Match on returnType
 * "V" to hit only the real body. clearBody() is required because the method
 * has try/catch ranges that would otherwise cause ART VerifyError.
 */
internal val SignatureCheckInitFingerprint = Fingerprint(
    returnType = "V",
    custom = { method, classDef ->
        classDef.type == "Lcom/picsart/appstart/items/SignatureCheckInit;" &&
            method.name == "initialize" &&
            method.returnType == "V" &&
            method.parameterTypes == listOf("Landroid/content/Context;")
    },
)
