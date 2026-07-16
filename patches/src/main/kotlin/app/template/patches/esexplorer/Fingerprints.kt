package app.template.patches.esexplorer

import app.morphe.patcher.Fingerprint

private const val ZZ4 = "Les/zz4;"
private const val FX4 = "Les/fx4;"
private const val FEXAPP = "Lcom/estrongs/android/pop/FexApplication;"

/**
 * zz4.t()Z — main isVip gate (55 callers).
 *
 * Chain: t() → fx4.G2() → fx4.E2() → SharedPrefs.getBoolean("hs_pay_premium", false)
 * Body: sget-boolean, invoke-static, move-result-object, invoke-virtual, move-result, return.
 * Matched by custom class+method (unique; no opcode filters needed).
 */
internal val IsVipFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == ZZ4 && method.name == "t"
    }
)

/**
 * fx4.n2()Z — lifetime/forever VIP flag.
 *
 * Reads SharedPrefs "wx_pay_forever" (WeChat lifetime purchase flag).
 * Used by zz4.q()Z = isVip && isLifetime (4 callers).
 * Body uses const-string/jumbo "wx_pay_forever" — matched via strings filter + custom.
 */
internal val IsLifetimeFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("wx_pay_forever"),
    custom = { method, classDef ->
        classDef.type == FX4 && method.name == "n2"
    }
)

/**
 * zz4.l()J — VIP expiry timestamp getter.
 *
 * Returns SharedPrefs long "hs_expire_time" (0 = no expiry set).
 * Patched to return Long.MAX_VALUE so any expiry UI shows "never expires".
 * Matched by custom class+method only.
 */
internal val VipExpireTimeFingerprint = Fingerprint(
    returnType = "J",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == ZZ4 && method.name == "l"
    }
)

/**
 * FexApplication.M()V — analytics/tracking initialisation method.
 *
 * Calls UMConfigure.preInit(ctx, appKey, "China"), UMConfigure.init(..., "China", ...),
 * UMCrash.registerUMCrashCallback(), and nw.c(FexApplication).
 * Matched via string "China" (channel arg) + custom class+method.
 */
internal val AnalyticsInitFingerprint = Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("China"),
    custom = { method, classDef ->
        classDef.type == FEXAPP && method.name == "M"
    }
)

/**
 * wb1.c()Z — APK signature verification gate (the actual "unofficial version" trigger).
 *
 * Computes MD5 of the APK signing certificate and compares against "3079a983587b13f6861dedfb6fad5502".
 * Called from FileExplorerActivity$u2$a.run():
 *   if wb1.c()=false → posts u2$a$a runnable → wb1.g() → apk_falsified_alert dialog
 *   if wb1.c()=true AND packageName="com.estrongs.android.pop" → skip dialog
 *
 * wb1.f() was the WRONG target (it reads pref "fex_version" for channel analytics only).
 * This (wb1.c) is the real signature check. Returning true suppresses the dialog.
 */
internal val SignatureCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("3079a983587b13f6861dedfb6fad5502"),
    custom = { method, classDef ->
        classDef.type == "Les/wb1;" && method.name == "c"
    }
)

/**
 * fx4.y2()Z — "not_show_falsified_alert" pref bypass.
 *
 * Reads pref "not_show_falsified_alert" (boolean). If true → FileExplorerActivity$u2$a
 * skips the signature check entirely (first gate before wb1.c()).
 * Patching to true gives a second layer of protection against the dialog.
 */
internal val SuppressAlertPrefFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("not_show_falsified_alert"),
    custom = { method, classDef ->
        classDef.type == FX4 && method.name == "y2"
    }
)

/**
 * com/estrongs/android/pop/app/account/util/b.t()Z — ES account login gate on VIP page.
 *
 * Returns !isEmpty(h8.d() = pref "token") — true if user is logged into an ES account.
 * Used by PremiumHelperActivity, premium/newui/b (VIP page UI), and zz4 to gate the
 * "already subscribed" view. Without a login token, the VIP page always shows "subscribe".
 * Patching to true spoofs a logged-in state so the page respects zz4.t()=true.
 * Matched by class+method (unique; 12 callers but all in premium/account context).
 */
internal val AccountLoginFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/estrongs/android/pop/app/account/util/b;" && method.name == "t"
    }
)

/**
 * AccountInfo.getIsVip()Z — server account-level VIP flag.
 *
 * Reads field isVip:Z from AccountInfo (populated by server login response).
 * Used by b$e.smali to update fx4 VIP state after login sync.
 * Patching to true ensures account-level VIP checks always pass regardless of server response.
 */
internal val AccountInfoIsVipFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/estrongs/android/pop/app/account/model/AccountInfo;" &&
            method.name == "getIsVip"
    }
)

/**
 * es/x07.a(String, x07$d)V — telemetry reporting thread launcher.
 *
 * Spawns es.x07$b background thread. Called from es/o90 independently of nw.c().
 * Nooping prevents the thread from starting → no telemetry sent, no NPE crash.
 * nw.c() still runs (sets dgb/e.a Context) so the app doesn't crash.
 */
internal val TelemetryThreadFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Les/x07\$d;"),
    custom = { method, classDef ->
        classDef.type == "Les/x07;" && method.name == "a"
    }
)
