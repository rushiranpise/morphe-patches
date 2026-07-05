package app.template.patches.wavveboating

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WAVVE_BOATING_COMPATIBILITY

@Suppress("unused")
val wavveBoatingUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Wavve Boating GPS premium features: charts, weather, tide data, and removes subscription paywall.",
    default = true
) {
    compatibleWith(WAVVE_BOATING_COMPATIBILITY)

    execute {
        // 1. hasPaddleLicense() → always true
        UserSettingsHasPaddleLicenseFingerprint.match(
            mutableClassDefBy(UserSettingsHasPaddleLicenseFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 2. hasStripeLicense() → always true
        UserSettingsHasStripeLicenseFingerprint.match(
            mutableClassDefBy(UserSettingsHasStripeLicenseFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 3. LicenseManager.l() — isLicensed() → always true (paddle/stripe/cert check)
        LicenseManagerIsLicensedFingerprint.match(
            mutableClassDefBy(LicenseManagerIsLicensedFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 4. LicenseManager.a() — isUnlicensed() → always false (inverted: true = show paywall)
        LicenseManagerIsUnlicensedFingerprint.match(
            mutableClassDefBy(LicenseManagerIsUnlicensedFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // 5. LicenseManager.b() — isSubscribedOrLicensed() → always true
        LicenseManagerIsSubscribedOrLicensedFingerprint.match(
            mutableClassDefBy(LicenseManagerIsSubscribedOrLicensedFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 6. PlayStoreBilling.e() — isActiveSubscription() → always true
        PlayStoreBillingIsActiveFingerprint.match(
            mutableClassDefBy(PlayStoreBillingIsActiveFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 7. PlayStoreBilling.g() — hasSubscriptionType() → always true
        PlayStoreBillingHasSubscriptionFingerprint.match(
            mutableClassDefBy(PlayStoreBillingHasSubscriptionFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // 8. PlayStoreBilling.f() — isUnpaid() → always false (false = no paywall prompt)
        PlayStoreBillingIsUnpaidFingerprint.match(
            mutableClassDefBy(PlayStoreBillingIsUnpaidFingerprint.definingClass!!)
        ).method.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}
