package app.template.patches.adguard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.ADGUARD_UNIFIED_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/**
 * AdGuard — Unlock Lifetime Premium (Unified: Phone + Android TV)
 *
 * ## Architecture
 *
 * AdGuard's license state flows through a sealed class hierarchy:
 *
 *   PlusState (interface I0/i or G0/i depending on version)
 *   ├── PaidLicense  — active license/subscription
 *   │     fields: licenseKey, licenseType, licenseDuration, devCount, maxDevCount, keyOwner
 *   ├── Free, Trial, Expired*, Blocked*, CachedFree, CachedPaid, CachedTrial, Unknown
 *
 *   LicenseDuration
 *   ├── Lifetime     — singleton object (I0/i$l$a or G0/i$l$a)
 *   └── WithExpirationDate, WithNextBillingDate, Unknown
 *
 *   LicenseType (enum) — Custom, Unknown, Personal, Family, Standard, Beta, Bonus
 *
 * ## PlusManager state propagation
 *
 * PlusManager is the single source of truth for premium state. Five paths:
 *
 *   getCachedPlusState()         reads in-memory cache; falls back to storage + writes back
 *   setPlusState(state)          writes to cache + storage + notifies observers
 *   fetchAndUpdatePlusState(...) network → cache → StateFlow → license screen UI
 *   fetchPlusStateForPromo(...)  network → MutableLiveData → promo/check-license dialog
 *   activateLicenseKey(String)   backend re-verification on license screen open
 *                                (phone: present but not always invoked on screen open;
 *                                 TV:    always called by TvAboutLicenseViewModel on open)
 *
 * ## Patch strategy (5 + 1 layers)
 *
 * A static helper `getPaidLicense()` is injected into PlusManager at patch time.
 * It constructs a synthetic PaidLicense(Family, Lifetime) so all state paths
 * return premium without consulting the network or on-disk storage.
 *
 * Why Family + Lifetime (not Personal):
 *   The AboutLicenseViewModel sets `needHidePositiveButton = true` only when
 *   licenseType == Family AND duration == Lifetime. When true, the "Upgrade"
 *   and secondary buttons are hidden. With Personal + Lifetime those buttons
 *   remain visible as upsell prompts — undesirable in a patched build.
 *
 *   Layer 1 — inject `getPaidLicense()` static helper into PlusManager class.
 *   Layer 2 — getCachedPlusState()       → return getPaidLicense() immediately.
 *   Layer 3 — setPlusState(incoming)     → replace incoming arg with getPaidLicense().
 *   Layer 4 — fetchAndUpdatePlusState()  → return getPaidLicense() (license screen).
 *   Layer 5 — fetchPlusStateForPromo()   → return getPaidLicense() (promo dialog).
 *   Layer 6 — activateLicenseKey()       → returnEarly() — skip backend re-verify.
 *              Applied via methodOrNull: gracefully skips on builds where this
 *              method is absent or its fingerprint changes. When present, prevents
 *              the "License activated" fallback text on the TV license screen and
 *              avoids unnecessary network calls on phone.
 *
 * ## Version matrix
 *
 *   Version       | Build  | PlusManager | PlusState   | Verified
 *   4.14.0 phone  | phone  | D0/b        | G0/i        | ✓ (original)
 *   4.13.1 phone  | phone  | F0/b        | I0/i        | ✓ (this update)
 *   4.13.0 TV     | TV     | F0/b        | I0/i        | ✓ (original)
 *
 * All fingerprints use stable non-obfuscated anchors (developer log strings,
 * data-class toString literals, opcode shapes) — no obfuscated class/method names.
 */
@Suppress("unused")
val adGuardUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime Premium",
    description = "Unlocks all features locked behind the subscription paywall.",
    default = true,
) {
    compatibleWith(ADGUARD_UNIFIED_COMPATIBILITY)

    execute {
        // Resolve obfuscated type refs from fingerprint results — never hardcoded.
        val paidLicenseType = PaidLicenseFingerprint.classDef.type
        val paidLicenseCtor = PaidLicenseFingerprint.method

        // Second constructor param is the LicenseType enum class
        val licenseTypeClass = paidLicenseCtor.parameters[1].type

        // LicenseDuration.Lifetime: the single static field on the Lifetime singleton class
        val lifetimeDurationField = LifetimeDurationFingerprint.classDef.staticFields.first()

        val plusManagerType = GetPlusStateFingerprint.classDef.type
        val plusStateReturnType = GetPlusStateFingerprint.method.returnType

        // ── Layer 1: inject static helper getPaidLicense() into PlusManager ──────
        //
        // Constructs: PaidLicense("", Family, Lifetime, 1, 9, "")
        //   licenseKey         = "" (no real key needed for Lifetime)
        //   licenseType        = LicenseType.Family
        //   licenseDuration    = LicenseDuration.Lifetime (singleton)
        //   licenseDevCount    = 1
        //   licenseMaxDevCount = 9   (Family plan: up to 9 devices)
        //   licenseKeyOwner    = ""  (nullable field — empty string)
        //
        // Static method: callable from patch layers without a PlusManager instance.
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

        // ── Layer 2: getCachedPlusState() — bypass cache + storage read ──────────
        GetPlusStateFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 3: setPlusState(incoming) — override the incoming state ────────
        //
        // Replaces p1 (incoming PlusState) with getPaidLicense() before persisting.
        // Ensures a server response of Free/Trial can never overwrite the fake license
        // in storage or notify observers with a non-premium state.
        SetPlusStateFingerprint.method.addInstructions(
            0,
            """
            invoke-static {}, $getPaidLicenseMethod
            move-result-object p1
            """.trimIndent(),
        )

        // ── Layer 4: fetchAndUpdatePlusState() — license screen path ─────────────
        StateFlowResolverFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 5: fetchPlusStateForPromo() — promo / check-license dialog ─────
        //
        // Without this, a Free/Unknown response triggers MutableLiveData.postValue(true)
        // on needShowCheckLicenseDialog, which opens the purchase URL in Chrome.
        PromoStateFlowResolverFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 6: activateLicenseKey(String) — skip backend re-verification ───
        //
        // Present in both phone and TV builds (verified in v4.13.1 phone + v4.13.0 TV).
        // TV's TvAboutLicenseViewModel always calls this on license screen open;
        // without it the screen shows "License activated" (error fallback) instead
        // of the "Lifetime" duration from our synthetic PaidLicense.
        //
        // Applied via methodOrNull: gracefully no-ops on builds where this method
        // is absent or its signature has changed. If Layer 6 is skipped, Layers 2-5
        // still fully protect the premium state; only the license screen label may
        // be slightly wrong on TV.
        LicenseKeyActivateFingerprint.methodOrNull?.returnEarly()
    }
}
