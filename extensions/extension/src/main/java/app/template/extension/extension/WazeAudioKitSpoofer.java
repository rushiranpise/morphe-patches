package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import java.lang.reflect.Field;
import java.util.Map;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

@SuppressWarnings("unused")
public class WazeAudioKitSpoofer {
    public static void init() {
        String packageName = "TARGET_PACKAGE_NAME";
        String certificateData = "TARGET_CERTIFICATE_BASE64";
        killPM(packageName, certificateData);
        killAM(packageName);
    }

    @SuppressWarnings({"ExtractMethodRecommender", "Convert2Diamond"})
    private static void killPM(String packageName, String certificateData) {
        Signature fakeSignature = new Signature(Base64.decode(certificateData, Base64.DEFAULT));
        Parcelable.Creator<PackageInfo> originalCreator = PackageInfo.CREATOR;
        Parcelable.Creator<PackageInfo> creator = new Parcelable.Creator<PackageInfo>() {
            @Override
            public PackageInfo createFromParcel(Parcel source) {
                PackageInfo packageInfo = originalCreator.createFromParcel(source);
                if (packageInfo.packageName != null && packageInfo.packageName.equals(packageName)) {
                    packageInfo.packageName = "com.google.android.apps.youtube.music";
                    if (packageInfo.applicationInfo != null) {
                        packageInfo.applicationInfo.packageName = "com.google.android.apps.youtube.music";
                    }
                    if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                        packageInfo.signatures[0] = fakeSignature;
                    }
                    if (isSDKAbove(28)) {
                        if (packageInfo.signingInfo != null) {
                            Signature[] signaturesArray = packageInfo.signingInfo.getApkContentsSigners();
                            if (signaturesArray != null && signaturesArray.length > 0) {
                                signaturesArray[0] = fakeSignature;
                            }
                        }
                    }
                }
                return packageInfo;
            }

            @Override
            public PackageInfo[] newArray(int size) {
                return originalCreator.newArray(size);
            }
        };

        try {
            findField(PackageInfo.class, "CREATOR").set(null, creator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (isSDKAbove(28)) {
            HiddenApiBypass.addHiddenApiExemptions("Landroid/os/Parcel;", "Landroid/content/pm", "Landroid/app");
        }

        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(null);
            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");

            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                    iPackageManagerClass.getClassLoader(),
                    new Class<?>[]{iPackageManagerClass},
                    new java.lang.reflect.InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                            if (args != null && args.length > 0 && args[0] instanceof String) {
                                String pkg = (String) args[0];
                                if ("com.google.android.apps.youtube.music".equals(pkg)) {
                                    String mName = method.getName();
                                    if (mName.equals("getPackageInfo") || mName.equals("getApplicationInfo") || mName.equals("getPackageUid") || mName.equals("resolveIntent")) {
                                        args[0] = packageName;
                                    }
                                }
                            }
                            if (args != null && args.length > 0 && args[0] instanceof android.content.Intent) {
                                android.content.Intent intent = (android.content.Intent) args[0];
                                if ("com.google.android.apps.youtube.music".equals(intent.getPackage())) {
                                    intent.setPackage(packageName);
                                } else if (intent.getComponent() != null && "com.google.android.apps.youtube.music".equals(intent.getComponent().getPackageName())) {
                                    intent.setComponent(new android.content.ComponentName(packageName, intent.getComponent().getClassName()));
                                }
                            }
                            try {
                                return method.invoke(sPackageManager, args);
                            } catch (java.lang.reflect.InvocationTargetException e) {
                                throw e.getTargetException();
                            }
                        }
                    }
            );
            sPackageManagerField.set(null, proxy);
            
            Object currentActivityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            if (currentActivityThread != null) {
                Object app = activityThreadClass.getMethod("getApplication").invoke(currentActivityThread);
                if (app != null) {
                    PackageManager pm = (PackageManager) app.getClass().getMethod("getPackageManager").invoke(app);
                    Field mPMField = pm.getClass().getDeclaredField("mPM");
                    mPMField.setAccessible(true);
                    mPMField.set(pm, proxy);
                }
            }
        } catch (Throwable e) {
            android.util.Log.e("WazeAudioKitSpoofer", "Error hooking IPackageManager", e);
        }

        try {
            Object cache = findField(PackageManager.class, "sPackageInfoCache").get(null);
            if (cache != null) {
                cache.getClass().getMethod("clear").invoke(cache);
            }
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> mCreators = (Map<?, ?>) findField(Parcel.class, "mCreators").get(null);
            if (mCreators != null) {
                mCreators.clear();
            }
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> sPairedCreators = (Map<?, ?>) findField(Parcel.class, "sPairedCreators").get(null);
            if (sPairedCreators != null) {
                sPairedCreators.clear();
            }
        } catch (Throwable ignored) {
        }
    }

    private static void killAM(String packageName) {
        try {
            Class<?> amClass = Class.forName("android.app.ActivityManager");
            Field singletonField = null;
            try {
                singletonField = amClass.getDeclaredField("IActivityManagerSingleton");
            } catch (NoSuchFieldException e) {
                try {
                    Class<?> amNativeClass = Class.forName("android.app.ActivityManagerNative");
                    singletonField = amNativeClass.getDeclaredField("gDefault");
                } catch (NoSuchFieldException ex) {
                }
            }
            if (singletonField != null) {
                singletonField.setAccessible(true);
                Object singleton = singletonField.get(null);
                Class<?> singletonClass = Class.forName("android.util.Singleton");
                Field mInstanceField = singletonClass.getDeclaredField("mInstance");
                mInstanceField.setAccessible(true);
                Object iActivityManager = mInstanceField.get(singleton);
                if (iActivityManager == null) {
                    java.lang.reflect.Method getMethod = singletonClass.getDeclaredMethod("get");
                    getMethod.setAccessible(true);
                    iActivityManager = getMethod.invoke(singleton);
                }
                
                final Object finalIActivityManager = iActivityManager;
                Class<?> iamClass = Class.forName("android.app.IActivityManager");
                Object iamProxy = java.lang.reflect.Proxy.newProxyInstance(
                        iamClass.getClassLoader(),
                        new Class<?>[]{iamClass},
                        new java.lang.reflect.InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                                if (args != null) {
                                    for (int i = 0; i < args.length; i++) {
                                        if (args[i] instanceof android.content.Intent) {
                                            android.content.Intent intent = (android.content.Intent) args[i];
                                            if ("com.google.android.apps.youtube.music".equals(intent.getPackage())) {
                                                intent.setPackage(packageName);
                                            } else if (intent.getComponent() != null && "com.google.android.apps.youtube.music".equals(intent.getComponent().getPackageName())) {
                                                intent.setComponent(new android.content.ComponentName(packageName, intent.getComponent().getClassName()));
                                            }
                                        }
                                    }
                                }
                                try {
                                    return method.invoke(finalIActivityManager, args);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    throw e.getTargetException();
                                }
                            }
                        }
                );
                mInstanceField.set(singleton, iamProxy);
            }
        } catch (Throwable e) {
            android.util.Log.e("WazeAudioKitSpoofer", "Error hooking IActivityManager", e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class)) {
                    break;
                }
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    private static boolean isSDKAbove(int sdk) {
        return Build.VERSION.SDK_INT >= sdk;
    }

    public static void onSubscribe(String parentId, Object callback) {
        try {
            android.util.Log.i("WazeAudioKitSpoofer", "Intercepted subscribe for " + parentId);
            java.lang.reflect.Method m = callback.getClass().getMethod("onChildrenLoaded", String.class, java.util.List.class);
            m.setAccessible(true);
            m.invoke(callback, parentId, new java.util.ArrayList());
        } catch (Exception e) {
            android.util.Log.e("WazeAudioKitSpoofer", "Failed to mock onChildrenLoaded", e);
        }
    }

    public static void onSubscribe(String parentId, android.os.Bundle options, Object callback) {
        try {
            android.util.Log.i("WazeAudioKitSpoofer", "Intercepted subscribe for " + parentId + " with options");
            java.lang.reflect.Method m = callback.getClass().getMethod("onChildrenLoaded", String.class, java.util.List.class, android.os.Bundle.class);
            m.setAccessible(true);
            m.invoke(callback, parentId, new java.util.ArrayList(), options);
        } catch (Exception e) {
            android.util.Log.e("WazeAudioKitSpoofer", "Failed to mock onChildrenLoaded with options", e);
        }
    }

    public static android.content.Context spoofContext(android.content.Context original) {
        // Context spoofing broke bindService, so we return the original context
        return original;
    }

    public static String spoofRoot(String original) {
        android.util.Log.i("WazeAudioKitSpoofer", "spoofRoot called with: " + original);
        if ("com.waze".equals(original)) {
            android.util.Log.i("WazeAudioKitSpoofer", "Spoofing root ID from com.waze to root");
            return "root";
        }
        return original;
    }
}
