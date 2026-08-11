package app.template.patches.relane.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.RELANE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.ensureRegisters

private const val ENTITLEMENT_ID = "premium_vpn"

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Relane VPN Premium by patching the RevenueCat SDK to " +
        "always report an active \"$ENTITLEMENT_ID\" entitlement, enabling premium " +
        "servers, unlimited data, and premium sync.",
) {
    compatibleWith(RELANE_COMPATIBILITY)

    execute {

        // ── Layer 1: EntitlementInfos.getActive() ─────────────────────────────
        //
        // Non-null params verified from smali checkNotNullParameter calls:
        //   identifier, periodType, latestPurchaseDate, originalPurchaseDate,
        //   store, productIdentifier, ownershipType, jsonObject
        //
        // Register constraints:
        //   invoke-direct (format 35c): each register must be v0–v15.
        //   invoke-direct/range: registers must be consecutive starting at v0.
        //   const-wide/16 (format 21s): register is 8-bit → v0–v255 OK.
        //
        // Register plan — stays entirely within v0–v16 (17 slots):
        //
        //   Step 1: Build two Date(0) objects using v0–v2 as scratch.
        //     const-wide/16 v1, 0   → v1:v2 = 0L (the epoch time argument)
        //     new-instance v0, Date
        //     invoke-direct {v0,v1,v2}  → v0 = Date(0) for latestPurchaseDate
        //     move-object v5, v0        → park latestPurchaseDate in v5
        //     new-instance v6, Date
        //     invoke-direct {v6,v1,v2}  → v6 = Date(0) for originalPurchaseDate
        //     (v1/v2 still hold 0L, not needed after this)
        //
        //   Step 2: Build EntitlementInfo across v0–v16 for invoke-direct/range.
        //     new-instance v0, EntitlementInfo   ← safe: v5/v6 already saved
        //     v1  = identifier (String)
        //     v2  = isActive (1)
        //     v3  = willRenew (1)
        //     v4  = PeriodType.NORMAL
        //     v5  = latestPurchaseDate (Date from Step 1)
        //     v6  = originalPurchaseDate (Date from Step 1)
        //     v7  = null (expirationDate — nullable)
        //     v8  = Store.PLAY_STORE
        //     v9  = productIdentifier (String)
        //     v10 = null (productPlanIdentifier — nullable)
        //     v11 = 0 (isSandbox)
        //     v12 = null (unsubscribeDetectedAt — nullable)
        //     v13 = null (billingIssueDetectedAt — nullable)
        //     v14 = OwnershipType.PURCHASED
        //     v15 = new JSONObject()
        //     v16 = VerificationResult.NOT_REQUESTED
        //     invoke-direct/range {v0..v16}  ← range form, no 4-bit limit
        //
        //   Step 3: Wrap EntitlementInfo in Map and return.
        //     v0 holds the constructed EntitlementInfo.
        //     Reuse v1/v2 for the Pair and array intermediates.

        entitlementInfosGetActiveFingerprint.method.apply {
            ensureRegisters(17)
            clearBody()
            addInstructions(0, """
                # Step 1: build two Date(0) objects using v0–v2 (all <= v15, 35c safe)
                const-wide/16 v1, 0x0
                new-instance v0, Ljava/util/Date;
                invoke-direct {v0, v1, v2}, Ljava/util/Date;-><init>(J)V
                move-object v5, v0
                new-instance v6, Ljava/util/Date;
                invoke-direct {v6, v1, v2}, Ljava/util/Date;-><init>(J)V

                # Step 2: build EntitlementInfo across v0–v16 for range invoke
                new-instance v0, Lcom/revenuecat/purchases/EntitlementInfo;
                const-string v1, "$ENTITLEMENT_ID"
                const/4 v2, 0x1
                const/4 v3, 0x1
                sget-object v4, Lcom/revenuecat/purchases/PeriodType;->NORMAL:Lcom/revenuecat/purchases/PeriodType;
                # v5 = latestPurchaseDate (Date from step 1)
                # v6 = originalPurchaseDate (Date from step 1)
                const/4 v7, 0x0
                sget-object v8, Lcom/revenuecat/purchases/Store;->PLAY_STORE:Lcom/revenuecat/purchases/Store;
                const-string v9, "$ENTITLEMENT_ID"
                const/4 v10, 0x0
                const/4 v11, 0x0
                const/4 v12, 0x0
                const/4 v13, 0x0
                sget-object v14, Lcom/revenuecat/purchases/OwnershipType;->PURCHASED:Lcom/revenuecat/purchases/OwnershipType;
                new-instance v15, Lorg/json/JSONObject;
                invoke-direct {v15}, Lorg/json/JSONObject;-><init>()V
                sget-object v16, Lcom/revenuecat/purchases/VerificationResult;->NOT_REQUESTED:Lcom/revenuecat/purchases/VerificationResult;
                invoke-direct/range {v0 .. v16}, Lcom/revenuecat/purchases/EntitlementInfo;-><init>(Ljava/lang/String;ZZLcom/revenuecat/purchases/PeriodType;Ljava/util/Date;Ljava/util/Date;Ljava/util/Date;Lcom/revenuecat/purchases/Store;Ljava/lang/String;Ljava/lang/String;ZLjava/util/Date;Ljava/util/Date;Lcom/revenuecat/purchases/OwnershipType;Lorg/json/JSONObject;Lcom/revenuecat/purchases/VerificationResult;)V

                # Step 3: wrap in Map<String, EntitlementInfo> and return
                const-string v1, "$ENTITLEMENT_ID"
                invoke-static {v1, v0}, Lkotlin/TuplesKt;->to(Ljava/lang/Object;Ljava/lang/Object;)Lkotlin/Pair;
                move-result-object v0
                const/4 v1, 0x1
                new-array v1, v1, [Lkotlin/Pair;
                const/4 v2, 0x0
                aput-object v0, v1, v2
                invoke-static {v1}, Lkotlin/collections/MapsKt;->mapOf([Lkotlin/Pair;)Ljava/util/Map;
                move-result-object v0
                return-object v0
            """.trimIndent())
        }

        // ── Layer 2: EntitlementInfo.isActive() ──────────────────────────────
        // Uses only v0; .registers 2 is sufficient — no ensureRegisters needed.

        entitlementInfoIsActiveFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
