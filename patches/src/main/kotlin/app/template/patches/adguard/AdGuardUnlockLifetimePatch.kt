package app.template.patches.adguard

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.ADGUARD_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod

/**
 * Unlocks Lifetime Premium in AdGuard for Android.
 *
 * ## State flow (two paths)
 *
 * Path 1 — License screen (A2/b AboutLicenseViewModel):
 *   B0/a.A() StateFlow → mapper B0/a$k → B0/a.u() → B0/a.t() → network
 *
 * Path 2 — Promo/Check license dialog (A2/T PromoViewModel):
 *   B0/a field StateFlow → mapper B0/a$j → B0/a.r() → B0/a.q() → network
 *   q() result feeds needShowCheckLicenseDialog via MutableLiveData.postValue()
 *   When Free/Unknown → shows "Check license" dialog → opens purchase URL in Chrome
 *
 * ## Patch layers
 *
 *   Layer 1 — inject `getPaidLicense()` static helper into B0/a.
 *   Layer 2 — B0/a.s() → return PaidLicense (non-reactive cache reads).
 *   Layer 3 — B0/a.B(LE0/i) → replace incoming state (event bus).
 *   Layer 4 — B0/a.t() → return PaidLicense (license screen StateFlow).
 *   Layer 5 — B0/a.q() → return PaidLicense (promo/check-license dialog StateFlow).
 */
@Suppress("unused")
val adGuardUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime Premium",
    description = "Unlocks all features locked behind the subscription paywall.",
    default = true,
) {
    compatibleWith(ADGUARD_COMPATIBILITY)

    execute {
        val licenseTypeClass = PaidLicenseFingerprint.method.parameters[1].type
        val lifetimeDurationInstance = LifetimeDurationFingerprint.classDef.staticFields.first()

        // Inject static helper: PaidLicense("", Personal, Lifetime, 1, 3, "")
        val getPaidLicenseMethod = ImmutableMethod(
            GetPlusStateFingerprint.classDef.type,
            "getPaidLicense",
            null,
            GetPlusStateFingerprint.method.returnType,
            AccessFlags.STATIC.value,
            null,
            null,
            MutableMethodImplementation(7)
        ).toMutable().apply {
            addInstructions(
                0,
                """
                    new-instance v0, ${PaidLicenseFingerprint.classDef.type}
                    const-string v1, ""
                    sget-object v2, $licenseTypeClass->Personal:$licenseTypeClass
                    sget-object v3, $lifetimeDurationInstance
                    const/4 v4, 0x1
                    const/4 v5, 0x3
                    const-string v6, ""
                    invoke-direct/range {v0 .. v6}, ${PaidLicenseFingerprint.method}
                    return-object v0
                """.trimIndent()
            )
        }

        GetPlusStateFingerprint.classDef.methods.add(getPaidLicenseMethod)

        // Layer 2: s() — non-reactive cache reads
        GetPlusStateFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $getPaidLicenseMethod
                move-result-object v0
                return-object v0
            """.trimIndent()
        )

        // Layer 3: B(LE0/i) — event bus propagation
        SetPlusStateFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $getPaidLicenseMethod
                move-result-object p1
            """.trimIndent()
        )

        // Layer 4: t() — license screen StateFlow (AboutLicenseViewModel path)
        StateFlowResolverFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $getPaidLicenseMethod
                move-result-object v0
                return-object v0
            """.trimIndent()
        )

        // Layer 5: q() — promo/check-license dialog StateFlow (PromoViewModel path)
        // Without this, network returns Free → dialog shows "Check license" → opens purchase URL
        PromoStateFlowResolverFingerprint.method.addInstructions(
            0,
            """
                invoke-static {}, $getPaidLicenseMethod
                move-result-object v0
                return-object v0
            """.trimIndent()
        )
    }
}
