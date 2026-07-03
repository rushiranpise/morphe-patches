package app.template.extension.extension;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

/**
 * Runtime GmsCore support hooks, injected into the patched app.
 *
 * Three methods are patched at build time by gmsCorePatch:
 *  - getOriginalPackageName() → returns the original Google package name
 *  - getGmsCoreVendorGroupId() → returns the user-selected GmsCore vendor
 *  - checkGmsCore() → called from Application.onCreate() to verify MicroG is running
 */
@SuppressWarnings("unused")
public class GmsCoreSupportPatch {

    private static String getOriginalPackageName() {
        return null; // Patched at build time to return the original package name.
    }

    private static String getGmsCoreVendorGroupId() {
        return "app.revanced"; // Patched at build time to return the user-selected vendor.
    }

    /**
     * Injection point — called from Application.onCreate().
     * Verifies MicroG / GmsCore is installed and shows a toast if not.
     */
    public static void checkGmsCore(Context context) {
        try {
            String originalPackageName = getOriginalPackageName();
            if (originalPackageName != null &&
                    originalPackageName.equals(context.getPackageName())) {
                // App is running under original package name (root mount) — skip check.
                return;
            }

            String gmsCorePackage = getGmsCoreVendorGroupId() + ".android.gms";

            try {
                context.getPackageManager()
                        .getPackageInfo(gmsCorePackage, PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException e) {
                // GmsCore not installed — show toast.
                Toast.makeText(
                        context,
                        "MicroG / GmsCore (" + gmsCorePackage + ") is not installed. " +
                                "Google sign-in will not work.",
                        Toast.LENGTH_LONG
                ).show();
            }
        } catch (Exception ignored) {}
    }
}
