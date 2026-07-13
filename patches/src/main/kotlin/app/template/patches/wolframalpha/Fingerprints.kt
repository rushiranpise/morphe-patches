package app.template.patches.wolframalpha

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val APPLICATION_CLASS = "Lcom/wolfram/android/alphapro/WolframAlphaProApplication;"
private const val SUBSCRIPTION_DATA_CLASS = "Lcom/wolfram/android/alphalibrary/data/SubscriptionData;"
private const val ACCOUNT_DETAILS_CLASS = "Lcom/wolfram/android/alphalibrary/data/ProUserAccountDetails;"
private const val ONBOARDING_ACTIVITY_CLASS = "Lcom/wolfram/android/alphapro/activity/ProUserOnBoardingActivity;"

object IsProfessionalFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "L",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsProEnabledFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "O",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsActiveSubscriptionFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "j0",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsAndroidSubscriptionFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "k0",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object IsWolframSubscriptionFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "l0",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SetSubscriptionDataFingerprint : Fingerprint(
    definingClass = APPLICATION_CLASS,
    name = "i0",
    returnType = "V",
    parameters = listOf("Lcom/wolfram/android/alphalibrary/data/SubscriptionData;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object OnboardingCreateFingerprint : Fingerprint(
    definingClass = ONBOARDING_ACTIVITY_CLASS,
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionSuccessFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "j",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionStatusFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "i",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionProductLevelFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "g",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionUseTypeFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "k",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionPaymentTermsFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "d",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionPeriodUnitFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "e",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object SubscriptionPeriodValueFingerprint : Fingerprint(
    definingClass = SUBSCRIPTION_DATA_CLASS,
    name = "f",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object AccountPlanPrimaryFingerprint : Fingerprint(
    definingClass = ACCOUNT_DETAILS_CLASS,
    name = "d",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object AccountPlanSecondaryFingerprint : Fingerprint(
    definingClass = ACCOUNT_DETAILS_CLASS,
    name = "e",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
