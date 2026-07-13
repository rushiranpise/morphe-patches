package app.template.patches.recipebro

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal object CheckLicenseFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
)

internal object GetShowPremiumAfterOnboardingFingerprint : Fingerprint(
    definingClass = "Lcom/recipebro/cookingbuddy/client/utils/FeatureFlagResponse;",
    name = "getShowPremiumAfterOnboarding",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal object GetBooleanFingerprint : Fingerprint(
    definingClass = "Lcom/recipebro/cookingbuddy/client/utils/FeatureFlagResponse;",
    name = "getBoolean",
    returnType = "Z",
    parameters = listOf("Lqd2;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal object GetHardImportLimitFingerprint : Fingerprint(
    definingClass = "Lcom/recipebro/cookingbuddy/client/utils/FeatureFlagResponse;",
    name = "getHardImportLimit",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

internal object GetSoftImportLimitFingerprint : Fingerprint(
    definingClass = "Lcom/recipebro/cookingbuddy/client/utils/FeatureFlagResponse;",
    name = "getSoftImportLimit",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Static method: full descriptor = (Lb9;Lcom/.../CustomerInfo;)La80;
// p0 = b9 instance, p1 = CustomerInfo, v0-v6 = true locals (no overlap).
// v1 = activeSubscriptions Set at instruction index 2 (move-result-object).
internal object B9CustomerInfoConverterFingerprint : Fingerprint(
    definingClass = "Lb9;",
    name = "a",
    returnType = "La80;",
    parameters = listOf("Lb9;", "Lcom/revenuecat/purchases/kmp/models/CustomerInfo;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
)
