package app.template.patches.opera_news.ads

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.OPERA_NEWS_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction

@Suppress("unused")
val operaNewsAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Blocks all ad display across Opera News's five ad networks and " +
        "all mediation layers (AdsFacade, AppLovin MAX, TopOn, MCSDK, SmartDigiMkt).",
    default = true,
) {
    compatibleWith(OPERA_NEWS_COMPATIBILITY)

    execute {
        // ── Layer 0a: AdsFacade — orchestration layer ──
        // s()Z/t()Z: F()/G() check these; true = return original list with NO ad slots.
        // r()Z: "isAdFree" gate for activity-level ads; true = no ads.
        // A(Activity)V: activity ad setup — wipe.
        // E(...)Z: interstitial/splash trigger — return false.
        // C(Activity,...)Z: static article ad display, no gate — return false.
        // v/y/u (...)V: article ad show methods that bypass s() — wipe.
        val adsFacade = mutableClassDefByOrNull("Lcom/opera/android/ads/AdsFacade;")
        adsFacade?.methods?.forEach { method ->
            when {
                method.name in listOf("s", "t", "r") &&
                    method.returnType == "Z" &&
                    method.parameters.isEmpty() ->
                    method.returnEarly(true)

                method.name == "A" && method.returnType == "V" && method.implementation != null -> {
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }

                method.name == "E" && method.returnType == "Z" && method.implementation != null -> {
                    method.clearBody()
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }

                method.name == "C" && method.returnType == "Z" && method.implementation != null -> {
                    method.clearBody()
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }

                method.name in listOf("v", "y", "u") && method.returnType == "V" && method.implementation != null -> {
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }
            }
        }

        // ── Layer 0b: PageAdSection — OBML browser page ad system ──
        mutableClassDefByOrNull("Lcom/opera/android/ads/PageAdSection;")
            ?.methods?.filter { it.name in listOf("q", "n", "h") && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // ── Layer 0c: ads/s — individual page ad slot class ──
        mutableClassDefByOrNull("Lcom/opera/android/ads/s;")
            ?.methods?.filter { it.returnType == "V" && it.name in listOf("b", "d", "e", "f", "g", "i") }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // ── Layer 0d: ngi subclasses — page/article ad unit loaders ──
        // gd=AdMob, kl9=TopOn+MCSDK, bw0=AdMob mediation, gr=legacy, que=legacy.
        // Wipe all void methods and return null from g() (the ad unit factory).
        listOf("Lgd;", "Lkl9;", "Lbw0;", "Lgr;", "Lque;").forEach { type ->
            mutableClassDefByOrNull(type)?.methods?.forEach { method ->
                if (method.implementation == null) return@forEach
                when {
                    method.returnType == "V" && !method.name.startsWith("<") -> {
                        method.clearBody()
                        method.addInstructions(0, "return-void")
                    }
                    method.name == "g" && method.returnType == "Lqkh;" -> {
                        method.clearBody()
                        method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
                    }
                }
            }
        }

        // ── Layer 0e: esc — ad request dispatcher ──
        // a() and b() are the concrete dispatch methods (final). d() and e() are abstract — skip.
        mutableClassDefByOrNull("Lesc;")
            ?.methods?.filter { it.returnType == "V" && it.name in listOf("a", "b") && it.implementation != null }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // ── Layer 0f: GMA wrappers, z base class, interstitial preloader ──

        // sd — GMA RewardedAd show wrapper
        mutableClassDefByOrNull("Lsd;")
            ?.methods?.filter { it.name == "u" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // nd — GMA InterstitialAd show wrapper
        mutableClassDefByOrNull("Lnd;")
            ?.methods?.filter { it.name == "u" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // ads/z — abstract interstitial base: y() schedules show Runnable, w() triggers load
        mutableClassDefByOrNull("Lcom/opera/android/ads/z;")
            ?.methods?.filter { it.name in listOf("y", "w") && it.returnType == "V" && it.implementation != null }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // lid — GMA InterstitialAdPreloader: preloads interstitials bypassing esc
        // c()V = destroyAll, d(String,dd)Z = start preloading — target only these
        mutableClassDefByOrNull("Llid;")?.methods
            ?.filter { it.name in listOf("c", "d") && !it.name.startsWith("<") }
            ?.forEach { method ->
                when (method.returnType) {
                    "V" -> { method.clearBody(); method.addInstructions(0, "return-void") }
                    "Z" -> { method.clearBody(); method.addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
                }
            }

        // ── Layer 1: AppLovin MAX — scan all classes ──
        val maxAdReferences = listOf(
            "Lcom/applovin/mediation/ads/MaxAdView;->loadAd",
            "Lcom/applovin/mediation/ads/MaxInterstitialAd;->loadAd",
            "Lcom/applovin/mediation/ads/MaxInterstitialAd;->showAd",
            "Lcom/applovin/mediation/ads/MaxRewardedAd;->loadAd",
            "Lcom/applovin/mediation/ads/MaxRewardedAd;->showAd",
            "Lcom/applovin/mediation/ads/MaxAppOpenAd;->loadAd",
            "Lcom/applovin/mediation/ads/MaxAppOpenAd;->showAd",
            "Lcom/applovin/mediation/nativeAds/MaxNativeAdLoader;->loadAd",
        )

        classDefForEach { classDef ->
            val targets = classDef.methods.filter { method ->
                method.returnType == "V" &&
                    method.implementation?.instructions?.any { insn ->
                        val ref = (insn as? ReferenceInstruction)?.reference?.toString()
                        insn.opcode.name.startsWith("INVOKE") &&
                            ref != null &&
                            maxAdReferences.any { ref.startsWith(it) }
                    } == true
            }
            if (targets.isEmpty()) return@classDefForEach

            val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
            targets.forEach { immutableMethod ->
                mutableClass.methods
                    .firstOrNull { m ->
                        m.name == immutableMethod.name &&
                            m.returnType == immutableMethod.returnType &&
                            m.parameterTypes.map { it.toString() } ==
                                immutableMethod.parameterTypes.map { it.toString() }
                    }
                    ?.apply {
                        clearBody()
                        addInstructions(0, "return-void")
                    }
            }
        }

        // ── Layer 2: TopOn / secmtp ──
        mutableClassDefByOrNull("Lcom/secmtp/sdk/banner/api/ATBannerView;")
            ?.methods?.filter { it.name == "loadAd" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        mutableClassDefByOrNull("Lcom/secmtp/sdk/interstitial/api/ATInterstitial;")
            ?.methods?.filter { it.name == "show" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        mutableClassDefByOrNull("Lcom/secmtp/sdk/rewardvideo/api/ATRewardVideoAd;")
            ?.methods?.filter { it.name == "show" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        mutableClassDefByOrNull("Lcom/secmtp/sdk/splashad/api/ATSplashAd;")
            ?.methods?.filter { it.name == "show" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        mutableClassDefByOrNull("Lcom/secmtp/sdk/nativead/api/ATNative;")
            ?.methods?.filter { it.name == "makeAdRequest" && it.returnType == "V" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        // ── Layer 3: MCSDK ──
        listOf(
            "Lcom/mcsdk/banner/api/MCAdView;",
            "Lcom/mcsdk/interstitial/api/MCInterstitialAd;",
            "Lcom/mcsdk/interstitial/api/MCInterstitialAdManager;",
            "Lcom/mcsdk/reward/api/MCRewardedAd;",
            "Lcom/mcsdk/splash/api/MCAppOpenAd;",
            "Lcom/mcsdk/splash/api/MCSplashAdManager;",
            "Lcom/mcsdk/nativead/api/MCNativeAdLoader;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)
                ?.methods?.filter { it.returnType == "V" && it.name in listOf("loadAd", "showAd", "load", "show") }
                ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }
        }

        // ── Layer 4: SmartDigiMkt (SDM) SDK — 5th ad network in classes6 ──
        listOf(
            "Lcom/smartdigimkt/sdk/api/format/myoffer/SDMMyOfferInterstitialAd;",
            "Lcom/smartdigimkt/sdk/api/format/myoffer/SDMMyOfferBannerView;",
            "Lcom/smartdigimkt/sdk/api/format/myoffer/SDMMyOfferRewardedVideoAd;",
            "Lcom/smartdigimkt/sdk/api/format/myoffer/SDMMyOfferSplashAd;",
            "Lcom/smartdigimkt/sdk/api/format/myoffer/SDMMyOfferNative;",
            "Lcom/smartdigimkt/sdk/api/format/SDMNativeAd;",
            "Lcom/smartdigimkt/sdk/api/format/SDMNative;",
            "Lcom/smartdigimkt/sdk/api/format/SDMRewardedVideoAd;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)
                ?.methods?.filter { it.returnType == "V" && it.name in listOf("load", "show", "loadAd", "showAd", "loadNativeAd") }
                ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }
        }
    }
}
