package app.template.patches.proxyman

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.literal
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * IsFeatureAllowed(featureEnum)Z — single feature access gate.
 *
 * Reads LicenseEntitlement.isPro (Lt9/b;->a:Z) first; if false also checks
 * a secondary boolean supplier. Then packed-switches on the feature enum ordinal.
 * Returning true grants access to every feature unconditionally.
 *
 * Stable anchors (verified v1.21.0 r9/f.a):
 * - IGET_BOOLEAN on Lt9/b;->a:Z — unique: only this method reads isPro in a Z-returning (L)→Z gate
 * - methodCall to Ljava/lang/Enum;->ordinal()I — confirms enum switch
 * - returnType Z + PUBLIC FINAL + one L param
 */
val IsFeatureAllowedFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lt9/b;",
            name = "a",
        ),
        methodCall(
            definingClass = "Ljava/lang/Enum;",
            name = "ordinal",
        ),
    ),
)

/**
 * GetFeatureLimit(featureEnum)I — feature usage-count gate.
 *
 * Returns 0x7fffffff (MAX_VALUE) for pro users; 1–5 for free users per feature.
 * Returns MAX_VALUE unconditionally to grant unlimited usage for all features.
 *
 * Stable anchor: literal 0x7fffffff — unique to this method.
 * (Verified v1.21.0 r9/f.b)
 */
val GetFeatureLimitFingerprint = Fingerprint(
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L"),
    filters = listOf(
        literal(0x7fffffff),
    ),
)

/**
 * AutoPaywallCheck(prefs, timestamp)V — paywall trigger on every resume.
 *
 * Tracks "auto_paywall_first_foreground_at" in SharedPreferences; shows the
 * subscription bottom sheet once count/time thresholds are met unless isPro.
 * No-op suppresses the paywall unconditionally.
 *
 * Stable anchor: "auto_paywall_first_foreground_at" — non-obfuscated SharedPrefs key.
 * (Verified v1.21.0 s9/b.c)
 */
val AutoPaywallFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("L", "J"),
    filters = listOf(
        string("auto_paywall_first_foreground_at"),
    ),
)

/**
 * LicenseEntitlement class locator (classFingerprint).
 *
 * Kotlin-generated toString() contains non-obfuscated field names that
 * survive R8 renaming because they originate from Kotlin metadata.
 * (Verified v1.21.0 t9/b)
 */
private val LicenseEntitlementClassFingerprint = Fingerprint(
    strings = listOf("LicenseEntitlement(isPremium="),
)

/**
 * LicenseEntitlement constructor — (isPro, planType, expiry, isLifetime, state, ts).
 *
 * Data class fields:
 *   a:Z     = isPro        — master boolean read by every feature gate
 *   b:enum  = planType     — MONTHLY/QUARTERLY/YEARLY/LIFETIME
 *   c:Long? = expiryDate   — null for lifetime
 *   d:Z     = isLifetime   — true for one-time lifetime purchase
 *   e:enum  = state        — NONE/ACTIVE_SUBSCRIPTION/ACTIVE_LIFETIME/…
 *   f:J     = timestamp
 *
 * Stable anchors:
 * - classFingerprint via "LicenseEntitlement(isPremium=" toString string
 * - parameter shape (Z, L, Long?, Z, L, J) unique to this constructor
 *
 * planType/state enum class names are read from parameterTypes at patch time
 * so they auto-adapt to R8 renames without any manual update.
 * (Verified v1.21.0 t9/b — v1.19.0 s9/b)
 */
val UserSubscriptionConstructorFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Z", "L", "Ljava/lang/Long;", "Z", "L", "J"),
    classFingerprint = LicenseEntitlementClassFingerprint,
)

// ─── PairIP — non-obfuscated SDK classes, stable across all versions ───────

/**
 * LicenseClient.checkLicense(Context)V — PairIP v2 entry point.
 *
 * Called from Application.attachBaseContext(). Connects to PairIP's external
 * licensing service; on failure launches LicenseActivity which blocks the UI.
 */
val PairIPCheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        string("Skipping license check in isolated process."),
    ),
)

/**
 * LicenseActivity.onStart()V — PairIP nuclear fallback.
 *
 * Reads "activitytype" extra; PAYWALL=0 or ERROR=1 both block the app.
 * Returning after super.onStart() prevents both paths from executing.
 */
val PairIPLicenseActivityFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onStart",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    filters = listOf(
        string("activitytype"),
    ),
)
