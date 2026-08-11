package app.template.patches.fitia.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FITIA_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags

// Original APK certificate SHA-1 — extracted from the APK v2 signing block.
//
// Firebase's API key has an Android app restriction that checks the calling
// APK's SHA-1 fingerprint. When Morphe re-signs the APK the fingerprint
// changes, causing API_KEY_ANDROID_APP_BLOCKED on every Firebase service call.
//
// Root-cause: all five Firebase services (FIS, Auth, RemoteConfig, Places, Nb/m)
// compute the cert SHA-1 through the same shared utility: R8/c.a([B)String which
// converts a raw SHA-1 byte array to an uppercase hex String. All five call
// R8/c.b(Context,String)[B to get the raw bytes, then R8/c.a([B) to get hex.
//
// Fix: patch R8/c.a([B)String to always return the original SHA-1 regardless of
// the byte array argument. One patch covers ALL five Firebase clients at once.
private const val CERT_SHA1 = "56C03EB22B04BF4AF14CF4500C5A6000BBEE2200"

// Fitia premium patch — v25.0.6
//
// Premium system overview:
//   Layer A — Local UserModel (Room database):
//     UserModel.isPremium:Z read by every UI gate in the app.
//
//   Layer B — Server subscription response:
//     SubscriptionFitiaDataResponse.premium:Z and Entitlement.active:Boolean?
//     drive the sync that updates UserModel.isPremium after API calls.
//
//   Layer C — Pairip LVL:
//     Newer LicenseClient variant (no VMRunner / libpairipcore.so).
//     initializeLicenseCheck() is the entry point — cleared via killPairIpFull().
//
//   Layer D — Firebase cert SHA-1 spoof:
//     R8/c.a([B)String is the shared hex encoder used by all Firebase services
//     to produce the X-Android-Cert header value. Returning CERT_SHA1 here
//     makes FIS, Firebase Auth, RemoteConfig, and Places all present the
//     original signing fingerprint to Google's servers.
//
//     Smali verified (v25.0.6, classes3.dex, LR8/c;->a([B)Ljava/lang/String;):
//       .method public static a([B)Ljava/lang/String;
//       .registers 6
//         array-length v0, p0
//         add-int v1, v0, v0
//         new-instance v2, Ljava/lang/StringBuilder;
//         ...  (hex encode loop)
//         return-object p0
//     Callers:
//       Lsb/c;->c(URL,String)HttpURLConnection (Firebase Installations)
//       Lcom/google/android/gms/internal/firebase-auth-api/zzafx;<init>(Context,String)
//       LNb/m; (RemoteConfig HTTP client)
//       Lcom/google/firebase/remoteconfig/internal/ConfigFetchHttpClient;
//       Lcom/google/android/libraries/places/internal/zznd;

@Suppress("unused")
val fitiaPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Fitia premium features by permanently reporting an active subscription.",
) {
    compatibleWith(FITIA_COMPATIBILITY)

    execute {

        // Layer A: local premium flag — always true.
        UserModelIsPremiumFingerprint.method.returnEarly(true)

        // Layer B: server-side billing response — always premium.
        SubscriptionResponsePremiumFingerprint.method.returnEarly(true)

        // Layer B: entitlement active flag — boxed Boolean, must return object.
        EntitlementActiveFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object p0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object p0
                """.trimIndent(),
            )
        }

        // Layer C: Pairip LVL — clear initializeLicenseCheck().
        killPairIpFull()

        // Layer D: Firebase cert SHA-1 spoof.
        //
        // R8/c.a([B)String is the single shared byte-array-to-uppercase-hex
        // encoder called by every Firebase service that sets X-Android-Cert.
        // We clear the method body and return CERT_SHA1 unconditionally.
        // This patches all five X-Android-Cert call sites in one go:
        //   FIS (sb/c), FirebaseAuth (zzafx), RemoteConfig (Nb/m),
        //   ConfigFetchHttpClient, Places (zznd).
        val certHexEncoderFp = Fingerprint(
            definingClass = "LR8/c;",
            name = "a",
            returnType = "Ljava/lang/String;",
            parameters = listOf("[B"),
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
        ).methodOrNull

        checkNotNull(certHexEncoderFp) {
            "Fitia: R8/c.a([B)String cert hex encoder not found."
        }

        certHexEncoderFp.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const-string p0, "$CERT_SHA1"
                    return-object p0
                """.trimIndent(),
            )
        }
    }
}
