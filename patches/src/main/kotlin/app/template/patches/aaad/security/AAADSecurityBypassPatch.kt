package app.template.patches.aaad.security

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.AAAD_COMPATIBILITY
import app.template.patches.shared.returnEarly

/**
 * AAAD Security Bypass Patch
 *
 * AAAD implements multiple runtime integrity and anti-analysis checks:
 *
 *  SecurityChecker:
 *   - isDebuggerAttached()  → wraps Debug.isDebuggerConnected()
 *   - detectFrida()         → scans /proc/maps and loaded libraries
 *   - detectXposed()        → inspects stack trace class names
 *   - isBeingTraced()       → reads /proc/self/status for TracerPid
 *
 *  IntegrityChecker:
 *   - verifyIntegrity()          → checks APK signature hash
 *   - verifyInstallerSource()    → ensures install came from Play Store
 *
 *  performSecurityChecks() aggregates all SecurityChecker results and produces a
 *  SecurityCheckResults object. If suspicious=true, the app may block functionality
 *  or show tamper warnings.
 *
 * Patch: return false (clean) from all detection methods to prevent any block.
 * verifyIntegrity() and verifyInstallerSource() are patched to return true
 * (they expect "is verified" = true for happy path).
 */
@Suppress("unused")
val aaadSecurityBypassPatch = bytecodePatch(
    name = "AAAD Security Bypass",
    description = "Disables AAAD's anti-tamper, anti-debug, and integrity checks to allow running on patched installations.",
) {
    compatibleWith(AAAD_COMPATIBILITY)

    execute {
        // Anti-debug checks → return false (not detected)
        IsDebuggerAttachedFingerprint.methodOrNull?.returnEarly(false)
        DetectFridaFingerprint.methodOrNull?.returnEarly(false)
        DetectXposedFingerprint.methodOrNull?.returnEarly(false)
        IsBeingTracedFingerprint.methodOrNull?.returnEarly(false)

        // Integrity checks → return true (verified/trusted)
        VerifyIntegrityFingerprint.methodOrNull?.returnEarly(true)
        VerifyInstallerSourceFingerprint.methodOrNull?.returnEarly(true)
    }
}
