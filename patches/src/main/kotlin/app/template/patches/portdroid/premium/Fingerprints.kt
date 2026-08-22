package app.template.patches.portdroid.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Target 1: Settings.getProVersion() ────────────────────────────────────
//
// com.stealthcopter.portdroid.core.Settings.getProVersion()Z
//
// Reads SharedPreferences.getBoolean("PRO_VERSION", false).
// This is the primary gate consulted across the whole app — every pro-gated
// feature (export, MDNS, UPnP, traceroute, port history) calls this.
//
// Fingerprint strategy: method name "getProVersion" is globally unique (1 hit
// across all 4334 smali files). Using name= alone is the most stable anchor.
//
// Access flags: PUBLIC STATIC
// Return type:  Z (boolean)
// Parameters:   none
// DEX: classes — smali verified against versionCode 113.
object GetProVersionFingerprint : Fingerprint(
    name = "getProVersion",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
)

// ─── Target 2: TamperCheck.validateAppSignature() ──────────────────────────
//
// com.stealthcopter.portdroid.core.utils.TamperCheck.validateAppSignature(App)Z
//
// Computes SHA1 of the APK signing certificate and checks it against three
// hardcoded hashes. On failure, App.onCreate writes INVALID_SIGNATURE=true to
// SharedPreferences (used as a Crashlytics tag, not a hard block).
// Returning true keeps the flag unset and telemetry clean.
//
// Fingerprint strategy: string "Application signature: %s" is unique (1 hit).
// Class and method names are non-obfuscated — also safe but string is more
// precise as it anchors the exact method body.
//
// Access flags: PUBLIC STATIC
// Return type:  Z
// Parameters:   Lcom/stealthcopter/portdroid/core/App;
// DEX: classes — smali verified against versionCode 113.
object ValidateAppSignatureFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/stealthcopter/portdroid/core/App;"),
    filters = listOf(
        string("Application signature: %s"),
    ),
)

// ─── Target 3: IAPViewModel$IAPStatus constructor ──────────────────────────
//
// com.stealthcopter.portdroid.feature.upgrade.IAPViewModel$IAPStatus.<init>(Purchase,Purchase)V
//
// Constructs the billing status object. proVersion = (sub != null || lifetime != null).
// All Activities observe IAPStatus via LiveData; the observer reads proVersion and
// calls BaseActivity.unlockProFeatures(Z). Forcing proVersion=true here propagates
// the pro state to every Activity automatically.
//
// Fingerprint strategy: string "IAPStatus(subscriptionPurchase=" from toString() is
// unique (1 hit). It lives in the same class, so classFingerprint resolves the
// constructor from the correct class scope.
//
// Access flags: PUBLIC CONSTRUCTOR
// Return type:  V
// Parameters:   Purchase, Purchase
// DEX: classes — smali verified against versionCode 113.
object IAPStatusToStringFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("IAPStatus(subscriptionPurchase="),
    ),
)

object IAPStatusConstructorFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Lcom/android/billingclient/api/Purchase;",
        "Lcom/android/billingclient/api/Purchase;",
    ),
    classFingerprint = IAPStatusToStringFingerprint,
)
