package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MoovitHelper {
    private static final String PKG = "com.tranzmate";

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

    private static void spoofApiKey(android.content.pm.ApplicationInfo info) {
        if (info.metaData == null) info.metaData = new android.os.Bundle();
        info.metaData.putString("com.google.android.geo.API_KEY", "REMOVED_GOOGLE_MAPS_API_KEY");
    }

    private static void spoofSignature(PackageInfo info) {
        byte[] certBytes = hexToBytes("308202e3308201cba00302010202044466415d300d06092a864886f70d01010b05003021310b300906035504061302494c31123010060355040a13095472616e7a6d6174653020170d3132303332363137343031315a180f32303632303331343137343031315a3021310b300906035504061302494c31123010060355040a13095472616e7a6d61746530820122300d06092a864886f70d01010105000382010f003082010a028201010087bedc6b170764c5bca1ea3e9d1ebb00a2398731e03f83e6c90f75379f9b178e4a27616211955991b7a18279f4b6f6c06e81fbfc440bcefafb63039dcff929bcc461ccaa7dfc04d6489785dae1abca340b404b83a0763950747b48a3239ba5b12968e4713b15272043c127d3df7b2eb692fc39ce7aac1411a82981815e2d2587952b05bcaf8b3b4d7f63e9dd7e4ca39c40b63a65e79a2c9b22b6699d6d1ee8c4bc890cf5c6bc4edc1bc18df450af3ea93be11d9cab8e7c240a29a987a6f1f9bf95ae59bd3d2d4387750788b0611678f3e8374bb8547c1ca3a68918a856d17f934e05c60c233447d2da5fe9dc50e2b0d0c481ea2e066d8531d26c77c5d65873ad0203010001a321301f301d0603551d0e04160414200a489821eac3b85b8905bf305210890a0b5229300d06092a864886f70d01010b050003820101002195db14b98be526c79eb10f2628da6c5f25a9123cdbe3fc1279d485758e6acc0a64bc36370801b6472bc212618481a29462e8890110ffac423195b9978500e0b7cbc3264d83288686659847c8fd701860ea4d23ee77a74b4cf9e18d2a2b1f0b540106c519c0c1a45622f756b8b2925d39fc93ea090858410cf2ceeb14f70571c5c16a3d0ab4b904bbf4efe8abc2322000513fe25863ec1e0f026c24d2986e67053726093b36c966a446768fa988d17a6d87dfe9820aa7b6267ba81f0ef446d93f7079ecbdb0ff0fdf5bca90cf0e3161a5aff941949e666f9b385ae723614f03b7c361c4f56a96ea453e24df932e67249714b1a762f511cde08f7135adcda580");
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

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        return data;
    }
}
