package app.template.extension.extension;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.os.Build;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public final class PixelSpoofHelper {
    private static volatile boolean initialized;
    private static volatile boolean photosMode;

    private static final String MODEL = "Pixel 10 Pro XL";
    private static final String DEVICE = "mustang";
    private static final String PRODUCT = "mustang";
    private static final String BRAND = "google";
    private static final String MANUFACTURER = "Google";
    private static final String FINGERPRINT = "google/mustang/mustang:17/CP2A.260605.012/15430684:user/release-keys";
    private static final String DESCRIPTION = "mustang-user 17 CP2A.260605.012 15430684 release-keys";
    private static final String ID = "CP2A.260605.012";
    private static final String BOOTLOADER = "deepspace-17.2-15372054";
    private static final String SOC_MANUFACTURER = "Google";
    private static final String SOC_MODEL = "Tensor G5";
    private static final String PLATFORM = "laguna";
    private static final String RELEASE = "17";
    private static final int SDK_INT = 37;
    private static final String SDK_FULL = "37.0";
    private static final String SECURITY_PATCH = "2026-06-05";
    private static final String INCREMENTAL = "15430684";
    private static final String FIRST_API_LEVEL = "37";
    private static final String BUILD_DATE = "Fri May 15 15:38:48 PDT 2026";
    private static final String BUILD_UUID = "Pp8difaf-KKDmY-2rNwgGVP0L-Oy5ujwsPvXWqUJuoo";
    private static final String BASEBAND = "g5400i-260317-260429-B-15308590";
    private static final String CLIENT_ID = "android-google";
    private static final String SERIAL = "39061FDJG000Q8";
    private static final long TIME = 1778884728000L;
    private static final String TIME_SEC = "1778884728";

    private static final String PHOTOS_MODEL = "Pixel XL";
    private static final String PHOTOS_DEVICE = "marlin";
    private static final String PHOTOS_PRODUCT = "marlin";
    private static final String PHOTOS_FINGERPRINT = "google/marlin/marlin:10/QP1A.191005.007.A3/5972272:user/release-keys";
    private static final String PHOTOS_DESCRIPTION = "marlin-user 10 QP1A.191005.007.A3 5972272 release-keys";
    private static final String PHOTOS_ID = "QP1A.191005.007.A3";
    private static final String PHOTOS_BOOTLOADER = "unknown";
    private static final String PHOTOS_SOC_MODEL = "MSM8996";
    private static final String PHOTOS_PLATFORM = "msm8996";
    private static final String PHOTOS_RELEASE = "10";
    private static final int PHOTOS_SDK_INT = 29;
    private static final String PHOTOS_SECURITY_PATCH = "2019-10-05";
    private static final String PHOTOS_INCREMENTAL = "5972272";
    private static final String PHOTOS_FIRST_API_LEVEL = "25";
    private static final String PHOTOS_TIME_SEC = "1570233600";
    private static final long PHOTOS_TIME = 1570233600000L;

    private static final Set<String> PIXEL_FEATURES = new HashSet<String>();
    private static final Set<String> PHOTOS_ENABLE_FEATURES = new HashSet<String>();
    private static final Set<String> PHOTOS_DISABLE_FEATURES = new HashSet<String>();

    static {
        String[] features = {
            "com.google.android.feature.PIXEL_EXPERIENCE",
            "com.google.android.feature.TURBO_PRELOAD",
            "com.google.android.feature.WELLBEING",
            "com.google.android.feature.D2D_CABLE_MIGRATION_FEATURE",
            "com.google.android.feature.PIXEL_2017_EXPERIENCE",
            "com.google.android.feature.PIXEL_2018_EXPERIENCE",
            "com.google.android.feature.PIXEL_2019_EXPERIENCE",
            "com.google.android.feature.PIXEL_2019_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2020_EXPERIENCE",
            "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.GOOGLE_BUILD",
            "com.google.android.feature.GOOGLE_EXPERIENCE",
            "com.google.android.feature.GOOGLE_CAMERA_EXPERIENCE",
            "com.google.android.feature.QUICK_TAP",
            "com.google.android.feature.NOW_PLAYING_APP_26Q1",
            "com.google.android.feature.NEXT_GENERATION_ASSISTANT",
            "com.google.android.feature.GEMINI_EXPERIENCE",
            "com.google.android.feature.AMBIENT_DATA",
            "com.google.android.feature.CONTEXTUAL_SEARCH",
            "com.google.android.feature.CONTEXTUAL_SEARCH_LIVE_TRANSLATE",
            "com.android.systemui.SUPPORTS_DRAG_ASSISTANT_TO_SPLIT"
        };
        for (String feature : features) PIXEL_FEATURES.add(feature);

        String[] photosEnabled = {
            "com.google.android.apps.photos.NEXUS_PRELOAD",
            "com.google.android.apps.photos.nexus_preload"
        };
        for (String feature : photosEnabled) PHOTOS_ENABLE_FEATURES.add(feature);

        String[] photosDisabled = {
            "com.google.android.apps.photos.PIXEL_2017_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2018_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2019_MIDYEAR_PRELOAD",
            "com.google.android.apps.photos.PIXEL_2019_PRELOAD",
            "com.google.android.feature.PIXEL_2020_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2020_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2021_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2022_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2023_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2024_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_MIDYEAR_EXPERIENCE",
            "com.google.android.feature.PIXEL_2025_EXPERIENCE"
        };
        for (String feature : photosDisabled) PHOTOS_DISABLE_FEATURES.add(feature);
    }

    public static void init(Context context, String mode) {
        if (initialized) return;
        initialized = true;
        photosMode = mode != null && mode.contains("photos");
        boolean build = mode == null || mode.contains("build");
        boolean props = mode == null || mode.contains("props");
        boolean features = mode == null || mode.contains("features");
        exemptHiddenApis();
        if (build) spoofBuildFields();
        if (features && context != null) spoofPackageFeatures(context);
    }

    private static void exemptHiddenApis() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Class<?> bypass = Class.forName("org.lsposed.hiddenapibypass.HiddenApiBypass");
                bypass.getMethod("addHiddenApiExemptions", String[].class)
                    .invoke(null, (Object) new String[]{"L"});
            }
        } catch (Throwable ignored) {}
    }

    private static void spoofBuildFields() {
        setStatic(Build.class, "DISPLAY", id());
        setStatic(Build.class, "BOOTLOADER", bootloader());
        setStatic(Build.class, "HARDWARE", device());
        setStatic(Build.class, "BOARD", device());
        setStatic(Build.class, "BRAND", BRAND);
        setStatic(Build.class, "DEVICE", device());
        setStatic(Build.class, "PRODUCT", product());
        setStatic(Build.class, "MANUFACTURER", MANUFACTURER);
        setStatic(Build.class, "MODEL", model());
        setStatic(Build.class, "SERIAL", SERIAL);
        setStatic(Build.class, "SOC_MANUFACTURER", SOC_MANUFACTURER);
        setStatic(Build.class, "SOC_MODEL", socModel());
        setStatic(Build.class, "ID", id());
        setStatic(Build.class, "TIME", buildTime());
        setStatic(Build.class, "TAGS", "release-keys");
        setStatic(Build.class, "TYPE", "user");
        setStatic(Build.class, "USER", "android-build");
        setStatic(Build.class, "HOST", "bcb8c9bcce95");
        setStatic(Build.class, "FINGERPRINT", fingerprint());
        setStatic(Build.class, "SUPPORTED_ABIS", new String[]{"arm64-v8a"});
        setStatic(Build.class, "SUPPORTED_64_BIT_ABIS", new String[]{"arm64-v8a"});
        setStatic(Build.class, "SUPPORTED_32_BIT_ABIS", new String[]{});
        setStatic(Build.VERSION.class, "RELEASE", release());
        setStatic(Build.VERSION.class, "SDK_INT", sdkInt());
        setStatic(Build.VERSION.class, "SECURITY_PATCH", securityPatch());
        setStatic(Build.VERSION.class, "INCREMENTAL", incremental());
    }

    private static void spoofPackageFeatures(Context context) {
        try {
            Object pm = context.getPackageManager();
            Class<?> apmClass = Class.forName("android.app.ApplicationPackageManager");
            Field mPMField = apmClass.getDeclaredField("mPM");
            mPMField.setAccessible(true);
            Object original = mPMField.get(pm);
            if (original == null) return;
            Object proxy = Proxy.newProxyInstance(
                original.getClass().getClassLoader(),
                original.getClass().getInterfaces(),
                new FeatureHandler(original)
            );
            mPMField.set(pm, proxy);
            try {
                Class<?> atClass = Class.forName("android.app.ActivityThread");
                Field sPMField = atClass.getDeclaredField("sPackageManager");
                sPMField.setAccessible(true);
                sPMField.set(null, proxy);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    private static final class FeatureHandler implements InvocationHandler {
        private final Object original;
        FeatureHandler(Object original) { this.original = original; }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("hasSystemFeature".equals(name) && args != null && args.length > 0) {
                Boolean forced = forcedFeatureValue(String.valueOf(args[0]));
                if (forced != null) return forced;
            }
            Object result;
            try {
                result = method.invoke(original, args);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getTargetException();
            }
            if ("getSystemAvailableFeatures".equals(name) && result instanceof FeatureInfo[]) {
                return addPixelFeatures((FeatureInfo[]) result);
            }
            return result;
        }
    }

    private static FeatureInfo[] addPixelFeatures(FeatureInfo[] original) {
        Set<String> featuresToAdd = photosMode ? PHOTOS_ENABLE_FEATURES : PIXEL_FEATURES;
        Set<String> seen = new HashSet<String>();
        int kept = 0;
        for (FeatureInfo info : original) {
            if (info == null || info.name == null) {
                kept++;
                continue;
            }
            if (photosMode && PHOTOS_DISABLE_FEATURES.contains(info.name)) continue;
            seen.add(info.name);
            kept++;
        }
        int extra = 0;
        for (String feature : featuresToAdd) if (!seen.contains(feature)) extra++;
        FeatureInfo[] out = (FeatureInfo[]) Array.newInstance(FeatureInfo.class, kept + extra);
        int index = 0;
        for (FeatureInfo info : original) {
            if (info != null && info.name != null && photosMode && PHOTOS_DISABLE_FEATURES.contains(info.name)) continue;
            out[index++] = info;
        }
        for (String feature : featuresToAdd) {
            if (seen.contains(feature)) continue;
            FeatureInfo info = new FeatureInfo();
            info.name = feature;
            out[index++] = info;
        }
        return out;
    }

    private static void setStatic(Class<?> cls, String name, Object value) {
        try {
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Throwable ignored) {}
    }

    public static String getSystemProperty(String key, String original) {
        if (key == null) return original;
        if ("ro.soc.model".equals(key)) return socModel();
        if ("ro.soc.manufacturer".equals(key)) return SOC_MANUFACTURER;
        if (key.endsWith(".model") || "ro.product.model".equals(key) || "ro.product.model_for_attestation".equals(key)) return model();
        if (key.endsWith(".device") || "ro.product.device".equals(key) || "ro.product.device_for_attestation".equals(key)) return device();
        if (key.endsWith(".name") || "ro.product.name".equals(key) || "ro.product.name_for_attestation".equals(key)) return product();
        if (key.endsWith(".brand") || "ro.product.brand".equals(key) || "ro.product.brand_for_attestation".equals(key)) return BRAND;
        if (key.endsWith(".manufacturer") || "ro.product.manufacturer".equals(key) || "ro.product.manufacturer_for_attestation".equals(key)) return MANUFACTURER;
        if (key.endsWith(".board") || "ro.product.board".equals(key)) return device();
        if (key.endsWith(".build.fingerprint") || "ro.build.fingerprint".equals(key)) return fingerprint();
        if (key.endsWith(".build.id") || "ro.build.id".equals(key)) return id();
        if (key.endsWith(".build.tags") || "ro.build.tags".equals(key)) return "release-keys";
        if (key.endsWith(".build.type") || "ro.build.type".equals(key)) return "user";
        if ("ro.build.user".equals(key)) return "android-build";
        if ("ro.build.host".equals(key)) return "bcb8c9bcce95";
        if ("ro.board.platform".equals(key)) return platform();
        if ("ro.bootloader".equals(key) || "ro.build.expect.bootloader".equals(key)) return bootloader();
        if ("ro.build.description".equals(key)) return description();
        if (key.startsWith("ro.com.google.clientidbase")) return CLIENT_ID;
        if ("ro.opa.eligible_device".equals(key)) return "true";
        if (key.endsWith(".build.version.release") || "ro.build.version.release_or_codename".equals(key)) return release();
        if (key.endsWith(".build.version.sdk")) return String.valueOf(sdkInt());
        if (key.endsWith(".build.version.security_patch")) return securityPatch();
        if (key.endsWith(".build.version.incremental")) return incremental();
        if ("ro.product.first_api_level".equals(key)) return firstApiLevel();
        if ("ro.build.characteristics".equals(key)) return "nosdcard";
        if (key.endsWith(".build.version.sdk_full")) return SDK_FULL;
        if (key.endsWith(".build.uuid")) return BUILD_UUID;
        if (key.endsWith(".build.date")) return BUILD_DATE;
        if (key.endsWith(".build.date.utc")) return timeSec();
        if (key.contains("baseband")) return BASEBAND;
        if (key.endsWith(".cpu.abilist")) return "arm64-v8a";
        if (key.endsWith(".cpu.abilist32")) return "";
        if (key.endsWith(".cpu.abilist64")) return "arm64-v8a";
        if ("ro.build.flavor".equals(key)) return device() + "-user";
        if ("ro.serialno".equals(key) || "ro.boot.serialno".equals(key)) return SERIAL;
        return original;
    }

    public static int getSystemPropertyInt(String key, int original) {
        if (key == null) return original;
        if ("ro.build.version.sdk".equals(key) || key.endsWith(".build.version.sdk")) return sdkInt();
        if ("ro.product.first_api_level".equals(key)) return Integer.parseInt(firstApiLevel());
        return original;
    }

    public static long getSystemPropertyLong(String key, long original) {
        if (key == null) return original;
        if ("ro.build.date.utc".equals(key) || key.endsWith(".build.date.utc")) return Long.parseLong(timeSec());
        return original;
    }

    public static boolean getSystemPropertyBoolean(String key, boolean original) {
        if (key == null) return original;
        if ("ro.opa.eligible_device".equals(key)) return true;
        return original;
    }

    public static boolean hasPixelSystemFeature(String feature, boolean original) {
        Boolean forced = forcedFeatureValue(feature);
        return forced != null ? forced.booleanValue() : original;
    }

    private static Boolean forcedFeatureValue(String feature) {
        if (feature == null) return null;
        if (photosMode) {
            if (PHOTOS_ENABLE_FEATURES.contains(feature)) return Boolean.TRUE;
            if (PHOTOS_DISABLE_FEATURES.contains(feature)) return Boolean.FALSE;
            return null;
        }
        return PIXEL_FEATURES.contains(feature) ? Boolean.TRUE : null;
    }

    private static String model() { return photosMode ? PHOTOS_MODEL : MODEL; }
    private static String device() { return photosMode ? PHOTOS_DEVICE : DEVICE; }
    private static String product() { return photosMode ? PHOTOS_PRODUCT : PRODUCT; }
    private static String fingerprint() { return photosMode ? PHOTOS_FINGERPRINT : FINGERPRINT; }
    private static String description() { return photosMode ? PHOTOS_DESCRIPTION : DESCRIPTION; }
    private static String id() { return photosMode ? PHOTOS_ID : ID; }
    private static String bootloader() { return photosMode ? PHOTOS_BOOTLOADER : BOOTLOADER; }
    private static String socModel() { return photosMode ? PHOTOS_SOC_MODEL : SOC_MODEL; }
    private static String platform() { return photosMode ? PHOTOS_PLATFORM : PLATFORM; }
    private static String release() { return photosMode ? PHOTOS_RELEASE : RELEASE; }
    private static int sdkInt() { return photosMode ? PHOTOS_SDK_INT : SDK_INT; }
    private static String securityPatch() { return photosMode ? PHOTOS_SECURITY_PATCH : SECURITY_PATCH; }
    private static String incremental() { return photosMode ? PHOTOS_INCREMENTAL : INCREMENTAL; }
    private static String firstApiLevel() { return photosMode ? PHOTOS_FIRST_API_LEVEL : FIRST_API_LEVEL; }
    private static long buildTime() { return photosMode ? PHOTOS_TIME : TIME; }
    private static String timeSec() { return photosMode ? PHOTOS_TIME_SEC : TIME_SEC; }
}
