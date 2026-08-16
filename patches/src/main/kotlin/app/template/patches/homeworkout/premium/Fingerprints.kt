package app.template.patches.homeworkout.premium

import app.morphe.patcher.Fingerprint

// ---------------------------------------------------------------------------
// Leap Fitness Group — shared IapSp class fingerprint
//
// Targets Z6/a (IapSp.kt) — the SharedPreferences-backed IAP state class
// shared across all 47 Leap Fitness Group apps.
//
// Strategy (per Entree3k reference):
//   Find the IapSp class via its SharedPreferences accessor method:
//     - returnType = SharedPreferences (the f()SharedPreferences getter)
//     - strings = ["iap_sp"]  ← SharedPreferences file name, plaintext, stable
//   Then classDefForEach to patch ALL (String)Z methods in that class.
//
// Z6/a methods returning Z with (String) param in v1.7.7:
//   a(String)Z — getPurchaseList().contains(sku) XOR 1 = isFree check
// Patching all (String)Z methods in the class covers this and any future variants.
//
// Note: PurchaseData.isPurchased() and pi/a ABTestHelper are NOT needed —
// Z6/a.a() is the actual gate used everywhere; isPurchased() is only called
// from the billing callback (Y6/c), not the UI layer.
// ---------------------------------------------------------------------------

/**
 * Locates the IapSp class (Z6/a) via its SharedPreferences file accessor.
 * The "iap_sp" string is the SharedPreferences file name — plaintext, @Keep stable.
 */
internal object IapSpFingerprint : Fingerprint(
    returnType = "Landroid/content/SharedPreferences;",
    parameters = emptyList(),
    strings = listOf("iap_sp"),
)
