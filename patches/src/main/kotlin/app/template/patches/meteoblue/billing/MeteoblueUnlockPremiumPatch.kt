package app.template.patches.meteoblue.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.returnEarly

// meteoblue Weather (com.meteoblue.droid) v Cirrus Uncinus 3.0.4
//
// Premium features are gated behind a Google Play Billing subscription
// (package: com.meteoblue.droid). The billing flow:
//
//   BillingDataSource.getStoreStateFlow(product) → Flow<StoreState>
//     StoreState sealed class:
//       PurchasedOK(purchaseDate: Long)  ← user has valid purchase
//       NotPurchasedOK                   ← no purchase found
//       ErrorNoInternet | ErrorBillingUnavailable | ...  ← error states
//
//   BillingRepository.isPurchased(product) maps the StoreState flow:
//     storeState instanceof StoreState.PurchasedOK → Boolean
//
//   AppMainViewModel and WeatherRepository observe isPurchased() and
//   call changePurchaseStatus(Boolean) to update the app's premium state.
//   StoreViewModel also reads getStoreStateFlow() directly to render the
//   purchase screen UI (showing "Purchased" vs purchase button).
//
// On a re-signed build:
//   - Google Play Billing rejects the session (cert mismatch)
//   - getStoreStateFlow() emits NotPurchasedOK or an error state
//   - isPurchased() emits false continuously
//   - All premium features remain locked
//
// Patch strategy:
//
// 1. IsPurchasedFingerprint → BillingRepository.isPurchased()
//    The method creates a mapped Flow that emits (storeState instanceof PurchasedOK).
//    We inject at index 0 to return a Flow that always emits true:
//      kotlinx.coroutines.flow.FlowKt.flowOf(true) returns a cold Flow<Boolean>
//      that emits true once and completes — which is what the observer expects.
//
// 2. GetStoreStateFlowFingerprint → BillingDataSource.getStoreStateFlow()
//    Returns the StoreState flow read directly by StoreViewModel for UI.
//    We return a flow that always emits PurchasedOK with epoch 0 as purchaseDate
//    (Long parameter accepted by PurchasedOK(J) constructor).
//    This makes the store screen show "Purchased" instead of the buy button.
//
// Both class names and method names are non-obfuscated — fingerprints use
// definingClass + name directly and are stable across minor app version updates.
//
@Suppress("unused")
val meteoblueUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all meteoblue Weather premium features by making the billing " +
        "repository always report an active purchase.",
    default = true,
) {
    compatibleWith(Constants.METEOBLUE_COMPATIBILITY)

    execute {
        // Patch 1: BillingRepository.isPurchased(product) → always return Flow<true>
        //
        // Original: returns FlowKt.map(storeStateFlow) { it instanceof PurchasedOK }
        // Patched:  returns FlowKt.flowOf(Boolean.TRUE) — Flow that emits true once
        //
        // FlowKt.flowOf(vararg elements: T): Flow<T> — emits each element in order.
        // Passing a single boxed Boolean true produces Flow<Boolean> emitting true.
        //
        // Smali injected at index 0 (before any original instructions):
        //   const/4 v0, 0x1
        //   invoke-static {v0}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
        //   move-result-object v0
        //   filled-new-array {v0}, [Ljava/lang/Object;
        //   move-result-object v0
        //   invoke-static {v0}, Lkotlinx/coroutines/flow/FlowKt;->flowOf([Ljava/lang/Object;)Lkotlinx/coroutines/flow/Flow;
        //   move-result-object v0
        //   return-object v0
        //
        IsPurchasedFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                invoke-static {v0}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
                move-result-object v0
                filled-new-array {v0}, [Ljava/lang/Object;
                move-result-object v0
                invoke-static {v0}, Lkotlinx/coroutines/flow/FlowKt;->flowOf([Ljava/lang/Object;)Lkotlinx/coroutines/flow/Flow;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )

        // Patch 2: BillingDataSource.getStoreStateFlow(product) → always emit PurchasedOK
        //
        // Original: returns a StateFlow<StoreState> backed by the billing client result
        // Patched:  returns flowOf(new PurchasedOK(purchaseDate)) — Flow emitting PurchasedOK
        //
        // PurchasedOK(J) constructor takes a Long purchaseDate (epoch millis).
        // Use 4102444800000L (2100-01-01 00:00:00 UTC) so the store screen shows
        // "valid 1 year" from a future date rather than 31.12.1969 (Unix epoch 0).
        // The app does not validate this value — it only checks instanceof PurchasedOK.
        //
        // Smali:
        //   new-instance v0, PurchasedOK
        //   const-wide v1, 0x3BA6A7A000L   # 4102444800000 ms = 2100-01-01 UTC
        //   invoke-direct {v0, v1, v2}, PurchasedOK-><init>(J)V
        //   filled-new-array {v0}, [Object;
        //   move-result-object v0
        //   invoke-static {v0}, FlowKt->flowOf([Object;)Flow;
        //   move-result-object v0
        //   return-object v0
        //
        GetStoreStateFlowFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Lcom/meteoblue/droid/data/billing/BillingDataSource${'$'}StoreState${'$'}PurchasedOK;
                const-wide v1, 0x3BA6A7A000L
                invoke-direct {v0, v1, v2}, Lcom/meteoblue/droid/data/billing/BillingDataSource${'$'}StoreState${'$'}PurchasedOK;-><init>(J)V
                filled-new-array {v0}, [Ljava/lang/Object;
                move-result-object v0
                invoke-static {v0}, Lkotlinx/coroutines/flow/FlowKt;->flowOf([Ljava/lang/Object;)Lkotlinx/coroutines/flow/Flow;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )
    }
}
