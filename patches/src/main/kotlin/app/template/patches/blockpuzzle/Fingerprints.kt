package app.template.patches.blockpuzzle

import app.morphe.patcher.Fingerprint

internal val InitAdmobFingerprint = Fingerprint(
    definingClass = "Lorg/cocos2dx/cpp/AppActivity;",
    name = "initAdmob",
    returnType = "V",
    parameters = emptyList(),
)

internal val LoadInterstitialFingerprint = Fingerprint(
    definingClass = "Lorg/cocos2dx/cpp/AppActivity;",
    name = "loadInterstitial",
    returnType = "V",
    parameters = emptyList(),
)

internal val ShowBannerFingerprint = Fingerprint(
    definingClass = "Lorg/cocos2dx/cpp/AppActivity;",
    name = "showBanner",
    returnType = "V",
    parameters = emptyList(),
)

internal val ShowInterstitialFingerprint = Fingerprint(
    definingClass = "Lorg/cocos2dx/cpp/AppActivity;",
    name = "showInterstitial",
    returnType = "V",
    parameters = emptyList(),
)
