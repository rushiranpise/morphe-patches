package app.template.patches.mikrotik.billing

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// IAB.isPurchased(String, Context)Z — the single premium gate used throughout
// the app (PurchaseActivity, MainActivity, check()). Logic:
//   1. reward_time delta < 24 h  → return true  (free trial)
//   2. Util.isPro() (companion app check)  → return true
//   3. SharedPrefs("kha.prog.mikrotik.pro_pref").getBoolean(sku, false) → return true/false
// Stable: non-obfuscated definingClass + name, public static, non-obfuscated params.
object IsPurchasedFingerprint : Fingerprint(
    definingClass = "Lkha/prog/mikrotik/IAB;",
    name = "isPurchased",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Ljava/lang/String;", "Landroid/content/Context;"),
    filters = listOf(
        string("reward_time"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getLong"
        )
    )
)

// Util.isPro(Context)Z — checks for companion app "netshare.key" via PackageManager.
// Called by IAB.isPurchased as a secondary unlock path. Returning true here would
// be a no-op since isPurchased already returns true, but patching it removes the
// PackageManager call that could produce side effects on some devices.
object IsProFingerprint : Fingerprint(
    definingClass = "Lkha/prog/mikrotik/Util;",
    name = "isPro",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("netshare.key"),
        methodCall(
            definingClass = "Landroid/content/pm/PackageManager;",
            name = "getPackageInfo"
        )
    )
)
