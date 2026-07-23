package app.template.patches.sendfilestotv.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// BilladsActivity.checkPurchase(Context, onPurchaseListener)V
//
// The sole purchase gate for the entire app. Called from MainActivity, ExplorerActivity,
// ShareActivity, and TransferActivity on every onResume. Calls onPurchase(true) only if
// Settings.getString("noads","").equals(":1") — meaning the "noads" in-app product was
// purchased and acknowledged via Google Play Billing.
//
// When onPurchase(false): ads shown (AdMob banner + interstitial + custom JSON banner),
//   and inciteDonate() randomly prompts the user to buy.
// When onPurchase(true): ads skipped entirely.
//
// Class and method are non-obfuscated (app's own code). Fingerprinted by the unique
// string("noads") → string(":1") → String.equals() call chain in the cached-purchase
// branch (:L1). These strings are product IDs defined as constants — stable across updates.
internal object CheckPurchaseFingerprint : Fingerprint(
    definingClass = "Lcom/yablio/sendfilestotv/ui/BilladsActivity;",
    name = "checkPurchase",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Lcom/yablio/sendfilestotv/ui/BilladsActivity\$onPurchaseListener;"),
    filters = listOf(
        string("noads"),
        string(":1"),
        methodCall(
            definingClass = "Ljava/lang/String;",
            name = "equals",
        ),
    ),
)
