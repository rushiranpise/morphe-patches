package app.template.patches.amazon.darkmode

import app.morphe.patcher.Fingerprint

// unique by definingClass+name - no other getTabIcon in BaseTabController
internal val BaseTabControllerGetTabIconFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/chrome/bottomtabs/BaseTabController;",
    name = "getTabIcon",
    returnType = "Landroid/widget/ImageView;",
    parameters = emptyList(),
)

internal val MShopWebViewContainerOnPageFinishedFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/MShopWebViewContainer;",
    name = "onPageFinished",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("about:blank"),
)
