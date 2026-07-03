package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class CitymapperHelper {
    private static final String PKG = "com.citymapper.app.release";

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
        byte[] certBytes = hexToBytes("3082034730820304a0030201020204651f7387300b06072a8648ce38040305003075310b3009060355040613024742311730150603550408130e47726561746572204c6f6e646f6e310f300d060355040713064c6f6e646f6e31183016060355040a130f436974796d6170706572204c74642e3110300e060355040b1307416e64726f69643110300e06035504031307556e6b6e6f776e301e170d3133303431313131333133395a170d3334303133313131333133395a3075310b3009060355040613024742311730150603550408130e47726561746572204c6f6e646f6e310f300d060355040713064c6f6e646f6e31183016060355040a130f436974796d6170706572204c74642e3110300e060355040b1307416e64726f69643110300e06035504031307556e6b6e6f776e308201b73082012c06072a8648ce3804013082011f02818100fd7f53811d75122952df4a9c2eece4e7f611b7523cef4400c31e3f80b6512669455d402251fb593d8d58fabfc5f5ba30f6cb9b556cd7813b801d346ff26660b76b9950a5a49f9fe8047b1022c24fbba9d7feb7c61bf83b57e7c6a8a6150f04fb83f6d3c51ec3023554135a169132f675f3ae2b61d72aeff22203199dd14801c70215009760508f15230bccb292b982a2eb840bf0581cf502818100f7e1a085d69b3ddecbbcab5c36b857b97994afbbfa3aea82f9574c0b3d0782675159578ebad4594fe67107108180b449167123e84c281613b7cf09328cc8a6e13c167a8b547c8d28e0a3ae1e2bb3a675916ea37f0bfa213562f1fb627a01243bcca4f1bea8519089a883dfe15ae59f06928b665e807b552564014c3bfecf492a03818400028180034e971556748afdfdef373c376f6336632c7f815bd012cc52e58777f4ee90caf4924085102e4cd1e4d44b3c1a9ffa57fb43b27fae04648f8c5e61dbfedb9cc08241c3e9e40143da347e858fe428a90a69c9d24566e406935731f692e4ed16018373bb62f36c4c4e1f312e3ec0f472085fa365c1d7dd05a59224b8de947b20dba321301f301d0603551d0e041604142c9f87838b29265c1832092666ae2de6961ccf0a300b06072a8648ce3804030500033000302d021466a505216de86bd4cdc0a7b521292c285049da5c02150090b606fd40c50e90e88df8a54558001bbb3846f5");
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
