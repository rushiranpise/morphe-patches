package app.template.patches.tiktok_lite.ads

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Feed-level ad signals on Aweme model ─────────────────────────────────────
// All methods are PUBLIC, no params, returns Z, on the stable Aweme class.
// DEX: classes3.

// Aweme.is3rdAd()Z -- third-party / MSDK ad slot; returns isAd:Z boolean field.
internal object AwemeIs3rdAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "is3rdAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isAppAd()Z -- in-app ad format (interstitial / banner inside feed).
internal object AwemeIsAppAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isAppAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isMsdkAdAweme()Z -- ByteDance MSDK ad type gate.
internal object AwemeIsMsdkAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isMsdkAdAweme",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isSoftAd()Z -- "soft ad" / native commerce-style ad.
// Called by isAdTraffic() as the secondary gate after isAd().
internal object AwemeIsSoftAdFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isSoftAd",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// Aweme.isMarketplace()Z -- marketplace/shopping ad post gate.
internal object AwemeIsMarketplaceFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "isMarketplace",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
)

// ── Splash ad init task ───────────────────────────────────────────────────────
// SplashAdInitTask.run(Context)V -- startup task that initialises TikTok's own
// splash-ad SDK. Returning void early prevents any splash ad from loading or
// rendering at app launch. Fingerprinted by the stable class+method name; the
// class is a lego init task with keyString()=="SplashAdInitTask" for extra safety.
internal object SplashAdInitTaskFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/legoImpl/task/SplashAdInitTask;",
    name = "run",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
