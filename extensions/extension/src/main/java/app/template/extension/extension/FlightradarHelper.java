package app.template.extension.extension;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class FlightradarHelper {
    public static void init() {
        android.util.Log.i("FlightradarHelper", "FlightradarHelper.init() started");
        try {
            // Bypass Android Hidden API restrictions (exempt all L-prefixed APIs)
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    org.lsposed.hiddenapibypass.HiddenApiBypass.addHiddenApiExemptions("L");
                }
            } catch (Throwable e) {
                // Ignore fallback
            }

            // Spoof signature returned by PackageManager to bypass Google Maps signature restriction
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            final Object originalPackageManager = sPackageManagerField.get(null);
            
            if (originalPackageManager == null) {
                Method getPackageManagerMethod = activityThreadClass.getDeclaredMethod("getPackageManager");
                getPackageManagerMethod.setAccessible(true);
                getPackageManagerMethod.invoke(null);
            }
            
            final Object currentPackageManager = sPackageManagerField.get(null);
            if (currentPackageManager == null) return;
            
            Class<?> iPackageManagerClass = Class.forName("android.content.pm.IPackageManager");
            
            final Object packageManagerProxy = Proxy.newProxyInstance(
                iPackageManagerClass.getClassLoader(),
                new Class<?>[] { iPackageManagerClass },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        if ("getPackageInfo".equals(methodName) || "getPackageInfoAsUser".equals(methodName)) {
                            String pkgName = (String) args[0];
                            if ("com.flightradar24free".equals(pkgName)) {
                                PackageInfo info = (PackageInfo) method.invoke(originalPackageManager, args);
                                if (info != null) {
                                    spoofPackageInfo(info);
                                }
                                return info;
                            }
                        }
                        if ("getApplicationInfo".equals(methodName) || "getApplicationInfoAsUser".equals(methodName)) {
                            String pkgName = (String) args[0];
                            if ("com.flightradar24free".equals(pkgName)) {
                                android.content.pm.ApplicationInfo info = (android.content.pm.ApplicationInfo) method.invoke(originalPackageManager, args);
                                if (info != null) {
                                    spoofApplicationInfo(info);
                                }
                                return info;
                            }
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

            // Hook ServiceManager.sCache to dynamically intercept new PackageManager instances
            try {
                Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
                Field sCacheField = serviceManagerClass.getDeclaredField("sCache");
                sCacheField.setAccessible(true);
                @SuppressWarnings("unchecked")
                java.util.Map<String, android.os.IBinder> sCache = (java.util.Map<String, android.os.IBinder>) sCacheField.get(null);
                
                final android.os.IBinder originalPackageBinder = (android.os.IBinder) serviceManagerClass.getMethod("getService", String.class).invoke(null, "package");
                if (originalPackageBinder != null) {
                    android.os.IBinder proxiedPackageBinder = (android.os.IBinder) Proxy.newProxyInstance(
                        android.os.IBinder.class.getClassLoader(),
                        new Class<?>[] { android.os.IBinder.class },
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                if ("queryLocalInterface".equals(method.getName())) {
                                    return packageManagerProxy;
                                }
                                try {
                                    return method.invoke(originalPackageBinder, args);
                                } catch (java.lang.reflect.InvocationTargetException e) {
                                    throw e.getTargetException();
                                }
                            }
                        }
                    );
                    sCache.put("package", proxiedPackageBinder);
                }
            } catch (Throwable e) {
                // Ignore ServiceManager fallback failure
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    // tempFeatures is set via sput-object in injected smali (constructor hook workaround)
    public static Object tempFeatures;

    public static void mockUserFeaturesFromTemp() {
        mockUserFeatures(tempFeatures);
    }

    public static void mockUserSessionData(Object sessionData) {
        if (sessionData == null) return;
        try {
            // Mock accountType and subscriptionKey
            try {
                Field accountTypeField = getDeclaredFieldRecursive(sessionData.getClass(), "accountType");
                if (accountTypeField != null) {
                    accountTypeField.setAccessible(true);
                    accountTypeField.set(sessionData, "Business");
                    android.util.Log.i("FlightradarHelper", "Set accountType to Business");
                }
                
                Field subscriptionKeyField = getDeclaredFieldRecursive(sessionData.getClass(), "subscriptionKey");
                if (subscriptionKeyField != null) {
                    subscriptionKeyField.setAccessible(true);
                    subscriptionKeyField.set(sessionData, "Business");
                    android.util.Log.i("FlightradarHelper", "Set subscriptionKey to Business");
                }
            } catch (Exception e) {
                android.util.Log.e("FlightradarHelper", "Error mocking UserSessionData strings", e);
            }

            Field featuresField = getDeclaredFieldRecursive(sessionData.getClass(), "features");
            if (featuresField != null) {
                featuresField.setAccessible(true);
                Object features = featuresField.get(sessionData);
                if (features == null) {
                    // Instantiate it since it's null
                    Class<?> featuresClass = featuresField.getType();
                    features = featuresClass.newInstance();
                    featuresField.set(sessionData, features);
                    android.util.Log.i("FlightradarHelper", "Created new UserFeatures instance for sessionData");
                }
                mockUserFeatures(features);
            }
        } catch (Exception e) {
            android.util.Log.e("FlightradarHelper", "Error mocking UserSessionData features", e);
        }
    }

    public static void mockUserFeatures(Object features) {
        if (features == null) {
            android.util.Log.i("FlightradarHelper", "mockUserFeatures received null object");
            return;
        }
        try {
            Class<?> clazz = features.getClass();
            android.util.Log.i("FlightradarHelper", "mockUserFeatures running on class: " + clazz.getName());
            
            // Print all declared fields in the class to debug obfuscation
            try {
                for (Field f : clazz.getDeclaredFields()) {
                    android.util.Log.i("FlightradarHelper", "  Found field: " + f.getName() + " of type " + f.getType().getName());
                }
                Class<?> superClazz = clazz.getSuperclass();
                if (superClazz != null && superClazz != Object.class) {
                    android.util.Log.i("FlightradarHelper", "  Superclass: " + superClazz.getName());
                    for (Field f : superClazz.getDeclaredFields()) {
                        android.util.Log.i("FlightradarHelper", "    Found super field: " + f.getName() + " of type " + f.getType().getName());
                    }
                }
            } catch (Throwable e) {
                android.util.Log.e("FlightradarHelper", "Error listing fields", e);
            }

            // Set integer fields - mapLabelsRows controls max label rows, userAlertsMax for alerts
            java.util.Map<String, Integer> intFields = new java.util.HashMap<>();
            intFields.put("mapLabelsRows", 4);
            intFields.put("mapFiltersMax", 60);
            intFields.put("userAlertsMax", 60);
            intFields.put("userBookmarksMax", 60);
            intFields.put("historyFlightKml", 60);
            intFields.put("historyPlaybackDays", 1095);
            intFields.put("airportFlightHistory", 1095);
            intFields.put("airportPanelArrivalsDepartures", 72);
            intFields.put("airportPanelOnGround", 90);

            for (java.util.Map.Entry<String, Integer> entry : intFields.entrySet()) {
                String fieldName = entry.getKey();
                int limitValue = entry.getValue();
                try {
                    Field field = getDeclaredFieldRecursive(clazz, fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        field.setInt(features, limitValue);
                        android.util.Log.i("FlightradarHelper", "Set int field " + fieldName + " to " + limitValue);
                    } else {
                        android.util.Log.w("FlightradarHelper", "Int field " + fieldName + " not found");
                    }
                } catch (Throwable e) {
                    android.util.Log.e("FlightradarHelper", "Failed to set int field: " + fieldName, e);
                }
            }

            // Set String fields - ALL must be "enabled" except mapInfoAircraft="full" and adverts
            String[] enabledFields = {
                "mapFiltersCategories", "mapLayerWeatherVolcano", "mapLayerNavdata",
                "mapLayerWeather", "mapLayerWeatherSatellite", "mapLayerWeatherRadar",
                "mapLayerWeatherAirmet", "mapLayerWeatherHighLevel", "mapLayerWeatherWind",
                "mapLayerWeatherLightning", "mapLayerAtc", "mapLayerTracksOceanic",
                "mapLayerWeatherInCloudTurbulence", "mapLayerWeatherClearAirTurbulence",
                "mapLayerWeatherIcing", "mapLayerWeatherNorthAmericanRadar",
                "mapLayerWeatherAustralianRadar", "airportPanelRunwayDetails",
                "airportPanelMovementsPerDay", "airportPanelRunwayUsage", "airportPanelLatestEvents",
                "mapView3d"
            };
            for (String fieldName : enabledFields) {
                try {
                    Field field = getDeclaredFieldRecursive(clazz, fieldName);
                    if (field != null) {
                        field.setAccessible(true);
                        field.set(features, "enabled");
                        android.util.Log.i("FlightradarHelper", "Set string field " + fieldName + " to 'enabled'");
                    } else {
                        android.util.Log.w("FlightradarHelper", "String field " + fieldName + " not found");
                    }
                } catch (Throwable e) {
                    android.util.Log.e("FlightradarHelper", "Failed to set string field: " + fieldName, e);
                }
            }

            // mapInfoAircraft="full" gates squawk/FIR display ("enabled" does NOT work!)
            try {
                Field field = getDeclaredFieldRecursive(clazz, "mapInfoAircraft");
                if (field != null) {
                    field.setAccessible(true);
                    field.set(features, "full");
                    android.util.Log.i("FlightradarHelper", "Set mapInfoAircraft to 'full'");
                } else {
                    android.util.Log.w("FlightradarHelper", "mapInfoAircraft field not found");
                }
            } catch (Throwable e) {
                android.util.Log.e("FlightradarHelper", "Failed to set mapInfoAircraft", e);
            }

            // Specifically set adverts to disabled
            try {
                Field field = getDeclaredFieldRecursive(clazz, "adverts");
                if (field != null) {
                    field.setAccessible(true);
                    field.set(features, "disabled");
                    android.util.Log.i("FlightradarHelper", "Set adverts to 'disabled'");
                } else {
                    android.util.Log.w("FlightradarHelper", "adverts field not found");
                }
            } catch (Throwable e) {
                android.util.Log.e("FlightradarHelper", "Failed to set adverts", e);
            }
        } catch (Throwable t) {
            android.util.Log.e("FlightradarHelper", "Error in mockUserFeatures", t);
        }
    }

    private static Field getDeclaredFieldRecursive(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static void spoofPackageInfo(PackageInfo info) {
        byte[] certBytes = hexStringToByteArray("308202013082016aa00302010202044c8f605b300d06092a864886f70d01010505003045310b3009060355040613023436311230100603550407130953746f636b686f6c6d31223020060355040a0c195376656e736b6120526573656ec3a4747665726b6574204142301e170d3130303931343131343533315a170d3335303930383131343533315a3045310b3009060355040613023436311230100603550407130953746f636b686f6c6d31223020060355040a0c195376656e736b6120526573656ec3a4747665726b657420414230819f300d06092a864886f70d010101050003818d0030818902818100807e5535e0e8b9ea9de684bc056c03ab244526ef4487252cf235bba572f56f0bbf1c22de30110ee0936f041d01ec07d672475a71a2536e3bfa2702ac59527b6b18fa75c39e0f2139bc5358a782ccc6b100ce177145f4f5b4c16211a80371f511cd42219e4425574dca9fcdd68d12767e35d72bf10dbebcab1c05dd05bd898c4f0203010001300d06092a864886f70d010105050003818100056626bedce30fec1f0fc5b27dd3047954dc8a5ccbfe20e7521e9b44e85e4ee3e5dcadc5702651308eaa89a92fdbb8baa533565fc1e9470069617b160f5cb10dcbc06d940d788413e37ef89728fb02355b1e7bd6ffc29152ab16308f3335471acc2535861864c99cd6bbcee367ed684744b958d7a3055976734ee06f266a0e10");
        Signature signature = new Signature(certBytes);
        
        info.signatures = new Signature[] { signature };
        
        // Populate signingInfo for Android 9+
        SigningInfo signingInfo = null;
        try {
            Class<?> signingDetailsClass = Class.forName("android.content.pm.SigningDetails");
            Object signingDetails = null;

            // Try using the Builder first (Android 12/13/14/15+)
            try {
                Class<?> builderClass = Class.forName("android.content.pm.SigningDetails$Builder");
                Object builder = builderClass.getConstructor().newInstance();
                
                Method setSignatures = builderClass.getDeclaredMethod("setSignatures", Signature[].class);
                setSignatures.setAccessible(true);
                setSignatures.invoke(builder, (Object) new Signature[] { signature });
                
                Method setSignatureSchemeVersion = builderClass.getDeclaredMethod("setSignatureSchemeVersion", int.class);
                setSignatureSchemeVersion.setAccessible(true);
                setSignatureSchemeVersion.invoke(builder, 1); // JAR version
                
                Method build = builderClass.getDeclaredMethod("build");
                build.setAccessible(true);
                signingDetails = build.invoke(builder);
            } catch (Throwable t) {
                // Fallback to constructors (Android 9/10/11)
                for (java.lang.reflect.Constructor<?> ctor : signingDetailsClass.getDeclaredConstructors()) {
                    ctor.setAccessible(true);
                    Class<?>[] params = ctor.getParameterTypes();
                    try {
                        if (params.length == 2 && params[0] == Signature[].class && params[1] == int.class) {
                            signingDetails = ctor.newInstance(new Signature[] { signature }, 1);
                            break;
                        } else if (params.length == 4 && params[0] == Signature[].class && params[1] == int.class && 
                                   (params[2] == java.util.Set.class || params[2].getName().contains("ArraySet")) && 
                                   params[3] == Signature[].class) {
                            signingDetails = ctor.newInstance(new Signature[] { signature }, 1, null, null);
                            break;
                        }
                    } catch (Throwable e) {
                        // ignore
                    }
                }
            }

            if (signingDetails != null) {
                try {
                    java.lang.reflect.Constructor<SigningInfo> ctor = SigningInfo.class.getDeclaredConstructor(signingDetailsClass);
                    ctor.setAccessible(true);
                    signingInfo = ctor.newInstance(signingDetails);
                } catch (Throwable t) {
                    signingInfo = new SigningInfo();
                    java.lang.reflect.Field field = SigningInfo.class.getDeclaredField("mSigningDetails");
                    field.setAccessible(true);
                    field.set(signingInfo, signingDetails);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        info.signingInfo = signingInfo;
    }
    
    private static void spoofApplicationInfo(android.content.pm.ApplicationInfo info) {
        if (info.metaData == null) {
            info.metaData = new android.os.Bundle();
        }
        info.metaData.putString("com.google.android.maps.v2.API_KEY", "REMOVED_GOOGLE_MAPS_API_KEY");
    }

    public static void cleanupEmsData(Object ems) {
        if (ems == null) return;
        try {
            Class<?> clazz = ems.getClass();
            Field hasAirspace = getDeclaredFieldRecursive(clazz, "hasAirspace");
            Field airspace = getDeclaredFieldRecursive(clazz, "airspace");
            if (hasAirspace != null && airspace != null) {
                hasAirspace.setAccessible(true);
                hasAirspace.setBoolean(ems, airspace.get(ems) != null);
            }

            Field hasIas = getDeclaredFieldRecursive(clazz, "hasIas");
            Field ias = getDeclaredFieldRecursive(clazz, "ias");
            if (hasIas != null && ias != null) {
                hasIas.setAccessible(true);
                hasIas.setBoolean(ems, ias.get(ems) != null);
            }

            Field hasTas = getDeclaredFieldRecursive(clazz, "hasTas");
            Field tas = getDeclaredFieldRecursive(clazz, "tas");
            if (hasTas != null && tas != null) {
                hasTas.setAccessible(true);
                hasTas.setBoolean(ems, tas.get(ems) != null);
            }

            Field hasMach = getDeclaredFieldRecursive(clazz, "hasMach");
            Field mach = getDeclaredFieldRecursive(clazz, "mach");
            if (hasMach != null && mach != null) {
                hasMach.setAccessible(true);
                hasMach.setBoolean(ems, mach.get(ems) != null);
            }

            Field hasOat = getDeclaredFieldRecursive(clazz, "hasOat");
            Field oat = getDeclaredFieldRecursive(clazz, "oat");
            if (hasOat != null && oat != null) {
                hasOat.setAccessible(true);
                hasOat.setBoolean(ems, oat.get(ems) != null);
            }

            Field hasWind = getDeclaredFieldRecursive(clazz, "hasWind");
            Field windSpeed = getDeclaredFieldRecursive(clazz, "windSpeed");
            Field windDirection = getDeclaredFieldRecursive(clazz, "windDirection");
            if (hasWind != null && windSpeed != null && windDirection != null) {
                hasWind.setAccessible(true);
                hasWind.setBoolean(ems, windSpeed.get(ems) != null && windDirection.get(ems) != null);
            }

            Field hasAgps = getDeclaredFieldRecursive(clazz, "hasAgps");
            Field agps = getDeclaredFieldRecursive(clazz, "agps");
            if (hasAgps != null && agps != null) {
                hasAgps.setAccessible(true);
                hasAgps.setBoolean(ems, agps.get(ems) != null);
            }
        } catch (Throwable t) {
            android.util.Log.e("FlightradarHelper", "Error in cleanupEmsData", t);
        }
    }

    public static Boolean getAvailability(Boolean hasFlag, Object value) {
        return value != null;
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
