package app.template.patches.blek.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Pairip (DEX-layer only — no VMRunner/StartupLauncher in this variant) ────

/**
 * LicenseClient.checkLicense(Context) — public static.
 * Short-circuits the entire Play licensing binder connection.
 * Stable: non-obfuscated com.pairip.* SDK class.
 */
internal object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * LicenseResponseHelper.validateResponse(Bundle, String) — public static.
 * RSA/JWS signature verification. Belt-and-suspenders bypass.
 * Stable: non-obfuscated com.pairip.* SDK class.
 */
internal object LicenseValidateResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)

/**
 * LicenseActivity.closeApp() — private.
 * Called on license failure → System.exit(0). No-op prevents process kill.
 * Stable: non-obfuscated com.pairip.* SDK class.
 */
internal object LicenseCloseAppFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "closeApp",
    returnType = "V",
    parameters = listOf(),
)

// ─── Billing / Premium ────────────────────────────────────────────────────────

/**
 * IsPremiumFingerprint → nz.v()Z
 *
 * Top-level isPremium boolean gate. Calls cz.q("premium_v1") || cz.q("premium_yearly").
 * Also called directly by xn.<init>(Lnz;) to set the Compose nav initial isPremium
 * value via rd3.setValue — so patching this covers both the feature gate AND the
 * nav Upgrade-tab initial state (NavIsPremiumInitFingerprint no longer needed).
 *
 * v6.22.0: ez.e()Z calling Luy;->h() ×2
 * v6.23.1: nz.v()Z calling Lcz;->q() ×2 — uy renamed cz, ez renamed nz
 *
 * Smali (nz.smali line 605, v6.23.1):
 *   .method public final v()Z
 *     sget-object v0, Lnz;->a:Ljava/lang/String;        ← static "premium_v1" field
 *     iget-object p0, p0, Lnz;->v:Lcz;
 *     invoke-virtual {p0, v0}, Lcz;->q(Ljava/lang/String;)Z   ← filter[0]
 *     move-result v0
 *     if-nez v0, :cond_15
 *     const-string v0, "premium_yearly"                 ← filter[1]
 *     invoke-virtual {p0, v0}, Lcz;->q(Ljava/lang/String;)Z   ← filter[2]
 *
 * Filters: first cz.q() call + string("premium_yearly") + second cz.q() call.
 * This combination is unique to nz.v()Z across the entire DEX.
 */
internal object IsPremiumFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcz;",
            name = "q",
            returnType = "Z",
            parameters = listOf("Ljava/lang/String;"),
        ),
        string("premium_yearly"),
        methodCall(
            definingClass = "Lcz;",
            name = "q",
            returnType = "Z",
            parameters = listOf("Ljava/lang/String;"),
        ),
    ),
)

/**
 * SkuStateQueryFingerprint → cz.q(String)Z
 *
 * Per-SKU boolean query. Reads jh4 StateFlow from HashMap, checks against
 * Lgy;->m (PURCHASED_AND_ACKNOWLEDGED enum constant).
 *
 * v6.22.0: uy.h(String)Z — HashMap.get + Lwf4;->getValue
 * v6.23.1: cz.q(String)Z — HashMap.get + Ljh4;->getValue  (wf4 → jh4)
 *
 * Smali (cz.smali line 4411, v6.23.1):
 *   .method public final q(Ljava/lang/String;)Z
 *     iget-object p0, p0, Lcz;->o:Ljava/util/HashMap;
 *     invoke-virtual {p0, p1}, Ljava/util/HashMap;->get(Object)Object   ← filter[0]
 *     move-result-object p0
 *     check-cast p0, Ljh4;
 *     if-eqz p0, :cond_14
 *     invoke-virtual {p0}, Ljh4;->getValue()Ljava/lang/Object;          ← filter[1]
 *     check-cast p0, Lgy;
 *     sget-object p1, Lgy;->m:Lgy;
 *     if-ne p0, p1, :cond_1b
 */
internal object SkuStateQueryFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/HashMap;",
            name = "get",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;"),
        ),
        methodCall(
            definingClass = "Ljh4;",
            name = "getValue",
            returnType = "Ljava/lang/Object;",
            parameters = listOf(),
        ),
    ),
)

/**
 * SkuStateInitFingerprint → cz.v(List)V
 *
 * Initialises one jh4 StateFlow per SKU from SharedPreferences at startup.
 * Reads getInt("SKU_"+skuId, 0) and calls Lgy;->values() to map ordinal → enum.
 *
 * v6.22.0: uy.c(List)V — Ley;->values()[Ley;
 * v6.23.1: cz.v(List)V — Lgy;->values()[Lgy;  (enum Ley renamed Lgy)
 *
 * Smali (cz.smali line 5276, v6.23.1):
 *   .method public final v(Ljava/util/List;)V
 *     ...
 *     const-string v3, "SKU_"                              ← filter[0]
 *     ...
 *     invoke-interface SharedPreferences;->getInt(S,I)I   ← filter[1]
 *     move-result v1                                       ← patch target: replace with const/4 v1, 0x3
 *     invoke-static {}, Lgy;->values()[Lgy;               ← filter[2]  (was Ley;->values)
 *
 * Patch: replace move-result v1 at (filter[1].index + 1) with const/4 v1, 0x3
 * → forces every jh4 StateFlow to initialise as PURCHASED_AND_ACKNOWLEDGED (ordinal 3).
 */
internal object SkuStateInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;"),
    filters = listOf(
        string("SKU_"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getInt",
            returnType = "I",
            parameters = listOf("Ljava/lang/String;", "I"),
        ),
        methodCall(
            definingClass = "Lgy;",
            name = "values",
            returnType = "[Lgy;",
            parameters = listOf(),
        ),
    ),
)

/**
 * SkuStateWriteFingerprint → cz.u(String, Lgy;)V
 *
 * Updates jh4 StateFlow and SharedPreferences when BillingClient reports a
 * purchase state change. Returning early blocks the overwrite of the
 * PURCHASED_AND_ACKNOWLEDGED value set by SkuStateInitFingerprint.
 *
 * v6.22.0: uy.u(String, Ley;)V — putInt + Lwf4;->h(Object,Object)Z CAS
 * v6.23.1: cz.u(String, Lgy;)V — putInt + Ljh4;->i(Object,Object)Z CAS
 *          (wf4 → jh4, CAS method h → i, enum Ley → Lgy)
 *
 * Smali (cz.smali line 5165, v6.23.1):
 *   .method public final u(Ljava/lang/String;Lgy;)V
 *     SharedPreferences;->edit()
 *     SharedPreferences$Editor;->putInt(String,I)               ← filter[0]
 *     SharedPreferences$Editor;->apply()
 *     HashMap;->get(Object)
 *     check-cast Ljh4;
 *     Ljh4;->i(Object,Object)Z                                  ← filter[1]  (was wf4.h)
 */
internal object SkuStateWriteFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;", "Lgy;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putInt",
            returnType = "Landroid/content/SharedPreferences\$Editor;",
            parameters = listOf("Ljava/lang/String;", "I"),
        ),
        methodCall(
            definingClass = "Ljh4;",
            name = "i",
            returnType = "Z",
            parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
        ),
    ),
)
