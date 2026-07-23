package app.template.patches.mobioffice.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOBIOFFICE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

private const val LICENSE_LEVEL = "Lcom/mobisystems/registration2/types/LicenseLevel;"
private const val PRICING_PLAN  = "Lcom/mobisystems/registration2/types/PricingPlan;"

@Suppress("unused")
val mobiOfficePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks MobiOffice premium and removes ads.",
) {
    compatibleWith(MOBIOFFICE_COMPATIBILITY)

    execute {

        // ── ROOT LAYER: PricingPlan entitlement injection ─────────────────────
        //
        // PricingPlan is constructed from the MSConnect server FeaturesResult.
        // The server returns "OSP-A"="no" and "license"="free" for free accounts.
        // Patching at this layer propagates through every write to SerialNumber2.g:Z
        // (S(), a0(), C(), b0()) — making the isPremium field true system-wide,
        // including in the Account screen which reads it from the singleton.

        // PricingPlan.c(String)String → "yes"
        // Every feature lookup ("OSP-A", "OSP-PDF", "OSP-A-FONTS", etc.) returns "yes".
        // This is the deepest common denominator: hasPremiumFeature(), d()Z, and all
        // direct c() call sites all get "yes" back.
        PricingPlanFeatureLookupFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string p0, "yes"
                    return-object p0
                """.trimIndent(),
            )
        }

        // PricingPlan.d()Z → true
        // The isPremium check on the plan itself (checks OSP-A="yes").
        // Its return value is the v8 written to SerialNumber2.g:Z at every
        // entitlement commit point. Returning true here makes g:Z = true
        // regardless of what the server sent.
        PricingPlanIsPremiumFingerprint.method.returnEarly(true)

        // LicenseLevel.a(String)LicenseLevel → LicenseLevel.premium
        // Maps server "license" string to enum. Server sends "free"; we return
        // premium instead. This sets PricingPlan.a = LicenseLevel.premium, which:
        //   • feeds getLicenseLevel() on the proxy
        //   • sets the plan name fallback: LicenseLevel.name() = "premium"
        //     → Account screen shows "premium" instead of "Free Edition"
        LicenseLevelFromServerFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, $LICENSE_LEVEL->premium:$LICENSE_LEVEL
                    return-object v0
                """.trimIndent(),
            )
        }

        // ── PROXY LAYER: OsFeaturesCheckProxy ────────────────────────────────
        //
        // Belt-and-suspenders: patch every proxy getter so cached/stale reads
        // that happen before the PricingPlan is reconstructed still return
        // the correct premium values.

        // Edit gates
        M1EditGateFingerprint.method.returnEarly(false)         // false → XOR 1 = canEdit
        CanFreeUsersEditDocsFingerprint.method.returnEarly(true)
        CanFreeUsersEditDocsWithQuotaFingerprint.method.returnEarly(true)

        // Create/save gates
        CanFreeUsersCreateDocsFingerprint.method.returnEarly(true)
        CanFreeUsersCreateDocsWithQuotaFingerprint.method.returnEarly(true)
        CanFreeUsersSaveOutsideDriveFingerprint.method.returnEarly(true)

        // Feature gates
        CanUseAddOnFontsFingerprint.method.returnEarly(true)
        HasPremiumFeatureFingerprint.method.returnEarly(true)

        // Premium flag + tier
        IsPremiumFingerprint.method.returnEarly(true)
        GetLicenseLevelFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, $LICENSE_LEVEL->premium:$LICENSE_LEVEL
                    return-object v0
                """.trimIndent(),
            )
        }

        // Expiry / trial
        IsExpiredFingerprint.method.returnEarly(false)
        IsTrialFingerprint.method.returnEarly(false)

        // Upgrade prompts — return false: suppress all "Upgrade to Premium" prompts
        OfferPremiumProxyFingerprint.method.returnEarly(false)

        // ── AD LAYER: AdLogicFactory ──────────────────────────────────────────
        //
        // AdLogicFactory.p(Z)Z reads SerialNumber2.g:Z directly, bypassing the
        // proxy. Returning false kills all ad types unconditionally.
        AdEligibilityFingerprint.method.returnEarly(false)

        // ── MISSING GATES (found in full proxy audit) ────────────────────────

        // canUseJapaneseFonts() — "offerOfficeSuiteJapaneseFontPack" GTM flag
        CanUseJapaneseFontsFingerprint.method.returnEarly(true)

        // showOxfordDictForPremium() — reads SN2.h:Z (isPremiumWithACE, separate from g:Z)
        // h:Z loaded from encrypted disk cache at startup, unaffected by PricingPlan patch.
        ShowOxfordDictFingerprint.method.returnEarly(true)

        // MonetizationUtils.C()Z (showQuickPdf) — also reads h:Z + wa/b.u() inAppItem
        MonetizationUtilsShowQuickPdfFingerprint.method.returnEarly(true)
    }
}
