package app.template.patches.stickermaker

import app.morphe.patcher.Fingerprint

internal val IsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/marsvard/stickermakerforwhatsapp/DB${'$'}Companion;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
)

internal val SetPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/marsvard/stickermakerforwhatsapp/DB${'$'}Companion;",
    name = "setPremium",
    returnType = "V",
    parameters = listOf("Z"),
)

internal val PremiumDialogShownFingerprint = Fingerprint(
    definingClass = "Lcom/marsvard/stickermakerforwhatsapp/DB${'$'}Companion;",
    name = "premiumDialogShown",
    returnType = "Z",
    parameters = emptyList(),
)

internal val SetPremiumDialogShownFingerprint = Fingerprint(
    definingClass = "Lcom/marsvard/stickermakerforwhatsapp/DB${'$'}Companion;",
    name = "setPremiumDialogShown",
    returnType = "V",
    parameters = listOf("Z"),
)
