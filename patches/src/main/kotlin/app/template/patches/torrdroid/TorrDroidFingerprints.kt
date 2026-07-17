package app.template.patches.torrdroid

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// LL4/b.<init>(MainActivity)V
// Reads SharedPrefs("adPreferencesFile").getBoolean("ad_free") → c:Z
// c:Z is then copied: V9/m.d:Z → MainActivity.j:Z (the ad gate)
// Unique: only constructor with (MainActivity) param that contains "adPreferencesFile"
internal val adFreeInitFingerprint = Fingerprint(
    definingClass = "LL4/b;",
    name = "<init>",
    parameters = listOf("Lintelligems/torrdroid/MainActivity;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    strings = listOf("adPreferencesFile", "ad_free")
)

// LV9/m.b(Purchase)V — purchase processing callback
// Reads SKU "ad_free", saves to SharedPrefs, sets d:Z (ad-free flag)
// Also verifies purchase via server "pk" endpoint
// Patching: after successful purchase flow, d:Z is set — we target the init path instead
internal val purchaseProcessorFingerprint = Fingerprint(
    definingClass = "LV9/m;",
    name = "b",
    parameters = listOf("Lcom/android/billingclient/api/Purchase;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("ad_free", "adPreferencesFile", "orderId")
)
