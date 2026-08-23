package app.template.patches.excel.login

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * firstrun.?.m0(Z, IOnTaskCompleteListener) — FTUX entry-point. Completing the listener
 * immediately skips sign-in UI. Drops definingClass "d" (obfuscated, will shift).
 *
 * Stable anchor: methodCall OHubSharedPreferences.isFTUXShown(Context,Z)Z combined
 * with (Z,IOnTaskCompleteListener)V params — verified globally unique.
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
 * Patched to call setFTUXShown and return-void, skipping the paywall.
 * Drops definingClass "d" (obfuscated single-letter class, will shift).
 *
 * Stable anchor: const-string "FRE Completed" (telemetry log) is globally unique
 * to exactly one ()V method in the app. Combined with PUBLIC FINAL access flags.
 */
internal val firstRunN0Fingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    strings = listOf("FRE Completed"),
)

/**
 * ??.C(Context, DrillInDialog, IOnTaskCompleteListener) — static launcher for FTUX
 * upsell screen. Class renames every update (a0→b0 observed in Word 16.0.20326).
 * Drops definingClass entirely.
 *
 * Stable anchor: exact 3-param signature (Context,DrillInDialog,IOnTaskCompleteListener)V
 * + PUBLIC STATIC is globally unique to exactly one method in the app.
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
 * FileActivationSSOManager.checkAndStartSSOIfRequired(Z) — public entry-point that
 * calls private isSSORequired() internally (isSSORequired became private in 16.0.20228).
 * Returning false means "SSO not required" → app opens directly without sign-in.
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
 * when sign-in name is null/empty. Returning null avoids the crash on the Timer thread
 * when no real account is present.
 *
 * No strings filter — the guard string changes between builds ("Sign-in name is empty or null"
 * vs "Sign-in name should not be null or empty."). definingClass + name + params are fully
 * non-obfuscated and uniquely identify this method without any string anchor.
 */
internal val getIdentityForSignInNameFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/identity/IdentityLiblet;",
    name = "GetIdentityForSignInName",
    returnType = "Lcom/microsoft/office/identity/Identity;",
    parameters = listOf("Ljava/lang/String;", "Z", "Z"),
)
