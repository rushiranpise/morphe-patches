package app.template.patches.esexplorer

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ES_EXPLORER_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ES File Explorer (com.estrongs.android.pop)
//
// Supports two target versions with different internal architectures.
// All fingerprints are anchored on stable non-obfuscated references or
// stable string constants — zero obfuscated class/method names used.
//
// v4.2.1.3.a (versionCode 20014) — companion-APK / IAP model:
//   Old* fingerprints patch vw0 (PopSharedPreferences) and FexApplication.K().
//   Each fingerprint is guarded with runCatching because these methods
//   do not exist in v4.4.3.5 (vw0 was renamed to zx4 and method names changed).
//
// v4.4.3.5 (versionCode 10351) — subscription model:
//   IsVip/IsLifetime/VipExpireTime/SignatureCheck/SuppressAlert/AccountLogin/
//   AccountInfoIsVip fingerprints. Also individually guarded because
//   the old version lacks AccountInfo, nb1 cert MD5, wx_pay_forever, etc.
//
// runCatching ensures each patch independently degrades — a fingerprint missing
// in one version does not abort the entire patch for the other version.
@Suppress("unused")
val esExplorerUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP Lifetime",
    description = "Unlocks VIP lifetime features in ES File Explorer.",
    default = true,
) {
    compatibleWith(ES_EXPLORER_COMPATIBILITY)

    execute {

        // ── v4.2.1.3.a: Old IAP / companion-APK model ────────────────────────

        // vw0.h2()Z → return true: subscription SKU check
        // wx0.v() (PremiumManager.isVip) calls h2(); cascades to unlock all gated features.
        runCatching {
            OldPremiumSkuSubscriptionFingerprint.method.returnEarly(true)
        }

        // vw0.c2()Z → return true: in-app purchase SKU check
        // wx0.v() also calls c2(); both must be patchable for full coverage.
        runCatching {
            OldPremiumSkuInAppFingerprint.method.returnEarly(true)
        }

        // vw0.b2()Z → return true: suppress "unofficial version" signature alert
        // When true, the calling code skips the signature mismatch dialog entirely.
        runCatching {
            OldSuppressAlertPrefFingerprint.method.returnEarly(true)
        }

        // FexApplication.K()Z → return true: subscription-days gate (y:I > 0)
        // Non-obfuscated anchor. Gates UI paths that check subscription duration.
        runCatching {
            OldFexApplicationKFingerprint.method.returnEarly(true)
        }

        // ── v4.4.3.5: New subscription model ─────────────────────────────────

        // t05.t()Z → return true: main isVip gate (46 callers)
        // Anchored on sget-boolean of non-obfuscated TestActivity.j field.
        runCatching {
            IsVipFingerprint.method.returnEarly(true)
        }

        // b.t()Z → return true: account login gate (VIP page display)
        // Non-obfuscated class com.estrongs.android.pop.app.account.util.b.
        runCatching {
            AccountLoginFingerprint.method.returnEarly(true)
        }

        // AccountInfo.getIsVip()Z → return true: server account-level VIP field
        // Populated on login sync; non-obfuscated class + method name.
        runCatching {
            AccountInfoIsVipFingerprint.method.returnEarly(true)
        }

        // zx4.n2()Z → return true: lifetime / wx_pay_forever flag
        runCatching {
            IsLifetimeFingerprint.method.returnEarly(true)
        }

        // t05.l()J → return Long.MAX_VALUE: VIP expiry timestamp
        // Long.MAX_VALUE ensures no UI expiry calculation can trigger.
        runCatching {
            VipExpireTimeFingerprint.method.returnEarly(Long.MAX_VALUE)
        }

        // nb1.c()Z → return true: APK signing cert MD5 check
        // Anchored on cert MD5 string "3079a983587b13f6861dedfb6fad5502".
        // true = pretend official build → no "unofficial version" dialog.
        runCatching {
            SignatureCheckFingerprint.method.returnEarly(true)
        }

        // zx4.y2()Z → return true: suppress falsified alert pref
        // When true, calling code bypasses nb1.c() entirely — double suppression.
        runCatching {
            SuppressAlertPrefFingerprint.method.returnEarly(true)
        }
    }
}
