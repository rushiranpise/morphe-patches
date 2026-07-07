package app.template.patches.toomics

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.blockerhero.ensureRegisters
import app.template.patches.shared.Constants.TOOMICS_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val toomicsUnlockPremiumPatch = bytecodePatch(
    name = "Remove ADS",
    description = "Remove in-app ads",
    default = true
) {
    compatibleWith(TOOMICS_COMPATIBILITY)

    execute {
        mutableClassDefByOrNull("Lcom/toomics/global/google/age/PlayAgeSignalsChecker;")?.methods
            ?.firstOrNull { it.name == "check" && it.returnType == "Ljava/lang/Object;" }
            ?.apply {
                clearBody()
                ensureRegisters(7)
                addInstructions(
                    0,
                    """
                    new-instance v0, Lcom/toomics/global/google/age/AgeSignalsDomain;
                    sget-object v1, Lcom/toomics/global/google/age/AgeCategory${'$'}AdultVerified;->INSTANCE:Lcom/toomics/global/google/age/AgeCategory${'$'}AdultVerified;
                    const-string v2, "adult_verified"
                    const/16 v3, 0x12
                    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v3
                    const/16 v4, 0x63
                    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v4
                    const/4 v5, 0x0
                    const-string v6, "morphe"
                    invoke-direct/range {v0 .. v6}, Lcom/toomics/global/google/age/AgeSignalsDomain;-><init>(Lcom/toomics/global/google/age/AgeCategory;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Long;Ljava/lang/String;)V
                    return-object v0
                    """.trimIndent()
                )
            }

        mutableClassDefByOrNull("Lcom/toomics/global/google/inapp/viewmodel/PurchaseViewModel;")?.methods
            ?.filter {
                (it.name == "requestBillingFlow" || it.name == "requestTryToServer" || it.name == "requestRestore") &&
                    it.returnType == "V"
            }
            ?.forEach { method ->
                method.clearBody()
                method.addInstructions(0, "return-void")
            }

        mutableClassDefByOrNull("Lcom/toomics/global/google/view/activity/WebviewBaseActivity;")?.methods
            ?.firstOrNull { it.name == "x0" && it.returnType == "V" && it.parameters.size == 1 }
            ?.apply {
                clearBody()
                addInstructions(0, "return-void")
            }

        listOf(
            "Lcom/toomics/global/google/view/activity/MainActivity;",
            "Lcom/toomics/global/google/view/activity/ViewerActivity;"
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter {
                    (it.name == "x0" || it.name == "y0") &&
                        it.returnType == "V" &&
                        it.parameters.size == 1
                }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }
        }

        mutableClassDefByOrNull("Lcom/toomics/global/google/helper/JSBridge;")?.methods
            ?.firstOrNull { it.name == "notiToSvrPurchaseHistoryCompleted" && it.returnType == "V" }
            ?.addInstructions(
                0,
                """
                const-string p2, "0000"
                """.trimIndent()
            )

        val appPrefix = "Lcom/toomics/global/google/"
        val adReferences = listOf(
            "Lcom/google/android/gms/ads/MobileAds;->initialize",
            "Lcom/google/android/gms/ads/BaseAdView;->loadAd",
            "Lcom/google/android/gms/ads/AdView;->loadAd",
            "Lcom/google/android/gms/ads/appopen/AppOpenAd;->load",
            "Lcom/google/android/gms/ads/appopen/AppOpenAd;->show",
            "Lcom/google/android/gms/ads/interstitial/InterstitialAd;->load",
            "Lcom/google/android/gms/ads/interstitial/InterstitialAd;->show",
            "Lcom/google/android/gms/ads/rewarded/RewardedAd;->load",
            "Lcom/google/android/gms/ads/rewarded/RewardedAd;->show",
            "Lcom/applovin/mediation/ads/MaxAdView;->loadAd",
            "Lcom/applovin/mediation/ads/MaxInterstitialAd;->loadAd",
            "Lcom/applovin/mediation/ads/MaxInterstitialAd;->showAd",
            "Lcom/applovin/mediation/ads/MaxRewardedAd;->loadAd",
            "Lcom/applovin/mediation/ads/MaxRewardedAd;->showAd",
        )

        classDefForEach { classDef ->
            if (!classDef.type.startsWith(appPrefix)) return@classDefForEach

            val targets = classDef.methods.filter { method ->
                method.returnType == "V" &&
                    method.implementation?.instructions?.any { instruction ->
                        val reference = (instruction as? ReferenceInstruction)?.reference?.toString()
                        instruction.opcode.name.startsWith("INVOKE") &&
                            reference != null &&
                            adReferences.any { reference.startsWith(it) }
                    } == true
            }

            if (targets.isEmpty()) return@classDefForEach

            val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
            targets.forEach { method ->
                mutableClass.methods.firstOrNull { candidate ->
                    candidate.name == method.name &&
                        candidate.returnType == method.returnType &&
                        candidate.parameterTypes.map { it.toString() } == method.parameterTypes.map { it.toString() }
                }?.apply {
                    clearBody()
                    addInstructions(0, "return-void")
                }
            }
        }
    }
}
