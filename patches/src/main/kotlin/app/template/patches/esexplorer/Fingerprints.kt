package app.template.patches.esexplorer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ES File Explorer (com.estrongs.android.pop) v4.4.3.7
//
// APP ARCHITECTURE OVERVIEW
// Framework: Native Java/Kotlin, targetSdk 30, R8 obfuscation.
// Non-obfuscated packages preserved under com.estrongs.android.pop.
// All internal es.* classes are R8-obfuscated (single/short names change each version).
//
// VIP GATE CHAIN (v4.4.3.7):
//   t05.t()Z (PremiumManager)  — 46 callers — the main client-side isVip gate
//     → zx4.L0() (PopSharedPreferences singleton)
//     → zx4.G2()Z
//     → zx4.E2()Z → SharedPrefs.getBoolean(r05.d, false)
//       (r05 = PremiumKey.java — SP key names loaded at runtime)
//
//   zx4.n2()Z — lifetime/forever VIP flag
//     → SharedPrefs.getBoolean("wx_pay_forever", false)
//
//   t05.l()J — VIP expiry timestamp
//     → zx4.L0().o1()J → SharedPrefs.getLong(r05.e, 0)
//
// ACCOUNT-LEVEL VIP (server-sync):
//   AccountInfo.getIsVip()Z — fully non-obfuscated getter, reads isVip:Z field
//     populated from server login response via y7 (AccountPref.java)
//   b.t()Z (ESAccountManager) — login gate; !isEmpty(token)
//     class com.estrongs.android.pop.app.account.util.b — NON-OBFUSCATED
//
// SIGNATURE CHECK:
//   nb1.c()Z (ESAppInfo.java) — computes APK signing cert MD5 "3079a983587b13f6861dedfb6fad5502"
//     Called from FileExplorerActivity; false → shows "unofficial version" dialog
//   zx4.y2()Z — reads pref "not_show_falsified_alert"; true → skips nb1.c() entirely
//
// CHANGED FROM v4.4.3.7 (previous targets):
//   All old fingerprints used custom = { classDef.type == "Les/zz4;" } or
//   custom = { classDef.type == "Les/fx4;" } — both class names were reassigned by R8.
//   In 4.4.3.5: zz4 → t05 (PremiumManager), fx4 → zx4 (PopSharedPreferences),
//   wb1 → nb1 (ESAppInfo). Every hardcoded obfuscated class name broke.
//   Fingerprints are now anchored only on stable strings, stable SDK calls,
//   non-obfuscated class/field references, or stable SP key string constants.
//   None of the new fingerprints contain any obfuscated class or method names.
//
// Constants version updated: 4.4.3.7 / 10353.

// ── t05.t()Z — main isVip gate (46 callers) ──────────────────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Les/t05;
//   .source "PremiumManager.java"
//   .method public t()Z  .registers 2
//   [0] sget-boolean v0, Lcom/estrongs/android/pop/TestActivity;->j:Z  ← filter
//   [1] invoke-static {}, Les/zx4;->L0()Les/zx4;
//   [2] move-result-object v0
//   [3] invoke-virtual {v0}, Les/zx4;->G2()Z
//   [4] move-result v0
//   [5] return v0
//
// FINGERPRINT ANCHOR: sget-boolean on Lcom/estrongs/android/pop/TestActivity;->j:Z
//   TestActivity is fully non-obfuscated (com.estrongs.android.pop package).
//   Field name "j" is obfuscated but its definingClass is stable.
//   Verified unique: only one ()Z method in the entire app reads TestActivity.j as sget-boolean.
internal val IsVipFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_BOOLEAN,
            definingClass = "Lcom/estrongs/android/pop/TestActivity;",
        ),
    ),
)

// ── zx4.n2()Z — lifetime/forever VIP flag ────────────────────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Les/zx4;
//   .source "PopSharedPreferences.java"
//   .method public n2()Z  .registers 4
//   [0] invoke-static {}, FexApplication->o()FexApplication
//   [1] move-result-object v0
//   [2] invoke-static {v0}, PreferenceManager->getDefaultSharedPreferences(Context)SP
//   [3] move-result-object v0
//   [4] const-string/jumbo v1, "wx_pay_forever"       ← filter
//   [5] const/4 v2, 0x0
//   [6] invoke-interface {v0,v1,v2}, SP->getBoolean(String,Z)Z
//   [7] move-result v0
//   [8] return v0
//
// FINGERPRINT ANCHOR: string("wx_pay_forever")
//   Stable SP key for the WeChat lifetime purchase flag. Only one ()Z method
//   in the app reads "wx_pay_forever". Method name n2 is stable (was n2 in fx4 too).
internal val IsLifetimeFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("wx_pay_forever"),
    ),
)

// ── t05.l()J — VIP expiry timestamp ──────────────────────────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Les/t05;
//   .source "PremiumManager.java"
//   .method public l()J  .registers 3
//   [0] invoke-static {}, Les/zx4;->L0()Les/zx4;
//   [1] move-result-object v0
//   [2] invoke-virtual {v0}, Les/zx4;->o1()J
//   [3] move-result-wide v0
//   [4] return-wide v0
//
// FINGERPRINT ANCHOR: sget-boolean of TestActivity.j identifies t05 as the class.
//   Then within t05, l()J is the only public ()J method on the instance (non-static).
//   classFingerprint = IsVipFingerprint (t05 is the same class).
//   Patch: return Long.MAX_VALUE so VIP never expires in UI.
internal val VipExpireTimeFingerprint = Fingerprint(
    returnType = "J",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    classFingerprint = IsVipFingerprint,
)

// ── nb1.c()Z — APK signature verification ("unofficial version" dialog) ───────
//
// SMALI VERIFIED (classes4.dex, v4.4.3.5):
//   .class public Les/nb1;
//   .source "ESAppInfo.java"
//   .method public static c()Z
//   Contains: const-string v3, "3079a983587b13f6861dedfb6fad5502"
//   Computes MD5 of signing cert, compares to the known official MD5.
//   Returns false on re-signed builds → triggers "unofficial version" dialog.
//
// FINGERPRINT ANCHOR: string("3079a983587b13f6861dedfb6fad5502")
//   Unique MD5 constant — only one method in the codebase contains it.
//   Was previously anchored on custom = classDef.type == "Les/wb1;" — wb1 renamed to nb1.
internal val SignatureCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("3079a983587b13f6861dedfb6fad5502"),
    ),
)

// ── zx4.y2()Z — "not_show_falsified_alert" pref bypass ───────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Les/zx4;  .source "PopSharedPreferences.java"
//   .method public y2()Z  — reads pref "not_show_falsified_alert"
//   First gate in FileExplorerActivity's signature check flow:
//   if y2()=true → skip nb1.c() entirely.
//
// FINGERPRINT ANCHOR: string("not_show_falsified_alert")
//   Stable SP key name. Unique: only one ()Z method in the app reads it.
//   Was previously anchored on custom = classDef.type == "Les/fx4;" — fx4 renamed to zx4.
internal val SuppressAlertPrefFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("not_show_falsified_alert"),
    ),
)

// ── b.t()Z — ES account login gate ───────────────────────────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Lcom/estrongs/android/pop/app/account/util/b;
//   .source "ESAccountManager.java"
//   .method public t()Z  — !isEmpty(q()) where q() returns the stored token
//   Body: invoke-virtual q()String → TextUtils.isEmpty → xor-int/lit8 0x1 → return
//   Used by PremiumHelperActivity, VIP page UI — gates the "already subscribed" view.
//
// FINGERPRINT: class + method name only. Both are stable:
//   - com.estrongs.android.pop.app.account.util.b (NON-OBFUSCATED package + class)
//   - method name "t" has remained stable across versions
//   No filters needed — unique within the class.
private const val ES_ACCOUNT_MANAGER = "Lcom/estrongs.android.pop/app/account/util/b;"

internal val AccountLoginFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == ES_ACCOUNT_MANAGER && method.name == "t"
    },
)

// ── AccountInfo.getIsVip()Z — server account-level VIP ───────────────────────
//
// SMALI VERIFIED (classes4.dex, v4.4.3.5):
//   .class public Lcom/estrongs/android/pop/app/account/model/AccountInfo;
//   .source "AccountInfo.java"
//   .method public getIsVip()Z  .registers 2
//   [0] iget-boolean v0, p0, AccountInfo->isVip:Z
//   [1] return v0
//
// FINGERPRINT: definingClass + method name — both NON-OBFUSCATED.
//   This is a JavaBean getter in a data model class. Will not change
//   unless the developers rename their own field.
internal val AccountInfoIsVipFingerprint = Fingerprint(
    definingClass = "Lcom/estrongs/android/pop/app/account/model/AccountInfo;",
    name = "getIsVip",
    returnType = "Z",
    parameters = emptyList(),
)

// ── UMeng Analytics & Telemetry — UMConfigure & UMCrash ───────────────────────
//
// SMALI VERIFIED (classes.dex, v4.4.3.5 & v4.4.3.7):
//   .class public Lcom/umeng/commonsdk/UMConfigure;
//   .method public static preInit(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V
//   .method public static init(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;ILjava/lang/String;)V
//   .class public Lcom/umeng/umcrash/UMCrash;
//   .method public static registerUMCrashCallback(Lcom/umeng/umcrash/IUMCrashCallbackWithType;)V
//
// FINGERPRINT: non-obfuscated third-party SDK class & method names.
//   Nooping these directly prevents UMeng tracking initialization while leaving
//   FexApplication.M()V intact so that Handler initialization (this.g = new Handler())
//   runs safely without triggering NullPointerException (NPE) crashes.
private const val UM_CONFIGURE = "Lcom/umeng/commonsdk/UMConfigure;"
private const val UM_CRASH = "Lcom/umeng/umcrash/UMCrash;"

internal val UMConfigurePreInitFingerprint = Fingerprint(
    returnType = "V",
    custom = { method, classDef ->
        classDef.type == UM_CONFIGURE && method.name == "preInit"
    },
)

internal val UMConfigureInitFingerprint = Fingerprint(
    returnType = "V",
    custom = { method, classDef ->
        classDef.type == UM_CONFIGURE && method.name == "init"
    },
)

internal val UMCrashRegisterCallbackFingerprint = Fingerprint(
    returnType = "V",
    custom = { method, classDef ->
        classDef.type == UM_CRASH && method.name == "registerUMCrashCallback"
    },
)
