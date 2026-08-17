package app.template.patches.telegramplus

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ════════════════════════════════════════════════════════════════════════════════
// TelegramPlus-specific fingerprints (org.telegram.plus)
// Classes unique to the Plus fork — not present in Web or regular Telegram.
// Verified against 12.9.0.1 (versionCode 22437).
// ════════════════════════════════════════════════════════════════════════════════

// ─── Analytics (Plus-specific) ────────────────────────────────────────────────

val AnalyticsEnableFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
    name = "enableAnalytics",
    returnType = "V",
    parameters = listOf("Landroid/app/Application;"),
)

val AnalyticsTrackEventFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
    name = "trackEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
)

val AnalyticsTrackEventMapFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/helpers/AnalyticsHelper;",
    name = "trackEvent",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/HashMap;"),
)

// ─── Auto-update (Plus-specific) ──────────────────────────────────────────────

val PlusUpdaterCheckAppUpdateFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/update/PlusUpdater;",
    name = "checkAppUpdate",
    returnType = "V",
)

val PlusSettingsIsUpdateEnabledFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/ui/ActionBar/PlusSettings;",
    name = "isUpdateEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

// ─── Ads (Plus-specific) ──────────────────────────────────────────────────────

val AdsControllerAdsDisabledFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/ads/AdsController;",
    name = "adsDisabled",
    returnType = "Z",
)

val AdsInstanceLoadAdsFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/ads/AdsInstance;",
    name = "loadAds",
    returnType = "V",
)

// loadNativeAd returns Z in Plus 12.9.0.1
val AdsInstanceLoadNativeAdFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/plus/ads/AdsInstance;",
    name = "loadNativeAd",
    parameters = listOf(
        "Landroid/content/Context;",
        "Z",
        "Lorg/telegram/plus/ads/AdsInstance\$AdsInstanceInterface;",
    ),
)

// ─── Typing (Plus-specific sig) ───────────────────────────────────────────────

// Plus MessagesController.sendTyping has (JJII)Z — different from Web's needSendTyping
// matchAll approach. Both are needed: needSendTyping handles the UI layer,
// sendTyping handles the controller dispatch layer.
val PlusSendTypingFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessagesController;",
    name = "sendTyping",
    returnType = "Z",
    parameters = listOf("J", "J", "I", "I"),
)

// ─── Premium (Plus-specific) ──────────────────────────────────────────────────

// premiumFeaturesBlocked()Z — gates premium nag dialogs
val PremiumFeaturesBlockedFingerprint = Fingerprint(
    definingClass = "Lorg/telegram/messenger/MessagesController;",
    name = "premiumFeaturesBlocked",
    returnType = "Z",
)


