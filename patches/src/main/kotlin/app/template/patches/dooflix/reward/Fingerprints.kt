package app.template.patches.dooflix.reward

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets Lcom/nodepointer/Bridge;->startNative()I — stable SDK class, not obfuscated
// Called from both the foreground service path and the background path in onComplete
// Patching the SDK entry point itself is more robust than patching the obfuscated caller
// Returns int → return 0 (success, do nothing)
object NodepointerBridgeStartNativeFingerprint : Fingerprint(
    definingClass = "Lcom/nodepointer/Bridge;",
    name = "startNative",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// Targets Lcom/nodepointer/Bridge;->initializeNative() — called before startNative
// Stable SDK class name
object NodepointerBridgeInitNativeFingerprint : Fingerprint(
    definingClass = "Lcom/nodepointer/Bridge;",
    name = "initializeNative",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// Targets Lcom/nodepointer/BootReceiver;->onReceive — restarts nodepointer on boot
// Stable: non-obfuscated class name
object NodepointerBootReceiverFingerprint : Fingerprint(
    definingClass = "Lcom/nodepointer/BootReceiver;",
    name = "onReceive",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "Landroid/content/Intent;"),
)

// Targets Lcom/nodepointer/service/ForegroundService;->onStartCommand — stable class
object NodepointerForegroundServiceFingerprint : Fingerprint(
    definingClass = "Lcom/nodepointer/service/ForegroundService;",
    name = "onStartCommand",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Intent;", "I", "I"),
)

// ── Update Gate ───────────────────────────────────────────────────────────────

// Targets the Firebase Remote Config onComplete callback (OnCompleteListener) in Lb2/y;
// which fetches latestVersionCode from server and shows forced update screen if too old.
// Stable fingerprint: onComplete(Task)V containing "latestVersionCode" + Integer.parseInt
// Unique across entire APK. Returns void — we skip the version comparison entirely.
object UpdateGateFingerprint : Fingerprint(
    name = "onComplete",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Lcom/google/android/gms/tasks/Task;"),
    filters = listOf(
        string("latestVersionCode"),
        methodCall(definingClass = "Ljava/lang/Integer;", name = "parseInt"),
        string("latestVersion"),
    )
)
