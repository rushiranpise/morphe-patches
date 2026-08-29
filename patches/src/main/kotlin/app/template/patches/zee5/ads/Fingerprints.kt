package app.template.patches.zee5.ads

import app.morphe.patcher.Fingerprint

object VideoAdDtoAdsUrlFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/zee5/data/network/dto/VideoAdDto;" &&
            method.name == "getAdsUrl"
    },
)

object VideoAdDtoIntervalsFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/zee5/data/network/dto/VideoAdDto;" &&
            method.name == "getIntervals"
    },
)

object AdsConfigInputAdsUrlFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/zee5/usecase/content/GetAdsConfigUseCase$Input;" &&
            method.name == "getAdsUrl"
    },
)

object AdsConfigInputImaAdsFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/zee5/usecase/content/GetAdsConfigUseCase$Input;" &&
            method.name == "getImaAdsMetaInfoList"
    },
)

object NoPrerollEnabledFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, classDef ->
        classDef.type == "Lcom/zee5/data/network/dto/player/NoPrerollAdsInContentDto;" &&
            method.name == "isEnabled"
    },
)