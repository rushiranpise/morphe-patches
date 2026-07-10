package app.template.patches.amazon.hiderufus

import app.morphe.patcher.Fingerprint

internal val SavXTabControllerIsEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/chrome/bottomtabs/SavXTabController;",
    name = "isEnabled",
    returnType = "Z",
    parameters = emptyList(),
)
