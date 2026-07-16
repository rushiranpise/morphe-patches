package app.template.patches.androidverifier

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Layer 1: PlatformVerificationService.onVerificationRequired(DeveloperVerificationSession)
// Called by PackageInstaller for every APK install — inject bypass here.
val OnVerificationRequiredFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/verifier/helpers/verification/impl/common/platform/PlatformVerificationService;",
    name = "onVerificationRequired",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/pm/verify/developer/DeveloperVerificationSession;"),
)

// Layer 2: PlatformVerificationService.onVerificationRetry(DeveloperVerificationSession)
// Called when a verification is retried — same bypass needed.
val OnVerificationRetryFingerprint = Fingerprint(
    definingClass = "Lcom/google/android/verifier/helpers/verification/impl/common/platform/PlatformVerificationService;",
    name = "onVerificationRetry",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/pm/verify/developer/DeveloperVerificationSession;"),
)

// Layer 3: hfy.a() — reads phenotype flag 45681539 (platform policy: 0=NONE, 3=CLOSED).
// Forcing return of Long(0) makes the policy manager always see NONE — no blocking.
val PlatformPolicyFlagFingerprint = Fingerprint(
    strings = listOf("45681539"),
    returnType = "Ljava/lang/Long;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Layer 4: hgz.a() — reads phenotype flag 45749715 (forced backport: short-circuits verification).
// Forcing return of Boolean.TRUE activates the backport bypass path.
val ForcedBackportFlagFingerprint = Fingerprint(
    strings = listOf("45749715"),
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
