package app.template.extension.extension;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Binder-level install source spoofer.
 *
 * Installs a {@link java.lang.reflect.Proxy} over {@code ActivityThread.sPackageManager}
 * (the singleton {@code IPackageManager} binder stub used by every {@code PackageManager}
 * call in the process). The proxy intercepts:
 *
 *  - {@code getInstallerPackageName(String)} — old API (pre-30)
 *  - {@code getInstallSourceInfo(String)}    — new API (30+), mutates all fields in place
 *
 * This is the same technique used by WhatsAppHelper but extracted as a generic, configurable
 * utility. It catches every install-source query regardless of which code path the app uses —
 * including native code that goes through the binder directly, which DEX-level const-string
 * replacement cannot reach.
 *
 * Also patches {@code ApplicationPackageManager.mPM} and the ServiceManager binder cache so
 * both the context's PackageManager and any freshly obtained instance see the proxy.
 *
 * Call {@link #init(String)} from {@code Application.onCreate()} via an
 * {@code invoke-static} instruction injected by the patch.
 */
@SuppressWarnings("unused")
public class InstallSourceHelper {

    private static volatile boolean initialized = false;
    private static volatile String targetInstaller = "com.android.vending";

    /**
     * Initialise the IPackageManager proxy with the given installer package name.
     * Safe to call multiple times — only the first call takes effect.
     *
     * @param installerPackageName the package name to return for all install-source queries.
     */
    public static void init(String installerPackageName) {
        if (initialized) return;
        initialized = true;
        if (installerPackageName != null && !installerPackageName.isEmpty()) {
            targetInstaller = installerPackageName;
        }
        try {
            // Bypass hidden API restrictions on Android P+.
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    Class<?> bypass = Class.forName("org.lsposed.hiddenapibypass.HiddenApiBypass");
                    bypass.getMethod("addHiddenApiExemptions", String[].class)
                        .invoke(null, (Object) new String[]{"L"});
                }
            } catch (Throwable ignored) {}

            Class<?> atClass = Class.forName("android.app.ActivityThread");

            // 1. Get or initialise the sPackageManager singleton.
            Field sPMField = atClass.getDeclaredField("sPackageManager");
            sPMField.setAccessible(true);
            Object originalPM = sPMField.get(null);
            if (originalPM == null) {
                Method getPM = atClass.getDeclaredMethod("getPackageManager");
                getPM.setAccessible(true);
                getPM.invoke(null);
                originalPM = sPMField.get(null);
            }
            if (originalPM == null) return;

            // 2. Build proxy over IPackageManager.
            Class<?> iPMClass = Class.forName("android.content.pm.IPackageManager");
            final Object finalOriginalPM = originalPM;
            Object proxy = Proxy.newProxyInstance(
                iPMClass.getClassLoader(),
                new Class<?>[]{iPMClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object p, Method method, Object[] args) throws Throwable {
                        String name = method.getName();
                        if ("getInstallerPackageName".equals(name)) {
                            return targetInstaller;
                        }
                        if ("getInstallSourceInfo".equals(name)) {
                            Object info = method.invoke(finalOriginalPM, args);
                            spoofInstallSourceInfo(info);
                            return info;
                        }
                        try {
                            return method.invoke(finalOriginalPM, args);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    }
                }
            );

            // 3. Replace sPackageManager with proxy.
            sPMField.set(null, proxy);

            // 4. Also patch ApplicationPackageManager.mPM so context.getPackageManager() uses proxy.
            try {
                Class<?> apmClass = Class.forName("android.app.ApplicationPackageManager");
                Field mPMField = apmClass.getDeclaredField("mPM");
                mPMField.setAccessible(true);

                Method currentAt = atClass.getDeclaredMethod("currentActivityThread");
                currentAt.setAccessible(true);
                Object at = currentAt.invoke(null);
                if (at != null) {
                    Method getApp = atClass.getDeclaredMethod("getApplication");
                    getApp.setAccessible(true);
                    android.content.Context ctx = (android.content.Context) getApp.invoke(at);
                    if (ctx != null) {
                        android.content.pm.PackageManager pm = ctx.getPackageManager();
                        if (apmClass.isInstance(pm)) {
                            mPMField.set(pm, proxy);
                        }
                    }
                }
            } catch (Throwable ignored) {}

            // 5. Patch ServiceManager binder cache so freshly fetched PackageManager instances
            //    also resolve through the proxy (same trick WhatsAppHelper uses).
            try {
                Class<?> smClass = Class.forName("android.os.ServiceManager");
                Field sCacheField = smClass.getDeclaredField("sCache");
                sCacheField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, android.os.IBinder> sCache =
                    (java.util.Map<String, android.os.IBinder>) sCacheField.get(null);

                android.os.IBinder origBinder =
                    (android.os.IBinder) smClass.getMethod("getService", String.class)
                        .invoke(null, "package");
                if (origBinder != null && sCache != null) {
                    final Object finalProxy = proxy;
                    android.os.IBinder binderProxy = (android.os.IBinder) Proxy.newProxyInstance(
                        android.os.IBinder.class.getClassLoader(),
                        new Class<?>[]{android.os.IBinder.class},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object p, Method method, Object[] args) throws Throwable {
                                if ("queryLocalInterface".equals(method.getName())) return finalProxy;
                                try {
                                    return method.invoke(origBinder, args);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    throw e.getTargetException();
                                }
                            }
                        }
                    );
                    sCache.put("package", binderProxy);
                }
            } catch (Throwable ignored) {}

        } catch (Throwable ignored) {}
    }

    /**
     * Mutates all known {@code InstallSourceInfo} package-name fields to {@link #targetInstaller}.
     * Field names vary by AOSP version so we try all known variants.
     */
    private static void spoofInstallSourceInfo(Object info) {
        if (info == null) return;
        String[] fields = {
            "mInitiatingPackageName",
            "mInstallingPackageName",
            "mOriginatingPackageName",
            "mUpdateOwnerPackageName",
            "initiatingPackageName",
            "installingPackageName",
            "originatingPackageName",
            "updateOwnerPackageName",
        };
        for (String fieldName : fields) {
            try {
                Field f = info.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(info, targetInstaller);
            } catch (Throwable ignored) {}
        }
    }
}
