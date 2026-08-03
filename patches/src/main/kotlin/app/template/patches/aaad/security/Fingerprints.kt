package app.template.patches.aaad.security

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets SecurityChecker.isDebuggerAttached()Z
 *
 * Wraps Debug.isDebuggerConnected(). Called from performSecurityChecks().
 *
 * Smali (classes3/com/legs/appsforaa/utils/SecurityChecker.smali):
 *   .method public final isDebuggerAttached()Z
 *     invoke-static {}, Landroid/os/Debug;->isDebuggerConnected()Z
 *     move-result v0
 *     return v0
 *
 * Access flags: PUBLIC FINAL
 */
object IsDebuggerAttachedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    definingClass = "Lcom/legs/appsforaa/utils/SecurityChecker;",
    name = "isDebuggerAttached",
    filters = listOf(
        methodCall(
            definingClass = "Landroid/os/Debug;",
            name = "isDebuggerConnected",
        ),
    ),
)

/**
 * Targets SecurityChecker.detectFrida()Z
 *
 * Scans process maps and loaded libraries for Frida artifacts.
 * Returns true if Frida is detected.
 *
 * Access flags: PUBLIC FINAL
 */
object DetectFridaFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    definingClass = "Lcom/legs/appsforaa/utils/SecurityChecker;",
    name = "detectFrida",
)

/**
 * Targets SecurityChecker.detectXposed()Z
 *
 * Checks class names in the call stack for Xposed/LSPosed hooks.
 *
 * Access flags: PUBLIC FINAL
 */
object DetectXposedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    definingClass = "Lcom/legs/appsforaa/utils/SecurityChecker;",
    name = "detectXposed",
)

/**
 * Targets SecurityChecker.isBeingTraced()Z
 *
 * Checks /proc/self/status for TracerPid != 0.
 *
 * Access flags: PUBLIC FINAL
 */
object IsBeingTracedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    definingClass = "Lcom/legs/appsforaa/utils/SecurityChecker;",
    name = "isBeingTraced",
)

/**
 * Targets IntegrityChecker.verifyIntegrity(Context)Z
 *
 * Performs APK signature verification. If the APK is re-signed (patched),
 * this returns false and the app may refuse to function.
 *
 * Smali (classes3/com/legs/appsforaa/utils/IntegrityChecker.smali):
 *   .method public final verifyIntegrity(Landroid/content/Context;)Z
 *     ... checks signature hash, installer source ...
 *
 * Access flags: PUBLIC FINAL
 */
object VerifyIntegrityFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    definingClass = "Lcom/legs/appsforaa/utils/IntegrityChecker;",
    name = "verifyIntegrity",
)

/**
 * Targets IntegrityChecker.verifyInstallerSource(Context)Z
 *
 * Checks that the app was installed from a known source (Play Store).
 *
 * Access flags: PUBLIC FINAL
 */
object VerifyInstallerSourceFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;"),
    definingClass = "Lcom/legs/appsforaa/utils/IntegrityChecker;",
    name = "verifyInstallerSource",
)
