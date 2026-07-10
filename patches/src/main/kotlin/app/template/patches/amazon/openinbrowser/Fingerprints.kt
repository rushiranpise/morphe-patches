package app.template.patches.amazon.openinbrowser

import app.morphe.patcher.Fingerprint

// shouldOverrideUrlLoading - unique by "404.html" string
internal val MShopWebViewClientShouldOverrideFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/web/MShopWebViewClient;",
    name = "shouldOverrideUrlLoading",
    returnType = "Z",
    parameters = listOf("Landroid/webkit/WebView;", "Ljava/lang/String;"),
    strings = listOf("404.html"),
)
