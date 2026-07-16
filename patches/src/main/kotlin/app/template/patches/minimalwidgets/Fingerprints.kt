package app.template.patches.minimalwidgets

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// PremiumManager.isUnlocked(Context) — reads SharedPrefs "premium_prefs"/"premium_unlocked".
// Every widget lock check funnels through this via PremiumWidgetUtils.isLocked().
val PremiumManagerIsUnlockedFingerprint = Fingerprint(
    definingClass = "Lcom/jndapp/minimal/widgets/billing/PremiumManager;",
    name = "isUnlocked",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

// BillingManager.setUnlocked — called by Play Billing after purchase acknowledged.
// Patching it to always write true ensures any re-sync path also unlocks.
val BillingManagerSetUnlockedFingerprint = Fingerprint(
    definingClass = "Lcom/jndapp/minimal/widgets/billing/PremiumManager;",
    name = "setUnlocked",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Z"),
)

// LicenseClient.initializeLicenseCheck() — pairip entry point.
// Called from LicenseContentProvider.onCreate() at app start.
// Connects to Play Store licensing service, validates purchase, and shuts down app
// if NOT_LICENSED (responseCode=2) via startPaywallActivity().
// Nooping this method prevents pairip from ever starting.
val PairIpInitializeLicenseCheckFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = emptyList(),
)

// LicenseResponseHelper.validateResponse — called on license response.
// Throws LicenseCheckException if RSA signature verification fails,
// triggering handleError() → createCloseAppIntentOrExitIfAppInBackground() → kill app.
// Nooping prevents crash even if processResponse() is somehow called.
val PairIpValidateResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseResponseHelper;",
    name = "validateResponse",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;", "Ljava/lang/String;"),
)