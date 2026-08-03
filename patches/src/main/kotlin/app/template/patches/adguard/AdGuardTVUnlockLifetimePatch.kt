package app.template.patches.adguard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.ADGUARD_TV_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/**
 * AdGuard for Android TV — Unlock Lifetime Premium
 *
 * ## TV vs phone differences
 *
 * The billing architecture is identical to the phone version (same PlusManager,
 * same PlusState sealed class, same LicenseDuration / LicenseType types).
 * However the TV leanback UI (TvAboutLicenseViewModel) adds one extra path
 * that the phone version does not trigger on license screen open:
 *
 *   L6(String)V — activateLicenseKey():
 *     Called by TvAboutLicenseViewModel on screen open. Reads any stored license
 *     key from storage and calls the backend to re-verify it (LI0/e interface).
 *     Without a real key this fails and the UI displays "License activated"
 *     (a loading/error fallback) instead of the "Lifetime" label from our
 *     synthetic PaidLicense state.
 *
 * ## Patch layers (extends phone layers with TV-specific additions)
 *
 *   Phone layers (shared logic — identical fingerprints, different obfuscated names):
 *     Layer 1: inject static getPaidLicense() helper into PlusManager
 *     Layer 2: getCachedPlusState()           → return getPaidLicense()
 *     Layer 3: setPlusState(incoming)         → override arg with getPaidLicense()
 *     Layer 4: fetchAndUpdatePlusState(...)   → return getPaidLicense() (license screen)
 *     Layer 5: fetchPlusStateForPromo(...)    → return getPaidLicense() (promo dialog)
 *
 *   TV-specific addition:
 *     Layer 6: activateLicenseKey(String)     → returnEarly() (skip backend re-verify)
 *              Prevents the "License activated" fallback text on the TV license screen.
 */
@Suppress("unused")
val adGuardTVUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime Premium",
    description = "Unlocks all features locked behind the subscription paywall.",
    default = true,
) {
    compatibleWith(ADGUARD_TV_COMPATIBILITY)

    execute {
        // Resolve types from fingerprints — never hardcode obfuscated names
        val paidLicenseType = PaidLicenseFingerprint.classDef.type
        val paidLicenseCtor = PaidLicenseFingerprint.method
        val licenseTypeClass = paidLicenseCtor.parameters[1].type
        val lifetimeDurationField = LifetimeDurationFingerprint.classDef.staticFields.first()
        val plusManagerType = GetPlusStateFingerprint.classDef.type
        val plusStateReturnType = GetPlusStateFingerprint.method.returnType

        // ── Layer 1: inject static getPaidLicense() helper ────────────────────
        val getPaidLicenseMethod = ImmutableMethod(
            plusManagerType,
            "getPaidLicense",
            null,
            plusStateReturnType,
            AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(7),
        ).toMutable().apply {
            addInstructions(
                0,
                """
                new-instance v0, $paidLicenseType
                const-string v1, ""
                sget-object v2, $licenseTypeClass->Family:$licenseTypeClass
                sget-object v3, $lifetimeDurationField
                const/4 v4, 0x1
                const/16 v5, 0x9
                const-string v6, ""
                invoke-direct/range {v0 .. v6}, $paidLicenseCtor
                return-object v0
                """.trimIndent(),
            )
        }

        GetPlusStateFingerprint.classDef.methods.add(getPaidLicenseMethod)

        val callHelper = """
            invoke-static {}, $getPaidLicenseMethod
            move-result-object v0
            return-object v0
        """.trimIndent()

        // ── Layer 2: getCachedPlusState() ─────────────────────────────────────
        GetPlusStateFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 3: setPlusState(incoming) — override arg before persist ─────
        SetPlusStateFingerprint.method.addInstructions(
            0,
            """
            invoke-static {}, $getPaidLicenseMethod
            move-result-object p1
            """.trimIndent(),
        )

        // ── Layer 4: fetchAndUpdatePlusState() — license screen path ──────────
        StateFlowResolverFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 5: fetchPlusStateForPromo() — promo dialog path ─────────────
        PromoStateFlowResolverFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 6 (TV-only): activateLicenseKey(String) ─────────────────────
        //
        // TvAboutLicenseViewModel calls this on license screen open to re-verify
        // any stored key with the backend. returnEarly() prevents the call entirely,
        // so the license screen reads state only via getCachedPlusState() (Layer 2)
        // and correctly displays the Lifetime duration from our synthetic PaidLicense.
        LicenseKeyActivateFingerprint.method.returnEarly()
    }
}
