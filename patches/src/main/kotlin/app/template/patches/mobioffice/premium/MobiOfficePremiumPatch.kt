package app.template.patches.mobioffice.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOBIOFFICE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

private const val LICENSE_LEVEL = "Lcom/mobisystems/registration2/types/LicenseLevel;"

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
        // (S(), a0(), C(), b0()) — making the isPremium field true system-wide.

        // PricingPlan.c(String)String → "yes"
        // Every feature lookup ("OSP-A", "OSP-PDF", "OSP-A-FONTS", etc.) returns "yes".
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
        // Return value written to SerialNumber2.g:Z at every entitlement commit.
        PricingPlanIsPremiumFingerprint.method.returnEarly(true)

        // LicenseLevel.a(String)LicenseLevel → LicenseLevel.premium
        // Server sends "free"; we return premium. Sets plan name = "premium".
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
        // that happen before PricingPlan is reconstructed still return correct values.

        // Edit gates
        // 16.5: Q1EditGateFingerprint replaces M1EditGateFingerprint (q1 replaced m1).
        Q1EditGateFingerprint.method.returnEarly(false)         // false → XOR 1 = canEdit
        CanFreeUsersEditDocsFingerprint.method.returnEarly(true)
        CanFreeUsersEditDocsWithQuotaFingerprint.method.returnEarly(true)

        // Create/save gates
        CanFreeUsersCreateDocsFingerprint.method.returnEarly(true)
        CanFreeUsersCreateDocsWithQuotaFingerprint.method.returnEarly(true)
        CanFreeUsersSaveOutsideDriveFingerprint.method.returnEarly(true)

        // Feature gates
        CanUseAddOnFontsFingerprint.method.returnEarly(true)
        CanUseJapaneseFontsFingerprint.method.returnEarly(true)
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

        // Upgrade prompts — false: suppress all "Upgrade to Premium" dialogs
        OfferPremiumProxyFingerprint.method.returnEarly(false)

        // ── H:Z LAYER — Oxford Dict + showQuickPdf ────────────────────────────
        //
        // SerialNumber2.h:Z is loaded from encrypted disk cache at startup,
        // bypassing the PricingPlan patch. These two methods read h:Z directly.
        // 16.5: hb/b replaces wa/b in both fingerprints.

        ShowOxfordDictFingerprint.method.returnEarly(true)
        MonetizationUtilsShowQuickPdfFingerprint.method.returnEarly(true)

        // Note: AdLogicFactory was removed in 16.5. Ad eligibility now reads
        // SerialNumber2.g:Z directly in ad-SDK glue code. Since PricingPlan.d()=true
        // propagates g:Z=true at every entitlement commit, no dedicated fingerprint
        // is needed — premium users are already excluded from ad eligibility checks.
    }
}
