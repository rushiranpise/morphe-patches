package app.template.patches.compsuite.protection

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── LicenseCheckFingerprint ───────────────────────────────────────────────────
//
// Targets: com.pairip.licensecheck.LicenseClient.checkLicense(Context)V
//
// Called from com.pairip.application.Application.attachBaseContext(). After
// Morphe re-signs the APK, signature hash differs from the Play Store cert →
// LicenseClient.checkLicense() detects mismatch → starts LicenseActivity →
// blocks the app UI.
//
// The manifest fix (android:name swap to android.app.Application) already
// prevents Application.attachBaseContext() from being called. This no-op
// is belt-and-suspenders in case any other path reaches checkLicense().
//
// com.pairip.application.Application extends android.app.Application directly
// (no custom WearOsToolset Application subclass exists). The swap to
// android.app.Application loses no app-level initialisation — all init is done
// in MainActivity and via lazy singletons (BillingLibrary, etc.).
//
// Access flags: PUBLIC STATIC — stable, non-obfuscated class/method name.
//
object LicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
