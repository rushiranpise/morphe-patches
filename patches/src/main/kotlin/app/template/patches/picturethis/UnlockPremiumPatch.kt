package app.template.patches.picturethis

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks PictureThis premium features.",
) {
    dependsOn(disableEncryptCheckPatch, bypassNativeKeyCheckPatch)

    compatibleWith(Constants.PICTURETHIS_COMPATIBILITY)

    execute {
        IsVipFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        IsVipInHistoryFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        VipInfoIsVipFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        VipInfoIsTrialFingerprint.method.addInstructions(0, """
            const/4 v0, 0x0
            return v0
        """)

        VipInfoIsAutoRenewFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        VipInfoIsVipInHistoryFingerprint.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """)

        VipInfoIsPaidInHistoryFingerprint.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """)

        VipInfoGetVipLevelFingerprint.method.addInstructions(0, """
            sget-object v0, Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;->PRO:Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;
            return-object v0
        """)

        GlmpGetVipLevelFingerprint.method.addInstructions(0, """
            sget-object v0, Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;->PRO:Lcom/glority/component/generatedAPI/kotlinAPI/enums/VipLevel;
            return-object v0
        """)

        UserGetVipFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        listOf(
            UserGetExpertConsultationCountFingerprint.method,
            GetExpertConsultationCountFingerprint.method,
            AskForHelpExpertConsultationCountFingerprint.method,
        ).forEach { method ->
            method.addInstructions(0, """
                const/16 v0, 0x270f
                return v0
            """)
        }

        UserSetExpertConsultationCountFingerprint.method.addInstructions(0, """
            return-void
        """)

        AskForHelpCountDownExpertConsultationCountFingerprint.method.addInstructions(0, """
            return-void
        """)

        AppContextIsVipFingerprint.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """)

        UserAdditionalDataIsVipInHistoryFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        listOf(
            QueryHasSubscribedExecuteFingerprint.method,
            QueryHasActiveSubscribedExecuteFingerprint.method,
            IsVipInHistoryHandlerExecuteFingerprint.method,
        ).forEach { method ->
            method.addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        listOf(
            QueryHasSubscribedPostFingerprint.method,
            QueryHasActiveSubscribedPostFingerprint.method,
        ).forEach { method ->
            method.apply {
                clearBody()
                addInstructions(0, """
                    invoke-virtual {p1}, Lcom/glority/android/core/route/RouteRequest;->getId()Ljava/lang/String;
                    move-result-object v0
                    sget-object v1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    invoke-static {v0, v1}, Lcom/glority/android/core/route/Router;->onResponse(Ljava/lang/String;Ljava/lang/Object;)V
                    return-void
                """)
            }
        }

        GetVipTypeExecuteFingerprint.method.addInstructions(0, """
            const-string v0, "vip"
            return-object v0
        """)

        listOf(
            GetVipStepExecuteFingerprint.method,
            PaymentGetVipStepExecuteFingerprint.method,
        ).forEach { method ->
            method.addInstructions(0, """
                const/4 v0, 0x0
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """)
        }

        listOf(
            BookPriceTierGetFreeFingerprint.method,
            BookCatalogGetFreeExperienceFingerprint.method,
        ).forEach { method ->
            method.addInstructions(0, """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """)
        }

        BookPriceTierGetVipOnlyFingerprint.method.addInstructions(0, """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
        """)

        BookInventoryIsPurchasedFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        BookUnlockTimesFingerprint.method.addInstructions(0, """
            const/16 v0, 0x270f
            return v0
        """)

        IsPendingVipFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
