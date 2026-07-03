package app.template.extension.extension;

import android.content.pm.ApplicationInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UDiscHelper {
    private static final String PKG = "com.regasoftware.udisc";
    private static final String MAPS_API_KEY = "REMOVED_GOOGLE_MAPS_API_KEY";

    public static void init() {
        try {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("L");
                }
            } catch (Throwable ignored) { }

            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);

            Object currentPackageManager = sPackageManagerField.get(null);
            if (currentPackageManager == null) {
                Method getPackageManagerMethod = activityThreadClass.getDeclaredMethod("getPackageManager");
                getPackageManagerMethod.setAccessible(true);
                getPackageManagerMethod.invoke(null);
                currentPackageManager = sPackageManagerField.get(null);
            }
            if (currentPackageManager == null) return;

            final Object originalPackageManager = currentPackageManager;
            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");

            final Object packageManagerProxy = Proxy.newProxyInstance(
                iPackageManagerClass.getClassLoader(),
                new Class<?>[] { iPackageManagerClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        if (("getApplicationInfo".equals(methodName) || "getApplicationInfoAsUser".equals(methodName))
                                && args != null && args.length > 0 && PKG.equals(args[0])) {
                            ApplicationInfo info = (ApplicationInfo) method.invoke(originalPackageManager, args);
                            if (info != null) spoofApiKey(info);
                            return info;
                        }
                        try {
                            return method.invoke(originalPackageManager, args);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    }
                }
            );

            sPackageManagerField.set(null, packageManagerProxy);

            try {
                Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
                Field sCacheField = serviceManagerClass.getDeclaredField("sCache");
                sCacheField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, android.os.IBinder> sCache =
                    (java.util.Map<String, android.os.IBinder>) sCacheField.get(null);
                final android.os.IBinder originalBinder =
                    (android.os.IBinder) serviceManagerClass.getMethod("getService", String.class).invoke(null, "package");
                if (originalBinder != null) {
                    android.os.IBinder proxiedBinder = (android.os.IBinder) Proxy.newProxyInstance(
                        android.os.IBinder.class.getClassLoader(),
                        new Class<?>[] { android.os.IBinder.class },
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("queryLocalInterface".equals(method.getName())) return packageManagerProxy;
                                try { return method.invoke(originalBinder, args); }
                                catch (java.lang.reflect.InvocationTargetException e) { throw e.getTargetException(); }
                            }
                        }
                    );
                    sCache.put("package", proxiedBinder);
                }
            } catch (Throwable ignored) { }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void spoofApiKey(ApplicationInfo info) {
        if (info.metaData == null) info.metaData = new android.os.Bundle();
        info.metaData.putString("com.google.android.maps.v2.API_KEY", MAPS_API_KEY);
    }

}
