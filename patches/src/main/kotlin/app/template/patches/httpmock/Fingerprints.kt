package app.template.patches.httpmock

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// VipConfigModel — all have definingClass so match(classDefBy(...)) works
val IsVipValidFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/VipConfigModel;",
    name = "isVipValid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val IsPermanentVipValidFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/VipConfigModel;",
    name = "isPermanentVipValid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val IsVipUserFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/VipConfigModel;",
    name = "isVipUser",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val GetVipTypeFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/VipConfigModel;",
    name = "getVipType",
    returnType = "Lcom/mock/sample/respository/model/VipType;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// ADConfigModel
val IfShowRewardsViewFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/ADConfigModel;",
    name = "ifShowRewardsView",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val GetMNeedTipsRewardsAdFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/ADConfigModel;",
    name = "getMNeedTipsRewardsAd",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// PairIP — string-pinned
val CheckLicenseFingerprint = Fingerprint(
    strings = listOf("Skipping license check in isolated process."),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

val VipVerifyCheckFingerprint = Fingerprint(
    strings = listOf("Force verify: true"),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;", "Z"),
)

// getVipStatusDescription — reads isVipUser/vipType as FIELDS (not getters),
// so we patch the method directly to return the PERMANENT string.
val GetVipStatusDescriptionFingerprint = Fingerprint(
    definingClass = "Lcom/mock/sample/respository/model/VipConfigModel;",
    name = "getVipStatusDescription",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
)
