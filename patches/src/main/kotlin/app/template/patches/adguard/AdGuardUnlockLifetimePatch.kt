package app.template.patches.adguard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.ADGUARD_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/**
 * AdGuard — Unlock Lifetime Premium
 *
 * ## Architecture
 *
 * AdGuard's license state flows through a sealed class hierarchy:
 *
 *   PlusState (interface G0/i)
 *   ├── PaidLicense  — active subscription / purchase
 *   │     fields: licenseKey, licenseType (LicenseType enum), licenseDuration
 *   │              (LicenseDuration sealed), devCount, maxDevCount, keyOwner
 *   ├── Free         — no license
 *   ├── Trial        — trial period
 *   └── (others)
 *
 *   LicenseDuration
 *   ├── Lifetime     — singleton object
 *   └── (Annual, etc.)
 *
 *   LicenseType (enum)
 *   └── Personal, Family, Standard, Beta, Bonus, Custom
 *
 * ## PlusManager state propagation
 *
 * PlusManager (D0/b) is the single source of truth. State flows through:
 *
 *   getCachedPlusState()           — reads in-memory cache (field `l`)
 *                                    falls back to storage, writes back
 *
 *   setPlusState(PlusState)        — persists to storage + notifies observers
 *
 *   fetchAndUpdatePlusState(...)   — license screen path (AboutLicenseViewModel):
 *                                    network → cache → StateFlow → UI
 *
 *   fetchPlusStateForPromo(...)    — promo/check-license dialog path (PromoViewModel):
 *                                    network → MutableLiveData → dialog decision
 *                                    Free/Unknown → "Check license" dialog → purchase URL
 *
 * ## Patch strategy
 *
 * A static helper `getPaidLicense()` is injected into PlusManager. It constructs
 * a synthetic PaidLicense with Lifetime duration and Personal type. All four state
 * propagation methods are patched to return this fake license before any real logic
 * runs, so neither the network path nor the storage path is ever consulted.
 *
 *   Layer 1 — inject `getPaidLicense()` static helper into PlusManager.
 *   Layer 2 — getCachedPlusState() returns getPaidLicense() immediately.
 *   Layer 3 — setPlusState(incoming) replaces incoming state with getPaidLicense()
 *              before persisting — prevents a server response from overwriting.
 *   Layer 4 — fetchAndUpdatePlusState() returns getPaidLicense() (license screen).
 *   Layer 5 — fetchPlusStateForPromo() returns getPaidLicense() (promo dialog).
 */
@Suppress("unused")
val adGuardUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime Premium",
    description = "Unlocks all features locked behind the subscription paywall.",
    default = true,
) {
    compatibleWith(ADGUARD_COMPATIBILITY)

    execute {
        // Resolve the obfuscated type refs we need for the injected constructor call.
        // These are read from fingerprint results at patch time — we never hardcode
        // the obfuscated names (D0/b, G0/i$n, G0/i$m, G0/i$l$a) into the patch.
        val paidLicenseType = PaidLicenseFingerprint.classDef.type
        val paidLicenseCtor = PaidLicenseFingerprint.method

        // LicenseType enum: second param of PaidLicense <init>
        val licenseTypeClass = paidLicenseCtor.parameters[1].type

        // LicenseDuration.Lifetime: single static field on the Lifetime class
        val lifetimeDurationField = LifetimeDurationFingerprint.classDef.staticFields.first()

        val plusManagerType = GetPlusStateFingerprint.classDef.type
        val plusStateReturnType = GetPlusStateFingerprint.method.returnType

        // ── Layer 1: inject static helper getPaidLicense() into PlusManager ──────
        //
        // Constructs: PaidLicense("", Family, Lifetime, 1, 9, "")
        //   licenseKey        = "" (no key needed for Lifetime)
        //   licenseType       = LicenseType.Family  ← not Personal; see note below
        //   licenseDuration   = LicenseDuration.Lifetime (singleton)
        //   licenseDevCount   = 1
        //   licenseMaxDevCount= 9  (Family plan = up to 9 devices)
        //   licenseKeyOwner   = "" (nullable — empty string)
        //
        //   Why Family not Personal:
        //   The ViewModel sets field j=true on the PaidLicense UI state only when
        //   licenseType==Family && duration==Lifetime. When j=true, i()=true, and the
        //   Fragment hides both upgrade buttons (a1(d$b) hides "Upgrade" button r,
        //   T0(b$b) hides secondary button s). With Personal+Lifetime j=false, so
        //   button r shows "Upgrade" (R.string.about_license_upgrade) as an upsell
        //   to upgrade from Personal→Family — undesirable for a patched install.
        //
        // Injected as a static method so it can be called from any patch layer
        // without needing a PlusManager instance reference (which is p0 in
        // non-static methods and would complicate the injection smali).
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
        //
        // Short-circuits before the iget-object cache read. Any subsequent call
        // that reads the cached field directly (bypassing this method) is also
        // covered by Layer 3 which writes the fake license back to storage.
        GetPlusStateFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 3: setPlusState(incoming) — override the incoming state ────────
        //
        // Intercepts at the top of the method, replacing p1 (the incoming PlusState)
        // with getPaidLicense(). The method then proceeds normally — persisting our
        // fake license to storage and notifying observers. This closes the loop:
        // even if a server response triggers setPlusState(Free), it persists
        // PaidLicense instead, keeping the in-memory and on-disk state consistent.
        SetPlusStateFingerprint.method.addInstructions(
            0,
            """
            invoke-static {}, $getPaidLicenseMethod
            move-result-object p1
            """.trimIndent(),
        )

        // ── Layer 4: fetchAndUpdatePlusState() — license screen path ─────────────
        //
        // Returns immediately with getPaidLicense() before the coroutine is
        // dispatched, so the network call and StateFlow update never occur.
        // Covers AboutLicenseViewModel → license screen UI.
        StateFlowResolverFingerprint.method.addInstructions(0, callHelper)

        // ── Layer 5: fetchPlusStateForPromo() — promo / check-license dialog ─────
        //
        // Without this, a network response of Free/Unknown triggers
        // MutableLiveData.postValue(true) on needShowCheckLicenseDialog, which
        // shows a dialog and opens the purchase URL in Chrome.
        // Returning PaidLicense prevents the dialog from appearing.
        PromoStateFlowResolverFingerprint.method.addInstructions(0, callHelper)
    }
}
