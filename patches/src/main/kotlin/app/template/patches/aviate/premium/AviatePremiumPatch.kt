package app.template.patches.aviate.premium

import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.hermesPatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.Constants.AVIATE_COMPATIBILITY

/**
 * Patches four Hermes bytecode sites in the React Native bundle (HBC v98).
 *
 * Root cause analysis:
 * The subscription gate has two independent paths:
 *   A) React context: SubscriptionProvider (fn #7763) seeds isPro=false via useState(false),
 *      then fn #15987 (state-update callback) is invoked by the API response and
 *      overwrites state. Server returns subscription="free" for free users.
 *   B) AsyncStorage path: isProUser (fn #6791) reads offline_subscription_state
 *      from device storage — used by widgets and native modules outside React context.
 *
 * Fix strategy (all anchors are stable non-obfuscated function names + unique byte windows):
 *   1. Seed isPro=true at first render (SubscriptionProvider useState seed, fn #7763 +0x4d):
 *      Flip LoadConstFalse r2 (0x96 02) -> LoadConstTrue r2 (0x95 02) for the isPro
 *      useState(false) call. No "free" flash on first render.
 *   2. No-op the state-update callback (fn #15987, offset 0x00622370):
 *      Returns undefined immediately so the API response never overwrites our
 *      seeded isPro=true. This is the critical fix — without it, server always
 *      resets subscription="free" for free users.
 *   3. Return true from computeIsPro (fn #7761, offset 0x004ee8ea):
 *      Belt-and-suspenders cover for any caller that bypasses SubscriptionProvider.
 *   4. Return true from isProUser (fn #6791, offset 0x004c885e):
 *      Covers widgets and native-module paths that read offline_subscription_state.
 *
 * HBC v98 opcodes (verified from hbc98.py opcode table):
 *   0x76 Reg8        = Ret
 *   0x93 Reg8        = LoadConstUndefined
 *   0x95 Reg8        = LoadConstTrue
 *   0x96 Reg8        = LoadConstFalse
 *
 * All four patterns are unique in the bundle (verified by exhaustive search).
 * SHA-1 footer is recalculated by hermesPatch automatically.
 *
 * Version history:
 *   v1.0.1 (versionCode=201): fn #6699, #7657, #7659, #15648 (old offsets)
 *   v1.1.0-beta.1 (versionCode=202): fn #6791, #7761, #7763, #15987 (current)
 */


private const val LICENSE_CLIENT = "Lcom/pairip/licensecheck/LicenseClient;"
private const val LICENSE_CHECK_STATE = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

private val  aviateLicensePatch = bytecodePatch(default = false){
    compatibleWith(AVIATE_COMPATIBILITY)

    execute {
        // 1. Force responseCode=LICENSED (0x0) before processResponse branches on it.
        //    Without this, re-signed APKs receive NOT_LICENSED and are killed.
        ProcessLicenseResponseFingerprint.method.addInstruction(
            0,
            "const/4 p1, 0x0"
        )

        // 2. Skip cryptographic signature validation.
        //    validateResponse() always throws LicenseCheckException on re-signed APKs
        //    because the APK signature no longer matches the original certificate.
        ValidateLicenseResponseFingerprint.method.returnEarly()

        // 3. Block the license check at the entry point.
        //    checkLicense(Context) is the public API called at app startup.
        //    Returning early prevents any connection to Google Play licensing service.
        CheckLicenseFingerprint.method.returnEarly()
    }
}


@Suppress("unused")
val aviatePremiumPatch = rawResourcePatch(
    name = "Aviate Premium",
    description = "Unlocks Aviate Pro and Lifetime Pro by patching the Hermes JS subscription gate."
) {
    compatibleWith(AVIATE_COMPATIBILITY)

    dependsOn(aviateLicensePatch , hermesPatch {
        // 1. SubscriptionProvider isPro useState seed (fn #7763, offset 0x004ee9ae +0x4d):
        //    LoadConstFalse r2 (96 02) is the false seed for isPro.
        //    Flip to LoadConstTrue r2 (95 02) so isPro=true from first render.
        //    Window: 96 02 6e 0a 08 01 02 3b — unique (1 occurrence).
        val subscriptionProviderInit =
            "96 02 6E 0A 08 01 02 3B" to
            "95 02 6E 0A 08 01 02 3B"

        // 2. State-update callback no-op (fn #15987, offset 0x00622370):
        //    This closure is invoked with the API response and calls setSubscription,
        //    setGrandfathered, setIsAdmin — overwriting our seeded isPro=true.
        //    Replace first 4 bytes with 93 00 76 00 (return undefined immediately).
        //    Window: 34 04 00 89 01 01 3b 03 — unique (1 occurrence).
        val refreshCallbackNoop =
            "34 04 00 89 01 01 3B 03" to
            "93 00 76 00 89 01 01 3B"

        // 3. computeIsPro return true (fn #7761, offset 0x004ee8ea):
        //    Checks subscription=="free" and subscription_expiry date.
        //    Replace first 4 bytes with 95 00 76 00 (LoadConstTrue r0, Ret r0).
        //    Window extended to 12 bytes for uniqueness (8-byte pattern has 2 hits).
        val computeIsPro =
            "89 01 01 45 02 01 00 D1 B1 90 03 0E" to
            "95 00 76 00 02 01 00 D1 B1 90 03 0E"

        // 4. isProUser return true (fn #6791, offset 0x004c885e):
        //    Reads offline_subscription_state from AsyncStorage (widget / native path).
        //    Replace first 4 bytes with 95 00 76 00 (LoadConstTrue r0, Ret r0).
        //    Window: 93 03 93 00 89 05 01 45 — unique (1 occurrence).
        val isProUser =
            "93 03 93 00 89 05 01 45" to
            "95 00 76 00 89 05 01 45"

        setOf(subscriptionProviderInit, refreshCallbackNoop, computeIsPro, isProUser)
    })
}