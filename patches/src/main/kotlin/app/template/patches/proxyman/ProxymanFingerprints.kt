package app.template.patches.proxyman

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * p9/f.a(p9/a)Z — Feature access gate
 *
 * Checks q9/c1.c.a (r9/b.isPro boolean) then does a packed-switch on the
 * p9/a feature enum ordinal (8 values: UNLIMITED_SSL_PROXYING_ENTRIES,
 * SAFE_LOCK, CUSTOM_FILTER, PIN_DOMAIN, UNLIMITED_BLOCK_LIST_RULES,
 * BODY_EDITOR_ADVANCED_SETTINGS, UNLIMITED_COMPOSE_ENTRIES, PROXYMAN_WIDGET).
 * Returns true if the user is allowed to use the requested feature.
 * Fingerprinted by the iget-boolean on Lr9/b;->a:Z inside a method
 * taking a single enum parameter and returning Z.
 */
val IsFeatureAllowedFingerprint = Fingerprint(
    definingClass = "Lp9/f;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Lp9/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * p9/f.b(p9/a)I — Feature usage count gate
 *
 * Returns Integer.MAX_VALUE (0x7fffffff) for pro users or a low free-tier
 * cap (1–5) for non-pro users, depending on the feature.
 * Returning MAX_VALUE unconditionally grants unlimited usage for all features.
 * Fingerprinted by the 0x7fffffff literal — unique to this method.
 */
val GetFeatureLimitFingerprint = Fingerprint(
    definingClass = "Lp9/f;",
    name = "b",
    returnType = "I",
    parameters = listOf("Lp9/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        literal(0x7fffffff)
    )
)

/**
 * q9/b.c(q9/c1, J)V — Auto-paywall trigger
 *
 * Called from MainActivity.onResume() on every foreground. Tracks
 * "auto_paywall_first_foreground_at", "auto_paywall_foreground_count", and
 * "auto_paywall_last_presented_at" in SharedPreferences to decide whether
 * to show the subscription paywall sheet. Checks r9/b.isPro first — if true,
 * skips the paywall. Making this a no-op suppresses the paywall entirely.
 * Fingerprinted by the unique "auto_paywall_first_foreground_at" key.
 */
val AutoPaywallFingerprint = Fingerprint(
    definingClass = "Lq9/b;",
    name = "c",
    returnType = "V",
    parameters = listOf("Lq9/c1;", "J"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("auto_paywall_first_foreground_at")
    )
)

/**
 * com.pairip.licensecheck.LicenseClient.checkLicense(Context)V
 *
 * Static entry point for PairIP license validation. Called from
 * Application.attachBaseContext() before the app fully starts.
 * Connects to PairIP's licensing service; on failure/non-purchase
 * launches LicenseActivity (PAYWALL or ERROR) which blocks the app UI.
 * Making this a no-op skips the entire check.
 * Fingerprinted by the unique "Skipping license check in isolated process." log string.
 */
val PairIPCheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        string("Skipping license check in isolated process.")
    )
)

/**
 * com.pairip.licensecheck.LicenseActivity.onStart()V
 *
 * Reads "activitytype" Intent extra (PAYWALL=0, ERROR=1).
 * PAYWALL → showPaywallAndCloseApp(); ERROR → showErrorDialog().
 * Both paths block the app behind a sheet or kill it.
 * Returning after super() prevents either branch — fallback layer.
 */
val PairIPLicenseActivityFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onStart",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    filters = listOf(
        string("activitytype")
    )
)

/**
 * r9/b.<init>(Z, r9/d, Long?, Z, r9/c, J)V — UserSubscription constructor
 *
 * Data class storing subscription state: a=isPro(Z), b=planType(r9/d),
 * c=expiryDate(Long?), d=isLifetime(Z), e=state(r9/c), f=timestamp(J).
 * The isPro field (a) is read by every feature gate and UI state builder.
 *
 * On startup the initial instance is created with isPro=false (no billing result yet),
 * causing the paywall to appear immediately before billing completes.
 * Forcing isPro=true in the constructor makes every instance report pro status.
 *
 * Fingerprinted by the unique parameter signature (Z,r9/d,Long?,Z,r9/c,J).
 */
val UserSubscriptionConstructorFingerprint = Fingerprint(
    definingClass = "Lr9/b;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Z", "Lr9/d;", "Ljava/lang/Long;", "Z", "Lr9/c;", "J"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR)
)
