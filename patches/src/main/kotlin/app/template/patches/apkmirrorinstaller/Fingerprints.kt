package app.template.patches.apkmirrorinstaller

import app.morphe.patcher.Fingerprint

internal val RewardedInstallHandlerFingerprint = Fingerprint(
    definingClass = "Lcom/apkmirror/presentation/installer/InstallerActivity;",
    name = "O1",
    returnType = "Ls9/u2;",
    parameters = listOf(
        "Lmb/r0;",
        "Lcom/apkmirror/presentation/installer/InstallerActivity;",
        "Landroidx/compose/runtime/MutableState;",
        "Landroidx/compose/runtime/State;",
        "Landroidx/compose/material3/SnackbarHostState;",
        "Landroidx/compose/runtime/MutableState;",
    ),
)

internal val PremiumManagerBillingDataFingerprint = Fingerprint(
    definingClass = "Ly0/f\$l;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)

internal val InitAdsFingerprint = Fingerprint(
    definingClass = "Lx0/c;",
    name = "l",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal val LoadRewardedAdFingerprint = Fingerprint(
    definingClass = "Lx0/c;",
    name = "m",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)

internal val LoadAdsFingerprint = Fingerprint(
    definingClass = "Lx0/c;",
    name = "n",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
)
