package app.template.patches.applockwatcher.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.APPLOCKWATCHER_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * AppLock Watcher — Unlock VIP
 *
 * AppLock Watcher uses Google Play Billing with three subscription SKUs:
 *   vip_monthly2, vip_quarterly, vip_yearly
 *
 * Purchase state is stored in SharedPreferences("purchase", MODE_PRIVATE).
 * The billing utilities live in com.bumptech.glide.{e,f} — a deliberate namespace
 * collision with the Glide image library to obscure them from analysis.
 *
 * Patch layers:
 *
 * 1. IsAnyVipFingerprint → e.h(Context)Z → returnEarly(true)
 *    The combined "isAnyVIP" gate. Called from 17+ sites across the app
 *    (HomeActivity, SettingsActivity, BackupMainActivity, VaultActivity, both
 *    theme fragment base classes, LockToolbarView, HomeSideMenuView, lock overlay,
 *    GlobalApp, and more). A single patch here cascades through all VIP gates.
 *
 * 2. IsPurchasedSkuFingerprint → f.a(Context, String)Z → returnEarly(true)
 *    The per-SKU SharedPrefs lookup called by e.i/j/k and HuaweiBillingActivity.
 *    Belt-and-suspenders: covers Huawei billing path and any future call sites
 *    that bypass e.h() and call the per-SKU check directly.
 */
@Suppress("unused")
val appLockWatcherPremiumPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks all VIP features by bypassing the subscription status checks for all billing paths.",
    default = true
) {
    compatibleWith(APPLOCKWATCHER_COMPATIBILITY)

    execute {
        // Layer 1: combined isAnyVIP gate — covers all 17+ call sites in one patch
        IsAnyVipFingerprint.method.returnEarly(true)

        // Layer 2: per-SKU SharedPrefs lookup — covers Huawei billing path and direct callers
        IsPurchasedSkuFingerprint.method.returnEarly(true)
    }
}
