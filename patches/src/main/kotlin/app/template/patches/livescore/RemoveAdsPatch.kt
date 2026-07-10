package app.template.patches.livescore

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.returnEarly

@Suppress("unused")
val liveScoreRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables LiveScore banner, native, and interstitial ad requests.",
    default = true,
) {
    compatibleWith(Constants.LIVESCORE_COMPATIBILITY)

    execute {
        val bannersHelper = mutableClassDefBy("Lcom/livescore/media/banners/BannersHelper;")

        bannersHelper.methods
            .filter { it.returnType == "Lcom/livescore/ads/views/BannerViewLoader\$JobTag;" }
            .forEach { method ->
                method.addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        return-object v0
                    """,
                )
            }

        bannersHelper.methods
            .filter {
                it.name in setOf("warmUp", "suppressBanner", "hideBanner", "setTargeting-qpaDVJs") &&
                    it.returnType == "V"
            }
            .forEach { method ->
                method.addInstructions(0, "return-void")
            }

        listOf(
            "Lcom/livescore/architecture/feature/mpuads/DirectLoader;",
            "Lcom/livescore/architecture/feature/mpuads/Preloader;",
            "Lcom/livescore/architecture/feature/mpuads/SharedCachePreloader;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods?.forEach { method ->
                when {
                    method.name == "load" &&
                        method.returnType == "Lcom/livescore/ads/views/BannerViewLoader\$JobTag;" ->
                        method.addInstructions(
                            0,
                            """
                                const/4 p0, 0x0
                                return-object p0
                            """,
                        )

                    method.name == "getShouldPreload" && method.returnType == "Z" ->
                        method.addInstructions(
                            0,
                            """
                                const/4 p0, 0x0
                                return p0
                            """,
                        )

                    method.name in setOf("preload", "reset", "cacheBanner", "unCacheBanner") &&
                        method.returnType == "V" ->
                        method.addInstructions(0, "return-void")
                }
            }
        }

        val inListBannerHolder = mutableClassDefBy("Lcom/livescore/architecture/feature/mpuads/ViewHolderInListBannerBase;")
        inListBannerHolder.methods.forEach { method ->
            when {
                method.name == "onBind" && method.returnType == "V" ->
                    method.addInstructions(
                        0,
                        """
                            iget-object p1, p0, Landroidx/recyclerview/widget/RecyclerView${'$'}ViewHolder;->itemView:Landroid/view/View;
                            const/16 p2, 0x8
                            invoke-virtual {p1, p2}, Landroid/view/View;->setVisibility(I)V
                            return-void
                        """,
                    )

                method.name in setOf("addBannerView", "fillWithShimmer", "fillWithContent", "reloadBanner", "scheduleUpdate") &&
                    method.returnType == "V" ->
                    method.addInstructions(0, "return-void")
            }
        }

        listOf(
            "Lcom/livescore/architecture/feature/mpuads/MpuAdapterDelegate;",
            "Lcom/livescore/architecture/feature/mpuads/secondplacement/SecondMpuPlacementAdapterDelegate;",
            "Lcom/livescore/architecture/feature/mpuads/secondplacement/RestMpuPlacementAdapterDelegate;",
            "Lcom/livescore/architecture/aggregatednews/landing/LandingMpuAdapterDelegate;",
            "Lcom/livescore/coverage_sponsorship/rv/CoverageSponsorshipMEVAdPlacementDelegate;",
            "Lcom/livescore/architecture/announcement/ABAdapterDelegate;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { method ->
                    method.implementation != null &&
                        method.name == "getItemViewType" &&
                        method.returnType == "Ljava/lang/Integer;"
                }
                ?.forEach { method ->
                    method.addInstructions(
                        0,
                        """
                            const/4 p0, 0x0
                            return-object p0
                        """,
                    )
                }
        }

        mutableClassDefByOrNull("Lcom/livescore/architecture/feature/mpuads/MpuLazyListDelegate;")?.methods?.forEach { method ->
            when {
                method.implementation != null &&
                    method.name == "handlesDataClass" &&
                    method.returnType == "Z" ->
                    method.addInstructions(
                        0,
                        """
                            const/4 p0, 0x0
                            return p0
                        """,
                    )

                method.implementation != null &&
                    method.name == "widget" &&
                    method.returnType == "V" ->
                    method.addInstructions(0, "return-void")
            }
        }

        mutableClassDefByOrNull("Lcom/livescore/architecture/announcement/ViewHolderAnnouncementBannerGamNativeBannerDelegate;")
            ?.methods
            ?.filter { method ->
                method.implementation != null &&
                    method.name in setOf("onBind", "reload") &&
                    method.returnType == "V"
            }
            ?.forEach { method -> method.addInstructions(0, "return-void") }

        listOf(
            "Lcom/google/android/gms/ads/MobileAds;",
            "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
            "Lcom/google/android/gms/ads/AdView;",
            "Lcom/google/android/gms/ads/BaseAdView;",
            "Lcom/google/android/gms/ads/AdLoader;",
            "Lcom/google/android/gms/ads/admanager/AdManagerAdView;",
            "Lcom/google/android/gms/ads/admanager/AdManagerInterstitialAd;",
            "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
            "Lcom/google/android/gms/ads/nativead/NativeAd;",
            "Lcom/google/android/gms/ads/nativead/NativeAdView;",
            "Lcom/google/android/gms/ads/nativead/MediaView;",
            "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
            "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
            "Lcom/google/ads/mediation/fyber/FyberMediationAdapter;",
            "Lcom/fyber/inneractive/sdk/external/InneractiveAdManager;",
            "Lcom/fyber/inneractive/sdk/external/InneractiveAdRequest;",
            "Lcom/amazon/device/ads/DTBAdRequest;",
            "Lcom/amazon/device/ads/DTBAdView;",
            "Lcom/amazon/aps/ads/ApsAdView;",
            "Lcom/vungle/mediation/VungleInterstitialAdapter;",
            "Lcom/vungle/ads/VungleAds;",
            "Lcom/vungle/ads/BaseAd;",
            "Lcom/vungle/ads/VungleBannerView;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { method ->
                    method.implementation != null &&
                    method.name in setOf(
                        "initialize",
                        "onCreate",
                        "load",
                        "loadAd",
                        "loadAds",
                        "loadAdManager",
                        "requestAd",
                        "requestBannerAd",
                        "requestInterstitialAd",
                        "requestNativeAd",
                        "requestRewardedAd",
                        "show",
                        "showAd",
                        "displayAd",
                        "render",
                        "resume",
                        "start",
                    )
                }
                ?.forEach { method -> method.returnEarly() }
        }
    }
}
