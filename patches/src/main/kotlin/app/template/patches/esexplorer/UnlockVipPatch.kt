package app.template.patches.esexplorer

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ES_EXPLORER_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ES File Explorer (com.estrongs.android.pop) v4.4.3.5
//
// CHANGED FROM v4.4.3.7:
//   All old fingerprints used custom = { classDef.type == "Les/zz4;" } etc. with
//   hardcoded obfuscated class names. R8 renamed zz4 → t05, fx4 → zx4, wb1 → nb1
//   in 4.4.3.5. Every fingerprint failed to match.
//
// NEW FINGERPRINT STRATEGY — zero obfuscated names:
//   IsVipFingerprint         sget-boolean on non-obfuscated TestActivity.j (stable)
//   IsLifetimeFingerprint    string("wx_pay_forever")              (stable SP key)
//   VipExpireTimeFingerprint classFingerprint = IsVipFingerprint   (same class t05)
//   SignatureCheckFingerprint string("3079a983587b13f6861dedfb6fad5502") (stable MD5)
//   SuppressAlertPrefFingerprint string("not_show_falsified_alert") (stable SP key)
//   AccountLoginFingerprint  custom on non-obfuscated class + method name "t"
//   AccountInfoIsVipFingerprint non-obfuscated definingClass + method name "getIsVip"
//
// VIP GATES PATCHED (same as before, different underlying code):
//   Gate 1 — pref-based (46 callers, was 55 in 4.4.3.7):
//     t05.t()Z (PremiumManager) → zx4.L0().G2() → zx4.E2() → SharedPrefs.getBoolean(r05.d)
//   Gate 2 — account login gate (VIP page display):
//     b.t()Z → !isEmpty(q()) where q() returns the stored auth token
//   Gate 3 — account-level server VIP (populated on login sync):
//     AccountInfo.getIsVip()Z → reads isVip:Z field from server login response
//   Gate 4a — lifetime flag:
//     zx4.n2()Z → SharedPrefs.getBoolean("wx_pay_forever", false)
//   Gate 4b — VIP expiry timestamp:
//     t05.l()J → zx4.L0().o1()J → SharedPrefs.getLong(r05.e, 0)
//     Long.MAX_VALUE = never expires in any UI expiry calculation
//   Signature check:
//     nb1.c()Z (ESAppInfo) computes APK cert MD5 "3079a983587b13f6861dedfb6fad5502"
//     → false on re-signed builds → shows "unofficial version" dialog
//   Alert suppression:
//     zx4.y2()Z reads "not_show_falsified_alert" — if true skips nb1.c() check entirely
@Suppress("unused")
val esExplorerUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP Lifetime",
    description = "Unlocks VIP lifetime features in ES File Explorer.",
    default = true,
) {
    compatibleWith(ES_EXPLORER_COMPATIBILITY)

    execute {
        // Gate 1: pref-based isVip — main gate (46 callers)
        IsVipFingerprint.method.returnEarly(true)

        // Gate 2: account login gate (VIP page display)
        AccountLoginFingerprint.method.returnEarly(true)

        // Gate 3: server account-level VIP flag
        AccountInfoIsVipFingerprint.method.returnEarly(true)

        // Gate 4a: lifetime flag
        IsLifetimeFingerprint.method.returnEarly(true)

        // Gate 4b: expiry timestamp — Long.MAX_VALUE = never expires
        VipExpireTimeFingerprint.method.returnEarly(Long.MAX_VALUE)

        // Signature check: return true (pretend official build, if fingerprint present in version)
        SignatureCheckFingerprint.methodOrNull?.returnEarly(true)

        // Alert suppression: return true (skip nb1.c() entirely in calling code)
        SuppressAlertPrefFingerprint.method.returnEarly(true)
    }
}
