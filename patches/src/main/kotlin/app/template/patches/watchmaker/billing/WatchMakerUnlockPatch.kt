package app.template.patches.watchmaker.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WATCHMAKER_COMPATIBILITY
import app.template.patches.shared.clearBody

// ── App overview ──────────────────────────────────────────────────────────────
// Package  : slide.watchFrenzy
// Play name: WatchMaker Watch Face & Clock
// Version  : 10.2.8 (versionCode 31102803), APKS, 5 DEX files
// Framework: Native Kotlin/Java + WebView (watch face editor in JS)
// Protection: NONE — no Pairip, no signature check, no anti-tamper
//   WMApplication extends android.app.Application directly; manifest
//   android:name="slide.watchFrenzy.WMApplication" is clean.

// ── Subscription model ────────────────────────────────────────────────────────
//
// Premium state is managed by StoreManager (singleton in classes5).
// A single boolean gate controls the entire experience:
//
//   getHasVIP(): Boolean
//     = getActiveProduct() != null || hasPremiumApp
//
//   where:
//     getActiveProduct() → SharedPref "active_product" (String | null)
//       Set by setActiveProduct(sku) on purchase acknowledgement.
//       Product IDs (Google Play Billing):
//         INAPP   : "app_premium"  (lifetime one-time purchase)
//         SUB yearly : "yearly", "yearly2"…"yearly10", "h_yearly", "yearly6"
//         SUB monthly: "monthly", "monthly2"…"monthly6"
//
//     hasPremiumApp → boolean field
//       Set by checkPremiumApp(): checks if companion package
//       "slide.watchFrenzy.premium" is installed via PackageManager.
//       Returns true if found, false on Exception (package not installed).
//
// getHasVIP() is the sole upstream gate for:
//   - Watch download / install (WatchMakerWebView, line 1128)
//   - Wear OS data push (WearListenerService, line 307)
//   - Purchase acknowledgement gating (StoreManager internal)
//   - entitlements check in StoreManager (lines 1001, 1353, 1485)
//
// ── No Pairip — no protection patch needed ───────────────────────────────────
//
// WMApplication.attachBaseContext() has NO Pairip call — it is a clean
// android.app.Application subclass. No LicenseClient, no VMRunner, no
// libpairipcore.so (arm64 split only has libandroidx.graphics.path.so).
// No manifest surgery required.
//
// ── Patch strategy ───────────────────────────────────────────────────────────
//
// Single target: getHasVIP() → always return true.
//
// clearBody() removes the getActiveProduct() call + iget-boolean + two conditional
// branches. Then inject:
//   const/4 v0, 1
//   return v0
//
// This is all that's needed — no SharedPreferences patch, no server bypass,
// no secondary gate. The JS-side isPremium flag in summariseChart /
// summariseProfile / summariseSearch is per-watch metadata (!is_free from watch
// JSON), not the user's subscription — unaffected and correct as-is.
//
@Suppress("unused")
val watchMakerUnlockPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks WatchMaker VIP by forcing getHasVIP() to always return " +
        "true, enabling watch downloads, Wear OS data sync, and all premium " +
        "features gated on active subscription or companion app status.",
    default = true,
) {
    compatibleWith(WATCHMAKER_COMPATIBILITY)

    execute {
        HasVIPFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }
    }
}
