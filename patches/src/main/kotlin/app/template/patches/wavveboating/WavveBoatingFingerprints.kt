package app.template.patches.wavveboating

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// LicenseManager.a() — returns true if user is NOT licensed (paywall check, inverted logic)
val LicenseManagerIsUnlicensedFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/LicenseManager;",
    name = "a",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// LicenseManager.l() — returns true if user is licensed (paddle OR stripe OR valid cert)
val LicenseManagerIsLicensedFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/LicenseManager;",
    name = "l",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// LicenseManager.b() — isSubscribedOrLicensed (used for coverage data gating)
val LicenseManagerIsSubscribedOrLicensedFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/LicenseManager;",
    name = "b",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// PlayStoreBilling.e() — returns true if active Play Store subscription (expiry-based)
val PlayStoreBillingIsActiveFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/PlayStoreBilling;",
    name = "e",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// PlayStoreBilling.g() — returns true if subscription type != Unknown
val PlayStoreBillingHasSubscriptionFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/PlayStoreBilling;",
    name = "g",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// PlayStoreBilling.f() — returns true if subscription is NOT paid (shows paywall)
val PlayStoreBillingIsUnpaidFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/billing/PlayStoreBilling;",
    name = "f",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// UserSettings.hasPaddleLicense() — reads "paddle licensed" pref
val UserSettingsHasPaddleLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/model/UserSettings;",
    name = "hasPaddleLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// UserSettings.hasStripeLicense() — reads "stripe licensed" pref
val UserSettingsHasStripeLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/wavve/boating/gps/model/UserSettings;",
    name = "hasStripeLicense",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
