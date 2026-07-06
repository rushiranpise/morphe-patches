package app.template.patches.picturemushroom

import app.morphe.patcher.Fingerprint

object AppViewModelIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/base/viewmodel/AppViewModel\$Companion;",
    name = "isVip",
    returnType = "Z",
    parameters = listOf(),
    strings = listOf("key_vip_info")
)

object AppViewModelIsVipInHistoryFingerprint : Fingerprint(
    definingClass = "Lcom/glority/base/viewmodel/AppViewModel\$Companion;",
    name = "isVipInHistory",
    returnType = "Z",
    parameters = listOf()
)

object VipInfoIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "isVip",
    returnType = "Z",
    parameters = listOf()
)

object UserGetVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/user/User;",
    name = "getVip",
    returnType = "Z",
    parameters = listOf()
)

object PaymentUtilsIsPaddingVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/utils/PaymentUtils;",
    name = "isPaddingVip",
    returnType = "Z",
    parameters = listOf()
)

object EncryptGetPrivateKeyFingerprint : Fingerprint(
    definingClass = "Lcom/glority/encrypt/Encrypt;",
    name = "getPrivateKey",
    returnType = "[B",
    parameters = listOf("Ljava/lang/Object;")
)
