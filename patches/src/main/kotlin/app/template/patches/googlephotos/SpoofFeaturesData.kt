package app.template.patches.googlephotos

internal const val DEFAULT_GOOGLE_PHOTOS_PROFILE = "pixel_xl"

internal enum class GooglePhotosFeatureLevel(val flags: List<String>) {
    PIXEL_2016(
        listOf(
            "com.google.android.apps.photos.NEXUS_PRELOAD",
            "com.google.android.apps.photos.nexus_preload",
            "com.google.android.feature.PIXEL_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2016_PRELOAD",
        ),
    ),
    PIXEL_2017(
        listOf(
            "com.google.android.feature.PIXEL_2017_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2017_PRELOAD",
        ),
    ),
    PIXEL_2018(
        listOf(
            "com.google.android.feature.PIXEL_2018_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2018_PRELOAD",
        ),
    ),
    PIXEL_2019_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2019_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2019_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2019(
        listOf(
            "com.google.android.feature.PIXEL_2019_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2019_PRELOAD",
        ),
    ),
    PIXEL_2020_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2020_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2020(
        listOf(
            "com.google.android.feature.PIXEL_2020_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2020_PRELOAD",
        ),
    ),
    PIXEL_2021_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2021_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2021_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2021(
        listOf(
            "com.google.android.feature.PIXEL_2021_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2021_PRELOAD",
        ),
    ),
    PIXEL_2022_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2022_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2022(
        listOf(
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2022_PRELOAD",
        ),
    ),
    PIXEL_2023_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2023_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2023_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2023(
        listOf(
            "com.google.android.feature.PIXEL_2023_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2023_PRELOAD",
        ),
    ),
    PIXEL_2024_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2024_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2024_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2024(
        listOf(
            "com.google.android.feature.PIXEL_2024_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2024_PRELOAD",
        ),
    ),
    PIXEL_2025_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2025_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2025_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2025(
        listOf(
            "com.google.android.feature.PIXEL_2025_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2025_PRELOAD",
        ),
    ),
    PIXEL_2026_MIDYEAR(
        listOf(
            "com.google.android.feature.PIXEL_2026_MIDYEAR_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2026_MIDYEAR_PRELOAD",
        ),
    ),
    PIXEL_2026(
        listOf(
            "com.google.android.feature.PIXEL_2026_EXPERIENCE",
            "com.google.android.apps.photos.PIXEL_2026_PRELOAD",
        ),
    ),
}

internal data class GooglePhotosPixelProfile(
    val key: String,
    val title: String,
    val buildProps: Map<String, String>,
    val featureLevel: GooglePhotosFeatureLevel,
    val androidVersion: GooglePhotosAndroidVersion?,
) {
    fun enabledFeatures(): Set<String> =
        GooglePhotosFeatureLevel.values()
            .take(featureLevel.ordinal + 1)
            .flatMap { it.flags }
            .toSet()
}

internal data class GooglePhotosAndroidVersion(
    val key: String,
    val title: String,
    val release: String,
    val sdk: Int,
) {
    val buildVersionProps = mapOf(
        "RELEASE" to release,
    )
}

internal const val GOOGLE_PHOTOS_ANDROID_VERSION_NONE = "none"
internal const val GOOGLE_PHOTOS_ANDROID_VERSION_FOLLOW_PROFILE = "follow_profile"

internal val GOOGLE_PHOTOS_ANDROID_VERSIONS = listOf(
    GooglePhotosAndroidVersion("nougat_7_1_2", "Nougat 7.1.2", "7.1.2", 25),
    GooglePhotosAndroidVersion("oreo_8_1_0", "Oreo 8.1.0", "8.1.0", 27),
    GooglePhotosAndroidVersion("pie_9_0", "Pie 9.0", "9", 28),
    GooglePhotosAndroidVersion("q_10_0", "Q 10.0", "10", 29),
    GooglePhotosAndroidVersion("r_11_0", "R 11.0", "11", 30),
    GooglePhotosAndroidVersion("s_12_0", "S 12.0", "12", 31),
    GooglePhotosAndroidVersion("android_13", "Android 13", "13", 33),
    GooglePhotosAndroidVersion("android_14", "Android 14", "14", 34),
    GooglePhotosAndroidVersion("android_15", "Android 15", "15", 35),
    GooglePhotosAndroidVersion("android_16", "Android 16", "16", 36),
    GooglePhotosAndroidVersion("android_17", "Android 17", "17", 37),
).associateBy { it.key }

private val GOOGLE_PHOTOS_ANDROID_VERSIONS_BY_RELEASE =
    GOOGLE_PHOTOS_ANDROID_VERSIONS.values.associateBy { it.release }

internal val GOOGLE_PHOTOS_ANDROID_VERSION_OPTIONS =
    linkedMapOf(
        "Do not spoof Android version" to GOOGLE_PHOTOS_ANDROID_VERSION_NONE,
        "Follow Pixel profile" to GOOGLE_PHOTOS_ANDROID_VERSION_FOLLOW_PROFILE,
    ).apply {
        putAll(GOOGLE_PHOTOS_ANDROID_VERSIONS.values.associate { it.title to it.key })
    }

internal val GOOGLE_PHOTOS_PROFILES = listOf(
    googlePhotosProfile(
        "pixel_xl",
        "Pixel XL - unlimited original quality",
        "marlin",
        "Pixel XL",
        "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2016,
    ),
    googlePhotosProfile(
        "pixel",
        "Pixel - unlimited original quality",
        "sailfish",
        "Pixel",
        "google/sailfish/sailfish:10/QP1A.191005.007.A3/5972272:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2016,
    ),
    googlePhotosProfile(
        "pixel_2",
        "Pixel 2 - storage saver",
        "walleye",
        "Pixel 2",
        "google/walleye/walleye:8.1.0/OPM1.171019.021/4565141:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2017,
    ),
    googlePhotosProfile(
        "pixel_3_xl",
        "Pixel 3 XL - storage saver",
        "crosshatch",
        "Pixel 3 XL",
        "google/crosshatch/crosshatch:11/RQ3A.211001.001/7641976:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2018,
    ),
    googlePhotosProfile(
        "pixel_3a_xl",
        "Pixel 3a XL - storage saver",
        "bonito",
        "Pixel 3a XL",
        "google/bonito/bonito:11/RQ3A.211001.001/7641976:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2019_MIDYEAR,
    ),
    googlePhotosProfile(
        "pixel_4_xl",
        "Pixel 4 XL - storage saver",
        "coral",
        "Pixel 4 XL",
        "google/coral/coral:12/SP1A.211105.002/7743617:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2019,
    ),
    googlePhotosProfile(
        "pixel_4a",
        "Pixel 4a - storage saver",
        "sunfish",
        "Pixel 4a",
        "google/sunfish/sunfish:11/RQ3A.211001.001/7641976:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2020_MIDYEAR,
    ),
    googlePhotosProfile(
        "pixel_5",
        "Pixel 5 - storage saver",
        "redfin",
        "Pixel 5",
        "google/redfin/redfin:12/SP1A.211105.003/7757856:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2020,
    ),
    googlePhotosProfile(
        "pixel_5a",
        "Pixel 5a - storage saver",
        "barbet",
        "Pixel 5a",
        "google/barbet/barbet:11/RD2A.211001.002/7644766:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2021_MIDYEAR,
    ),
    googlePhotosProfile(
        "pixel_6_pro",
        "Pixel 6 Pro - feature gates only",
        "raven",
        "Pixel 6 Pro",
        "google/raven/raven:12/SD1A.210817.036/7805805:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2021,
    ),
    googlePhotosProfile(
        "pixel_6a",
        "Pixel 6a - feature gates only",
        "bluejay",
        "Pixel 6a",
        "google/bluejay/bluejay:15/AP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2022,
    ),
    googlePhotosProfile(
        "pixel_7",
        "Pixel 7 - feature gates only",
        "panther",
        "Pixel 7",
        "google/panther/panther:15/AP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2022,
    ),
    googlePhotosProfile(
        "pixel_7_pro",
        "Pixel 7 Pro - feature gates only",
        "cheetah",
        "Pixel 7 Pro",
        "google/cheetah/cheetah:13/TQ2A.230305.008.C1/9619669:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2022,
    ),
    googlePhotosProfile(
        "pixel_7a",
        "Pixel 7a - feature gates only",
        "lynx",
        "Pixel 7a",
        "google/lynx/lynx:15/AP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2023,
    ),
    googlePhotosProfile(
        "pixel_8",
        "Pixel 8 - feature gates only",
        "shiba",
        "Pixel 8",
        "google/shiba/shiba:15/AP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2023,
    ),
    googlePhotosProfile(
        "pixel_8_pro",
        "Pixel 8 Pro - Video Boost",
        "husky",
        "Pixel 8 Pro",
        "google/husky/husky:14/UD1A.230803.041/10808477:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2023,
    ),
    googlePhotosProfile(
        "pixel_8a",
        "Pixel 8a - feature gates only",
        "akita",
        "Pixel 8a",
        "google/akita/akita:15/AP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2024,
    ),
    googlePhotosProfile(
        "pixel_9",
        "Pixel 9 - AI editing",
        "tokay",
        "Pixel 9",
        "google/tokay/tokay:16/BP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2024,
    ),
    googlePhotosProfile(
        "pixel_9_pro",
        "Pixel 9 Pro - AI editing",
        "caiman",
        "Pixel 9 Pro",
        "google/caiman/caiman:16/BP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2024,
    ),
    googlePhotosProfile(
        "pixel_9_pro_xl",
        "Pixel 9 Pro XL - AI editing",
        "komodo",
        "Pixel 9 Pro XL",
        "google/komodo/komodo:14/AD1A.240530.047.F1/12150327:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2024,
    ),
    googlePhotosProfile(
        "pixel_9a",
        "Pixel 9a - AI editing",
        "tehua",
        "Pixel 9a",
        "google/tehua/tehua:16/BP1A.250405.002/13115780:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2024,
    ),
    googlePhotosProfile(
        "pixel_10_pro_xl",
        "Pixel 10 Pro XL - latest Pixel AI",
        "mustang",
        "Pixel 10 Pro XL",
        "google/mustang/mustang:16/BP1A.250805.005/14000000:user/release-keys",
        GooglePhotosFeatureLevel.PIXEL_2025,
    ),
).associateBy { it.key }

internal val GOOGLE_PHOTOS_PROFILE_OPTIONS =
    GOOGLE_PHOTOS_PROFILES.values.associate { it.title to it.key }

internal val ALL_GOOGLE_PHOTOS_PIXEL_FEATURES =
    GooglePhotosFeatureLevel.values().flatMap { it.flags }.toSet()

internal fun GooglePhotosPixelProfile.disabledFeatures() =
    ALL_GOOGLE_PHOTOS_PIXEL_FEATURES - enabledFeatures()

internal fun GooglePhotosPixelProfile.systemPropertyOverrides(): Map<String, String> {
    val props = buildProps
    val out = linkedMapOf<String, String>()

    fun putKey(key: String, value: String?) {
        if (!value.isNullOrEmpty()) out[key] = value
    }

    putKey("ro.product.brand", props["BRAND"])
    putKey("ro.product.manufacturer", props["MANUFACTURER"])
    putKey("ro.product.device", props["DEVICE"])
    putKey("ro.product.name", props["PRODUCT"])
    putKey("ro.product.model", props["MODEL"])
    putKey("ro.product.model.marketname", props["MODEL"])
    putKey("ro.build.product", props["PRODUCT"])
    putKey("ro.build.fingerprint", props["FINGERPRINT"])
    putKey("ro.vendor.build.fingerprint", props["FINGERPRINT"])
    putKey("ro.system.build.fingerprint", props["FINGERPRINT"])
    putKey("ro.bootimage.build.fingerprint", props["FINGERPRINT"])
    putKey("ro.odm.build.fingerprint", props["FINGERPRINT"])
    putKey("ro.product.system.brand", props["BRAND"])
    putKey("ro.product.system.device", props["DEVICE"])
    putKey("ro.product.system.model", props["MODEL"])
    putKey("ro.product.system.name", props["PRODUCT"])
    putKey("ro.product.system.manufacturer", props["MANUFACTURER"])
    putKey("ro.product.vendor.brand", props["BRAND"])
    putKey("ro.product.vendor.device", props["DEVICE"])
    putKey("ro.product.vendor.model", props["MODEL"])
    putKey("ro.product.vendor.name", props["PRODUCT"])
    putKey("ro.product.vendor.manufacturer", props["MANUFACTURER"])
    putKey("ro.product.odm.brand", props["BRAND"])
    putKey("ro.product.odm.device", props["DEVICE"])
    putKey("ro.product.odm.model", props["MODEL"])
    putKey("ro.product.odm.name", props["PRODUCT"])
    putKey("ro.product.odm.manufacturer", props["MANUFACTURER"])
    putKey("ro.build.id", props["ID"])
    putKey("ro.build.version.incremental", props["FINGERPRINT"]?.fingerprintIncremental())

    return out
}

private fun googlePhotosProfile(
    key: String,
    title: String,
    device: String,
    model: String,
    fingerprint: String,
    featureLevel: GooglePhotosFeatureLevel,
): GooglePhotosPixelProfile {
    val release = fingerprint.substringAfter(':').substringBefore('/')
    val id = fingerprint.substringAfter(':').substringAfter('/').substringBefore('/')
    val props = linkedMapOf(
        "BRAND" to "google",
        "MANUFACTURER" to "Google",
        "BOARD" to device,
        "DEVICE" to device,
        "PRODUCT" to device,
        "HARDWARE" to device,
        "MODEL" to model,
        "ID" to id,
        "DISPLAY" to id,
        "FINGERPRINT" to fingerprint,
        "TAGS" to "release-keys",
        "TYPE" to "user",
        "USER" to "android-build",
    )

    return GooglePhotosPixelProfile(
        key,
        title,
        props,
        featureLevel,
        GOOGLE_PHOTOS_ANDROID_VERSIONS_BY_RELEASE[release],
    )
}

private fun String.fingerprintIncremental(): String? =
    substringAfter(':', missingDelimiterValue = "")
        .split('/')
        .getOrNull(2)
        ?.substringBefore(':')
        ?.takeIf { it.isNotEmpty() }
