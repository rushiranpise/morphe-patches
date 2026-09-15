package app.template.patches.blek.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── STABILITY CONTRACT ────────────────────────────────────────────────────────
// All filters use ONLY non-obfuscated, update-stable anchors:
//   - Named Pairip SDK classes (never renamed)
//   - Android SDK / Java stdlib calls (never renamed)
//   - String literals that are billing-semantic (stable across renames)
//   - Lgy; SKU state enum (stable: same name across v6.22–v6.23.2+)
// REMOVED: all methodCall filters referencing obfuscated class names
//   (Lcz;->q, Ljh4;->getValue, Ljh4;->i — these changed v6.23.1→v6.23.2).
// ─────────────────────────────────────────────────────────────────────────────

// ─── Pairip (stable non-obfuscated SDK classes) ───────────────────────────────

internal object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal object LicenseValidateResponseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)

internal object LicenseCloseAppFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "closeApp",
    returnType = "V",
    parameters = listOf(),
)

// ─── Billing / Premium ────────────────────────────────────────────────────────

/**
 * IsPremiumFingerprint
 *
 * Top-level isPremium boolean gate. Checks two SKU IDs via the billing manager.
 *
 * v6.22.0: ez.e()Z  calling Luy;->h() ×2
 * v6.23.1: nz.v()Z  calling Lcz;->q() ×2  (uy→cz, ez→nz)
 * v6.23.2: rz.f()Z  calling Lez;->d() ×2  (cz→ez, nz→rz)
 *
 * Previous filters used Lcz;->q() — BROKE v6.23.1→v6.23.2 because the
 * obfuscated class name changed. Now anchored ONLY on stable string literals:
 *
 *   string("premium_yearly")  ← the second SKU ID, always a plain string constant
 *
 * "premium_v1" is stored in a static field (not const-string in the method body),
 * so only "premium_yearly" appears as a const-string filter.
 * This string is billing-semantic and stable across app updates.
 *
 * The combination of returnType=Z + PUBLIC FINAL + params=[] + string("premium_yearly")
 * is unique to this one method across the entire DEX.
 */
internal object IsPremiumFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        string("premium_yearly"),
    ),
)

/**
 * SkuStateQueryFingerprint
 *
 * Per-SKU boolean purchase check. Reads the SKU StateFlow from a HashMap.
 *
 * v6.22.0: uy.h(String)Z  — HashMap.get + Lwf4;->getValue
 * v6.23.1: cz.q(String)Z  — HashMap.get + Ljh4;->getValue  (wf4→jh4)
 * v6.23.2: ez.d(String)Z  — HashMap.get + Lsh4;->getValue  (jh4→sh4, cz→ez)
 *
 * Previous filters used Ljh4;->getValue — BROKE v6.23.1→v6.23.2.
 * Now uses only stable SDK anchors:
 *
 *   Object.getClass() — null-check on the String param (first call)
 *   HashMap.get(Object) — reads SKU from internal map
 *
 * The StateFlow class name changes each version (wf4→jh4→sh4) — removed entirely.
 * returnType=Z + PUBLIC FINAL + params=[String] + these two filters is unique.
 */
internal object SkuStateQueryFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Object;",
            name = "getClass",
        ),
        methodCall(
            definingClass = "Ljava/util/HashMap;",
            name = "get",
        ),
    ),
)

/**
 * SkuStateInitFingerprint
 *
 * Initialises one StateFlow per SKU from SharedPreferences at startup.
 * Reads getInt("SKU_"+skuId, 0) and maps ordinal → gy enum.
 *
 * v6.22.0: uy.c(List)V  — Ley;->values()[Ley;
 * v6.23.1: cz.v(List)V  — Lgy;->values()[Lgy;  (enum Ley→Lgy)
 * v6.23.2: ez.f(List)V  — Lgy;->values()[Lgy;  (cz→ez, gy UNCHANGED)
 *
 * Filters unchanged from v6.23.1 — all stable:
 *   string("SKU_")                          — billing key prefix, never changes
 *   SharedPreferences.getInt(String, I)     — standard Android SDK
 *   Lgy;->values()[Lgy;                     — gy enum name stable since v6.23.1
 *
 * Patch: replace move-result at (getInt.index + 1) with const/4 vREG, 0x3
 * → forces every SKU StateFlow to initialise as PURCHASED_AND_ACKNOWLEDGED (ordinal 3).
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
 * SkuStateWriteFingerprint
 *
 * Updates StateFlow and SharedPreferences when BillingClient reports a state change.
 * Returning early blocks overwrite of the PURCHASED_AND_ACKNOWLEDGED value.
 *
 * v6.22.0: uy.u(String, Ley;)V  — putInt + Lwf4;->h()
 * v6.23.1: cz.u(String, Lgy;)V  — putInt + Ljh4;->i()   (wf4→jh4, h→i, Ley→Lgy)
 * v6.23.2: ez.r(String, Lgy;)V  — putInt + Lsh4;->e()   (jh4→sh4, i→e, cz→ez)
 *
 * Previous filters used Ljh4;->i() — BROKE v6.23.1→v6.23.2.
 * Now uses only stable anchors:
 *
 *   SharedPreferences$Editor.putInt — standard Android SDK
 *   HashMap.get(Object)             — reads StateFlow by SKU key
 *
 * The StateFlow CAS method (wf4.h / jh4.i / sh4.e) changes every version — removed.
 * params=[String, Lgy;] pins this to the write method (gy is stable since v6.23.1).
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
            definingClass = "Ljava/util/HashMap;",
            name = "get",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;"),
        ),
    ),
)
