package app.template.patches.meteoblue.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPurchasedFingerprint ─────────────────────────────────────────────────────
//
// Targets: BillingRepository.isPurchased(PurchasableProduct)Flow<Boolean>
//   Lcom/meteoblue/droid/data/repository/BillingRepository;
//   -> isPurchased(Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface$PurchasableProduct;)Lkotlinx/coroutines/flow/Flow;
//
// This is the central premium gate. The method:
//   1. Calls BillingDataSource.getStoreStateFlow(product) → Flow<StoreState>
//   2. Maps: storeState instanceof StoreState.PurchasedOK → Boolean
//   3. Returns Flow<Boolean> — observed by every premium feature check
//
// The observer flow yields false whenever the billing session has no valid
// purchase (re-signed APK, no Play subscription). Patching this to emit
// true unconditionally bypasses the entire Google Play Billing gate.
//
// Both class name and method name are NON-OBFUSCATED (package-private class
// com.meteoblue.droid.data.repository.BillingRepository, method isPurchased).
// Using definingClass + name is stable and safe across minor app versions.
//
// Fingerprint uses a stable anchor: the log string "isPurchased: " which appears
// in the coroutine body of this exact method, uniquely identifying it.
//
// DEX: classes4.dex
// Access: public, non-static
// Return: Lkotlinx/coroutines/flow/Flow;
// Params: [Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface$PurchasableProduct;]
//
object IsPurchasedFingerprint : Fingerprint(
    definingClass = "Lcom/meteoblue/droid/data/repository/BillingRepository;",
    name = "isPurchased",
    returnType = "Lkotlinx/coroutines/flow/Flow;",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface\$PurchasableProduct;"),
)

// ── GetStoreStateFlowFingerprint ───────────────────────────────────────────────
//
// Targets: BillingDataSource.getStoreStateFlow(PurchasableProduct)Flow<StoreState>
//   Lcom/meteoblue/droid/data/billing/BillingDataSource;
//   -> getStoreStateFlow(Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface$PurchasableProduct;)Lkotlinx/coroutines/flow/Flow;
//
// Returns a StateFlow<StoreState> for the given product. The downstream
// isPurchased() maps this to Boolean by checking instanceof PurchasedOK.
// Also used directly by StoreViewModel to show purchase UI state.
//
// Non-obfuscated class and method name — safe to pin directly.
// DEX: classes4.dex
// Access: public, non-static
// Return: Lkotlinx/coroutines/flow/Flow;
// Params: [Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface$PurchasableProduct;]
//
object GetStoreStateFlowFingerprint : Fingerprint(
    definingClass = "Lcom/meteoblue/droid/data/billing/BillingDataSource;",
    name = "getStoreStateFlow",
    returnType = "Lkotlinx/coroutines/flow/Flow;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/meteoblue/droid/data/repository/BillingRepositoryInterface\$PurchasableProduct;"),
)
