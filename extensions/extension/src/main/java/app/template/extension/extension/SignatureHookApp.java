package app.template.extension.extension;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Source: https://github.com/L-JINBIN/ApkSignatureKillerEx
 */
public class SignatureHookApp extends Application {

    static {
        String packageName = "<package-name>";
        String signature = "<signature>";

        killPackageManager(packageName, signature);
    }

    private static void killPackageManager(String packageName, String signature) {
        try {
            Signature fakeSignature = new Signature(Base64.decode(signature, Base64.DEFAULT));

            Parcelable.Creator<PackageInfo> creator =
                    getPackageInfoCreator(packageName, fakeSignature);

            Field packageInfoField = findField(PackageInfo.class, "CREATOR");
            packageInfoField.set(null, creator);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.addHiddenApiExemptions(
                        "Landroid/os/Parcel;",
                        "Landroid/content/pm",
                        "Landroid/app"
                );
            }
            clearPackageInfoCache();
            clearParcelCreators();
        } catch (Exception e) {
            throw new RuntimeException("Failed to modify package manager", e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            while ((clazz = clazz.getSuperclass()) != null && !clazz.equals(Object.class)) {
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

    private static Parcelable.Creator<PackageInfo> getPackageInfoCreator(
            String packageName, Signature fakeSignature) {
        Parcelable.Creator<PackageInfo> originalCreator = PackageInfo.CREATOR;
        return new Parcelable.Creator<>() {
            @Override
            public PackageInfo createFromParcel(Parcel source) {
                PackageInfo packageInfo = originalCreator.createFromParcel(source);

                if (packageInfo.packageName.equals(packageName)) {
                    if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                        packageInfo.signatures[0] = fakeSignature;
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        if (packageInfo.signingInfo != null) {
                            Signature[] signaturesArray =
                                    packageInfo.signingInfo.getApkContentsSigners();

                            if (signaturesArray != null && signaturesArray.length > 0) {
                                signaturesArray[0] = fakeSignature;
                            }

                            Signature[] historyArray =
                                    packageInfo.signingInfo.getSigningCertificateHistory();

                            if (historyArray != null && historyArray.length > 0) {
                                for (int i = 0; i < historyArray.length; i++) {
                                    historyArray[i] = fakeSignature;
                                }
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
    }

    private static void clearPackageInfoCache() {
        try {
            Field packageCacheField = findField(PackageManager.class, "sPackageInfoCache");
            Object cache = packageCacheField.get(null);
            if (cache != null) {
                cache.getClass().getMethod("clear").invoke(cache);
            }
        } catch (Exception ignored) {
        }
    }

    private static void clearParcelCreators() {
        try {
            Map<?, ?> mCreators = (Map<?, ?>) findField(Parcel.class, "mCreators").get(null);
            if (mCreators != null) {
                mCreators.clear();
            }
        } catch (Exception ignored) {
        }

        try {
            Map<?, ?> sPairedCreators = (Map<?, ?>) findField(Parcel.class, "sPairedCreators").get(null);
            if (sPairedCreators != null) {
                sPairedCreators.clear();
            }
        } catch (Exception ignored) {
        }
    }
}
