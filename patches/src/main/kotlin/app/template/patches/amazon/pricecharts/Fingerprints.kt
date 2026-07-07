package app.template.patches.amazon.pricecharts

import app.morphe.patcher.Fingerprint

// onPageFinished for non-jumpstarted pages
internal val MShopWebViewClientOnPageFinishedFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/MShopWebViewClient;",
    name = "onPageFinished",
    returnType = "V",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;"),
    strings = listOf("onPageFinished_withDuration"),
)

// fires when any page (incl jumpstarted) becomes visible to user
internal val InteractionWebFragmentPostShownFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/InteractionWebFragment;",
    name = "onFragmentPostShown",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("No animation associated with "),
)
