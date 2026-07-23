package app.template.patches.slopes.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SLOPES_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val slopesPremiumPatch = bytecodePatch(
    name = "Slopes Premium Unlock",
    description = "Unlocks all premium features."
) {
    compatibleWith(SLOPES_COMPATIBILITY)

    execute {
        // Inject far-future expiration (year 2100 = epoch milli 0x7258118000) into
        // getPassExpiration(). This method has .registers 4 (v0-v2, p0) so const-wide fits.
        // getPremiumStatus() calls this and formats the result into a real date string.
        GetPassExpirationFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, Ljava/time/Instant;->now()Ljava/time/Instant;
                move-result-object v0
                const-wide/32 v1, 0x77359400
                invoke-virtual { v0, v1, v2 }, Ljava/time/Instant;->plusSeconds(J)Ljava/time/Instant;
                move-result-object v0
                return-object v0
            """
        )

        // Force getAutoRenewing() → true so getPremiumStatus() takes the
        // PassStatus$Subscribed branch (Unlimited Plan) rather than PassStatus$Active.
        // .registers 2 — returnEarly(true) only needs const/4 + return, no extra registers.
        GetAutoRenewingFingerprint.method.returnEarly(true)

        // Force isSubscribed() → true to pass the initial gate in getPremiumStatus()
        // that decides between the Subscribed/Expiring/Active/Inactive branches.
        IsSubscribedFingerprint.method.returnEarly(true)
    }
}
