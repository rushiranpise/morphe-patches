package app.template.patches.allreader

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/** UtilsRepository.isPremiumUser()Z */
val IsPremiumUserFingerprint = Fingerprint(
    definingClass = "Lalldocumentreader/office/viewer/filereader/pdfviewer/respositories/UtilsRepository;",
    name = "isPremiumUser",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

/** InterstitialPreloadManager.showInterAd — static */
val ShowInterAdFingerprint = Fingerprint(
    definingClass = "Lcom/reader/office/ad/InterstitialPreloadManager;",
    name = "showInterAd",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Lc7/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("<this>", "callBack"),
)

/** InterstitialPreloadManager.showPreloadTimeInter — instance */
val ShowPreloadTimeInterFingerprint = Fingerprint(
    definingClass = "Lcom/reader/office/ad/InterstitialPreloadManager;",
    name = "showPreloadTimeInter",
    returnType = "V",
    parameters = listOf("Landroid/app/Activity;", "Lc7/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("<this>", "callBack"),
)

/** k.c.b()Z — reads SharedPref "purchase" */
val IsPurchasedFingerprint = Fingerprint(
    definingClass = "Lk/c;",
    name = "b",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("purchase"),
)

/** k.c.a()Z — reads SharedPref "firstLaunch"; true = first launch → shows LanguageActivity onboarding */
val IsFirstLaunchFingerprint = Fingerprint(
    definingClass = "Lk/c;",
    name = "a",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("firstLaunch"),
)


/** K8.j.u() — loads native/banner ad into MaterialCardView container */
val LoadNativeAdFingerprint = Fingerprint(
    definingClass = "LK8/j;",
    name = "u",
    returnType = "V",
    parameters = listOf("Lcom/admobads/data/RemoteModel;", "LZ1/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("default_ad_format"),
)
