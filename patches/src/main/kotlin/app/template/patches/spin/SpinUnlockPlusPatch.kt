package app.template.patches.spin

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPIN_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val spinUnlockPlusPatch = bytecodePatch(
    name = "Unlock SPIN Plus",
    description = "Unlocks SPIN Plus",
    default = true,
) {
    compatibleWith(SPIN_COMPATIBILITY)

    execute {
        fun replaceWith(fingerprint: Fingerprint, instructions: String) {
            fingerprint
                .match(classDefBy(fingerprint.definingClass!!))
                .method
                .apply {
                    clearBody()
                    addInstructions(0, instructions.trimIndent())
                }
        }

        fun forceTrue(fingerprint: Fingerprint) = replaceWith(
            fingerprint,
            """
            const/4 v0, 0x1
            return v0
            """
        )

        forceTrue(SpinSubscriptionGetterFingerprint)
        forceTrue(SpinSubscriptionComponentFingerprint)
        replaceWith(
            SpinSubscriptionFlowFingerprint,
            """
            new-instance v0, Lcom/nationaledtech/spinbrowser/plus/subscription/SubscriptionState;
            sget-object v1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            move-object v2, v1
            const/4 v3, 0x0
            move-object v4, v1
            move-object v5, v1
            const/4 v6, 0x0
            const/4 v7, 0x0
            const/4 v8, 0x1
            invoke-direct/range {v0 .. v8}, Lcom/nationaledtech/spinbrowser/plus/subscription/SubscriptionState;-><init>(Ljava/lang/Boolean;Ljava/lang/Boolean;Lcom/android/billingclient/api/ProductDetails;Ljava/lang/Boolean;Ljava/lang/Boolean;Lcom/android/billingclient/api/ProductDetails;Ljava/util/List;Z)V
            return-object v0
            """
        )
    }
}
