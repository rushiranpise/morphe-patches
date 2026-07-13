package app.template.patches.picturethis

import app.morphe.patcher.Fingerprint

object IsVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/glmp/GLMPAccount;",
    name = "isVip",
    returnType = "Z",
    parameters = listOf()
)

object IsVipInHistoryFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/glmp/GLMPAccount;",
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

object VipInfoIsTrialFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "isTrial",
    returnType = "Z",
    parameters = listOf()
)

object VipInfoIsAutoRenewFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "isAutoRenew",
    returnType = "Z",
    parameters = listOf()
)

object VipInfoIsVipInHistoryFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "isVipInHistory",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object VipInfoIsPaidInHistoryFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "isPaidInHistory",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object VipInfoGetVipLevelFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/vip/VipInfo;",
    name = "getVipLevel",
    returnType = "Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;",
    parameters = listOf()
)

object GlmpGetVipLevelFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/glmp/GLMPAccount;",
    name = "getVipLevel",
    returnType = "Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;",
    parameters = listOf()
)

object UserGetVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/user/User;",
    name = "getVip",
    returnType = "Z",
    parameters = listOf()
)

object UserGetExpertConsultationCountFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/user/User;",
    name = "getExpertConsultationCount",
    returnType = "I",
    parameters = listOf()
)

object UserSetExpertConsultationCountFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/user/User;",
    name = "setExpertConsultationCount",
    returnType = "V",
    parameters = listOf("I")
)

object GetExpertConsultationCountFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/expert/GetExpertConsultationCountMessage;",
    name = "getExpertConsultationCount",
    returnType = "I",
    parameters = listOf()
)

object AskForHelpExpertConsultationCountFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/features/tools/viewmodel/AskForHelpViewModel;",
    name = "expertConsultationCount",
    returnType = "I",
    parameters = listOf()
)

object AskForHelpCountDownExpertConsultationCountFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/features/tools/viewmodel/AskForHelpViewModel;",
    name = "countDownExpertConsultationCount",
    returnType = "V",
    parameters = listOf()
)

object AppContextIsVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/core/app/AppContext;",
    name = "isVip",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object UserAdditionalDataIsVipInHistoryFingerprint : Fingerprint(
    definingClass = "Lcom/glority/component/generatedAPI/kotlinAPI/user/UserAdditionalData;",
    name = "isVipInHistory",
    returnType = "Z",
    parameters = listOf()
)

object QueryHasSubscribedExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/handler/QueryHasSubscribedHandler;",
    name = "execute",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object QueryHasSubscribedPostFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/handler/QueryHasSubscribedHandler;",
    name = "post",
    returnType = "V",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object QueryHasActiveSubscribedExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/handler/QueryHasActiveSubscribedHandler;",
    name = "execute",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object QueryHasActiveSubscribedPostFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/handler/QueryHasActiveSubscribedHandler;",
    name = "post",
    returnType = "V",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object GetVipTypeExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/handler/user/GetVipTypeHandler;",
    name = "execute",
    returnType = "Ljava/lang/String;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object GetVipStepExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/handler/user/GetVipStepRequestHandler;",
    name = "execute",
    returnType = "Ljava/lang/Integer;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object PaymentGetVipStepExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/payment/handler/GetVipStepHandler;",
    name = "execute",
    returnType = "Ljava/lang/Integer;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object IsVipInHistoryHandlerExecuteFingerprint : Fingerprint(
    definingClass = "Lcom/glority/android/handler/user/IsVipInHistoryHandler;",
    name = "execute",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf("Lcom/glority/android/core/route/RouteRequest;")
)

object BookPriceTierGetVipOnlyFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/book/PriceTierModel;",
    name = "getVipOnly",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object BookPriceTierGetFreeFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/book/PriceTierModel;",
    name = "getFree",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object BookInventoryIsPurchasedFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/book/BookInventoryItemModel;",
    name = "isPurchased",
    returnType = "Z",
    parameters = listOf()
)

object BookCatalogGetFreeExperienceFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/book/CatalogItemModel;",
    name = "getFreeExperience",
    returnType = "Ljava/lang/Boolean;",
    parameters = listOf()
)

object BookUnlockTimesFingerprint : Fingerprint(
    definingClass = "Lcom/glority/picturethis/generatedAPI/kotlinAPI/book/BookInitialiseAndGetBookUnlockTimesMessage;",
    name = "getBookUnlockTimes",
    returnType = "I",
    parameters = listOf()
)

object IsPendingVipFingerprint : Fingerprint(
    definingClass = "Lcom/glority/billing/utils/PaymentUtils;",
    name = "isPendingVip\$base_billing_release",
    returnType = "Z",
    parameters = listOf(),
    strings = listOf("keyIsPendingVip")
)

// Lambda that creates AESCrypt, calls encrypt("test") to verify key, shows toast on failure.
// Patch: skip the test, just create and return AESCrypt.
object AesDelegateLambdaFingerprint : Fingerprint(
    strings = listOf("encrypt lib is error,plz contact linkman")
)

object EncryptGetPrivateKeyFingerprint : Fingerprint(
    definingClass = "Lcom/glority/encrypt/Encrypt;",
    name = "getPrivateKey",
    returnType = "[B",
    parameters = listOf("Ljava/lang/Object;")
)
