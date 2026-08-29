package app.template.patches.zee5.ads

import app.morphe.patcher.Fingerprint

private fun booleanGetter(name: String) = object : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, _ -> method.name == name },
) {}

object RegisteredUserAdsVisibilityFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, _ -> method.name == "getRegisteredUserAdsVisibility" },
)

object GuestAdsVisibilityFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, _ -> method.name == "getGuestAdsVisibility" },
)

object PremiumUserAdsVisibilityFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, _ -> method.name == "getPremiumUserAdsVisibility" },
)