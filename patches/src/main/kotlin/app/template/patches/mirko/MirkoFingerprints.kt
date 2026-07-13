package app.template.patches.mirko

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * z3/c.E()Z — SharedPreferences ads/premium gate.
 *
 * Reads "key_ads_2" with default=true (= has ads = not premium).
 * Returning false signals "no ads" (premium) to all callers:
 *  - E3/k.h(): skips app-open ads
 *  - d4.1/c.onClick(): skips SubscribeActivity launch
 *  - HomeActivity.onBackPressed(): skips interstitial ad
 *  - SettingsActivity.onResume(): hides subscribe button
 *  - NavigationActivity, SearchActivity, ViewActivity: various ad gates
 */
val AdsStateFingerprint = Fingerprint(
    definingClass = "Lz3/c;",
    name = "E",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    filters = listOf(string("key_ads_2"))
)

/**
 * ResponseValidator.validateResponse(Bundle, String)V — PairIP RSA license check.
 *
 * Validates license response from PairIP's service using SHA256withRSA.
 * On failure throws LicenseCheckException → handleError → startErrorDialogActivity
 * → LicenseActivity launches → process killed.
 *
 * return-void makes every license check silently succeed.
 */
val PairIpValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/ResponseValidator;",
    name = "validateResponse",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * LicenseContentProvider.onCreate()Z — PairIP startup license trigger.
 *
 * ContentProviders run before any Activity. onCreate() creates LicenseClient
 * and calls initializeLicenseCheck() which connects to Play Store's licensing
 * service and eventually launches LicenseActivity, killing the process.
 *
 * return true skips all license initialization at startup.
 */
val PairIpContentProviderFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseContentProvider;",
    name = "onCreate",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
