package app.template.patches.universaltv

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * s8/f.e(Context)Z — the main isPremium() gate.
 *
 * Logic:
 *   1. Reads SharedPreferences key "isPremium" (Boolean)
 *   2. If false, checks q2/i.g (inapp cache) for "sensustech.universal.tv.remote.control.premium"
 *   3. If still false, checks q2/i.h (subs cache) for "universal.tv.remote.control.premium.sub"
 *   4. If still false, checks q2/i.h for "universal.tv.remote.control.premium.year"
 *
 * Fingerprinted by the SharedPreferences.getBoolean("isPremium") call
 * and the inapp product-id string that follows it.
 */
val IsPremiumFingerprint = Fingerprint(
    definingClass = "Ls8/f;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("isPremium"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
        string("sensustech.universal.tv.remote.control.premium")
    )
)

/**
 * s8/f.f(Application)V — AppOpenAd preloader.
 *
 * Fingerprinted by the AdMob ad-unit ID string and the
 * AppOpenAdPreloader.start() call that follows.
 */
val AppOpenAdPreloaderFingerprint = Fingerprint(
    definingClass = "Ls8/f;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/app/Application;"),
    filters = listOf(
        string("ca-app-pub-6584936772141433/1766759386"),
        methodCall(
            definingClass = "Lcom/google/android/libraries/ads/mobile/sdk/appopen/AppOpenAdPreloader;",
            name = "start"
        )
    )
)

/**
 * s8/f.a()V — CHECK_PREMIUM LocalBroadcast dispatcher.
 *
 * Sends a LocalBroadcast with action "CHECK_PREMIUM" which triggers the
 * paywall Activity from multiple call sites. No-oping this prevents the
 * paywall from launching.
 *
 * Fingerprinted by the broadcast action string and the sendBroadcast call.
 */
val CheckPremiumBroadcastFingerprint = Fingerprint(
    definingClass = "Ls8/f;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        string("CHECK_PREMIUM"),
        methodCall(
            definingClass = "Landroidx/localbroadcastmanager/content/LocalBroadcastManager;",
            name = "sendBroadcast"
        )
    )
)