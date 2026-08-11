package app.template.patches.rainbowweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RAINBOW_WEATHER_COMPATIBILITY

/**
 * Unlocks Rainbow Weather premium by injecting isPremium=true into the
 * Adapty profile success callback before it writes PaymentInfoDataStore.
 *
 * ## Architecture
 *
 * Adapty SDK calls eq.e (p112eq.C3092e) when a profile fetch succeeds.
 * invokeSuspend() reads `accessLevels.get("premium").isActive()` to set
 * isPremium, then writes PaymentInfoDataStore(paymentId, isPremium) to DataStore.
 *
 * For free users: accessLevels["premium"] is null → isPremium=false saved.
 * All UI premium gates observe this DataStore field via Kotlin Flow.
 *
 * ## Fix
 *
 * instructionMatches[4] = the `invoke-direct Ldn/e0;-><init>(String;Z)V` call.
 * Register v3 is the isPremium boolean passed as the second argument.
 * We inject `const/4 v3, 0x1` immediately before this invoke, forcing
 * isPremium=true regardless of whether the "premium" access level exists.
 *
 * This persists to DataStore → all downstream Flow collectors see isPremium=true
 * → full premium UI unlocked without any server-side payment.
 *
 * ## Why isActive() alone was insufficient
 *
 * The previous attempt patched AdaptyProfile$AccessLevel.isActive() → true.
 * This only affects the return value when the AccessLevel *object exists*.
 * For free users, getAccessLevels().get("premium") returns null, and the
 * null-check branch (`if-eqz v18, :cond_226`) is taken before isActive() is
 * ever called, bypassing the patch entirely.
 */
@Suppress("unused")
val rainbowWeatherPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Rainbow Weather premium features by forcing isPremium=true in the Adapty DataStore write.",
) {
    compatibleWith(RAINBOW_WEATHER_COMPATIBILITY)

    execute {
        // instructionMatches[4] = invoke-direct {v5, v2, v3}, Ldn/e0;-><init>(String;Z)V
        // v3 is the isPremium boolean. Inject const/4 v3, 0x1 before it.
        val initIndex = SavePaymentInfoFingerprint.instructionMatches[4].index

        SavePaymentInfoFingerprint.method.addInstructions(
            initIndex,
            "const/4 v3, 0x1",
        )
    }
}
