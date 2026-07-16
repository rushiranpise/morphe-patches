package app.template.patches.esexplorer

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ES_EXPLORER_COMPATIBILITY
import app.template.patches.shared.clearBody

/**
 * Unlocks ES File Explorer VIP Lifetime by patching all three VIP gate layers.
 *
 * Gate 1 — pref-based (non-account features, 55 callers):
 *   zz4.t()Z → fx4.G2() → fx4.E2() → pref "hs_pay_premium"
 *
 * Gate 2 — account login gate (VIP page display, 12 callers):
 *   b.t()Z → !isEmpty(h8.d() = pref "token") — ES account login required
 *   Without a login token, PremiumHelperActivity always shows "subscribe" option.
 *
 * Gate 3 — account-level server VIP (populated on login sync):
 *   AccountInfo.getIsVip()Z → reads isVip:Z field from server response
 *
 * Gate 4 — lifetime + expiry:
 *   fx4.n2()Z → pref "wx_pay_forever"
 *   zz4.l()J → pref "hs_expire_time" → Long.MAX_VALUE = never expires
 */
@Suppress("unused")
val esExplorerUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP Lifetime",
    description = "Unlock Vip Features in APP.",
    default = true,
) {
    compatibleWith(ES_EXPLORER_COMPATIBILITY)

    execute {
        // Gate 1: pref-based isVip (55 callers — non-account features)
        IsVipFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Gate 2: ES account login gate (VIP page display)
        AccountLoginFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Gate 3: server account-level VIP flag
        AccountInfoIsVipFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Gate 4a: lifetime flag (wx_pay_forever)
        IsLifetimeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Gate 4b: expire timestamp — Long.MAX_VALUE = never expires
        VipExpireTimeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-wide/high16 v0, 0x7FF8000000000000L\nreturn-wide v0")
        }

        // Suppress "unofficial version" dialog:
        // wb1.c() checks APK signature MD5 "3079a983587b13f6861dedfb6fad5502"
        // → false (sig mismatch after re-sign) → u2$a posts dialog runnable → wb1.g() fires
        SignatureCheckFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Belt-and-suspenders: fx4.y2() reads pref "not_show_falsified_alert"
        // First gate in u2$a.run() — if true, skips wb1.c() entirely
        SuppressAlertPrefFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
