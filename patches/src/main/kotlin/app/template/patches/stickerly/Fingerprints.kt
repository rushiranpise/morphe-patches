package app.template.patches.stickerly

import app.morphe.patcher.Fingerprint

internal val SubscriptionCacheReadFingerprint = Fingerprint(
    definingClass = "Ldroom/daro/a/rx/m;",
    name = "a",
    returnType = "Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;",
    parameters = emptyList(),
)

internal val SubscriptionResponseMapperFingerprint = Fingerprint(
    definingClass = "Ldroom/daro/a/rx/l;",
    name = "h",
    returnType = "Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;",
    parameters = listOf("Lcom/snowcorp/stickerly/android/base/data/serverapi/SubscriptionResponse;"),
)
