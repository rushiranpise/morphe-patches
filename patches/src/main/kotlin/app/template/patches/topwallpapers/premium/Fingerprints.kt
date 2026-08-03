package app.template.patches.topwallpapers.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// HD UHD Live Wallpapers - TopWallpapers v6.0.1 (versionCode 114)
// Package: hd.uhd.live.wallpapers.topwallpapers
// Smali verified: classes.dex (gl0), classes3.dex (AppLoader, m44)
//
// Premium model: Google Play Billing (subs + inapp) via AppLoader
//   - m44.m(SharedPrefs)Z = master pro gate (true = pro unlocked)
//     reads SharedPrefs.getBoolean(ul2.f, false) where ul2.f is a
//     runtime-decrypted key (int-array decoded via m44.j([I)String)
//   - gl0.o(Context, SharedPrefs)Z = ad display gate (true = show ads)
//   - AppLoader.c()I = days remaining (≥5 = premium subscription)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * m44.m(SharedPreferences)Z — classes3.dex
 *
 * The REAL premium / pro gate. Called in 20+ places across all preview
 * activities (ImageDisplayActivity, VideoDisplayActivity, LiveGlittersPreview,
 * CustomGlitterWallpaper), SplashScreenNew, OnBoardingActivity, and multiple
 * obfuscated helper classes.
 *
 * Implementation: SharedPreferences.getBoolean(ul2.f, false)
 *   where ul2.f is a runtime-decrypted string (product purchase boolean key).
 *
 * true  → pro/purchased (unlock premium features)
 * false → not purchased (show upgrade prompts, lock content)
 *
 * Stable fingerprint: public static, (SharedPreferences)Z,
 * reads a boolean from SharedPrefs via a sget-object field reference.
 */
val PremiumCheckFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/SharedPreferences;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    )
)

/**
 * gl0.o(Context, SharedPreferences)Z — classes.dex
 *
 * Ad display gate called from AppLoader.l() (show interstitial) and
 * AppLoader.g() (pre-load ads on activity init).
 * true  → show ads (not subscribed)
 * false → skip ads (subscribed)
 *
 * Anchored via the "mpkgname" SharedPrefs key (non-obfuscated purchase
 * package name stored at purchase time).
 */
val AdGateFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;", "Landroid/content/SharedPreferences;"),
    filters = listOf(
        string("mpkgname"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getString",
        ),
    )
)

/**
 * AppLoader.c()I — classes3.dex (app package, non-obfuscated class path)
 *
 * Subscription days remaining calculator.
 * Returns AppLoader.D: -1 = uninitialised, 0 = expired, ≥5 = active premium.
 * Callers in nq2/jq2 check: if c() >= 5 → full premium UI, no upgrade prompts.
 *
 * Stable: non-obfuscated class path (hd.uhd.live.wallpapers.*) + method name 'c'.
 */
val SubscriptionCheckFingerprint = Fingerprint(
    definingClass = "Lhd/uhd/live/wallpapers/topwallpapers/application/AppLoader;",
    name = "c",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
