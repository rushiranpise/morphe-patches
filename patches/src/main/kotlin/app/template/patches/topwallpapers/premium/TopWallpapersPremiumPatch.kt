package app.template.patches.topwallpapers.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import app.template.patches.shared.Constants.TOPWALLPAPERS_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks premium features and removes interstitial ads in HD UHD Live Wallpapers.
 *
 * ## Premium architecture (v6.0.1)
 *
 * Two independent gate systems:
 *
 * ### 1. Pro / Feature unlock — m44.m(SharedPrefs)Z
 * The real premium gate. Called 20+ times across every preview Activity:
 * ImageDisplayActivity, VideoDisplayActivity, LiveGlittersPreviewActivity,
 * CustomGlitterWallpaperActivity, SplashScreenNew, OnBoardingActivity, etc.
 *
 * Reads SharedPrefs.getBoolean(ul2.f, false) where ul2.f is a product
 * purchase flag key (runtime-decrypted via m44.j([I)String — obfuscated).
 *   true  → pro purchased → unlock content / show "You have unlocked premium features"
 *   false → not purchased → show upgrade prompts / lock wallpaper download/set
 *
 * ### 2. Subscription / Days remaining — AppLoader.c()I
 * Returns days left on subscription plan. Used in category browser (nq2/jq2):
 *   c() >= 5  → full premium grid UI, no upgrade banner
 *   c() <  5  → show "upgrade" overlay on grid
 * Also controls which wallpaper plans appear as selectable in InAppProActivity.
 *
 * ### 3. Interstitial ads — gl0.o(Context, SharedPrefs)Z
 * Master ad display gate checked by AppLoader.l() before showing IronSource
 * interstitial and by AppLoader.g() before pre-loading ads.
 *   true  → show ads (not subscribed)
 *   false → skip ads (subscribed)
 *
 * ## Patches (3 layers)
 *
 * Layer 1 — m44.m(SharedPreferences)Z → true   [CRITICAL — unlocks all features]
 *   The root pro gate. Returning true makes every feature check across
 *   all Activities see the user as having purchased the pro version.
 *
 * Layer 2 — AppLoader.c()I → 127              [unlocks category browser premium UI]
 *   Returns 127 days (>> 5 threshold) so nq2/jq2 always render premium grid.
 *
 * Layer 3 — gl0.o(Context, SharedPreferences)Z → false   [removes interstitial ads]
 *   AppLoader.l() skips ad show when this returns false.
 */

// ─────────────────────────────────────────────────────────────────────────────
// Pairip variant: bytecode-only LVL (no VMRunner, no SignatureCheck,
// no libpairipcore.so). The arm64 split contains only libdatastore and
// libunitycoherencenative — no native pairip component.
//
// attachBaseContext (classes2.dex):
//   invoke-static {p1}, LicenseClient;->checkLicense(Context)V
//   invoke-super   {p0, p1}, super->attachBaseContext(Context)V
//
// super class: hd.uhd.live.wallpapers.topwallpapers.application.AppLoader
// ─────────────────────────────────────────────────────────────────────────────

/**
 * LicenseClient.checkLicense(Context)V — classes2.dex
 *
 * Static entry point called from Application.attachBaseContext on every launch.
 * Instantiates LicenseClient and calls initializeLicenseCheck() which connects
 * to the Play Store licensing service, then processes the response via
 * processResponse() → validateResponse(). On a failed check it shows
 * LicenseActivity (a blocking fullscreen "not licensed" screen).
 *
 * No-oping this at the entry point prevents the entire check from starting.
 */
private val CheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;")
)

/**
 * LicenseResponseHelper.validateResponse(Bundle, String)V — classes2.dex
 *
 * Called from LicenseClient.processResponse() with the raw Play Store LVL
 * response. Verifies the JWS signature against the app's public RSA key.
 * Throws LicenseCheckException on any mismatch → triggers blocking LicenseActivity.
 *
 * No-oping ensures that even if checkLicense somehow runs (e.g. from a cached
 * pending task), signature verification always passes silently.
 */
private val ValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;")
)

@Suppress("unused")
val topWallpapersPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium wallpapers and removes interstitial ads.",
) {
    compatibleWith(TOPWALLPAPERS_COMPATIBILITY)

    execute {
        // Stop the license check at the entry point — no connection to Play Store LVL
        CheckLicenseFingerprint.method.returnEarly()

        // Belt-and-suspenders: no-op signature verification so any pending
        // or background check always passes without throwing LicenseCheckException
        ValidateResponseFingerprint.method.returnEarly()

        // Layer 1: Pro gate → true (unlocks all feature gates across the app)
        PremiumCheckFingerprint.method.returnEarly(true)

        // Layer 2: Subscription days → 127 (premium category browser UI)
        SubscriptionCheckFingerprint.method.returnEarly(127)

        // Layer 3: Ad gate → false (skip interstitial ad loading and display)
        AdGateFingerprint.method.returnEarly(false)
    }
}
