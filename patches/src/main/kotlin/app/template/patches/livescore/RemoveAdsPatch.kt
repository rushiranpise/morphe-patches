package app.template.patches.livescore

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants

// ═══════════════════════════════════════════════════════════════════════════════
// Stability architecture — why this patch survives updates
// ═══════════════════════════════════════════════════════════════════════════════
//
// Every target class uses its full non-obfuscated package name (com.livescore.*
// or the SDK's own stable name). No obfuscated class, method, or field name is
// used anywhere in this file.
//
// Method matching is done by ROLE (return type) not exhaustive name lists, so
// new overloads added in future releases are covered automatically.
//
// SDK kill-switch note — return type discipline:
//   All targeted SDK entry points return void EXCEPT:
//     MobileAdsInitProvider.onCreate()Z — ContentProvider subclass, must return
//     boolean. We return false (provider failed to init) not return-void, which
//     would produce a VerifyError crashing the app at startup.
//   This has been verified against smali for every method in the kill list.
// ═══════════════════════════════════════════════════════════════════════════════

@Suppress("unused")
val liveScoreRemoveAdsPatch = bytecodePatch(
    name = "Remove ads",
    description = "Disables LiveScore banner, native, and interstitial ad requests.",
    default = true,
) {
    compatibleWith(Constants.LIVESCORE_COMPATIBILITY)

    execute {

        // ── 1. BannersHelper ──────────────────────────────────────────────────
        // App-owned singleton managing ALL ad session state. Every ad request
        // method returns BannerViewLoader$JobTag — returning null cancels it.
        // Stable anchor: returnType == BannerViewLoader$JobTag covers all current
        // overloads and any future ones automatically.
        // Void helpers matched by name.startsWith("setTargeting") to absorb
        // Kotlin name-mangling changes (suffix depends on parameter types).
        val bannersHelper = mutableClassDefBy("Lcom/livescore/media/banners/BannersHelper;")

        bannersHelper.methods
            .filter { it.returnType == "Lcom/livescore/ads/views/BannerViewLoader\$JobTag;" }
            .forEach { method ->
                method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
            }

        bannersHelper.methods
            .filter {
                it.returnType == "V" && it.implementation != null &&
                    (it.name in setOf("warmUp", "suppressBanner", "hideBanner") ||
                        it.name.startsWith("setTargeting"))
            }
            .forEach { method -> method.addInstructions(0, "return-void") }

        // ── 2. BannerLoader implementors ──────────────────────────────────────
        // DirectLoader, Preloader, SharedCachePreloader all implement BannerLoader.
        // load() → null; getShouldPreload() → false; lifecycle voids → return-void.
        listOf(
            "Lcom/livescore/architecture/feature/mpuads/DirectLoader;",
            "Lcom/livescore/architecture/feature/mpuads/Preloader;",
            "Lcom/livescore/architecture/feature/mpuads/SharedCachePreloader;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods?.forEach { method ->
                when {
                    method.name == "load" &&
                        method.returnType == "Lcom/livescore/ads/views/BannerViewLoader\$JobTag;" ->
                        method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")

                    method.name == "getShouldPreload" && method.returnType == "Z" ->
                        method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")

                    method.name in setOf("preload", "reset", "cacheBanner", "unCacheBanner") &&
                        method.returnType == "V" ->
                        method.addInstructions(0, "return-void")
                }
            }
        }

        // ── 3. InterstitialAdLoader ───────────────────────────────────────────
        // loadInterstitial() returns JobTag — null cancels it.
        // Skip the private overload (accessFlags & ACC_PRIVATE = 0x2).
        mutableClassDefByOrNull("Lcom/livescore/ads/interstitial/InterstitialAdLoader;")
            ?.methods
            ?.filter {
                it.name == "loadInterstitial" &&
                    it.returnType == "Lcom/livescore/ads/views/BannerViewLoader\$JobTag;" &&
                    it.accessFlags and 0x2 == 0
            }
            ?.forEach { method ->
                method.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
            }

        // ── 4. ViewHolderInListBannerBase ─────────────────────────────────────
        // RecyclerView ViewHolder that renders in-list MPU banners.
        // return-void at entry: safe regardless of parameter count changes.
        mutableClassDefBy(
            "Lcom/livescore/architecture/feature/mpuads/ViewHolderInListBannerBase;",
        ).methods.forEach { method ->
            if (method.returnType != "V" || method.implementation == null) return@forEach
            if (method.name in setOf(
                    "onBind", "addBannerView", "fillWithShimmer", "fillWithContent",
                    "reloadBanner", "scheduleUpdate", "onBannerExtracted",
                    "refreshBannerSize", "updateBannerInitialSize",
                )
            ) method.addInstructions(0, "return-void")
        }

        // ── 5. Adapter delegates — getItemViewType → null ─────────────────────
        // Returning null tells the adapter this delegate owns no item type.
        // LandingMpuAdapterDelegate has no own override in 9.9.1 (inherits from
        // MpuAdapterDelegate which IS patched) — kept in list for when it
        // regains its own override in a future version.
        listOf(
            "Lcom/livescore/architecture/feature/mpuads/MpuAdapterDelegate;",
            "Lcom/livescore/architecture/feature/mpuads/secondplacement/SecondMpuPlacementAdapterDelegate;",
            "Lcom/livescore/architecture/feature/mpuads/secondplacement/RestMpuPlacementAdapterDelegate;",
            "Lcom/livescore/architecture/aggregatednews/landing/LandingMpuAdapterDelegate;",
            "Lcom/livescore/coverage_sponsorship/rv/CoverageSponsorshipMEVAdPlacementDelegate;",
            "Lcom/livescore/architecture/announcement/ABAdapterDelegate;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.firstOrNull { method ->
                    method.name == "getItemViewType" &&
                        method.returnType == "Ljava/lang/Integer;" &&
                        method.implementation != null
                }
                ?.addInstructions(0, "const/4 v0, 0x0\nreturn-object v0")
        }

        // ── 6. MpuLazyListDelegate (Compose lazy-list ad slot) ────────────────
        // handlesDataClass(Object)Z → false: delegate never claims any item.
        // widget(...) → return-void: defence-in-depth, covers all Compose overloads.
        mutableClassDefByOrNull(
            "Lcom/livescore/architecture/feature/mpuads/MpuLazyListDelegate;",
        )?.methods?.forEach { method ->
            if (method.implementation == null) return@forEach
            when {
                method.name == "handlesDataClass" && method.returnType == "Z" ->
                    method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                method.name == "widget" && method.returnType == "V" ->
                    method.addInstructions(0, "return-void")
            }
        }

        // ── 7. Announcement banner ────────────────────────────────────────────
        mutableClassDefByOrNull(
            "Lcom/livescore/architecture/announcement/ViewHolderAnnouncementBannerGamNativeBannerDelegate;",
        )?.methods
            ?.filter { it.implementation != null && it.returnType == "V" &&
                it.name in setOf("onBind", "reload") }
            ?.forEach { method -> method.addInstructions(0, "return-void") }

        // ── 8. SDK-level kill switches ────────────────────────────────────────
        // Prevents ad SDKs from initialising even if a new app-level entry point
        // is added. SDK class names are never obfuscated (third-party).
        //
        // CRITICAL: MobileAdsInitProvider extends ContentProvider.
        //   ContentProvider.onCreate() returns boolean (Z), not void.
        //   Injecting return-void into a ()Z method causes VerifyError at class
        //   load → instant crash before the app starts.
        //   Fix: detect boolean-returning onCreate() and emit
        //        "const/4 v0, 0x0 / return v0" (return false) instead.
        //   All other matched methods in this list return void — verified against
        //   smali for every class below.
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
            "Lcom/amazon/aps/ads/ApsAdView;",
            "Lcom/vungle/mediation/VungleInterstitialAdapter;",
            "Lcom/vungle/ads/VungleAds;",
            "Lcom/vungle/ads/BaseAd;",
            "Lcom/vungle/ads/VungleBannerView;",
            "Lcom/appharbr/sdk/engine/AppHarbr;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { method ->
                    method.implementation != null &&
                        method.name in setOf(
                            "initialize", "onCreate", "load", "loadAd", "loadAds",
                            "loadAdManager", "requestAd", "requestBannerAd",
                            "requestInterstitialAd", "requestNativeAd", "requestRewardedAd",
                            "show", "showAd", "displayAd", "render", "resume", "start",
                        )
                }
                ?.forEach { method ->
                    // ContentProvider.onCreate() returns Z (boolean), not void.
                    // Emitting return-void into a ()Z method causes VerifyError.
                    // Return false for any boolean-returning method in this list.
                    if (method.returnType == "Z") {
                        method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                    } else {
                        method.addInstructions(0, "return-void")
                    }
                }
        }
    }
}
