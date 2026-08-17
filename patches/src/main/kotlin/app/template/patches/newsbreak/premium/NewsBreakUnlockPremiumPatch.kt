package app.template.patches.newsbreak.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

@Suppress("unused")
val newsBreakUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks NewsBreak Premium: ad-free reading, premium article access, and Reading Mode.",
    default = true,
) {
    compatibleWith(Constants.NEWSBREAK_COMPATIBILITY)

    execute {
        // 1. isPremium() — SharedPrefs gate for all feature checks.
        IsPremiumFingerprint.method.returnEarly(true)

        // 2. isPremiumOrInTrial() — article reading mode and trial gates.
        IsPremiumOrInTrialFingerprint.method.returnEarly(true)

        // 3. processPremiumContent() — no-op server flag writer (cross-DEX crash if injected).
        ProcessPremiumContentFingerprint.method.apply {
            clearBody()
            returnEarly()
        }

        // 4. processSubscriptionStatus() — no-op, prevents nb_premium_user=false overwrite.
        ProcessSubscriptionStatusFingerprint.method.apply {
            clearBody()
            returnEarly()
        }

        // 5. PremiumStatus.getSubscriptionStatus() → "paid".
        PremiumStatusGetSubscriptionStatusFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "paid"
                return-object v0
            """.trimIndent(),
        )

        // 6. PremiumStatus.getPaidStatus() → "active".
        PremiumStatusGetPaidStatusFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "active"
                return-object v0
            """.trimIndent(),
        )

        // 7. SocialProfile.isPremiumUser() → true.
        SocialProfileIsPremiumUserFingerprint.method.returnEarly(true)

        // 8. ParticleAccount.isGuestAccount() → false.
        //    isGuestAccount() returns true when accountType==0 (default on re-signed builds
        //    where Google OAuth fails). E1.F.n() delegates to this method, and when true
        //    the profile header shows the "Try Premium for FREE — no ads" guestPremiumBanner
        //    unconditionally on every tab switch, regardless of subscription status.
        //    returnEarly(false) ensures the app always treats the user as a full account.
        IsGuestAccountFingerprint.method.returnEarly(false)
    }
}

@Suppress("unused")
val newsBreakLiteUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks NewsBreak Lite Premium: ad-free reading, premium article access, and Reading Mode.",
    default = true,
) {
    compatibleWith(Constants.NEWSBREAKLITE_COMPATIBILITY)

    execute {
        IsPremiumFingerprint.method.returnEarly(true)
        IsPremiumOrInTrialFingerprint.method.returnEarly(true)
        ProcessPremiumContentFingerprint.method.apply {
            clearBody()
            returnEarly()
        }
        ProcessSubscriptionStatusFingerprint.method.apply {
            clearBody()
            returnEarly()
        }
        PremiumStatusGetSubscriptionStatusFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "paid"
                return-object v0
            """.trimIndent(),
        )
        PremiumStatusGetPaidStatusFingerprint.method.addInstructions(
            0,
            """
                const-string v0, "active"
                return-object v0
            """.trimIndent(),
        )
        SocialProfileIsPremiumUserFingerprint.method.returnEarly(true)
        IsGuestAccountFingerprint.method.returnEarly(false)
    }
}
