package app.template.patches.word.login

import app.morphe.patcher.Fingerprint

/**
 * firstrun.d.m0(Z, IOnTaskCompleteListener) — FTUX entry-point called by
 * FirstRunController when d0()=false (normal install). Calling onTaskComplete(success)
 * immediately completes the boot chain without showing sign-in UI.
 * Stable: definingClass + name + params are non-obfuscated.
 */
internal val firstRunM0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "m0",
    returnType = "V",
    parameters = listOf("Z", "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;"),
)

/**
 * firstrun.d.n0() — shows FTUX upsell screen after sign-in.
 * Patched to set state=FINAL + call setFTUXShown without showing the paywall.
 * Field renamed H→B in 16.0.20228; patch body updated accordingly.
 * Stable: definingClass + name pinned; body uses stable sget FINAL which is verified.
 */
internal val firstRunN0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "n0",
    returnType = "V",
    parameters = emptyList(),
)

/**
 * a0.C(Context, DrillInDialog, IOnTaskCompleteListener) — static launcher for the
 * FTUX upsell screen (renamed D→C in 16.0.20228, same params).
 * No-opping prevents PaywallActivity from showing during FTUX.
 * Stable: all params are non-obfuscated SDK types.
 */
internal val ftuxPaywallLauncherFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/a0;",
    name = "C",
    returnType = "V",
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
 */
internal val getIdentityForSignInNameFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/identity/IdentityLiblet;",
    name = "GetIdentityForSignInName",
    returnType = "Lcom/microsoft/office/identity/Identity;",
    parameters = listOf("Ljava/lang/String;", "Z", "Z"),
    strings = listOf("Sign-in name is empty or null"),
)
