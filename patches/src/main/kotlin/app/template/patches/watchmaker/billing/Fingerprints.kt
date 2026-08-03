package app.template.patches.watchmaker.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── HasVIPFingerprint ─────────────────────────────────────────────────────────
//
// Targets: StoreManager.getHasVIP()Z  [classes5.dex]
//
// The single master premium gate for the entire app. Returns true when the user
// has any active entitlement. Called from:
//   - WatchMakerWebView: gates watch download/install
//   - WearListenerService: gates wear OS data sync
//   - StoreManager internally: gates acknowledgement and purchase flows
//
// Logic:
//   getActiveProduct() != null → true   (active Google Play subscription/INAPP)
//   OR hasPremiumApp == true  → true    (companion app "slide.watchFrenzy.premium" installed)
//   else                      → false
//
// getActiveProduct() reads SharedPref "active_product" — written by setActiveProduct(sku)
//   which is called from purchase acknowledgement callbacks with the purchased SKU.
//   Product IDs: "app_premium" (INAPP lifetime), "yearly", "yearly2"…"yearly10",
//   "h_yearly", "monthly"…"monthly6".
//
// hasPremiumApp: set by checkPremiumApp() which calls getPackageInfo("slide.watchFrenzy.premium").
//   If companion app is installed → true. Fails after Morphe re-signs (package not found).
//
// Smali evidence (classes5/slide/watchFrenzy/StoreManager.smali line 1506):
//   .method public final getHasVIP()Z
//   .registers 2
//   .line 36
//     invoke-virtual { p0 }, StoreManager;->getActiveProduct()Ljava/lang/String;
//     move-result-object v0
//     if-nez v0, :L1
//     iget-boolean v0, p0, StoreManager;->hasPremiumApp:Z
//     if-eqz v0, :L0
//     goto :L1
//   :L0
//     const/4 v0, 0; return v0
//   :L1
//     const/4 v0, 1; return v0
//
// Fingerprint: definingClass + name are non-obfuscated — stable across updates.
// No filters needed: the method is uniquely identified by class + name + signature.
//
object HasVIPFingerprint : Fingerprint(
    definingClass = "Lslide/watchFrenzy/StoreManager;",
    name = "getHasVIP",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)
