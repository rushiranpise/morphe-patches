package app.template.patches.amazon.removeads

import app.morphe.patcher.Fingerprint

// Normal (non-jumpstarted) page loads
internal val MShopWebViewClientOnPageFinishedFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/MShopWebViewClient;",
    name = "onPageFinished",
    returnType = "V",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;"),
    strings = listOf("onPageFinished_withDuration"),
)

// All pages (normal + jumpstarted): fires when WebView is assigned to the fragment
internal val InteractionWebFragmentSetWebViewFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/InteractionWebFragment;",
    name = "setWebView",
    returnType = "V",
    parameters = listOf("Lcom/amazon/mShop/web/MShopWebView;"),
)
