package app.template.patches.sendfilestotv.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SEND_FILES_TO_TV_COMPATIBILITY

// Send Files To TV v1.4.22 — Remove Ads / Premium bypass
//
// PURCHASE MODEL
// This app uses Google Play Billing with one-time in-app purchases (no subscription):
//   - "noads"     → Remove Ads (the premium gate we target)
//   - "supporter" → Supporter (cosmetic, no ad removal)
//   - "thankyou"  → Thank You (cosmetic, no ad removal)
//
// Purchase state persists in SharedPreferences via Settings class:
//   key="noads", value=":1" means purchased & acknowledged.
//
// PURCHASE FLOW
// BilladsActivity.checkPurchase(Context, onPurchaseListener):
//   1. If myApplication.forceNotPurchased == true → onPurchase(false) (dev override)
//   2. If BilladsActivity.CHECK_PURCHASED == true (already queried this session)
//      → read Settings.getString("noads","").equals(":1") → onPurchase(result)
//   3. Else → BillingClient.queryPurchasesAsync() → parse results → onPurchase(result)
//
// onPurchase(true)  → ads skipped (ExplorerActivity, MainActivity, ShareActivity, TransferActivity)
// onPurchase(false) → AdMob banner + interstitial shown; inciteDonate() popup (random 1-in-30)
//
// PATCH
// Inject at index 0 of checkPurchase(): immediately call onPurchase(true) and return.
// This short-circuits ALL three branches (forceNotPurchased, cached, billing query).
//
// Smali: p0=Context, p1=onPurchaseListener, registers 5 (v0–v4)
//   const/4 v0, 0x1
//   invoke-interface { p1, v0 }, .../onPurchaseListener;->onPurchase(Z)V
//   return-void
// v0 is safe (unused at index 0 before any other instruction writes it).

@Suppress("unused")
val sendFilesToTVUnlockPremiumPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes ads.",
) {
    compatibleWith(SEND_FILES_TO_TV_COMPATIBILITY)

    execute {
        CheckPurchaseFingerprint.method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                invoke-interface { p1, v0 }, Lcom/yablio/sendfilestotv/ui/BilladsActivity${'$'}onPurchaseListener;->onPurchase(Z)V
                return-void
            """,
        )
    }
}
