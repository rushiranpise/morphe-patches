package app.template.patches.cpuz

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

// r23.f() — billing purchase callback. Sets MainActivity.N=true on purchase.
// Stub so billing can never reset N to false.
val CPUZPurchaseCallbackFingerprint = Fingerprint(
    definingClass = "Lr23;",
    name = "f",
    returnType = "V",
    parameters = listOf("Lya;", "Lh80;"),
)

// MobileAdsInitProvider.attachInfo — ContentProvider auto-init of ad SDK.
val CPUZMobileAdsInitProviderAttachInfoFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/MobileAdsInitProvider;",
    name = "attachInfo",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Landroid/content/pm/ProviderInfo;"),
)

// MainActivity.w() — actual ad loader (MobileAds.initialize + banner/interstitial).
// Gated by l04.a() (consent check), NOT by N:Z/purchase state.
val CPUZAdLoaderFingerprint = Fingerprint(
    definingClass = "Lcom/cpuid/cpu_z/MainActivity;",
    name = "w",
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        string("com.huawei.hwid"),
    ),
)

// wc.d() — ad resume/refresh trigger, also calls l04.a() then w().
val CPUZAdResumeFingerprint = Fingerprint(
    definingClass = "Lwc;",
    name = "d",
    returnType = "V",
    parameters = emptyList(),
)
