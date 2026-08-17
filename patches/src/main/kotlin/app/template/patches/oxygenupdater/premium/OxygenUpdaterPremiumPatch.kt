package app.template.patches.oxygenupdater.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OXYGENUPDATER_COMPATIBILITY

/**
 * Removes ads from Oxygen Updater by spoofing the ad-free purchase state.
 *
 * ## How the app handles ad-free
 *
 * Oxygen Updater sells a one-time "ad-free" IAP via Google Play Billing
 * (SKU: "oxygen_updater_ad_free", plus monthly/yearly subscription variants).
 * Purchase state is tracked locally in x50 (BillingRepository) as a coroutine
 * StateFlow<SkuState> stored per-SKU in a LinkedHashMap.
 *
 * SkuState integer values (class l50):
 *   0 = Unknown, 1 = NotPurchased, 2 = PurchaseInitiated,
 *   3 = Pending, 4 = Purchased, 5 = PurchasedAndAcknowledged
 *
 * The d60 (MainViewModel) derives a StateFlow<Boolean> (field f7661h) by
 * subscribing to the SkuState flow through the v50 Collector operator:
 *
 *   isAdFree = (skuState == 5)  // or SharedPrefs fallback if state == 0
 *   sharedPrefs.putBoolean(KEY, isAdFree)
 *   downstream.emit(!isAdFree)  // showAds Boolean fed to Compose UI
 *
 * The UI observes f7661h to decide whether to load and show interstitial ads
 * (Google AdMob, InMobi, Meta Audience Network) and banner ads.
 *
 * There is no server-side validation of the ad-free status — only a local
 * RSA signature check in x50.f() against a hardcoded Play public key.
 *
 * ## Patch strategy
 *
 * x50.j(int state, String sku) is the single choke point for all SkuState
 * updates. It is called from:
 *   - onBillingSetupFinished (initial query result for each SKU)
 *   - processPurchaseList (purchase / restore callback)
 *   - explicit reset (when product details unavailable)
 *
 * Each call creates a new l50(state) and calls oc5.h() to push it into the
 * matching StateFlow. The downstream v50 collector maps state 5 to
 * isAdFree=true, persists it to SharedPreferences, and emits showAds=false
 * into the Boolean flow that the Compose UI observes.
 *
 * Patch: prepend "const/4 p1, 0x5" to override the state parameter before
 * the method reads it. This makes every state update emit l50(5) regardless
 * of actual billing outcome, which propagates through the entire reactive
 * chain to suppress all ad loading and display.
 *
 * ## What is and is not affected
 *   - Interstitial ads (AdMob): removed — gated by the Boolean flow
 *   - Banner ads (InMobi, Meta Audience Network): removed — same gate
 *   - Settings screen buy button: shows "Bought" state (l50==5 UI branch)
 *   - RSA purchase signature check in x50.f(): not triggered (no real purchase)
 *   - Server purchase validation POST (z10): not triggered
 *
 * Verified smali: classes.dex, x50.smali lines 2493–2560
 *   .method public final j(ILjava/lang/String;)Ljava/lang/Object;
 *     .registers 4
 *     iget-object v0, p0, Lx50;->g:Ljava/util/LinkedHashMap;
 *     invoke-virtual { v0, p2 }, Ljava/util/LinkedHashMap;->get(...)
 *     ...
 *     new-instance p0, Ll50;
 *     invoke-direct { p0, p1 }, Ll50;-><init>(I)V  <- p1 is our target
 *     check-cast v0, Loc5;
 *     invoke-virtual { v0, p1, p0 }, Loc5;->h(...)
 *     sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
 *     return-object p0
 */
@Suppress("unused")
val oxygenUpdaterPremiumPatch = bytecodePatch(
    name = "Oxygen Updater — Remove Ads",
    description = "Unlocks the ad-free experience by spoofing the purchase state as acknowledged.",
    default = true,
) {
    compatibleWith(OXYGENUPDATER_COMPATIBILITY)

    execute {
        // x50.j(int state, String sku) — setSkuState choke point
        //
        // Override p1 (the state int) to 5 (PurchasedAndAcknowledged) before
        // the method runs. Every SkuState update for every SKU variant now
        // emits l50(5), which the v50 collector maps to isAdFree=true and
        // persists to SharedPreferences for the initial-value fast-path too.
        SetSkuStateFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x5",
        )

        println("[OxygenUpdaterPremiumPatch] SkuState spoofed to PurchasedAndAcknowledged (5)")
    }
}
