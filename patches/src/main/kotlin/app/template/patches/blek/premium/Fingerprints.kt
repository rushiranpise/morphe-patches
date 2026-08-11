package app.template.patches.blek.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Pairip (DEX-layer only — no VMRunner/StartupLauncher in this variant) ────

/**
 * LicenseClient.checkLicense(Context) — public static; bootstraps the entire
 * Play licensing flow. Returning void here short-circuits before any binder
 * connection is attempted.
 *
 * Smali: classes/com/pairip/licensecheck/LicenseClient.smali
 *   .method public static checkLicense(Landroid/content/Context;)V
 *   invoke-static {}, Lcom/pairip/licensecheck/LicenseClient;->isIsolatedProcess()Z
 *   ...
 *   new-instance v0, Lcom/pairip/licensecheck/LicenseClient;
 *   invoke-virtual {v0}, Lcom/pairip/licensecheck/LicenseClient;->initializeLicenseCheck()V
 */
internal object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

/**
 * LicenseResponseHelper.validateResponse(Bundle, String) — public static;
 * performs RSA/JWS signature verification and throws LicenseCheckException
 * on any tampered response. Belt-and-suspenders in case initializeLicenseCheck
 * somehow runs (e.g. via FULL_CHECK_OK state path).
 *
 * Smali: classes/com/pairip/licensecheck/LicenseResponseHelper.smali
 *   .method public static validateResponse(Landroid/os/Bundle;Ljava/lang/String;)V
 */
internal object LicenseValidateResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)

/**
 * LicenseActivity.closeApp() — private; called when license check fails;
 * launches closeAllTasks() then System.exit(0). No-op prevents any residual
 * LicenseActivity from killing the process.
 *
 * Smali: classes/com/pairip/licensecheck/LicenseActivity.smali
 *   .method private closeApp()V  (line 56)
 */
internal object LicenseCloseAppFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "closeApp",
    returnType = "V",
    parameters = listOf(),
)

// ─── Billing / Premium ────────────────────────────────────────────────────────

/**
 * ez.e() — isPremium gate; returns true if SKU "premium_v1" OR "premium_yearly"
 * is in SKU_STATE_PURCHASED_AND_ACKNOWLEDGED in the local HashMap.
 * This is the top-level gate consumed by the PremiumStatus (fj3) data class
 * that drives all premium-gated UI across the app.
 *
 * Smali: classes/ez.smali  .method public final e()Z  (line 280)
 *   .registers 2
 *   const-string v0, "premium_v1"
 *   iget-object p0, p0, Lez;->v:Luy;
 *   invoke-virtual {p0, v0}, Luy;->h(Ljava/lang/String;)Z
 *   move-result v0
 *   if-nez v0, :cond_15
 *   const-string v0, "premium_yearly"
 *   invoke-virtual {p0, v0}, Luy;->h(Ljava/lang/String;)Z
 *   ...
 *   return p0
 */
internal object IsPremiumFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        string("premium_v1"),
        methodCall(
            definingClass = "Luy;",
            name = "h",
            returnType = "Z",
            parameters = listOf("Ljava/lang/String;"),
        ),
        string("premium_yearly"),
        methodCall(
            definingClass = "Luy;",
            name = "h",
            returnType = "Z",
            parameters = listOf("Ljava/lang/String;"),
        ),
    ),
)

/**
 * uy.h(String) — per-SKU state query; compares the SKU's StateFlow value
 * against EnumC0200ey.f6015h (SKU_STATE_PURCHASED_AND_ACKNOWLEDGED).
 * Called directly by IsPremiumFingerprint's method and by other scattered
 * premium-gate checks across the UI layer.
 *
 * Smali: classes/uy.smali  .method public final h(Ljava/lang/String;)Z  (line 7729)
 *   .registers 2
 *   iget-object p0, p0, Luy;->d:Ljava/util/HashMap;
 *   invoke-virtual {p0, p1}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;
 *   move-result-object p0
 *   check-cast p0, Lwf4;
 *   if-eqz p0, :cond_11
 *   invoke-virtual {p0}, Lwf4;->getValue()Ljava/lang/Object;
 *   move-result-object p0
 *   check-cast p0, Ley;
 *   :goto_12
 *   sget-object p1, Ley;->h:Ley;          ← SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
 *   if-ne p0, p1, :cond_18
 *   const/4 p0, 0x1 / const/4 p0, 0x0 / return p0
 *
 * Stable filters: HashMap.get call + sget-object on SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
 * field (Ley;->h is the PURCHASED_AND_ACKNOWLEDGED enum constant, never obfuscated
 * in value — the field name h is obfuscated but the containing class Ley is co-located).
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
            definingClass = "Lwf4;",
            name = "getValue",
            returnType = "Ljava/lang/Object;",
            parameters = listOf(),
        ),
    ),
)

/**
 * uy.c(List) — StateFlow initializer; called once per SKU list in the constructor.
 * Reads SharedPreferences.getInt("SKU_" + skuId, 0) and initialises a wf4 StateFlow
 * per SKU with the persisted EnumC0200ey ordinal (0 = UNPURCHASED by default).
 * The Compose UI observes these wf4 StateFlows reactively via a combine() chain in
 * C0964zn.f24963e — which means patching uy.h() or ez.e() alone is insufficient.
 * We must force the StateFlow initial value to ordinal 3
 * (SKU_STATE_PURCHASED_AND_ACKNOWLEDGED) so the reactive Boolean StateFlow emits
 * true on the first subscription, before any BillingClient response arrives.
 *
 * Smali: classes/uy.smali  .method public final c(Ljava/util/List;)V  (line 6543)
 *   ...
 *   const-string v3, "SKU_"
 *   invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(...)
 *   invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()
 *   move-result-object v2
 *   const/4 v3, 0x0                          ← DEFAULT 0 = UNPURCHASED (patch target)
 *   invoke-interface {v1, v2, v3}, Landroid/content/SharedPreferences;->getInt(Ljava/lang/String;I)I
 *   move-result v1
 *   invoke-static {}, Ley;->values()[Ley;    ← enum ordinal → enum instance
 *   ...
 *   invoke-static {v1}, Lqe5;->k(Ljava/lang/Object;)Lwf4;  ← creates wf4 StateFlow
 *
 * Patch: replace the const/4 v3, 0x0 (SharedPrefs default) with const/4 v3, 0x3
 * so the wf4 StateFlow always starts as SKU_STATE_PURCHASED_AND_ACKNOWLEDGED,
 * making the Compose isPremium Boolean StateFlow emit true immediately.
 *
 * Verified unique: only uy.smali contains both "SKU_" const-string AND Ley;->values() call.
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
            definingClass = "Ley;",
            name = "values",
            returnType = "[Ley;",
            parameters = listOf(),
        ),
    ),
)

/**
 * tu5.m8990e — the main Compose navigation composable. Contains the call
 * to collectAsState(C0457ly(wf4_premium_v1, 1), initialValue = Boolean.FALSE)
 * whose result is passed as the `isPremium` boolean to the bottom navigation
 * builder (qe5.m7722n → C0815vm.f20918d).
 *
 * C0815vm line 126: `else if (!this.f20918d)` — when f20918d=FALSE, renders the
 * Upgrade nav item via zs5.m10969e(). When f20918d=TRUE, renders the subscription
 * management view instead. So this Boolean.FALSE initial value is the direct gate
 * for the "Upgrade" tab appearing in the bottom navigation bar.
 *
 * Smali: classes/tu5.smali  method public static final e(Lql0;Lfd2;Li74;...)V
 *   line 1886: const-string v1, "premium_v1"               ← filter[0]
 *   line 1888: invoke-virtual HashMap->get(Object)Object    ← filter[1]
 *   line 1914: invoke-direct Lly;-><init>(Lwf4;I)V          ← filter[2]  [matches[2]]
 *   line 1917: move-object v0, v1                           ← index+1
 *   line 1922: sget-object v1, Boolean;->FALSE              ← index+2 PATCH TARGET
 *   line 1939: invoke-static Lxv5;->m(...)Ldz2;            ← filter[3]
 *
 * Patch: replace sget-object v1, Boolean.FALSE
 *              with sget-object v1, Boolean.TRUE
 * Forces isPremium=TRUE as the initial value on the first composition frame,
 * so the bottom nav immediately shows subscription management (not Upgrade button).
 *
 * Verified unique: only tu5.smali contains "premium_v1" + HashMap.get + Lly;<init> + Lxv5;->m.
 */
internal object NavIsPremiumInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lql0;", "Lfd2;", "Li74;", "Lzn;", "Liz;", "Lir1;", "I"),
    filters = listOf(
        string("premium_v1"),
        methodCall(
            definingClass = "Ljava/util/HashMap;",
            name = "get",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;"),
        ),
        methodCall(
            definingClass = "Lly;",
            name = "<init>",
            returnType = "V",
            parameters = listOf("Lwf4;", "I"),
        ),
        methodCall(
            definingClass = "Lxv5;",
            name = "m",
            returnType = "Ldz2;",
        ),
    ),
)

/**
 * uy.u(String, EnumC0200ey) — the wf4 StateFlow updater called by BillingClient
 * callbacks whenever purchase state changes.
 *
 * This method is the root cause of the "pro flag briefly disappears then comes back"
 * race condition: our SkuStateInitFingerprint forces the wf4 StateFlow to start as
 * PURCHASED_AND_ACKNOWLEDGED, but when the BillingClient responds with no active
 * purchases (because we haven't actually bought anything), it calls:
 *   m9379b() → m9384j() → m9386u("premium_v1", SKU_STATE_UNPURCHASED)
 *   → wf4.h(null, EnumC0200ey.UNPURCHASED)  ← overwrites our patched value
 *   → combine() StateFlow emits false → isPremium=false → PRO badges reappear
 *
 * Smali: classes/uy.smali  .method public final u(Ljava/lang/String;Ley;)V  (line 8504)
 *   sget-object v0, SharedPreferences
 *   SharedPreferences.edit() → putInt("SKU_"+str, ordinal) → apply()
 *   iget-object f20298d → HashMap.get(str) → check-cast wf4
 *   if-eqz wf4, :done
 *   const/4 p1, 0x0
 *   invoke-virtual {wf4, p1, p2}, Lwf4;->h(Object, Object)Z  ← StateFlow CAS update
 *   :done return-void
 *
 * Patch: returnEarly() — prevent ALL writes to SharedPrefs and wf4 StateFlow.
 * Safe because:
 *   - SharedPrefs write is irrelevant (SkuStateInitFingerprint bypasses reading it)
 *   - wf4 StateFlow is already PURCHASED_AND_ACKNOWLEDGED from Layer 1 init patch
 *   - All three call sites that pass PURCHASED enum are also blocked, but the wf4
 *     is already in the correct state so no functional impact
 *
 * Unique: only uy.smali has PUBLIC FINAL method with (String, Ley;) params + wf4.h call.
 */
internal object SkuStateWriteFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;", "Ley;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putInt",
            returnType = "Landroid/content/SharedPreferences\$Editor;",
            parameters = listOf("Ljava/lang/String;", "I"),
        ),
        methodCall(
            definingClass = "Lwf4;",
            name = "h",
            returnType = "Z",
            parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
        ),
    ),
)
