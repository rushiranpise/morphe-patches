package app.template.patches.kinemaster

import app.morphe.patcher.Fingerprint

private const val LIFELINE_MANAGER = "Lcom/kinemaster/app/modules/lifeline/LifelineManager;"
private const val SUBSCRIBE_RESPONSE_DTO = "Lcom/kinemaster/module/network/communication/account/dto/SubscribeResponseDto;"

/**
 * Fingerprint for SubscribeResponseDto.isSubscribed()Z — ROOT subscription flag.
 *
 * This is the deepest source of truth for KineMaster's subscription state.
 * All other checks ultimately read from this DTO:
 *
 *   LifelineBasePresent.Q() → reads SUBSCRIPTION_STATUS pref (JSON) → Gson.fromJson
 *     → SubscribeResponseListDto { kinemaster: SubscribeResponseDto, spring: SubscribeResponseDto }
 *   LifelineManager.J0() → Q().getKinemaster().isSubscribed()  ← here
 *   LifelineManager.J()Z  → compares J0() result against active da/d enum states
 *   LifelineManager.f0()Z → checks if status is in free da/d states
 *   LifelineBasePresent.d / f / checkOnetimePurchases → also call isSubscribed() directly
 *
 * When SUBSCRIPTION_STATUS pref is empty (no server response cached), Q() returns
 * a default SubscribeResponseListDto with isSubscribed=false — meaning J()Z/f0()Z
 * alone are insufficient; the DTO getter must also return true.
 *
 * Patching isSubscribed()=true ensures ALL callers see subscribed=true regardless
 * of cached server data or network availability.
 */
internal val IsSubscribedDtoFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == SUBSCRIBE_RESPONSE_DTO && method.name == "isSubscribed"
    }
)

/**
 * Fingerprint for LifelineManager.J()Z — isSubscribed() high-level gate.
 *
 * Secondary gate — compares J0() (SubscribeResponseDto status) against active da/d
 * enum states. Called by BaseViewModel.j() → PurchaseState emission.
 * Kept as a belt-and-suspenders patch alongside IsSubscribedDtoFingerprint.
 */
internal val IsSubscribedFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == LIFELINE_MANAGER && method.name == "J"
    }
)

/**
 * Fingerprint for LifelineManager.f0()Z — hasPremiumPurchase().
 *
 * Despite the name suggesting "free", f0()Z actually checks if the current da/d
 * subscription status is in the set of VALID/PURCHASED states via da/d$a.q(J0()).
 * Returns TRUE = user has a premium purchase (subscription OR onetime).
 *
 * Key callers:
 *   - SaveAsProcessPresenter.R0(): if (C() && f0()) → setWatermark(false) = NO watermark
 *                                  if (C() && !f0()) → setWatermark(true) = HAS watermark
 *   - BaseViewModel.i(ZIZ): y(true, f0()) → PurchaseState($isPurchased=f0())
 *     → f0()=true → isPurchased=true → UI shows premium
 *
 * Previous patch returned FALSE (wrong). Must return TRUE for premium.
 */
internal val IsFreeFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == LIFELINE_MANAGER && method.name == "f0"
    }
)
