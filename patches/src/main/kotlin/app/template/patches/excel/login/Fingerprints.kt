package app.template.patches.excel.login

import app.morphe.patcher.Fingerprint

/**
 * firstrun.d.m0(Z, IOnTaskCompleteListener) — FTUX entry point called by
 * FirstRunController when d0()=false (normal install). Calling onTaskComplete(success)
 * immediately completes the boot chain without showing sign-in UI.
 * Pinned by definingClass + name + params.
 */
internal val firstRunM0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "m0",
    returnType = "V",
    parameters = listOf(
        "Z",
        "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;",
    ),
)

/**
 * firstrun.d.n0() — shows FTUX upsell/confirmation screen (a0.D) after sign-in.
 * Patched to set D=FINAL + setFTUXShown without showing the paywall.
 * Pinned by unique field write to d$s.FINAL inside body.
 */
internal val firstRunN0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "n0",
    returnType = "V",
    parameters = emptyList(),
)

/**
 * a0.D(Context, DrillInDialog, IOnTaskCompleteListener) — static launcher for the
 * FTUX confirmation/upsell screen that shows PaywallActivity.
 * No-opping this prevents the paywall from ever being shown during FTUX.
 * Pinned by unique string "FTUXConfirmationView" in a0.J() which is called from show().
 */
internal val ftuxPaywallLauncherFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/controls/a0;",
    name = "D",
    returnType = "V",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/microsoft/office/docsui/common/DrillInDialog;",
        "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;",
    ),
)

/**
 * FileActivationSSOManager.isSSORequired() — returns true when no account is added,
 * triggering interactive sign-in on every cold start (ProtocolActivation).
 * Returning false skips the sign-in UI entirely — app opens directly.
 */
internal val isSSORequiredFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/docsui/common/FileActivationSSOManager;",
    name = "isSSORequired",
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * IdentityLiblet.GetIdentityForSignInName(String,Z,Z) — throws IllegalArgumentException
 * when sign-in name is null/empty. With no real account, AccountProfileInfo passes null
 * causing crash on Timer thread. Returning null safely instead of throwing.
 */
internal val getIdentityForSignInNameFingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/identity/IdentityLiblet;",
    name = "GetIdentityForSignInName",
    returnType = "Lcom/microsoft/office/identity/Identity;",
    parameters = listOf("Ljava/lang/String;", "Z", "Z"),
    strings = listOf("Sign-in name is empty or null"),
)
