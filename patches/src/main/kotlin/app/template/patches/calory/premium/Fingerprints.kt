package app.template.patches.calory.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Targets sk.n.d(CustomerInfo) — the RevenueCat entitlement handler.
// Called whenever CustomerInfo is updated (on app start, restore, purchase).
// Reads "calory-pro" and "remove-ads" entitlements → writes booleans to SharedPrefs:
//   "isRevenueCatPurchased" (pro) and "isRevenueCatRemoveAdsPurchased" (ads).
// We return early after forcing both values to true via addInstructions.
// Stable anchors: non-obfuscated RevenueCat SDK methods + the literal string key.
// DEX: classes2 — smali verified against versionCode 201.
object EntitlementHandlerFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/CustomerInfo;",
            name = "getEntitlements"
        ),
        methodCall(
            definingClass = "Lcom/revenuecat/purchases/EntitlementInfos;",
            name = "getActive"
        ),
        string("isRevenueCatPurchased"),
    )
)

// Targets com.pairip.licensecheck.LicenseClient.checkLicense(Context) — Pairip license check.
// Communicates with Google Play to verify the app licence.
// On failure shows a blocking paywall/denial screen.
// Stable: non-obfuscated class + method name + unique log string.
// DEX: classes — smali verified against versionCode 201.
object PairipCheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("Skipping license check in isolated process.")
    )
)

// Targets rk.h$a.c(CaloryApplication) — the "isPremium" gate.
// Returns true if user has premium: checks "purchase" OR "isRevenueCatPurchased" SharedPrefs.
// Used throughout the app to gate premium features.
// returnEarly(true) makes all feature gates open unconditionally.
// DEX: classes2 — smali verified against versionCode 201.
object IsPurchasedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/funnmedia/calory/utils/CaloryApplication;"),
    filters = listOf(
        string("purchase"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
        string("isRevenueCatPurchased"),
    )
)

// Targets rk.h$a.d(CaloryApplication) — the "isAdsRemoved" gate.
// Returns true if user has remove-ads: checks "removeAds" OR "isRevenueCatRemoveAdsPurchased".
// DEX: classes2 — smali verified against versionCode 201.
object IsRemoveAdsPurchasedFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Lcom/funnmedia/calory/utils/CaloryApplication;"),
    filters = listOf(
        string("removeAds"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean"
        ),
        string("isRevenueCatRemoveAdsPurchased"),
    )
)
