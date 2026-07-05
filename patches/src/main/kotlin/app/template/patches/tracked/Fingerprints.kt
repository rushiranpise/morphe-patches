package app.template.patches.tracked

import app.morphe.patcher.Fingerprint

internal val EntitlementIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/EntitlementInfo;",
    name = "isActive",
    returnType = "Z",
)
