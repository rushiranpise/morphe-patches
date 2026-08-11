package app.template.patches.aviate.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.hermesPatch
import app.template.patches.shared.Constants.AVIATE_COMPATIBILITY

/**
 * Patches four Hermes bytecode sites in the React Native bundle (HBC v98).
 *
 * Root cause analysis:
 * The subscription gate has two independent paths:
 *   A) React context: SubscriptionProvider seeds isPro=false via useState(false),
 *      then a refresh callback (fn #15648) makes an API call and overwrites state
 *      from the server response. Server always returns isPro=false for free users.
 *   B) AsyncStorage path: isProUser (fn #6699) reads offline_subscription_state
 *      from device storage — used by widgets and native modules outside React context.
 *
 * Fix strategy:
 *   1. Seed isPro=true at first render (useState patch) so no "free" flash.
 *   2. No-op the refresh callback (fn #15648) so the API call never fires and
 *      never overwrites our seeded state. This is the critical missing fix.
 *   3. Return true from computeIsPro (fn #7657) as a belt-and-suspenders measure
 *      in case any other caller invokes it.
 *   4. Return true from isProUser (fn #6699) to cover the AsyncStorage path.
 *
 * HBC v98 opcodes used:
 *   0x93 0x00 = CreateEnvironment r0 (produces undefined when used as return value)
 *   0x95 = LoadConstTrue, 0x96 = LoadConstFalse, 0x76 = Ret
 *
 * All four patterns are unique in the bundle (verified).
 * SHA-1 footer is recalculated by hermesPatch automatically.
 */
@Suppress("unused")
val aviatePremiumPatch = rawResourcePatch(
    name = "Aviate Premium",
    description = "Unlocks Aviate Pro and Lifetime Pro by patching the Hermes JS subscription gate."
) {
    compatibleWith(AVIATE_COMPATIBILITY)

    dependsOn(hermesPatch {
        // 1. SubscriptionProvider useState seed (fn #7659, +0x4d):
        //    Single LoadConstFalse r2 is reused across all useState(false) calls.
        //    Flip to LoadConstTrue → isPro, isGrandfathered, isEnterprise seed as true.
        val subscriptionProviderInit =
            "01 56 96 02 6E 0A 08 01" to
            "01 56 95 02 6E 0A 08 01"

        // 2. Refresh callback no-op (fn #15648, offset 0x00608a53):
        //    This function makes the API call that overwrites isPro from the server.
        //    Replace first 4 bytes with 93 00 76 00 (return-void) to abort immediately.
        //    Without this, the server response always resets isPro=false for free users.
        val refreshCallbackNoop =
            "34 04 00 89 01 01 3B 03" to
            "93 00 76 00 01 01 3B 03"

        // 3. computeIsPro (fn #7657, offset 0x004df415):
        //    Returns true unconditionally. Covers any other caller paths.
        val computeIsPro =
            "89 01 01 45 02 01 00 03 B3 90 03 86 AB D3" to
            "95 00 76 00 02 01 00 03 B3 90 03 86 AB D3"

        // 4. isProUser (fn #6699, offset 0x004b9dfc):
        //    Returns true unconditionally. Covers widgets and native-module paths.
        val isProUser =
            "93 03 93 00 89 05 01 45" to
            "95 00 76 00 89 05 01 45"

        setOf(subscriptionProviderInit, refreshCallbackNoop, computeIsPro, isProUser)
    })
}
