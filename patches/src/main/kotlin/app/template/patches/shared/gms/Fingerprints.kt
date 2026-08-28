package app.template.patches.shared.gms

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * static void(Context, int) — throws "Google Play Services not available" when GMS is absent.
 * Bypass so MicroG handles GMS requests instead of crashing the app.
 */
internal val ServiceCheckFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("L", "I"),
    strings = listOf("Google Play Services not available"),
)

/**
 * static int(Context, int) — returns Play Services version code.
 * Callers ignore a 0 return, so returning 0 early is safe and prevents GMS-absent crashes.
 * Note: the string anchors were removed from Google Photos in 7.86+, so this only matches
 * apps that still bundle the old GooglePlayServicesUtilLight.
 */
internal val GooglePlayUtilityFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "I",
    parameters = listOf("L", "I"),
    strings = listOf(
        "This should never happen.",
        "MetadataValueReader",
        "com.google.android.gms",
    ),
)

/**
 * static int(Context, int) — GoogleApiAvailabilityLight.isGooglePlayServicesAvailable equivalent.
 * Returns a ConnectionResult status code (0 == SUCCESS); some bundled Maps SDKs gate map/place
 * initialization on it (e.g. Google Photos' "Map"/"Places" crashes with
 * "IBitmapDescriptorFactory is not initialized" when the check fails for MicroG, which is not
 * Google-signed). Return 0 early so those features work under GmsCore.
 *
 * Anchored on the GMS version resource strings rather than the method name, mirroring ReVanced.
 */
internal val IsGooglePlayServicesAvailableFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "I",
    parameters = listOf("L", "I"),
    strings = listOf(
        "com.google.android.gms.version",
        "com.google.app.id",
    ),
)
