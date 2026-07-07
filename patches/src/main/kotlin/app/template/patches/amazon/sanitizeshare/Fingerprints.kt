package app.template.patches.amazon.sanitizeshare

import app.morphe.patcher.Fingerprint

// copyTextToClipboard(String) - unique by "clipboard" string + class
internal val ShareToClipboardActivityFingerprint = Fingerprint(
    definingClass = "Lcom/amazon/mShop/share/copy/ShareToClipboardActivity;",
    name = "copyTextToClipboard",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;"),
    strings = listOf("clipboard"),
)
