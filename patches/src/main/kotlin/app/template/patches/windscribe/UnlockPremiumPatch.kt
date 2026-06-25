package app.template.patches.windscribe

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WINDSCRIBE_COMPATIBILITY

@Suppress("unused")
val windscribeUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Windscribe premium account.",
    default = true
) {
    compatibleWith(WINDSCRIBE_COMPATIBILITY)

    execute {
        listOf(
            LoginPremiumFingerprint,
            LoginBillingPlanFingerprint,
            RegistrationPremiumFingerprint,
            SessionPremiumFingerprint,
            SessionBillingPlanFingerprint,
            SessionUserClassFingerprint,
            SessionReBillFingerprint
        ).forEach { fingerprint ->
            fingerprint.match(classDefBy(fingerprint.definingClass!!)).method.addInstructions(
                0,
                """
                const/4 v0, 0x1
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
                """.trimIndent()
            )
        }

        listOf(LoginTrafficMaxFingerprint, RegistrationTrafficMaxFingerprint, SessionTrafficMaxFingerprint)
            .forEach { fingerprint ->
                fingerprint.match(classDefBy(fingerprint.definingClass!!)).method.addInstructions(
                    0,
                    """
                    const-string v0, "-1"
                    return-object v0
                    """.trimIndent()
                )
            }

        SessionPremiumExpiryFingerprint
            .match(classDefBy(SessionPremiumExpiryFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        listOf(LocationPremiumOnlyFingerprint, LocationPremiumOnlyComponentFingerprint)
            .forEach { fingerprint ->
                fingerprint.match(classDefBy(fingerprint.definingClass!!)).method.addInstructions(
                    0,
                    "const/4 v0, 0x0\nreturn v0"
                )
            }
    }
}
