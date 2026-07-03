package app.template.patches.newsbreak

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NEWSBREAK_COMPATIBILITY

private const val MEDIA_SUB_INFO_CLASS =
    "Lcom/particlemedia/feature/subscription/data/MediaSubInfo;"

private const val SUBSCRIPTION_VIEW_MODEL_CLASS =
    "Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;"

private const val PREMIUM_HELPER_CLASS =
    "Lcom/particlemedia/feature/premium/ui/PremiumHelper;"

private const val PREMIUM_STATUS_CLASS =
    "Lcom/particlemedia/feature/premium/data/PremiumStatus;"

private const val PREMIUM_VIEW_MODEL_CLASS =
    "Lcom/particlemedia/feature/premium/ui/PremiumViewModel;"

private const val SOCIAL_PROFILE_CLASS =
    "Lcom/particlemedia/feature/content/social/bean/SocialProfile;"

private const val UNIFIED_PROFILE_FRAGMENT_CLASS =
    "Lcom/particlemedia/feature/profile/v1/UnifiedProfileFragment;"

private const val UNIFIED_PROFILE_HEADER_FRAGMENT_CLASS =
    "Lcom/particlemedia/feature/profile/v1/UnifiedProfileHeaderFragment;"

private const val USER_CHANNEL_LIST_API_CLASS =
    "Lcom/particlemedia/api/channel/UserChannelListApi;"

private const val ANALYTICS_CACHE_CLASS =
    "LAd/m;"

private const val PREMIUM_TRIGGER_CHECKER_CLASS =
    "Lcom/particlemedia/feature/premium/util/PremiumTriggerChecker;"

private const val PREMIUM_SYNC_WORKER_CLASS =
    "Lcom/particlemedia/common/service/PremiumSyncWorker;"

object MediaSubInfoStatusFingerprint : Fingerprint(
    definingClass = MEDIA_SUB_INFO_CLASS,
    name = "getSubscriptionStatus"
)

object MediaSubInfoStatusStringFingerprint : Fingerprint(
    definingClass = MEDIA_SUB_INFO_CLASS,
    name = "getSubscriptionStatusString"
)

object MediaSubInfoPaidStatusFingerprint : Fingerprint(
    definingClass = MEDIA_SUB_INFO_CLASS,
    name = "getPaidStatus"
)

object ViewModelStatusForJavaFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "getMediaSubscriptionStatusForJava"
)

object ViewModelStatusFlowFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "getMediaSubscriptionStatus"
)

object ViewModelSubscriptionTypeFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "getMediaSubscriptionType"
)

object ViewModelSetSubscriptionTypeFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "setMediaSubscriptionType"
)

object UpdateMediaSubscriptionStatusFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "updateMediaSubscriptionStatus"
)

object OnIapPaidSubscriptionConfirmedFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_VIEW_MODEL_CLASS,
    name = "onIapPaidSubscriptionConfirmed"
)

object LaunchSingleItemPurchaseFlowFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/subscription/ui/ipa/IapViewModel;",
    name = "launchSingleItemSubscriptionPurchaseFlow"
)

object PremiumHelperIsPremiumFingerprint : Fingerprint(
    definingClass = PREMIUM_HELPER_CLASS,
    name = "isPremium"
)

object PremiumHelperIsPremiumOrInTrialFingerprint : Fingerprint(
    definingClass = PREMIUM_HELPER_CLASS,
    name = "isPremiumOrInTrial"
)

object PremiumHelperInTrialFingerprint : Fingerprint(
    definingClass = PREMIUM_HELPER_CLASS,
    name = "inTrial"
)

object PremiumStatusPaidStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getPaidStatus"
)

object PremiumStatusSubscriptionStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getSubscriptionStatus"
)

object PremiumStatusSkuFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getSku"
)

object PremiumStatusSubscriptionChannelFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getSubscriptionChannel"
)

object PremiumStatusInFreeTrialFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getInFreeTrial"
)

object PremiumStatusExpireAtFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getExpireAt"
)

object PremiumStatusComponentSkuFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component1"
)

object PremiumStatusSubscriptionAtFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "getSubscriptionAt"
)

object PremiumStatusComponentExpireAtFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component3"
)

object PremiumStatusComponentSubscriptionAtFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component4"
)

object PremiumStatusComponentPaidStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component5"
)

object PremiumStatusComponentSubscriptionChannelFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component6"
)

object PremiumStatusComponentSubscriptionStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "component7"
)

object PremiumStatusSetSubscriptionStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "setSubscriptionStatus"
)

object PremiumStatusSetInFreeTrialFingerprint : Fingerprint(
    definingClass = PREMIUM_STATUS_CLASS,
    name = "setInFreeTrial"
)

object PremiumViewModelUpdatePremiumStatusFingerprint : Fingerprint(
    definingClass = PREMIUM_VIEW_MODEL_CLASS,
    name = "updatePremiumStatus"
)

object SocialProfileIsPremiumUserFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "isPremiumUser"
)

object SocialProfileSetPremiumUserFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "setPremiumUser"
)

object SocialProfileSetPaidStatusFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "setPaidStatus"
)

object SocialProfileSetSubscriptionStatusFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "setSubscriptionStatus"
)

object SocialProfileMediaSubscriptionTypeFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "getMediaSubscriptionType"
)

object SocialProfileSetMediaSubscriptionTypeFingerprint : Fingerprint(
    definingClass = SOCIAL_PROFILE_CLASS,
    name = "setMediaSubscriptionType"
)

object UnifiedProfileShouldShowSubscribeButtonFingerprint : Fingerprint(
    definingClass = UNIFIED_PROFILE_FRAGMENT_CLASS,
    name = "shouldShowSubscribeButton"
)

object UnifiedProfileHeaderIsValidSubscriberFingerprint : Fingerprint(
    definingClass = UNIFIED_PROFILE_HEADER_FRAGMENT_CLASS,
    name = "isValidSubscriber"
)

object UserChannelProcessPremiumContentFingerprint : Fingerprint(
    definingClass = USER_CHANNEL_LIST_API_CLASS,
    name = "processPremiumContent"
)

object AnalyticsPremiumCacheFingerprint : Fingerprint(
    definingClass = ANALYTICS_CACHE_CLASS,
    name = "g"
)

object PremiumSyncWorkerRunFingerprint : Fingerprint(
    definingClass = PREMIUM_SYNC_WORKER_CLASS,
    name = "b"
)

object PremiumTriggerAdMenuFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "shouldTriggerAdMenuClickPaywall"
)

object PremiumTriggerAdViewFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "shouldTriggerAdViewPaywall"
)

object PremiumTriggerCommentFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "shouldTriggerCommentPostPaywall"
)

object PremiumTriggerHighFreqFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "shouldTriggerHighFreqPaywall"
)

object PremiumTriggerArticleFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "shouldTriggerPremiumArticlePaywall"
)

object PremiumTriggerFrequencyFingerprint : Fingerprint(
    definingClass = PREMIUM_TRIGGER_CHECKER_CLASS,
    name = "isWithinFrequencyLimit"
)

@Suppress("unused")
val newsBreakUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium features in app.."
) {
    compatibleWith(NEWSBREAK_COMPATIBILITY)

    execute {
        val paidStatusInstructions = """
            sget-object v0, Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            return-object v0
        """.trimIndent()

        for (fingerprint in listOf(MediaSubInfoStatusFingerprint, ViewModelStatusForJavaFingerprint)) {
            val method = fingerprint.match(classDefBy(fingerprint.definingClass!!)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, paidStatusInstructions)
        }

        val viewModelStatusFlowMethod = ViewModelStatusFlowFingerprint
            .match(classDefBy(SUBSCRIPTION_VIEW_MODEL_CLASS))
            .method
        viewModelStatusFlowMethod.removeInstructions(0, viewModelStatusFlowMethod.instructions.count())
        viewModelStatusFlowMethod.addInstructions(
            0,
            """
            iget-object v0, p0, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->_mediaSubscriptionStatus:Lkotlinx/coroutines/flow/MutableStateFlow;
            sget-object p0, Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            invoke-interface {v0, p0}, Lkotlinx/coroutines/flow/MutableStateFlow;->setValue(Ljava/lang/Object;)V
            return-object v0
            """.trimIndent()
        )

        val paidTypeInstructions = """
            sget-object v0, Lcom/particlemedia/feature/subscription/data/SubscriptionType;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            return-object v0
        """.trimIndent()

        val viewModelSubscriptionTypeMethod = ViewModelSubscriptionTypeFingerprint
            .match(classDefBy(SUBSCRIPTION_VIEW_MODEL_CLASS))
            .method
        viewModelSubscriptionTypeMethod.removeInstructions(0, viewModelSubscriptionTypeMethod.instructions.count())
        viewModelSubscriptionTypeMethod.addInstructions(0, paidTypeInstructions)

        val viewModelSetSubscriptionTypeMethod = ViewModelSetSubscriptionTypeFingerprint
            .match(classDefBy(SUBSCRIPTION_VIEW_MODEL_CLASS))
            .method
        viewModelSetSubscriptionTypeMethod.removeInstructions(0, viewModelSetSubscriptionTypeMethod.instructions.count())
        viewModelSetSubscriptionTypeMethod.addInstructions(
            0,
            """
            sget-object p1, Lcom/particlemedia/feature/subscription/data/SubscriptionType;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            iput-object p1, p0, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->mediaSubscriptionType:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            return-void
            """.trimIndent()
        )

        val paidStringInstructions = """
            const-string v0, "paid"
            return-object v0
        """.trimIndent()

        for (fingerprint in listOf(MediaSubInfoStatusStringFingerprint, MediaSubInfoPaidStatusFingerprint)) {
            val method = fingerprint.match(classDefBy(MEDIA_SUB_INFO_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, paidStringInstructions)
        }

        for (fingerprint in listOf(
            PremiumStatusPaidStatusFingerprint,
            PremiumStatusSubscriptionStatusFingerprint,
            PremiumStatusComponentPaidStatusFingerprint,
            PremiumStatusComponentSubscriptionStatusFingerprint
        )) {
            val method = fingerprint.match(classDefBy(PREMIUM_STATUS_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, paidStringInstructions)
        }

        val premiumPlanInstructions = """
            const-string v0, "newsbreak_premium"
            return-object v0
        """.trimIndent()

        for (fingerprint in listOf(
            PremiumStatusSkuFingerprint,
            PremiumStatusSubscriptionChannelFingerprint,
            PremiumStatusComponentSkuFingerprint,
            PremiumStatusComponentSubscriptionChannelFingerprint
        )) {
            val method = fingerprint.match(classDefBy(PREMIUM_STATUS_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, premiumPlanInstructions)
        }

        val trueInstructions = """
            const/4 v0, 0x1
            return v0
        """.trimIndent()

        val falseInstructions = """
            const/4 v0, 0x0
            return v0
        """.trimIndent()

        for (fingerprint in listOf(PremiumHelperIsPremiumFingerprint, PremiumHelperIsPremiumOrInTrialFingerprint)) {
            val method = fingerprint.match(classDefBy(PREMIUM_HELPER_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, trueInstructions)
        }

        val premiumHelperInTrialMethod = PremiumHelperInTrialFingerprint
            .match(classDefBy(PREMIUM_HELPER_CLASS))
            .method
        premiumHelperInTrialMethod.removeInstructions(0, premiumHelperInTrialMethod.instructions.count())
        premiumHelperInTrialMethod.addInstructions(0, falseInstructions)

        val profileSubscriberMethod = UnifiedProfileHeaderIsValidSubscriberFingerprint
            .match(classDefBy(UNIFIED_PROFILE_HEADER_FRAGMENT_CLASS))
            .method
        profileSubscriberMethod.removeInstructions(0, profileSubscriberMethod.instructions.count())
        profileSubscriberMethod.addInstructions(0, trueInstructions)

        val profileSubscribeButtonMethod = UnifiedProfileShouldShowSubscribeButtonFingerprint
            .match(classDefBy(UNIFIED_PROFILE_FRAGMENT_CLASS))
            .method
        profileSubscribeButtonMethod.removeInstructions(0, profileSubscribeButtonMethod.instructions.count())
        profileSubscribeButtonMethod.addInstructions(0, falseInstructions)

        val analyticsPremiumCacheMethod = AnalyticsPremiumCacheFingerprint
            .match(classDefBy(ANALYTICS_CACHE_CLASS))
            .method
        analyticsPremiumCacheMethod.removeInstructions(0, analyticsPremiumCacheMethod.instructions.count())
        analyticsPremiumCacheMethod.addInstructions(
            0,
            """
            const-string p0, "paid"
            const-string p1, "newsbreak_premium"
            const/4 p2, 0x1
            const/4 p3, 0x0
            new-instance v0, Ljava/util/HashMap;
            invoke-direct {v0}, Ljava/util/HashMap;-><init>()V
            invoke-static {p2}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object p2
            const-string v1, "premium"
            invoke-virtual {v0, v1, p2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            invoke-static {p3}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object p2
            const-string p3, "premium_trial"
            invoke-virtual {v0, p3, p2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string p2, "premium_status"
            invoke-virtual {v0, p2, p0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const-string p0, "premium_plan"
            invoke-virtual {v0, p0, p1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
            const/4 p0, 0x0
            invoke-static {v0, p0}, LAd/n;->b(Ljava/util/Map;Z)V
            return-void
            """.trimIndent()
        )

        val processPremiumContentMethod = UserChannelProcessPremiumContentFingerprint
            .match(classDefBy(USER_CHANNEL_LIST_API_CLASS))
            .method
        processPremiumContentMethod.removeInstructions(0, processPremiumContentMethod.instructions.count())
        processPremiumContentMethod.addInstructions(
            0,
            """
            const-string v0, "show_nb_premium"
            const/4 v1, 0x1
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string v0, "nb_premium_user"
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string v0, "scene_premium_paid"
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string v0, "premium_purchase_success"
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            sput-boolean v1, Lfd/a;->c:Z
            invoke-direct {p0, p1}, Lcom/particlemedia/api/channel/UserChannelListApi;->processPremiumAdConfig(Lorg/json/JSONObject;)V
            invoke-direct {p0, p1}, Lcom/particlemedia/api/channel/UserChannelListApi;->processReadingModeConfig(Lorg/json/JSONObject;)V
            const-string v0, "paid"
            const-string v2, "newsbreak_premium"
            const/4 p1, 0x0
            invoke-static {v0, v2, v1, p1}, LAd/m;->g(Ljava/lang/String;Ljava/lang/String;ZZ)V
            return-void
            """.trimIndent()
        )

        val syncWorkerMethod = PremiumSyncWorkerRunFingerprint
            .match(classDefBy(PREMIUM_SYNC_WORKER_CLASS))
            .method
        syncWorkerMethod.removeInstructions(0, syncWorkerMethod.instructions.count())
        syncWorkerMethod.addInstructions(
            0,
            """
            const-string v0, "show_nb_premium"
            const/4 v1, 0x1
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string v0, "nb_premium_user"
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string v0, "scene_premium_paid"
            invoke-static {v0, v1}, Luf/x;->k(Ljava/lang/String;Z)V
            sput-boolean v1, Lfd/a;->c:Z
            new-instance v0, Landroidx/work/c${'$'}a${'$'}c;
            invoke-direct {v0}, Landroidx/work/c${'$'}a${'$'}c;-><init>()V
            return-object v0
            """.trimIndent()
        )

        for (fingerprint in listOf(
            PremiumTriggerAdMenuFingerprint,
            PremiumTriggerAdViewFingerprint,
            PremiumTriggerCommentFingerprint,
            PremiumTriggerHighFreqFingerprint,
            PremiumTriggerArticleFingerprint,
            PremiumTriggerFrequencyFingerprint
        )) {
            val method = fingerprint.match(classDefBy(PREMIUM_TRIGGER_CHECKER_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, falseInstructions)
        }

        val socialPremiumMethod = SocialProfileIsPremiumUserFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialPremiumMethod.removeInstructions(0, socialPremiumMethod.instructions.count())
        socialPremiumMethod.addInstructions(0, trueInstructions)

        val premiumIsoDateInstructions = """
            const-string v0, "2100-01-01T00:00:00Z"
            return-object v0
        """.trimIndent()

        for (fingerprint in listOf(
            PremiumStatusExpireAtFingerprint,
            PremiumStatusSubscriptionAtFingerprint,
            PremiumStatusComponentExpireAtFingerprint,
            PremiumStatusComponentSubscriptionAtFingerprint
        )) {
            val method = fingerprint.match(classDefBy(PREMIUM_STATUS_CLASS)).method
            method.removeInstructions(0, method.instructions.count())
            method.addInstructions(0, premiumIsoDateInstructions)
        }

        val setSubscriptionStatusMethod = PremiumStatusSetSubscriptionStatusFingerprint
            .match(classDefBy(PREMIUM_STATUS_CLASS))
            .method
        setSubscriptionStatusMethod.removeInstructions(0, setSubscriptionStatusMethod.instructions.count())
        setSubscriptionStatusMethod.addInstructions(
            0,
            """
            const-string p1, "paid"
            iput-object p1, p0, Lcom/particlemedia/feature/premium/data/PremiumStatus;->subscriptionStatus:Ljava/lang/String;
            return-void
            """.trimIndent()
        )

        val premiumStatusInFreeTrialMethod = PremiumStatusInFreeTrialFingerprint
            .match(classDefBy(PREMIUM_STATUS_CLASS))
            .method
        premiumStatusInFreeTrialMethod.removeInstructions(0, premiumStatusInFreeTrialMethod.instructions.count())
        premiumStatusInFreeTrialMethod.addInstructions(
            0,
            """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
            """.trimIndent()
        )

        val setInFreeTrialMethod = PremiumStatusSetInFreeTrialFingerprint
            .match(classDefBy(PREMIUM_STATUS_CLASS))
            .method
        setInFreeTrialMethod.removeInstructions(0, setInFreeTrialMethod.instructions.count())
        setInFreeTrialMethod.addInstructions(
            0,
            """
            sget-object p1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            iput-object p1, p0, Lcom/particlemedia/feature/premium/data/PremiumStatus;->inFreeTrial:Ljava/lang/Boolean;
            return-void
            """.trimIndent()
        )

        val updatePremiumStatusMethod = PremiumViewModelUpdatePremiumStatusFingerprint
            .match(classDefBy(PREMIUM_VIEW_MODEL_CLASS))
            .method
        updatePremiumStatusMethod.removeInstructions(0, updatePremiumStatusMethod.instructions.count())
        updatePremiumStatusMethod.addInstructions(
            0,
            """
            if-eqz p1, :cond_0
            const-string v0, "paid"
            invoke-virtual {p1, v0}, Lcom/particlemedia/feature/premium/data/PremiumStatus;->setSubscriptionStatus(Ljava/lang/String;)V
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            invoke-virtual {p1, v0}, Lcom/particlemedia/feature/premium/data/PremiumStatus;->setInFreeTrial(Ljava/lang/Boolean;)V
            iput-object p1, p0, Lcom/particlemedia/feature/premium/ui/PremiumViewModel;->premiumStatus:Lcom/particlemedia/feature/premium/data/PremiumStatus;
            iget-object v0, p0, Lcom/particlemedia/feature/premium/ui/PremiumViewModel;->_premiumStatusFlow:Lkotlinx/coroutines/flow/MutableStateFlow;
            invoke-interface {v0, p1}, Lkotlinx/coroutines/flow/MutableStateFlow;->setValue(Ljava/lang/Object;)V
            :cond_0
            const-string p1, "show_nb_premium"
            const/4 v0, 0x1
            invoke-static {p1, v0}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string p1, "nb_premium_user"
            invoke-static {p1, v0}, Luf/x;->k(Ljava/lang/String;Z)V
            const-string p1, "scene_premium_paid"
            invoke-static {p1, v0}, Luf/x;->k(Ljava/lang/String;Z)V
            sput-boolean v0, Lfd/a;->c:Z
            return-void
            """.trimIndent()
        )

        val socialSetPremiumMethod = SocialProfileSetPremiumUserFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialSetPremiumMethod.removeInstructions(0, socialSetPremiumMethod.instructions.count())
        socialSetPremiumMethod.addInstructions(
            0,
            """
            const/4 p1, 0x1
            iput-boolean p1, p0, Lcom/particlemedia/feature/content/social/bean/SocialProfile;->isPremiumUser:Z
            return-void
            """.trimIndent()
        )

        val socialSetPaidMethod = SocialProfileSetPaidStatusFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialSetPaidMethod.removeInstructions(0, socialSetPaidMethod.instructions.count())
        socialSetPaidMethod.addInstructions(
            0,
            """
            const-string p1, "paid"
            iput-object p1, p0, Lcom/particlemedia/feature/content/social/bean/SocialProfile;->paidStatus:Ljava/lang/String;
            return-object p0
            """.trimIndent()
        )

        val socialSetSubscriptionMethod = SocialProfileSetSubscriptionStatusFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialSetSubscriptionMethod.removeInstructions(0, socialSetSubscriptionMethod.instructions.count())
        socialSetSubscriptionMethod.addInstructions(
            0,
            """
            sget-object p1, Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            iput-object p1, p0, Lcom/particlemedia/feature/content/social/bean/SocialProfile;->subscriptionStatus:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            return-object p0
            """.trimIndent()
        )

        val socialMediaSubscriptionTypeMethod = SocialProfileMediaSubscriptionTypeFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialMediaSubscriptionTypeMethod.removeInstructions(0, socialMediaSubscriptionTypeMethod.instructions.count())
        socialMediaSubscriptionTypeMethod.addInstructions(
            0,
            """
            sget-object v0, Lcom/particlemedia/feature/subscription/data/SubscriptionType;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            return-object v0
            """.trimIndent()
        )

        val socialSetMediaSubscriptionTypeMethod = SocialProfileSetMediaSubscriptionTypeFingerprint
            .match(classDefBy(SOCIAL_PROFILE_CLASS))
            .method
        socialSetMediaSubscriptionTypeMethod.removeInstructions(0, socialSetMediaSubscriptionTypeMethod.instructions.count())
        socialSetMediaSubscriptionTypeMethod.addInstructions(
            0,
            """
            sget-object p1, Lcom/particlemedia/feature/subscription/data/SubscriptionType;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            iput-object p1, p0, Lcom/particlemedia/feature/content/social/bean/SocialProfile;->mediaSubscriptionType:Lcom/particlemedia/feature/subscription/data/SubscriptionType;
            return-object p0
            """.trimIndent()
        )

        val updateMethod = UpdateMediaSubscriptionStatusFingerprint
            .match(classDefBy(SUBSCRIPTION_VIEW_MODEL_CLASS))
            .method
        updateMethod.removeInstructions(0, updateMethod.instructions.count())
        updateMethod.addInstructions(
            0,
            """
            sget-object p1, Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            iget-object v0, p0, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->_mediaSubscriptionStatus:Lkotlinx/coroutines/flow/MutableStateFlow;
            invoke-interface {v0, p1}, Lkotlinx/coroutines/flow/MutableStateFlow;->setValue(Ljava/lang/Object;)V
            iput-object p1, p0, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->mediaSubscriptionStatusForJava:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            return-void
            """.trimIndent()
        )

        val onIapMethod = OnIapPaidSubscriptionConfirmedFingerprint
            .match(classDefBy(SUBSCRIPTION_VIEW_MODEL_CLASS))
            .method
        onIapMethod.removeInstructions(0, onIapMethod.instructions.count())
        onIapMethod.addInstructions(
            0,
            """
            sget-object p1, Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;->PAID:Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;
            invoke-virtual {p0, p1}, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->updateMediaSubscriptionStatus(Lcom/particlemedia/feature/subscription/data/SubscriptionStatus;)V
            invoke-virtual {p0}, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->setProfileToRefresh()V
            invoke-virtual {p0}, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->setNeedToShowSubscriptionConfirmationDialog()V
            invoke-virtual {p0}, Lcom/particlemedia/feature/subscription/ui/SubscriptionViewModel;->followMedia()V
            return-void
            """.trimIndent()
        )

        val launchMethod = LaunchSingleItemPurchaseFlowFingerprint
            .match(classDefBy(LaunchSingleItemPurchaseFlowFingerprint.definingClass!!))
            .method
        launchMethod.removeInstructions(0, launchMethod.instructions.count())
        launchMethod.addInstructions(
            0,
            """
            new-instance v0, Lcom/android/billingclient/api/Purchase;
            const-string v1, "{\"orderId\":\"morphe\",\"packageName\":\"com.particlenews.newsbreak\",\"productId\":\"newsbreak_premium\",\"productIds\":[\"newsbreak_premium\"],\"purchaseTime\":4102444800000,\"purchaseState\":1,\"purchaseToken\":\"morphe\",\"acknowledged\":true}"
            const-string v2, "morphe"
            invoke-direct {v0, v1, v2}, Lcom/android/billingclient/api/Purchase;-><init>(Ljava/lang/String;Ljava/lang/String;)V
            new-instance v1, Lcom/particlemedia/feature/subscription/ui/ipa/IapViewModel${'$'}PurchaseStatus${'$'}Success;
            invoke-direct {v1, v0}, Lcom/particlemedia/feature/subscription/ui/ipa/IapViewModel${'$'}PurchaseStatus${'$'}Success;-><init>(Lcom/android/billingclient/api/Purchase;)V
            iget-object v0, p0, Lcom/particlemedia/feature/subscription/ui/ipa/IapViewModel;->_purchaseStatus:Lkotlinx/coroutines/flow/MutableStateFlow;
            invoke-interface {v0, v1}, Lkotlinx/coroutines/flow/MutableStateFlow;->setValue(Ljava/lang/Object;)V
            return-void
            """.trimIndent()
        )
    }
}
