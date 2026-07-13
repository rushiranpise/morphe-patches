package app.template.patches.blurams

import app.morphe.patcher.Fingerprint

// b1.hasAvailableBasicMeal() - checks if user has active basic cloud storage subscription
internal val HasAvailableBasicMealFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "hasAvailableBasicMeal",
    returnType = "Z",
    parameters = emptyList(),
)

// b1.hasAvailableAIMeal() - checks if user has active AI subscription
internal val HasAvailableAIMealFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "hasAvailableAIMeal",
    returnType = "Z",
    parameters = emptyList(),
)

// b1.hasAIMealEver() - checks if user ever had an AI subscription
internal val HasAIMealEverFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "hasAIMealEver",
    returnType = "Z",
    parameters = emptyList(),
)

// hk/a.getHasMoneyPkg() - checks if user has any paid plan (cloud playback gate)
internal val GetHasMoneyPkgFingerprint = Fingerprint(
    definingClass = "Lhk/a;",
    name = "getHasMoneyPkg",
    returnType = "Z",
    parameters = emptyList(),
)

// b1.getAIStatus() - returns enum: Using/NotPurchase/Expired/NotBind
internal val GetAIStatusFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "getAIStatus",
    returnType = "Lcom/blurams/common/util/b1\$a;",
    parameters = listOf("Lcom/v2/nhe/model/CameraInfo;"),
)

// CameraInfo.getServiceStatus() - per-camera service status (0x1=UnExpired, 0x2=Expiring, 0x3=Expired, 0x4=NOT_PURCHASED)
internal val GetServiceStatusFingerprint = Fingerprint(
    definingClass = "Lcom/v2/nhe/model/CameraInfo;",
    name = "getServiceStatus",
    returnType = "I",
    parameters = emptyList(),
)

// CameraInfo.getServiceStatusNew() - new service status field (same codes)
internal val GetServiceStatusNewFingerprint = Fingerprint(
    definingClass = "Lcom/v2/nhe/model/CameraInfo;",
    name = "getServiceStatusNew",
    returnType = "I",
    parameters = emptyList(),
)

// CameraInfo.getDvrStatus() - DVR/cloud recording status
internal val GetDvrStatusFingerprint = Fingerprint(
    definingClass = "Lcom/v2/nhe/model/CameraInfo;",
    name = "getDvrStatus",
    returnType = "I",
    parameters = emptyList(),
)

// CameraInfo.isExpired() - returns true when dvrStatus == 0
internal val CameraInfoIsExpiredFingerprint = Fingerprint(
    definingClass = "Lcom/v2/nhe/model/CameraInfo;",
    name = "isExpired",
    returnType = "Z",
    parameters = emptyList(),
)

// devicecomponents.b.isExpired() - wraps CameraInfo.isExpired + isPreLifeService
internal val DeviceComponentsIsExpiredFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/devicecomponents/b;",
    name = "isExpired",
    returnType = "Z",
    parameters = emptyList(),
)

// devicecomponents.b.isServiceEnable() - checks serviceStatusNew is active
internal val IsServiceEnableFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/devicecomponents/b;",
    name = "isServiceEnable",
    returnType = "Z",
    parameters = emptyList(),
)

// devicecomponents.b.isBindCloudService() - checks serviceStatus is 0x1 or 0x2
internal val IsBindCloudServiceFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/devicecomponents/b;",
    name = "isBindCloudService",
    returnType = "Z",
    parameters = emptyList(),
)

// ── closeli model/a (parallel device model used in Mine UI) ─────────────────

// closeli model/a.getDvrStatus() - reads static field I3
internal val CloseliGetDvrStatusFingerprint = Fingerprint(
    definingClass = "Lcom/closeli/devicecomponents/model/a;",
    name = "getDvrStatus",
    returnType = "I",
    parameters = emptyList(),
)

// closeli model/a.getServiceStatus() - reads instance field w1
internal val CloseliGetServiceStatusFingerprint = Fingerprint(
    definingClass = "Lcom/closeli/devicecomponents/model/a;",
    name = "getServiceStatus",
    returnType = "I",
    parameters = emptyList(),
)

// closeli model/a.getServiceStatusNew() - reads instance field x1 (same as isExpired check)
internal val CloseliGetServiceStatusNewFingerprint = Fingerprint(
    definingClass = "Lcom/closeli/devicecomponents/model/a;",
    name = "getServiceStatusNew",
    returnType = "I",
    parameters = emptyList(),
)

// closeli model/a.isExpired() - x1==0x3||x1==0x4 → true (drives "Expired" label in Mine UI)
internal val CloseliIsExpiredFingerprint = Fingerprint(
    definingClass = "Lcom/closeli/devicecomponents/model/a;",
    name = "isExpired",
    returnType = "Z",
    parameters = emptyList(),
)

// ── Source-level patches (server data ingestion) ─────────────────────────────

// b1.parseMealResult() - called after API response; sets aiGoods/basicGoods/hasMoneyPkg
// Patching this to return true (success) while skipping actual parsing prevents
// server data from overwriting our patched state
internal val ParseMealResultFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "parseMealResult",
    returnType = "Z",
    parameters = listOf("Lcom/nhe/clhttpclient/api/model/PurchaseMealResult;"),
)

// b1.filterPurchase() - splits meal list into aiGoods vs basicGoods lists
// Patching to no-op prevents empty server lists from clearing our state
internal val FilterPurchaseFingerprint = Fingerprint(
    definingClass = "Lcom/blurams/common/util/b1;",
    name = "filterPurchase",
    returnType = "V",
    parameters = listOf("Ljava/util/List;"),
)
