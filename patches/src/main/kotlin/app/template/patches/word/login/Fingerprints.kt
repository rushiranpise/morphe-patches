package app.template.patches.word.login

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * firstrun.?.m0(Z, IOnTaskCompleteListener) — FTUX entry-point called by
 * FirstRunController when d0()=false (normal install). Calling onTaskComplete(success)
 * immediately completes the boot chain without showing sign-in UI.
 *
 * Stable anchor: OHubSharedPreferences.isFTUXShown(Context,Z)Z is a non-obfuscated
 * call inside m0(). Combined with the exact (Z,IOnTaskCompleteListener)V param
 * signature, verified globally unique to exactly one method in the app.
 * Drops definingClass "d" (obfuscated single-letter class, will shift).
 *
 * Filter matches smali instruction order in m0():
 *   invoke-static {v0,v1}, OHubSharedPreferences;->isFTUXShown(Context;Z)Z
 */
internal val firstRunM0Fingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf("Z", "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/microsoft/office/officehub/util/OHubSharedPreferences;",
            name = "isFTUXShown",
        ),
    ),
)

/**
 * firstrun.?.n0() — shows FTUX upsell screen after sign-in.
 * Patched to set state=FINAL + call setFTUXShown without showing the paywall.
 *
 * Stable anchor: const-string "FRE Completed" is non-obfuscated telemetry string
 * inside n0(). Verified globally unique to exactly one ()V method in the entire app.
 * Drops definingClass "d" (obfuscated single-letter class, will shift).
 *
 * Filter matches smali instruction order in n0():
 *   const-string v5, "FRE Completed"
 */
internal val firstRunN0Fingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    strings = listOf("FRE Completed"),
)

/**
 * ??.C(Context, DrillInDialog, IOnTaskCompleteListener) — static launcher for the
 * FTUX upsell screen. Class renamed a0→b0 in 16.0.20326 and will likely rename again.
 *
 * Stable anchor: the exact 3-param signature (Context, DrillInDialog, IOnTaskCompleteListener)V
 * with PUBLIC STATIC access is verified globally unique to exactly one method in the entire
 * app — no definingClass needed. Survives any future class rename.
 */
internal val ftuxPaywallLauncherFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/microsoft/office/docsui/common/DrillInDialog;",
        "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;",
    ),
)

/**
 * FileActivationSSOManager.checkAndStartSSOIfRequired(Z) — the public entry-point
 * that calls the private isSSORequired() internally. isSSORequired() became private
 * in 16.0.20228 so we target this public wrapper instead. Returning false(0) means
 * "SSO not required" → app opens directly without triggering sign-in on ProtocolActivation.
 * Stable: method name and param are non-obfuscated.
 */
internal val checkAndStartSSOIfRequiredFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/FileActivationSSOManager;",
    name = "checkAndStartSSOIfRequired",
    returnType = "Z",
    parameters = listOf("Z"),
)

/**
 * IdentityLiblet.GetIdentityForSignInName(String,Z,Z) — throws IllegalArgumentException
 * when sign-in name is null/empty. With no real account, AccountProfileInfo passes null
 * causing a crash on the Timer thread. Returning null safely avoids the throw.
 * Stable: non-obfuscated public API method.
 * Note: guard string changed to "Sign-in name should not be null or empty." in 16.0.20326.
 */
// No strings filter — guard string changes between builds. definingClass + name + params
// are fully non-obfuscated and uniquely identify this method without any string anchor.
internal val getIdentityForSignInNameFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/identity/IdentityLiblet;",
    name = "GetIdentityForSignInName",
    returnType = "Lcom/microsoft/office/identity/Identity;",
    parameters = listOf("Ljava/lang/String;", "Z", "Z"),
)
