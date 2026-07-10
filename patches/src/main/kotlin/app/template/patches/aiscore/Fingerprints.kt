package app.template.patches.aiscore

import app.morphe.patcher.Fingerprint

internal val UserPreferenceIsVipFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/base/preference/UserPreference;",
    name = "P",
    returnType = "Z",
    parameters = emptyList(),
)

internal val UserPreferenceIsLoggedInFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/base/preference/UserPreference;",
    name = "N",
    returnType = "Z",
    parameters = emptyList(),
)

internal val UserPreferenceVipFlagFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/base/preference/UserPreference;",
    name = "O",
    returnType = "I",
    parameters = emptyList(),
)

internal val UserPreferenceVipExpiryFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/base/preference/UserPreference;",
    name = "M",
    returnType = "J",
    parameters = emptyList(),
)

internal val UserProtoIsVipFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/network/protobuf/UserOuterClass\$User;",
    name = "getIsVip",
    returnType = "I",
    parameters = emptyList(),
)

internal val UserProtoVipExpiryFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/network/protobuf/UserOuterClass\$User;",
    name = "getVipExpiredAt",
    returnType = "J",
    parameters = emptyList(),
)

internal val TipsDetailIsFreeFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/network/protobuf/Tips\$TipsDetail;",
    name = "getIsFree",
    returnType = "I",
    parameters = emptyList(),
)

internal val TipsDetailShowPaidContentFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/network/protobuf/Tips\$TipsDetail;",
    name = "getShowPaidContent",
    returnType = "I",
    parameters = emptyList(),
)

internal val TipsDetailPurchaseTimeFingerprint = Fingerprint(
    definingClass = "Lcom/onesports/score/network/protobuf/Tips\$TipsDetail;",
    name = "getPurchaseTime",
    returnType = "I",
    parameters = emptyList(),
)
