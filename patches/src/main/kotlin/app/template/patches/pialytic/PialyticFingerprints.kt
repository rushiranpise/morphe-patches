package app.template.patches.pialytic

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Layer 1: PairIP License Check Core Entry Point ──────────────────────────────
// LicenseActivity.onCreate() - Main entry point that shows paywall/error dialogs
// This Activity gets launched when license check fails. Dismissing it immediately
// prevents all downstream paywall UI and forced app exits.
val LicenseActivityOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "onCreate",
    parameters = listOf("Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 2: PairIP License Check Helper Methods ────────────────────────────────
// These methods are called by LicenseActivity to show paywall/errors and close app.
// No-op them as defense-in-depth in case onCreate patch doesn't fully block execution.

val ShowPaywallAndCloseAppFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "showPaywallAndCloseApp",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

val ShowErrorDialogFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "showErrorDialog",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

val CloseAppFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseActivity;",
    name = "closeApp",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 3: PairIP LicenseClient Backend Checks ─────────────────────────────────
// LicenseClient methods that perform the actual license verification logic.
// Patching these prevents license checks from ever being initiated or processed.

val LicenseClientInitFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val LicenseClientCheckInternalFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicenseInternal",
    parameters = listOf("Landroid/os/IBinder;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

val LicenseClientHandleErrorFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "handleError",
    parameters = listOf("Lcom/pairip/licensecheck/LicenseCheckException;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

val LicenseClientProcessResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    parameters = listOf("I", "Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

// ── Layer 4: BillingService Initialization ───────────────────────────────────────
// BillingService.init() sets up Google Play billing. No-op prevents any billing checks.

val BillingServiceInitFingerprint = Fingerprint(
    definingClass = "Lverbosus/verbtex/billing/BillingService;",
    name = "init",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 5: BillingService Query Methods ────────────────────────────────────────
// These methods query Play Store for purchases. No-op them to prevent purchase checks.

val BillingServiceQueryPurchasesFingerprint = Fingerprint(
    definingClass = "Lverbosus/verbtex/billing/BillingService;",
    name = "queryPurchases",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val BillingServiceQueryProductDetailsFingerprint = Fingerprint(
    definingClass = "Lverbosus/verbtex/billing/BillingService;",
    name = "queryProductDetailsAsync",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
    returnType = "Ljava/util/concurrent/CompletableFuture;",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val BillingServiceStartPurchaseFingerprint = Fingerprint(
    definingClass = "Lverbosus/verbtex/billing/BillingService;",
    name = "startPurchase",
    parameters = listOf("Ljava/lang/String;", "Landroid/app/Activity;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── Layer 6: Premium Feature Gates ───────────────────────────────────────────────
// Validator.isProVersion() - Core premium check used throughout app to gate features.
// This method checks SharedPreferences for "prefIsVerbtexPro" boolean.
// Patching it to always return true unlocks all premium features:
// - Cloud sync (UploadFileTask, remote projects)
// - PDF generation (GeneratePdfLocalTask, VelaraGenerateTask)
// - Editor features (VerbosusEditor, EditorFragment)
// - Project limits (LocalProjectListActivity - removes "2 projects" limit)
// - Advanced preferences (EditorPreferencesActivity, AppPreferencesActivity)

val ValidatorIsProVersionFingerprint = Fingerprint(
    definingClass = "Lverbosus/verbtex/common/utility/Validator;",
    name = "isProVersion",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
