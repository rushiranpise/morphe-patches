package app.template.patches.applockwatcher.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets com.bumptech.glide.e.h(Context)Z — the combined "isAnyVIP" gate.
 *
 * Despite the package name, this class is NOT the Glide image library. The app
 * deliberately places its billing utility in com.bumptech.glide to obscure it.
 *
 * e.h() returns true if ANY of the three subscription SKUs
 * (vip_yearly, vip_quarterly, vip_monthly / vip_monthly2) is active.
 * It is called from 17+ sites across the app:
 *   HomeActivity, SettingsActivity, BackupMainActivity, VaultActivity,
 *   BasePictureThemeFragment, BaseDesignThemeFragment, BaseThemeApplyActivity,
 *   BaseThemeFlowerActivity, LockToolbarView, HomeSideMenuView, lock overlay,
 *   GoogleBillingActivity, HuaweiBillingActivity, GlobalApp, and obfuscated callers.
 *
 * Smali (classes12/com/bumptech/glide/e.smali):
 *   .method public static h(Landroid/content/Context;)Z
 *     const-string v0, "context"
 *     invoke-static { p0, v0 }, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V
 *     invoke-static { p0 }, Lcom/bumptech/glide/e;->k(Landroid/content/Context;)Z  ← FILTER 2
 *     move-result v0
 *     if-nez v0, :L1
 *     invoke-static { p0 }, Lcom/bumptech/glide/e;->j(Landroid/content/Context;)Z
 *     ...
 *
 * Filters (in smali instruction order):
 *   1. string("context")     — Kotlin null-check sentinel, stable, unique in this method
 *   2. methodCall(Intrinsics, checkNotNullParameter) — the null-check call itself
 *   3. methodCall(e, k)      — first inner VIP sub-check (isVIPYearly)
 *
 * The definingClass in filter 3 uses the stable (non-obfuscated) SDK class path
 * that R8 preserves across releases. The method name "k" is obfuscated but the
 * combination of three filters makes false-positive matching effectively impossible.
 */
object IsAnyVipFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("context"),
        methodCall(
            definingClass = "Lkotlin/jvm/internal/Intrinsics;",
            name = "checkNotNullParameter"
        ),
        methodCall(
            definingClass = "Lcom/bumptech/glide/e;",
            name = "k"
        ),
    )
)

/**
 * Targets com.bumptech.glide.f.a(Context, String)Z — the per-SKU SharedPrefs lookup.
 *
 * Reads SharedPreferences("purchase", MODE_PRIVATE) for the given SKU key
 * (after hashing via f.r()) and returns true iff the stored value equals "1".
 *
 * Called by e.i() (monthly), e.j() (quarterly), e.k() (yearly), and
 * HuaweiBillingActivity.reloadPurchaseInfo().
 *
 * Patching this method as a secondary belt-and-suspenders measure ensures all
 * billing paths (including Huawei) are covered even if e.h() is ever bypassed
 * or the call graph changes in a future version.
 *
 * Smali (classes12/com/bumptech/glide/f.smali):
 *   .method public static a(Landroid/content/Context;Ljava/lang/String;)Z
 *     ...
 *     const-string v0, "purchase"    ← FILTER 1
 *     const/4 v1, 0
 *     invoke-virtual { p0, v0, v1 }, Landroid/content/Context;->getSharedPreferences(...)
 *     move-result-object p0
 *     invoke-static { p1 }, Lcom/bumptech/glide/f;->r(Ljava/lang/String;)Ljava/lang/String;
 *     move-result-object p1
 *     const-string v0, ""
 *     invoke-interface { p0, p1, v0 }, Landroid/content/SharedPreferences;->getString(...)  ← FILTER 2
 *     move-result-object p0
 *     ...
 *     const-string p0, "1"           ← FILTER 3
 *     invoke-static { v0, p0 }, Lkotlin/jvm/internal/Intrinsics;->areEqual(...)
 *     return p0
 *
 * Filters (in smali instruction order):
 *   1. string("purchase")                         — SharedPrefs file name, stable
 *   2. methodCall(SharedPreferences, getString)   — SDK call, never obfuscated
 *   3. string("1")                                — equality target, stable
 */
object IsPurchasedSkuFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    filters = listOf(
        string("purchase"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getString"
        ),
        string("1"),
    )
)
