package app.template.patches.kinemaster

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.KINEMASTER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks KineMaster Premium by patching the subscription DTO and LifelineManager gates.
 *
 * Root cause of "still shows free" (v2 fix — f0() was inverted):
 *   f0()Z = hasPremiumPurchase(), NOT isFree(). Confirmed via:
 *     SaveAsProcessPresenter.R0(): if (C() && f0()=true) → setWatermark(false) = no watermark
 *     BaseViewModel.i(ZIZ): y(true, f0()) → PurchaseState(isPurchased=f0())
 *   Previous patch returned false → isPurchased=false → UI showed FREE.
 *
 * Also: LifelineBasePresent.Q() reads SUBSCRIPTION_STATUS pref (JSON) → Gson.fromJson.
 *   When pref empty, default DTO has isSubscribed=false, bypassing J()Z entirely.
 *   Patching SubscribeResponseDto.isSubscribed()=true covers all direct DTO callers.
 *
 * Fix:
 *   1. SubscribeResponseDto.isSubscribed() → true  (root DTO, covers all LifelineBasePresent paths)
 *   2. LifelineManager.J()Z              → true  (BaseViewModel.j() → PurchaseState subscription)
 *   3. LifelineManager.f0()Z             → true  (hasPremiumPurchase: BaseViewModel.i() + watermark)
 *
 * No pairip/LVL found. libpglarmor.so = ByteDance PGL ad SDK only.
 */
@Suppress("unused")
val kineMasterUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock Premium features after login.",
) {
    compatibleWith(KINEMASTER_COMPATIBILITY)

    execute {
        // ROOT FIX: SubscribeResponseDto.isSubscribed() → always true
        // Covers LifelineBasePresent.d/f, checkOnetimePurchases, and J0() path
        IsSubscribedDtoFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // LifelineManager.J()Z → always true (BaseViewModel.j() → PurchaseState subscription path)
        IsSubscribedFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }

        // LifelineManager.f0()Z → always TRUE (hasPremiumPurchase, NOT isFree)
        // f0()=true → BaseViewModel.i() → isPurchased=true → premium UI
        // f0()=true → SaveAsProcessPresenter.R0()=true → setWatermark(false) → no watermark
        IsFreeFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent()
            )
        }
    }
}
