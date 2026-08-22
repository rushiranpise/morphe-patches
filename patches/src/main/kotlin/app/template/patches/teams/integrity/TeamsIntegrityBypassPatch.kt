package app.template.patches.teams.integrity

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags

// =============================================================================
// Fix 1 — CodeTransparency bypass
// =============================================================================
// CodeValidationTask.isApplicable() gates whether execute() runs.
// execute() calls CodeTransparencyValidator.validateFileHash() which computes
// SHA-256 of each DEX file against the signed JWT in the APK. Patching changes
// the hash → SecurityException → "App not installed from Play Store" crash.
// Fix: returnEarly(false) → execute() never runs.
private val codeTransparencyIsApplicableFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/skype/teams/applifecycle/task/CodeValidationTask;",
    name = "isApplicable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// =============================================================================
// Fix 2 — OneAuth redirect_uri cert bypass
// =============================================================================
// libOneAuth.so validates the app's signing cert SHA1 against Azure AD on startup.
// Patched APK is resigned → cert mismatch → FATAL EXCEPTION before login screen.
// Fix: returnEarly(false) routes getAuthProvider() to MsalAuthenticationProvider
// which validates creds server-side, not against the local signing cert.
private val shouldUseOneAuthFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "Lcom/microsoft/teams/nativecore/INativeCoreExperimentationManager;",
        "Lcom/microsoft/skype/teams/storage/configuration/IConfigurationManager;",
    ),
    filters = listOf(
        string("oneauthEnabled"),
    ),
)

// shouldUseOneAuthSDM uses "oneauthEnabledSDM" (different string from non-SDM path).
private val shouldUseOneAuthSdmFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "Lcom/microsoft/teams/nativecore/INativeCoreExperimentationManager;",
    ),
    filters = listOf(
        string("oneauthEnabledSDM"),
    ),
)

// =============================================================================
// Fix 3 — MSAL redirect_uri runtime cert check
// =============================================================================
// PublicClientApplicationConfiguration.verifyRedirectUriWithAppSignature() computes
// SHA1(installed cert) → Base64 → compares with mRedirectUri from config JSON.
// Config JSONs are already patched by Morphe, but MSAL re-validates at runtime
// → MsalClientException("redirect_uri_validation_error") → MSAL_PUBLIC_APPLICATION_IS_NULL
// → every login attempt fails silently after 8-second timeout.
// Fix: returnEarly() skips the redundant check; config-based validation is sufficient.
private val verifyRedirectUriFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/identity/client/PublicClientApplicationConfiguration;",
    name = "verifyRedirectUriWithAppSignature",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = emptyList(),
)

// Hidden from the Morphe UI (no name/description). Exposed only via the
// dependency accessor below, consumed by TeamsUnlockPremiumPatch and TeamsPrivacyPatch.
private val teamsIntegrityBypassPatch = bytecodePatch {
    execute {
        // Fix 1: skip CodeTransparency DEX hash validation.
        codeTransparencyIsApplicableFingerprint.method.returnEarly(false)

        // Fix 2: use MSAL instead of OneAuth (avoids native cert check).
        shouldUseOneAuthFingerprint.method.returnEarly(false)
        shouldUseOneAuthSdmFingerprint.method.returnEarly(false)

        // Fix 3: skip MSAL's redundant runtime redirect_uri cert validation.
        verifyRedirectUriFingerprint.method.returnEarly()
    }
}

@JvmSynthetic
internal fun teamsIntegrityBypassDependency() = teamsIntegrityBypassPatch
