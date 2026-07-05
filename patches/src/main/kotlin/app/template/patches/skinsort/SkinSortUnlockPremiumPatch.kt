package app.template.patches.skinsort

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.SKINSORT_COMPATIBILITY

private val skinSortDisablePaywallConfigPatch = rawResourcePatch(
    name = "Disable Paywall Config",
    description = "Disables SkinSort local paywall configuration.",
    default = false,
) {
    compatibleWith(SKINSORT_COMPATIBILITY)

    execute {
        get("assets/json/path-configuration.json")?.replaceText(
            "\"scan_paywall_limit\": 5" to "\"scan_paywall_limit\": 999999",
            "\"sale_offering_enabled\": true" to "\"sale_offering_enabled\": false",
            "\"paywall_type\": \"revenueCatV2\"" to "\"paywall_type\": \"none\"",
        )
    }
}

@Suppress("unused")
val skinSortUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks SkinSort premium features."
) {
    compatibleWith(SKINSORT_COMPATIBILITY)
    dependsOn(skinSortDisablePaywallConfigPatch)

    execute {
        fun replaceWith(fingerprint: Fingerprint, instructionsText: String) {
            fingerprint
                .match(classDefBy(fingerprint.definingClass!!))
                .method
                .apply {
                    removeInstructions(0, instructions.count())
                    addInstructions(0, instructionsText.trimIndent())
                }
        }

        fun forceTrue(fingerprint: Fingerprint) = replaceWith(
            fingerprint,
            """
            const/4 v0, 0x1
            return v0
            """
        )

        fun noOp(fingerprint: Fingerprint) = replaceWith(fingerprint, "return-void")

        replaceWith(
            SkinSortEntitlementInfosGetActiveFingerprint,
            """
            iget-object v0, p0, Lcom/revenuecat/purchases/EntitlementInfos;->all:Ljava/util/Map;
            return-object v0
            """
        )
        forceTrue(SkinSortEntitlementInfoIsActiveFingerprint)

        val premiumProducts = """
            new-instance v0, Ljava/util/HashSet;
            invoke-direct {v0}, Ljava/util/HashSet;-><init>()V
            const-string v1, "Premium"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "premium"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "AndroidSaleOffering1999"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "skinsort_premium"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "skinsort_premium_annual"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            const-string v1, "skinsort_premium_monthly"
            invoke-interface {v0, v1}, Ljava/util/Set;->add(Ljava/lang/Object;)Z
            return-object v0
        """.trimIndent()
        replaceWith(SkinSortCustomerInfoGetActiveSubscriptionsFingerprint, premiumProducts)
        replaceWith(SkinSortCustomerInfoGetAllPurchasedProductIdsFingerprint, premiumProducts)

        SkinSortPremiumStatusReplyFingerprint
            .match(classDefBy(SkinSortPremiumStatusReplyFingerprint.definingClass!!))
            .method
            .apply {
                val index = instructions.indexOfFirst {
                    it.opcode.name == "iget-boolean" && it.toString().contains("Lod/k0;->d:Z")
                }
                if (index >= 0) {
                    addInstructions(
                        index + 1,
                        """
                        const/4 v5, 0x1
                        """.trimIndent()
                    )
                }
            }

        SkinSortPremiumStatusBroadcastFingerprint
            .match(classDefBy(SkinSortPremiumStatusBroadcastFingerprint.definingClass!!))
            .method
            .apply {
                val index = instructions.indexOfFirst {
                    it.opcode.name == "invoke-virtual" && it.toString().contains("->putExtra(Ljava/lang/String;Z)")
                }
                if (index >= 0) {
                    addInstructions(index, "const/4 v1, 0x1")
                }
            }

        SkinSortPaywallGateCallbackFingerprint
            .match(classDefBy(SkinSortPaywallGateCallbackFingerprint.definingClass!!))
            .method
            .addInstructions(
                0,
                """
                iget v0, p0, Lnd/k;->c:I
                if-eqz v0, :morphe_skinsort_original_callback
                iget-object v0, p0, Lnd/k;->d:Ljava/lang/Object;
                check-cast v0, Lsd/u;
                iget-object v1, p0, Lnd/k;->f:Ljava/lang/Object;
                check-cast v1, Lu0/f;
                const-string v2, "PaywallService"
                const-string v3, "User is already premium, blocking paywall"
                invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                invoke-virtual {v0, v1}, Lsd/u;->c(Lu0/f;)V
                sget-object p1, Lfe/c0;->a:Lfe/c0;
                return-object p1
                :morphe_skinsort_original_callback
                nop
                """.trimIndent()
            )

        noOp(SkinSortPairIpLicenseCheckFingerprint)
        replaceWith(
            SkinSortPairIpApplicationAttachFingerprint,
            """
            invoke-super {p0, p1}, Lcom/skinsort/SkinSortApplication;->attachBaseContext(Landroid/content/Context;)V
            return-void
            """
        )
        forceTrue(SkinSortPairIpLicenseContentProviderOnCreateFingerprint)
    }
}

private fun java.io.File.replaceText(vararg replacements: Pair<String, String>) {
    if (!exists()) return
    var text = readText()
    replacements.forEach { (old, new) ->
        text = text.replace(old, new)
    }
    writeText(text)
}
