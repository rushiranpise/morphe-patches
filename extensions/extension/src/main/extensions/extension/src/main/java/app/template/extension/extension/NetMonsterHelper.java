package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class NetMonsterHelper {
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
                                && args != null && args.length > 0 && "cz.mroczis.netmonster".equals(args[0])) {
                            PackageInfo info = (PackageInfo) method.invoke(originalPackageManager, args);
                            if (info != null) spoofSignature(info);
                            return info;
                        }
                        if (("getApplicationInfo".equals(methodName) || "getApplicationInfoAsUser".equals(methodName))
                                && args != null && args.length > 0 && "cz.mroczis.netmonster".equals(args[0])) {
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
        byte[] certBytes = hexToBytes("3082030f308201f7a00302010202040dca795a300d06092a864886f70d01010b05003037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f69642044656275673020170d3234303831323030353234305a180f32303531313232393030353234305a3037310b30090603550406130255533110300e060355040a1307416e64726f6964311630140603550403130d416e64726f696420446562756730820122300d06092a864886f70d01010105000382010f003082010a0282010100b0f132f7ccfefb7ea35ec2b776d3ae5dd1ebb8c3add7357e9b037b6476f22d0bbae248c8f6440a6ecdb9b88ba0a63777a5707d855c4575b7da2b3267a4c2f14426690f1a3a681c7b6118a31098308c2e27421942247ed563aa10785e7ac7ad991a15f421b073a46cde25e5e6acdbfa0319ea4a6d0ca2384f1316dc117b589e64e08d04666719961415052e10cde5ed24a11cbf1ad7463be3e54f2e7df505e89af999fc768a7109cbdcd041e2f400345ff31a9e1a71c87cfec7a74be1f32c72a302b7b01cb76dd8455ce205f0661aa539d06c007125d9a04b62c9afea2be0e6e57c5c4b28f882fef9a1f110fd0364708161f39f19f5fa61d9456e45463d84b16d0203010001a321301f301d0603551d0e041604142e4f0b90dd1419decfbf7555d9ef1b6ba99942d3300d06092a864886f70d01010b0500038201010046703d7b5d60371fccb06c02965b54b339970c5b58e272112fe81585f675af1905f58070c1e866cc722f528b33ad4470eb3177e92c0e782d59dc92445e17d66145148c34f16349e88db50654ec643e19972ccfb043721bd407df6e69bbca04d940ac12a84aa4b07f4f53ded00fc082d7ded667a95f47874361c2d122b9e5bf3ba64117b289b02bdd475baf268dc687a69168c435d1164fe5f5a3b507fa7af364b53a5bd3aa5c2694b40fbc6c5229dd1ea5f4ceba6d7f1ba5895d03d12a4554112b6f7160c7506c13811ccd5f5d69b2ea6f8e0fd605a7fd471b43b08a8b3ee46cd15451d043dd7a81e4dba8f5c14b688c5dd8f45da6337bf7dba4a381b2c1c19f");
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
        info.metaData.putString("com.google.android.geo.API_KEY", "REMOVED_GOOGLE_MAPS_API_KEY");
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        return data;
    }
}
