package app.template.patches.slopes.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SLOPES_COMPATIBILITY
import app.template.patches.shared.returnEarly

@Suppress("unused")
val slopesPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features.",
) {
    compatibleWith(SLOPES_COMPATIBILITY)

    execute {
        // Inject a far-future expiration (now + ~63 years, 2_000_000_000
        // seconds) into the pass/membership-expiration Instant getter.
        // Whatever downstream status computation reads this value will
        // treat the pass as perpetually current rather than expired or absent.
        GetPassExpirationFingerprint.method.addInstructions(
            0,
            """
                invoke-static { }, Ljava/time/Instant;->now()Ljava/time/Instant;
                move-result-object v0
                const-wide/32 v1, 0x77359400
                invoke-virtual { v0, v1, v2 }, Ljava/time/Instant;->plusSeconds(J)Ljava/time/Instant;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )

        // Force the "has an active, non-expired pass" gate to true.
        IsSubscribedFingerprint.method.returnEarly(true)

        // Force the "has any pass/membership on file" gate to true.
        HasAnyPassFingerprint.method.returnEarly(true)
    }
}
