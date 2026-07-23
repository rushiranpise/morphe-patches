package app.template.patches.sai

import app.morphe.patcher.Fingerprint

internal val HasActiveSubscriptionPreferenceFingerprint = Fingerprint(
    definingClass = "Lf89;",
    name = "b",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("has_active_subscription"),
)

internal val SetActiveSubscriptionPreferenceFingerprint = Fingerprint(
    definingClass = "Lf89;",
    name = "c",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Landroid/content/Context;", "Z", "Lys1;"),
    strings = listOf("has_active_subscription"),
)

internal val ActiveSubscriptionFlowEmitFingerprint = Fingerprint(
    definingClass = "Lb89;",
    name = "emit",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Lvs1;"),
)
