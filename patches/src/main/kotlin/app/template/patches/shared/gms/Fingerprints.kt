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
