package app.template.patches.word.login

import app.morphe.patcher.Fingerprint

internal val firstRunM0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "m0",
    returnType = "V",
    parameters = listOf("Z", "Lcom/microsoft/office/officehub/objectmodel/IOnTaskCompleteListener;"),
)

internal val firstRunN0Fingerprint = Fingerprint(
    definingClass = "Lcom/microsoft/office/firstrun/d;",
    name = "n0",
    returnType = "V",
    parameters = emptyList(),
)

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
