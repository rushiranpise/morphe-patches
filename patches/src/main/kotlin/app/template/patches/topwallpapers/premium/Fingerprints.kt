package app.template.patches.topwallpapers.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// HD UHD Live Wallpapers — TopWallpapers (hd.uhd.live.wallpapers.topwallpapers)
// Smali verified: v6.0.1 (versionCode 114) + v6.1 (versionCode 115)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * PremiumCheckFingerprint — master pro gate.
 *
 * PUBLIC STATIC (SharedPreferences)Z.
 * Body: sget-object <decrypted-key-field> → SharedPreferences.getBoolean → return.
 * Called from 70+ sites across every preview Activity, SplashScreenNew, etc.
 *
 * ## Stable anchors
 * Obfuscated class letter changes every update (m44 → u44 in v6.0.1 → v6.1).
 * Two ordered filters uniquely identify this method:
 *   1. SGET_OBJECT of a String field — loads the runtime-decrypted SharedPrefs key.
 *      Settings-key methods (e.g. LIVECHANGEONPHONEUNLOCK) use const-string instead.
 *   2. SharedPreferences.getBoolean — the actual read.
 * The sget-object filter is critical: it is present only in this gate method
 * and absent from all other (SharedPreferences)Z methods in the APK.
 *
 * Smali verified:
 *   v6.0.1 → Lm44;->m(Landroid/content/SharedPreferences;)Z  (classes3)
 *   v6.1   → Lu44;->n(Landroid/content/SharedPreferences;)Z  (classes3)
 */
val PremiumCheckFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/SharedPreferences;"),
    filters = listOf(
        // sget-object of a String field (the runtime-decrypted premium key).
        // Absent from all non-premium getBoolean methods that use const-string keys.
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            type = "Ljava/lang/String;",
        ),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

/**
 * AdGateFingerprint — interstitial ad display gate.
 *
 * PUBLIC STATIC (Context, SharedPreferences)Z.
 * Returns true → show IronSource interstitial; false → skip.
 *
 * ## Stable anchors
 * "mpkgname" is a non-obfuscated SharedPrefs key written at purchase time
 * (package name of the purchased product). Business-level constant; has not
 * changed across any observed version. Unique to this method across the APK.
 *
 * Smali verified:
 *   v6.0.1 → Lgl0;->o(Context;SharedPreferences;)Z  (classes)
 *   v6.1   → Lhl0;->o(Context;SharedPreferences;)Z  (classes)
 */
val AdGateFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Landroid/content/SharedPreferences;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getString",
        ),
    ),
    strings = listOf("mpkgname"),
)

/**
 * SubscriptionCheckFingerprint — days-remaining gate on AppLoader.
 *
 * PUBLIC FINAL ()I on the non-obfuscated AppLoader class.
 * Returns days left on subscription. Callers check result >= 5 for premium UI.
 * Returning Integer.MAX_VALUE (0x7fffffff) satisfies every threshold permanently —
 * the user is treated as having a perpetual purchase, not a weekly subscription.
 *
 * ## Stable anchors
 * AppLoader is in the app's own non-obfuscated package and will never be
 * renamed by R8. The method letter IS obfuscated and changes between versions:
 *   v6.0.1 → AppLoader.c()I
 *   v6.1   → AppLoader.d()I
 *
 * Identified without pinning the name by a structural invariant:
 * the days-remaining method is the only PUBLIC FINAL ()I on AppLoader whose
 * first two instructions are IGET (read cached day count from 'this') followed
 * by CONST_4 -0x1 (the uninitialised-sentinel check). This pattern is unique
 * and stable because it reflects the caching logic, not the obfuscator output.
 *
 * Smali verified:
 *   v6.0.1 → AppLoader.c()I  — iget v0, p0, AppLoader->D:I; const/4 v1, -0x1
 *   v6.1   → AppLoader.d()I  — iget v0, p0, AppLoader->E:I; const/4 v1, -0x1
 */
val SubscriptionCheckFingerprint = Fingerprint(
    definingClass = "Lhd/uhd/live/wallpapers/topwallpapers/application/AppLoader;",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    custom = { method, _ ->
        val insns = method.implementation?.instructions?.take(2)?.toList() ?: return@Fingerprint false
        insns.size == 2 &&
            insns[0].opcode == Opcode.IGET &&
            insns[1].opcode == Opcode.CONST_4
    },
)
