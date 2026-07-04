package app.template.patches.spin

import app.morphe.patcher.Fingerprint

internal val SpinSubscriptionGetterFingerprint = Fingerprint(
    definingClass = "Lcom/nationaledtech/spinbrowser/plus/subscription/SubscriptionState;",
    name = "getHasSubscription",
    returnType = "Z",
)

internal val SpinSubscriptionComponentFingerprint = Fingerprint(
    definingClass = "Lcom/nationaledtech/spinbrowser/plus/subscription/SubscriptionState;",
    name = "component8",
    returnType = "Z",
)

internal val SpinSubscriptionFlowFingerprint = Fingerprint(
    definingClass = "Lcom/nationaledtech/spinbrowser/plus/subscription/SubscriptionManager${'$'}userCurrentSubscriptionFlow${'$'}1;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)
