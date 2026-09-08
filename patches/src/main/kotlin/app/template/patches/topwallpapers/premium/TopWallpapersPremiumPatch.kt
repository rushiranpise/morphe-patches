package app.template.patches.topwallpapers.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TOPWALLPAPERS_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * Unlocks all premium wallpapers and removes interstitial ads in HD UHD Live Wallpapers.
 *
 * ## Gate architecture (verified v6.0.1 + v6.1)
 *
 * ### Gate 1 — Pro unlock: obfuscated(SharedPreferences)Z
 * Master feature gate. Called 70+ times across every preview Activity,
 * SplashScreenNew, OnBoardingActivity. Reads a runtime-decrypted SharedPrefs
 * boolean to determine if the pro IAP was purchased.
 *   true  → content unlocked, no upgrade prompts
 *   false → locked / upgrade prompts
 *
 * ### Gate 2 — Subscription days: AppLoader.d()I (was c()I in v6.0.1)
 * Returns days remaining on subscription plan. Checked against threshold ≥ 5
 * for premium grid UI in the category browser.
 * Returning Integer.MAX_VALUE (0x7fffffff = 2147483647) makes the app treat
 * the purchase as perpetual — equivalent to a one-time lifetime unlock.
 *
 * ### Gate 3 — Interstitial ads: obfuscated(Context, SharedPreferences)Z
 * Controls whether IronSource interstitial is pre-loaded and shown.
 *   true  → show ads
 *   false → suppress ads
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
    description = "Unlocks all premium wallpapers and removes interstitial ads permanently.",
) {
    compatibleWith(TOPWALLPAPERS_COMPATIBILITY)

    execute {
        // Stop the license check at the entry point — no connection to Play Store LVL
        CheckLicenseFingerprint.method.returnEarly()

        // Belt-and-suspenders: no-op signature verification so any pending
        // or background check always passes without throwing LicenseCheckException
        ValidateResponseFingerprint.method.returnEarly()

        // Gate 1: pro purchase gate → true
        PremiumCheckFingerprint.method.returnEarly(true)

        // Gate 2: subscription days → Integer.MAX_VALUE (permanent / lifetime purchase)
        // returnEarly(Int) would use const/4 which only handles -8..7.
        // Use addInstructions for the full 32-bit constant.
        SubscriptionCheckFingerprint.method.addInstructions(
            0, "const p0, 0x7fffffff\nreturn p0"
        )

        // Gate 3: ad display gate → false (suppress all interstitials)
        AdGateFingerprint.method.returnEarly(false)
    }
}
