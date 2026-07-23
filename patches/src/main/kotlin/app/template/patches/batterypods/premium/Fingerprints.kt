package app.template.patches.batterypods.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets fb.c(Context)Z — the isPurchased static helper.
 *
 * Used by n80 (AirPodsService overlay UI) to gate premium features.
 * When fb.c() returns false, n80 installs i80 click listener (case 0) which
 * launches PremiumPlanActivity when the user taps premium features in the overlay.
 *
 * Also used by: z31.b(Context, Intent) (widget handler), AbstractC0260fb.
 *
 * Stable anchors:
 *   1. string "PURCHASED_ITEM_NO_ADS"
 *   2. SharedPreferences.getBoolean()
 * Access flags: PUBLIC STATIC FINAL.
 */
object BatteryPodsIsPurchasedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("PURCHASED_ITEM_NO_ADS"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

/**
 * Targets MainActivity.onCreate(Bundle)V.
 *
 * Writes PURCHASED_ITEM_NO_ADS=true to SharedPreferences at the MobileAds.a()
 * injection point (after super.onCreate, context fully initialized). Covers all
 * direct SharedPreferences read sites: MainActivity.C(Z)V, AirPodsService,
 * BatteryWidgetThemeSelectActivity, d50, wd, y4.
 *
 * Stable anchor: MobileAds.a(ContextWrapper)V — unique to this method.
 * Access flags: PUBLIC FINAL. Defining class non-obfuscated.
 */
object BatteryPodsMainActivityOnCreateFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lcom/sumyapplications/bluetoothearphone/MainActivity;",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/android/gms/ads/MobileAds;",
            name = "a",
        ),
    ),
)

/**
 * Targets LicenseResponseHelper.validateResponse(Bundle, String)V.
 * Patch: return-void → skips RSA-SHA256 JWS signature verification.
 *
 * Stable anchors:
 *   1. string "RS256"                          — alg check literal
 *   2. LicenseResponseHelper.verifySignature() — the call that follows
 *
 * Access flags: PUBLIC STATIC (no FINAL). Confirmed in smali.
 */
object PairIPValidateResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    filters = listOf(
        string("RS256"),
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
            name = "verifySignature",
        ),
    ),
)

/**
 * Targets LicenseClient.processResponse(int, Bundle)V.
 *
 * Routes license response: 0=LICENSED→proceed, 2=NOT_LICENSED→paywall, else→exit.
 * Patch: inject const/4 p1, 0x0 at offset 0 to force responseCode=LICENSED(0).
 *
 * Access flags: PRIVATE (confirmed in smali: ".method private processResponse(ILandroid/os/Bundle;)V")
 *
 * Stable anchors:
 *   1. string "License check succeeded."             — Log.i() on the LICENSED path (line 501)
 *   2. LicenseResponseHelper.getRepeatedCheckMetadata() — conditional call on LICENSED path (line 504)
 */
object PairIPProcessResponseFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("I", "Landroid/os/Bundle;"),
    filters = listOf(
        string("License check succeeded."),
        methodCall(
            definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
            name = "getRepeatedCheckMetadata",
        ),
    ),
)

/**
 * Targets LicenseResponseHelper.getRepeatedCheckMetadata(Bundle)RepeatedCheckMetadata.
 *
 * WHY THIS IS NEEDED (root cause from logcat):
 *   After processResponse(0, bundle) is forced via our p1=0 patch, the LICENSED path runs:
 *     1. validateResponse() → bypassed (return-void) ✓
 *     2. Log.i "License check succeeded." ✓
 *     3. if (repeatedCheckEnabled) getRepeatedCheckMetadata(bundle) ← CRASHES HERE
 *   getRepeatedCheckMetadata → getJwsPartsForLicenseData → throws LicenseCheckException
 *   "Invalid response" because the bundle came from a NOT_LICENSED(2) server response
 *   and lacks valid JWS data. Exception is caught by handleError → launches LicenseActivity
 *   → System.exit(0).
 *
 * Fix: returnEarly(null) — the caller already handles null RepeatedCheckMetadata via
 *   :cond_1a path (const/4 p1, 0x0) when repeatedCheckEnabled=false. Returning null
 *   here produces the same effect: the license success callback fires with no metadata.
 *
 * Access flags: PUBLIC STATIC (no FINAL). Confirmed in smali.
 * Stable anchors: class + method name are not obfuscated (com.pairip.* SDK).
 * Using definingClass + name directly (no filters needed — method is globally unique).
 */
object PairIPGetRepeatedCheckMetadataFingerprint : Fingerprint(
    returnType = "Lcom/pairip/licensecheck/RepeatedCheckMetadata;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "getRepeatedCheckMetadata",
)
