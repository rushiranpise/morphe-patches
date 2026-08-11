package app.template.patches.proxyman

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROXYMAN_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks Lifetime in Proxyman — Network Debugger (com.proxyman.proxymanandroid).
 *
 * ## Architecture
 *
 * Proxyman gates features through a data class (Kotlin: LicenseEntitlement) and
 * two methods: IsFeatureAllowed(featureEnum)Z and GetFeatureLimit(featureEnum)I.
 * A paywall is shown via SharedPreferences-tracked triggers on MainActivity.onResume.
 * PairIP v2 adds a licensing SDK that runs before the app starts.
 *
 * ## Patch layers
 *
 * 1. IsFeatureAllowed → true       All feature access checks pass immediately.
 * 2. GetFeatureLimit → MAX_VALUE   All feature usage counts become unlimited.
 * 3. AutoPaywall → no-op           Paywall bottom sheet never appears on resume.
 * 4. LicenseEntitlement ctor       Every subscription state instance reports
 *                                   isPro=true, planType=LIFETIME, isLifetime=true,
 *                                   state=ACTIVE_LIFETIME from construction.
 * 5. PairIP checkLicense → no-op   Licensing service connection never made.
 * 6. PairIP LicenseActivity        Nuclear fallback — activity exits immediately
 *                                   if somehow launched despite Layer 5.
 */
@Suppress("unused")
val proxymanUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime",
    description = "Unlocks all Lifetime features in Proxyman.",
    default = true,
) {
    compatibleWith(PROXYMAN_COMPATIBILITY)

    execute {

        // ── 1. Feature access gate → true ─────────────────────────────────────
        // Located by IGET_BOOLEAN on isPro field in the only Z-returning method
        // whose class also contains the 0x7fffffff (MAX_VALUE) literal.
        IsFeatureAllowedFingerprint.method.returnEarly(true)

        // ── 2. Feature usage-count gate → MAX_VALUE ────────────────────────────
        // Located by 0x7fffffff literal. Returns per-feature cap; MAX_VALUE = unlimited.
        GetFeatureLimitFingerprint.method.addInstructions(
            0,
            "const v0, 0x7fffffff\nreturn v0",
        )

        // ── 3. Auto-paywall → no-op ────────────────────────────────────────────
        // Located by "auto_paywall_first_foreground_at" SharedPrefs key.
        AutoPaywallFingerprint.method.returnEarly()

        // ── 4. LicenseEntitlement constructor → inject Lifetime values ──────────
        // Located via "LicenseEntitlement(isPremium=" toString anchor + param shape.
        // The planType and state enum class names drift with R8 every update;
        // we read them from the method's own parameter type list at patch time,
        // which is always correct regardless of what letter R8 assigns.
        val ctor = UserSubscriptionConstructorFingerprint.method
        val planTypeClass  = ctor.parameterTypes[1].toString() // b:enum (MONTHLY/YEARLY/LIFETIME)
        val stateClass     = ctor.parameterTypes[4].toString() // e:enum (NONE/ACTIVE_LIFETIME/…)

        ctor.addInstructions(
            0,
            // p1 = isPro      → true
            // p2 = planType   → LIFETIME   (enum field d, 4th value, 0-indexed: a/b/c/d)
            // p4 = isLifetime → true
            // p5 = state      → ACTIVE_LIFETIME  (enum field c, 3rd value: a/b/c)
            """
                const/4 p1, 0x1
                sget-object p2, $planTypeClass->d:$planTypeClass
                const/4 p4, 0x1
                sget-object p5, $stateClass->c:$stateClass
            """.trimIndent(),
        )

        // ── 5. PairIP checkLicense → no-op ────────────────────────────────────
        PairIPCheckLicenseFingerprint.method.returnEarly()

        // ── 6. PairIP LicenseActivity → nuclear fallback ───────────────────────
        // Return immediately after super.onStart() so neither
        // showPaywallAndCloseApp() nor showErrorDialog() executes.
        PairIPLicenseActivityFingerprint.method.addInstructions(
            0,
            "invoke-super {p0}, Landroid/app/Activity;->onStart()V\nreturn-void",
        )
    }
}
