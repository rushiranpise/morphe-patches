package app.template.extension.extension;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.app.AppOpsManager;
import android.app.UiModeManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;

import java.lang.reflect.Method;

@SuppressWarnings("unused")
public final class UniversalPatchHelper {
    public static void forceDarkTheme(Context context) {
        try {
            Class<?> appCompatDelegate = Class.forName("androidx.appcompat.app.AppCompatDelegate");
            Method setDefaultNightMode = appCompatDelegate.getMethod("setDefaultNightMode", int.class);
            setDefaultNightMode.invoke(null, 2);
        } catch (Throwable ignored) {}

        try {
            if (context != null) {
                Object service = context.getSystemService(Context.UI_MODE_SERVICE);
                if (service instanceof UiModeManager) {
                    ((UiModeManager) service).setApplicationNightMode(2);
                }
            }
        } catch (Throwable ignored) {}
    }

    public static int forceNightMode(int original) {
        return 2;
    }

    public static int forceUiModeNightYes(int original) {
        return (original & ~0x30) | 0x20;
    }

    public static int getSettingsGlobalInt(android.content.ContentResolver resolver, String name) throws Settings.SettingNotFoundException {
        if ("adb_enabled".equals(name) || "development_settings_enabled".equals(name)) return 0;
        return Settings.Global.getInt(resolver, name);
    }

    public static int getSettingsGlobalInt(android.content.ContentResolver resolver, String name, int def) {
        if ("adb_enabled".equals(name) || "development_settings_enabled".equals(name)) return 0;
        return Settings.Global.getInt(resolver, name, def);
    }

    public static String getSettingsSecureString(android.content.ContentResolver resolver, String name) {
        if ("mock_location".equals(name)) return "0";
        return Settings.Secure.getString(resolver, name);
    }

    public static String spoofAndroidId(String name, String original, String spoofed) {
        if ("android_id".equals(name)) return spoofed;
        return original;
    }

    public static int getSettingsSecureInt(android.content.ContentResolver resolver, String name) throws Settings.SettingNotFoundException {
        if ("mock_location".equals(name)) return 0;
        return Settings.Secure.getInt(resolver, name);
    }

    public static int getSettingsSecureInt(android.content.ContentResolver resolver, String name, int def) {
        if ("mock_location".equals(name)) return 0;
        return Settings.Secure.getInt(resolver, name, def);
    }

    public static float getSettingsSecureFloat(android.content.ContentResolver resolver, String name) throws Settings.SettingNotFoundException {
        if ("mock_location".equals(name)) return 0.0f;
        return Settings.Secure.getFloat(resolver, name);
    }

    public static float getSettingsSecureFloat(android.content.ContentResolver resolver, String name, float def) {
        if ("mock_location".equals(name)) return 0.0f;
        return Settings.Secure.getFloat(resolver, name, def);
    }

    public static long getSettingsSecureLong(android.content.ContentResolver resolver, String name) throws Settings.SettingNotFoundException {
        if ("mock_location".equals(name)) return 0L;
        return Settings.Secure.getLong(resolver, name);
    }

    public static long getSettingsSecureLong(android.content.ContentResolver resolver, String name, long def) {
        if ("mock_location".equals(name)) return 0L;
        return Settings.Secure.getLong(resolver, name, def);
    }

    public static boolean bindService(Context context, Intent intent, ServiceConnection connection, int flags) {
        if (intent != null) {
            String action = String.valueOf(intent.getAction()).toLowerCase();
            String pkg = String.valueOf(intent.getPackage()).toLowerCase();
            String text = action + " " + pkg + " " + String.valueOf(intent).toLowerCase();
            if (text.contains("integrity") || text.contains("playcore.integrity")) return false;
        }
        return context.bindService(intent, connection, flags);
    }

    public static void addWindowFlags(Window window, int flags) {
        if (window != null) window.addFlags(flags & ~0x2000);
    }

    public static void setWindowFlags(Window window, int flags, int mask) {
        if (window != null) window.setFlags(flags & ~0x2000, mask & ~0x2000);
    }

    public static Bundle patchLocationExtras(Bundle extras) {
        if (extras == null) return null;
        Bundle patched = new Bundle(extras);
        patched.remove("mockLocation");
        patched.remove("isFromMockProvider");
        patched.remove("is_mock");
        patched.remove("mock_location");
        patched.putBoolean("mockLocation", false);
        return patched;
    }

    public static String normalizeLocationProvider(String provider) {
        return normalizeLocationProvider(provider, "gps");
    }

    public static String normalizeLocationProvider(String provider, String replacement) {
        if (provider == null) return null;
        if ("gps".equals(provider) || "network".equals(provider) || "passive".equals(provider) || "fused".equals(provider)) {
            return provider;
        }
        if ("network".equals(replacement) || "passive".equals(replacement) || "fused".equals(replacement)) return replacement;
        return "gps";
    }

    public static int checkOp(AppOpsManager manager, int op, int uid, String packageName) {
        if (op == 58) return AppOpsManager.MODE_ERRORED;
        return AppOpsManager.MODE_ALLOWED;
    }

    public static int checkOp(AppOpsManager manager, String op, int uid, String packageName) {
        if (AppOpsManager.OPSTR_MOCK_LOCATION.equals(op)) return AppOpsManager.MODE_ERRORED;
        return manager.checkOp(op, uid, packageName);
    }

    public static int checkOpNoThrow(AppOpsManager manager, int op, int uid, String packageName) {
        if (op == 58) return AppOpsManager.MODE_ERRORED;
        return AppOpsManager.MODE_ALLOWED;
    }

    public static int checkOpNoThrow(AppOpsManager manager, String op, int uid, String packageName) {
        if (AppOpsManager.OPSTR_MOCK_LOCATION.equals(op)) return AppOpsManager.MODE_ERRORED;
        return manager.checkOpNoThrow(op, uid, packageName);
    }

    public static NetworkInfo.State connectedState(NetworkInfo.State original) {
        return NetworkInfo.State.CONNECTED;
    }

    public static NetworkInfo.DetailedState connectedDetailedState(NetworkInfo.DetailedState original) {
        return NetworkInfo.DetailedState.CONNECTED;
    }

    public static AudioAttributes.Builder setAllowedCapturePolicy(AudioAttributes.Builder builder, int capturePolicy) {
        return builder == null ? null : builder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
    }

    public static void setAllowedCapturePolicy(AudioManager manager, int capturePolicy) {
        if (manager != null) manager.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL);
    }

    public static String hideProxyProperty(String key, String original) {
        if ("http.proxyHost".equals(key) || "https.proxyHost".equals(key) || "socksProxyHost".equals(key)) return "";
        if ("http.proxyPort".equals(key) || "https.proxyPort".equals(key) || "socksProxyPort".equals(key)) return "-1";
        return original;
    }

    public static boolean hasTransport(NetworkCapabilities capabilities, int transportType) {
        if (transportType == NetworkCapabilities.TRANSPORT_VPN) return false;
        return capabilities != null && capabilities.hasTransport(transportType);
    }

    public static boolean hasCapability(NetworkCapabilities capabilities, int capability) {
        if (capability == NetworkCapabilities.NET_CAPABILITY_NOT_VPN) return true;
        return capabilities != null && capabilities.hasCapability(capability);
    }

    public static int hideVpnNetworkType(int type) {
        return type == 17 ? 1 : type;
    }

    public static String hideVpnNetworkName(String name) {
        if ("VPN".equalsIgnoreCase(name)) return "WIFI";
        return name;
    }

    public static String hideVpnInterfaceName(String name) {
        if (name == null) return null;
        if (name.matches("^(tun[0-9]+|ppp[0-9]+).*")) return "";
        return name;
    }
}
