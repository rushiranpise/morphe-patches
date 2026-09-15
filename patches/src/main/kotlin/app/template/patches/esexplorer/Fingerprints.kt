package app.template.patches.esexplorer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// ES File Explorer (com.estrongs.android.pop)
//
// TWO TARGET VERSIONS — different architectures:
//
//   v4.2.1.3.a (versionCode 20014) — "Old" IAP model
//     In-app purchase / companion-APK model.
//     Central singleton: wx0 = PremiumManager.java
//     SharedPrefs wrapper: vw0 = PopSharedPreferences.java
//     VIP check chain: wx0.v()Z → vw0.h2()Z ("es_premium_sku") and vw0.c2()Z ("es_premium_sku_inapp")
//     Subscription-days gate: FexApplication.K()Z (y:I > 0)
//     Signature alert: vw0.b2()Z ("not_show_falsified_alert")
//
//   v4.4.3.5 (versionCode 10351) — "New" subscription model
//     All internal es.* classes R8-obfuscated. Fingerprints use string constants
//     and stable non-obfuscated references instead of obfuscated class names.
//     PremiumManager = t05, SharedPrefs = zx4, Signature = nb1 (changed from prior versions).
//
// STABLE FINGERPRINT RULE: zero obfuscated class names in any fingerprint.
//   All anchors use non-obfuscated definingClass, stable SP key strings,
//   stable SDK methodCall references, or stable field references on non-obfuscated classes.
// ─────────────────────────────────────────────────────────────────────────────

// ── v4.2.1.3.a targets ───────────────────────────────────────────────────────

// vw0.h2()Z — subscription SKU check (PopSharedPreferences, old version)
//
// SMALI VERIFIED (classes.dex, v4.2.1.3.a):
//   .source "PopSharedPreferences.java"
//   .method public h2()Z
//   Reads SharedPrefs getString("es_premium_sku", null)
//   Returns true when the stored SKU starts with "es_premiun" (their typo, stable).
//
// FINGERPRINT ANCHOR: string("es_premium_sku") — unique SP key in one ()Z method.
// wx0.v() (PremiumManager.isVip) calls h2() and c2(). Patching h2→true cascades.
internal val OldPremiumSkuSubscriptionFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("es_premium_sku"),
    ),
)

// vw0.c2()Z — in-app purchase SKU check (PopSharedPreferences, old version)
//
// SMALI VERIFIED (classes.dex, v4.2.1.3.a):
//   .method public c2()Z
//   Reads SharedPrefs getString("es_premium_sku_inapp", null)
//   Returns true when SKU starts with "es_premium_inapp".
//
// FINGERPRINT ANCHOR: string("es_premium_sku_inapp") — unique SP key in one ()Z method.
internal val OldPremiumSkuInAppFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("es_premium_sku_inapp"),
    ),
)

// vw0.b2()Z — signature alert suppression pref (PopSharedPreferences, old version)
//
// SMALI VERIFIED (classes.dex, v4.2.1.3.a):
//   .method public b2()Z
//   Reads SharedPrefs getBoolean("not_show_falsified_alert", false)
//   When true → calling code skips the "unofficial version" signature alert.
//
// FINGERPRINT ANCHOR: string("not_show_falsified_alert") — unique in one ()Z getter.
// Same key present in v4.4.3.5 (zx4.y2()) — SuppressAlertPrefFingerprint covers that.
internal val OldSuppressAlertPrefFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("not_show_falsified_alert"),
    ),
)

// FexApplication.K()Z — subscription days gate (old version)
//
// SMALI VERIFIED (classes.dex, v4.2.1.3.a):
//   .class public Lcom/estrongs/android/pop/FexApplication;
//   .method public K()Z  .registers 2
//   [0] iget v0, p0, FexApplication->y:I
//   [1] if-lez v0, :L0          (y is subscription-days int, 0 at init)
//   [2] const/4 v0, 0x1; goto :L1
//   :L0 const/4 v0, 0x0
//   :L1 return v0
//   Called by nk1.e()Z (UnlockUtils.isActivated) and a few UI paths.
//
// FINGERPRINT: definingClass + name — both NON-OBFUSCATED (com.estrongs.android.pop package).
//   FexApplication is the Application subclass; K is a stable public method name.
internal val OldFexApplicationKFingerprint = Fingerprint(
    definingClass = "Lcom/estrongs/android/pop/FexApplication;",
    name = "K",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// ── v4.4.3.5 targets ─────────────────────────────────────────────────────────
// (unchanged from last session — stable string/non-obfuscated anchors)

// t05.t()Z — main isVip gate (46 callers, PremiumManager, new version)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .source "PremiumManager.java"
//   .method public t()Z
//   [0] sget-boolean v0, Lcom/estrongs/android/pop/TestActivity;->j:Z  ← filter
//   [1] invoke-static {}, Les/zx4;->L0()Les/zx4;
//   [3] invoke-virtual {v0}, Les/zx4;->G2()Z
//   ...
//
// FINGERPRINT ANCHOR: sget-boolean on Lcom/estrongs/android/pop/TestActivity;->j:Z
//   TestActivity is fully non-obfuscated. Only one ()Z method reads TestActivity.j.
//   (Note: v4.2.1.3.a uses TestActivity.I — different field, different version.)
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

// zx4.n2()Z — lifetime/forever VIP flag (PopSharedPreferences, new version)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .method public n2()Z  — reads SharedPrefs.getBoolean("wx_pay_forever", false)
//
// FINGERPRINT ANCHOR: string("wx_pay_forever") — unique in one ()Z method.
internal val IsLifetimeFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("wx_pay_forever"),
    ),
)

// t05.l()J — VIP expiry timestamp (new version)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .method public l()J — calls zx4.L0().o1()J (SharedPrefs getLong)
//   classFingerprint = IsVipFingerprint (same class t05)
//   Only public ()J non-static method in t05.
//
// Patch: return Long.MAX_VALUE → VIP never expires in any UI date calculation.
internal val VipExpireTimeFingerprint = Fingerprint(
    returnType = "J",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    classFingerprint = IsVipFingerprint,
)

// nb1.c()Z — APK signature verification (new version)
//
// SMALI VERIFIED (classes4.dex, v4.4.3.5):
//   .source "ESAppInfo.java"
//   .method public static c()Z
//   Contains: const-string v3, "3079a983587b13f6861dedfb6fad5502"
//   Returns false on re-signed builds → "unofficial version" dialog.
//
// FINGERPRINT ANCHOR: string("3079a983587b13f6861dedfb6fad5502") — unique.
internal val SignatureCheckFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("3079a983587b13f6861dedfb6fad5502"),
    ),
)

// zx4.y2()Z — "not_show_falsified_alert" pref (new version)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .method public y2()Z — reads SharedPrefs.getBoolean("not_show_falsified_alert", false)
//   First gate in FileExplorerActivity signature check; true → skip nb1.c().
//
// FINGERPRINT ANCHOR: string("not_show_falsified_alert") — unique in one ()Z getter.
// (In v4.2.1.3.a this is vw0.b2() — OldSuppressAlertPrefFingerprint covers that.)
internal val SuppressAlertPrefFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("not_show_falsified_alert"),
    ),
)

// b.t()Z — ES account login gate (new version, non-obfuscated package)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Lcom/estrongs/android/pop/app/account/util/b;
//   .source "ESAccountManager.java"
//   .method public t()Z — !isEmpty(q()) where q() returns stored auth token.
//
// FINGERPRINT: custom predicate on non-obfuscated class + stable method name "t".
private const val ES_ACCOUNT_MANAGER = "Lcom/estrongs/android/pop/app/account/util/b;"

internal val AccountLoginFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == ES_ACCOUNT_MANAGER && method.name == "t"
    },
)

// AccountInfo.getIsVip()Z — server account-level VIP (new version)
//
// SMALI VERIFIED (classes4.dex, v4.4.3.5):
//   .class public Lcom/estrongs/android/pop/app/account/model/AccountInfo;
//   .method public getIsVip()Z — iget-boolean v0, p0, AccountInfo->isVip:Z
//
// FINGERPRINT: definingClass + name — both NON-OBFUSCATED.
internal val AccountInfoIsVipFingerprint = Fingerprint(
    definingClass = "Lcom/estrongs/android/pop/app/account/model/AccountInfo;",
    name = "getIsVip",
    returnType = "Z",
    parameters = emptyList(),
)

// FexApplication.M()V — UMeng analytics init (new version)
//
// SMALI VERIFIED (classes.dex, v4.4.3.5):
//   .class public Lcom/estrongs/android/pop/FexApplication;
//   .method public final M()V — contains UMConfigure.preInit/init calls with "China" arg.
//
// FINGERPRINT: string("China") narrowed to FexApplication.M via custom predicate.
private const val FEXAPP = "Lcom/estrongs/android/pop/FexApplication;"

internal val AnalyticsInitFingerprint = Fingerprint(
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        string("China"),
    ),
    custom = { method, classDef ->
        classDef.type == FEXAPP && method.name == "M"
    },
)
