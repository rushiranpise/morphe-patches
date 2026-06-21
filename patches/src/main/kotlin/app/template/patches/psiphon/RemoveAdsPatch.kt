package app.template.patches.psiphon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.blockerhero.ensureRegisters
import app.template.patches.shared.Constants.PSIPHON_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val removeAdsPatch = bytecodePatch(
    name = "Remove Ads / Unlock Premium",
    description = "Forces SubscriptionStateImpl.getStatus() (LB2/c.h) to always return " +
        "HAS_UNLIMITED_SUBSCRIPTION and getPurchase() (LB2/c.g) to return a well-formed " +
        "fake Purchase, removing ads and the upgrade button/banner without crashing on " +
        "the now-expected non-null Purchase object.",
    default = true,
) {
    compatibleWith(PSIPHON_COMPATIBILITY)

    execute {
        // h() is the single source of truth for subscription status. j0.hasValidPurchase()
        // (c) computes from it via virtual dispatch, and UI code (com/psiphon3/j upgrade
        // button + rate-limit banner) reads it directly — so this one patch covers both
        // ad gating and the upgrade-prompt UI.
        GetSubscriptionStatusFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                sget-object v0, LB2/j0${'$'}a;->e:LB2/j0${'$'}a;
                return-object v0
                """.trimIndent()
            )
        }

        // getPurchase() → a real, well-formed Purchase instead of the (often null) field.
        // h() now always reports a subscription, so any code reading g() afterward must
        // get a usable object — otherwise getProducts().get(0) / getOrderId() etc. crash.
        // Needs 3 scratch registers (v0-v2); original method only had 2 (1 local + this).
        GetPurchaseFingerprint.method.apply {
            ensureRegisters(3)
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, Lcom/android/billingclient/api/Purchase;
                const-string v1, "{\"productId\":\"basic_ad_free_subscription\",\"purchaseToken\":\"morphe-patch-token\",\"orderId\":\"GPA.morphe-patch\",\"purchaseState\":1,\"purchaseTime\":0,\"acknowledged\":true}"
                const-string v2, "morphe-patch-signature"
                invoke-direct {v0, v1, v2}, Lcom/android/billingclient/api/Purchase;-><init>(Ljava/lang/String;Ljava/lang/String;)V
                return-object v0
                """.trimIndent()
            )
        }
    }
}
