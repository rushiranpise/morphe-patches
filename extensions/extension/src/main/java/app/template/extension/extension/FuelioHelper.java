package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FuelioHelper {
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
                                && args != null && args.length > 0 && "com.kajda.fuelio".equals(args[0])) {
                            PackageInfo info = (PackageInfo) method.invoke(originalPackageManager, args);
                            if (info != null) spoofSignature(info);
                            return info;
                        }
                        if (("getApplicationInfo".equals(methodName) || "getApplicationInfoAsUser".equals(methodName))
                                && args != null && args.length > 0 && "com.kajda.fuelio".equals(args[0])) {
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
        // Original Fuelio signing cert (DER, extracted from base.apk v2 signing block)
        byte[] certBytes = hexToBytes(
            "308201ef30820158a00302010202044e3a8c4b300d06092a864886f70d0101050500" +
            "303b310e300c060355040713054f706f6c6531123010060355040a13096b616a6461" +
            "2e636f6d311530130603550403130c41647269616e204b616a64613020170d313130" +
            "3830343132313035315a180f32303631303732323132313035315a303b310e300c06" +
            "0355040713054f706f6c6531123010060355040a13096b616a64612e636f6d311530" +
            "130603550403130c41647269616e204b616a646130819f300d06092a864886f70d01" +
            "0101050003818d00308189028181008b2d243d3b538dc972f3700f9ddc47c11a25d4" +
            "909d2626171926d42437292c6954528fca125be56a1e069ea2bdfd248c0bcccff790" +
            "1cdbe44adece94ce099f89f876aeeb585f18a41ca67ebb89a3cd9ab05e230d44e364" +
            "2a610fab435a24e77b47483fb62d3eb3121b3fe0197e4e227dbb0d05fc71f1b11134" +
            "5201b660a45e730203010001300d06092a864886f70d01010505000381810026cb08" +
            "55e45146a05e4a88c27ea58503852ec08aeb9ad072bdf4583561bcd6bbc0c3ff6395" +
            "39af0e8c1fecbe742c3766bd05035ac4fd0317bf4346dcf51ca3bdf54e0fa9b4bc9a" +
            "ca2c1249634f0b6955c8b315e0a77339a1aedb8711b17149549099174b31cac4e768" +
            "a03a96a7c01c4bf39969524c73118591efd22f6f60dba7"
        );
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
        info.metaData.putString("com.google.android.maps.v2.API_KEY", ApiKeys.SHARED_MAPS);
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        return data;
    }
}
