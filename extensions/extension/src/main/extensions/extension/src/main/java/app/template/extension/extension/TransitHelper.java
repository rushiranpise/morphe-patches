package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransitHelper {
    private static final String PKG = "com.thetransitapp.droid";

    public static void init() {
        try {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("L");
                }
            } catch (Throwable e) { }

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
                        if (("getPackageInfo".equals(methodName) || "getPackageInfoAsUser".equals(methodName))
                                && args != null && args.length > 0 && PKG.equals(args[0])) {
                            PackageInfo info = (PackageInfo) method.invoke(originalPackageManager, args);
                            if (info != null) spoofSignature(info);
                            return info;
                        }
                        if (("getApplicationInfo".equals(methodName) || "getApplicationInfoAsUser".equals(methodName))
                                && args != null && args.length > 0 && PKG.equals(args[0])) {
                            android.content.pm.ApplicationInfo info =
                                (android.content.pm.ApplicationInfo) method.invoke(originalPackageManager, args);
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
            } catch (Throwable e) { }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void spoofSignature(PackageInfo info) {
        byte[] certBytes = hexToBytes("3082038130820269a00302010202047a1bd445300d06092a864886f70d01010b05003071310b3009060355040613024341310b30090603550408130251633111300f060355040713084d6f6e747265616c31183016060355040a130f546865205472616e736974204170703110300e060355040b1307556e6b6e6f776e311630140603550403130d53616d75656c2044696f6e6e65301e170d3133303530333232323130345a170d3430303931383232323130345a3071310b3009060355040613024341310b30090603550408130251633111300f060355040713084d6f6e747265616c31183016060355040a130f546865205472616e736974204170703110300e060355040b1307556e6b6e6f776e311630140603550403130d53616d75656c2044696f6e6e6530820122300d06092a864886f70d01010105000382010f003082010a0282010100a3154a31c4e7bcd2eb73679bcea1ccdaadbe6149e2fe879cd2cf890b217ff7ae2600ba789e6f0d8d2f26f007d0ef3071e61039e7ecbac13a55efe4cecf519518cf7b75e0a668a34bee2df523e6fe7f4b0ef8a6609572c90e8d6d857f38bef2257d8ad65a4994a2d0fe0bdb0009fb0c9b6a8046d59e4e479ee30cc7f432c0fd93b076863651e08832828d79c35d747580ce3fe6acdd60f895daff0ce5702008a675a88eb80d27188d656159720ed87f17c69529cfc36caa0314368c4b1c0cfa1b846e00c446278d504dbe160f11cbd8a45cd1c214ea36d208aee3a9e5d8fb7204c531610ade278686b66e4d80b13c3c61bd837f1acd78d5716e0dbdbcffa6d4e70203010001a321301f301d0603551d0e04160414ca6f80b1786e5ba660abf3dc98bc6c42cfc338b3300d06092a864886f70d01010b0500038201010073d17e1fb8b9424d8bd09316a3ef71dbb73d973bbe021000fd7e05226ab16d0bf906f2654c45474997390dded446804d0017444a6a942e8106233031e22f0afb9345987c0194c8834a0ac35c6b4b3485b22a6935ca8c7c46d9ff41c8996b4c1cf8d74669e2dfc7e1b29d8f3248a3ae7a5398135fdf165b97f32abf33997ab59d36ee632fc0fab3bdc8cd78c057a6c4dea84baf547db5e49f0f03c69dd430d136220894d623afa092b34965ec4a369ecdf3d669a600d217e5bc62b09dc92eddf8e94d47785c978220771df853d76b2db5a245a668a240ff12297dc78efe50b58c2c7d119b58e12883a42e38adf28301bce298672a6262fbfac36146f919bb4d0e");
        Signature sig = new Signature(certBytes);
        info.signatures = new Signature[] { sig };
        try {
            Class<?> sdClass = Class.forName("android.content.pm.SigningDetails");
            Object signingDetails = null;
            try {
                Class<?> builderClass = Class.forName("android.content.pm.SigningDetails$Builder");
                Object builder = builderClass.getConstructor().newInstance();
                builderClass.getDeclaredMethod("setSignatures", Signature[].class)
                    .invoke(builder, (Object) new Signature[]{sig});
                builderClass.getDeclaredMethod("setSignatureSchemeVersion", int.class)
                    .invoke(builder, 1);
                signingDetails = builderClass.getDeclaredMethod("build").invoke(builder);
            } catch (Throwable t) {
                for (java.lang.reflect.Constructor<?> c : sdClass.getDeclaredConstructors()) {
                    c.setAccessible(true);
                    Class<?>[] p = c.getParameterTypes();
                    try {
                        if (p.length == 2 && p[0] == Signature[].class && p[1] == int.class) {
                            signingDetails = c.newInstance(new Signature[]{sig}, 1); break;
                        }
                    } catch (Throwable ignored) {}
                }
            }
            if (signingDetails != null) {
                SigningInfo si;
                try {
                    java.lang.reflect.Constructor<SigningInfo> c =
                        SigningInfo.class.getDeclaredConstructor(sdClass);
                    c.setAccessible(true);
                    si = c.newInstance(signingDetails);
                } catch (Throwable t) {
                    si = new SigningInfo();
                    Field f = SigningInfo.class.getDeclaredField("mSigningDetails");
                    f.setAccessible(true);
                    f.set(si, signingDetails);
                }
                info.signingInfo = si;
            }
        } catch (Throwable t) { t.printStackTrace(); }
    }

    private static void spoofApiKey(android.content.pm.ApplicationInfo info) {
        if (info.metaData == null) info.metaData = new android.os.Bundle();
        info.metaData.putString("com.google.android.maps.v2.API_KEY", "REMOVED_GOOGLE_MAPS_API_KEY");
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        return data;
    }
}
