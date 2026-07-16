package app.template.patches.androidverifier

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ANDROID_VERIFIER_COMPATIBILITY

/**
 * Bypasses the Android Developer Verifier (com.google.android.verifier).
 *
 * Background: Starting Sep 2026, this pre-installed system service blocks APK installs on
 * certified Android 16+ devices in BR/ID/SG/TH when the signing certificate isn't in
 * Google's developer registry. Policy is controlled by phenotype flags pulled from Google's
 * servers. This patch short-circuits all enforcement paths in the DEX layer.
 *
 * Note: requires the patched APK to be installed as a system app (replacing the original)
 * since com.google.android.verifier holds DEVELOPER_VERIFICATION_AGENT privilege.
 * ADB installs are exempt from verification regardless.
 *
 * Bypass layers:
 * 1. onVerificationRequired / onVerificationRetry → call reportVerificationBypassed(1) immediately.
 * 2. Platform policy flag (45681539) → return Long(0) = NONE (no blocking).
 * 3. Forced backport flag (45749715) → return Boolean.TRUE (short-circuit path).
 */
@Suppress("unused")
val androidVerifierBypassPatch = bytecodePatch(
    name = "Bypass developer verification",
    description = "Forces all APK install verification sessions to be bypassed, preventing " +
        "the verifier from blocking sideloaded or unsigned apps on Android 16+ devices."  + 
        "Important: Requires pushing the patched APK as a system app replacement (e.g. via Magisk module or ADB with root)" +
        "since the original holds DEVELOPER_VERIFICATION_AGENT permission. ADB installs are exempt from verification regardless.",
    default = true,
) {
    compatibleWith(ANDROID_VERIFIER_COMPATIBILITY)

    execute {
        // Layer 1 & 2: Immediately report bypass on every verification request.
        // reportVerificationBypassed(1) signals the PackageInstaller to allow the install.
        val bypassInstructions =
            """
                const/4 v0, 0x1
                invoke-virtual {p1, v0}, Landroid/content/pm/verify/developer/DeveloperVerificationSession;->reportVerificationBypassed(I)V
                return-void
            """

        runCatching {
            OnVerificationRequiredFingerprint.method.addInstructions(0, bypassInstructions)
        }

        runCatching {
            OnVerificationRetryFingerprint.method.addInstructions(0, bypassInstructions)
        }

        // Layer 3: Force platform policy flag (45681539) to return Long(0) = NONE.
        // NONE means: do not block any install regardless of verification result.
        runCatching {
            PlatformPolicyFlagFingerprint.method.addInstructions(
                0,
                """
                    const-wide/16 v0, 0x0
                    invoke-static {v0, v1}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
                    move-result-object v0
                    return-object v0
                """,
            )
        }

        // Layer 4: Force forced-backport flag (45749715) to return Boolean.TRUE.
        // This activates the backport short-circuit path which bypasses fingerprint matching.
        runCatching {
            ForcedBackportFlagFingerprint.method.addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object v0
                """,
            )
        }
    }
}
