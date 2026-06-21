package app.template.patches.adguard

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

// ---------------------------------------------------------------------------
// Stable class-level anchors (string-based)
// ---------------------------------------------------------------------------

/**
 * PlusManager class anchor.
 * Unique retry log string — stable semantic message.
 */
internal val PlusManagerClassFingerprint = Fingerprint(
    strings = listOf("Failed to get state from backend. Remaining retry count: ")
)

/**
 * PaidLicense class anchor.
 */
private val PaidLicenseClassFingerprint = Fingerprint(
    strings = listOf("PaidLicense(licenseKey=")
)

// ---------------------------------------------------------------------------
// Method fingerprints
// ---------------------------------------------------------------------------

/**
 * PlusManager — cached PlusState getter (s()).
 *
 * No params, returns object. Unique pattern: IGET_OBJECT → IPUT_OBJECT
 * (cache-read → fallback → write-back).
 */
internal val GetPlusStateFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.IPUT_OBJECT),
    ),
    custom = { method, _ -> method.parameterTypes.isEmpty() }
)

/**
 * PlusManager — state propagator / setPlusState (B()).
 *
 * Single PlusState param, void return. Unique: only void method in PlusManager
 * that calls invoke-interface (the PlusManagerNotificationAssistant emit).
 */
internal val SetPlusStateFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "V",
    filters = listOf(
        opcode(Opcode.INVOKE_INTERFACE),
    ),
    custom = { method, _ -> method.parameterTypes.size == 1 }
)

/**
 * PlusManager — license screen StateFlow resolver (t()).
 *
 * Takes 2 params, returns object. Unique pattern: INSTANCE_OF opcode
 * (converts first param to boolean for the forceRefresh flag) followed by
 * a virtual call returning an object. The only 2-param object-returning
 * method in PlusManager with this pattern.
 */
internal val StateFlowResolverFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    filters = listOf(
        opcode(Opcode.INSTANCE_OF),
        opcode(Opcode.INVOKE_VIRTUAL),
    ),
    custom = { method, _ -> method.parameterTypes.size == 2 }
)

/**
 * PlusManager — promo/check-license dialog StateFlow resolver (q()).
 *
 * Unique: contains "cacheStrategy" and "retryStrategy" string constants
 * used as coroutine lambda constructor arguments.
 */
internal val PromoStateFlowResolverFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    strings = listOf("cacheStrategy", "retryStrategy"),
)

/**
 * PaidLicense constructor.
 */
internal val PaidLicenseFingerprint = Fingerprint(
    classFingerprint = PaidLicenseClassFingerprint,
    name = "<init>",
)

/**
 * PlusState.Lifetime — toString().
 * Single static field (singleton). toString returns "Lifetime".
 */
internal val LifetimeDurationFingerprint = Fingerprint(
    name = "toString",
    strings = listOf("Lifetime"),
)
