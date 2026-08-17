package app.template.patches.oxygenupdater.premium

import app.morphe.patcher.Fingerprint

/**
 * x50.j(int, String) — setSkuState(int state, String sku)
 *
 * Sets the SkuState for a given SKU in the BillingRepository's internal
 * LinkedHashMap<String, StateFlow<SkuState>>. Called from:
 *   - onBillingSetupFinished / queryPurchasesAsync result processing
 *   - onPurchasesUpdated purchase callback
 *   - explicit state reset when SKU product details are unavailable
 *
 * Each call creates a new l50(state) and calls oc5.h() on the matching
 * StateFlow to push the new SkuState. Downstream, the v50 coroutine
 * Collector maps SkuState(5) to isAdFree=true, persists the result to
 * SharedPreferences, and emits showAds=false into the Boolean flow
 * observed by the Compose UI.
 *
 * Patch: prepend "const/4 p1, 0x5" to override the state int parameter
 * to 5 (PurchasedAndAcknowledged) before the method reads it.
 *
 * Location: classes.dex
 * Class:    Lx50;  (BillingRepository)
 * Method:   j(ILjava/lang/String;)Ljava/lang/Object;
 * Access:   public final
 * Smali verified: YES (x50.smali lines 2493–2560)
 *
 * Unique anchor: the only method in the app that:
 *   - takes (int, String) and returns Object
 *   - constructs new Ll50;(int) from its first parameter
 *   - logs "[setSkuState] unknown SKU: " on the error path
 */
internal object SetSkuStateFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    parameters = listOf("I", "Ljava/lang/String;"),
    strings = listOf("[setSkuState] unknown SKU: "),
)
