package app.template.patches.windscribe

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val LoginPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserLoginResponse;",
    name = "isPremium",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val LoginTrafficMaxFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserLoginResponse;",
    name = "getTrafficMax",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val LoginBillingPlanFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserLoginResponse;",
    name = "getBillingPlanID",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val RegistrationPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserRegistrationResponse;",
    name = "isPremium",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val RegistrationTrafficMaxFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserRegistrationResponse;",
    name = "getTrafficMax",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "isPremium",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionTrafficMaxFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "getTrafficMax",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionBillingPlanFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "getBillingPlanID",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionUserClassFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "getUserClass",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionReBillFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "getReBill",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val SessionPremiumExpiryFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/UserSessionResponse;",
    name = "getPremiumExpiryDate",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val LocationPremiumOnlyFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/LocationData;",
    name = "getPremiumOnly",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val LocationPremiumOnlyComponentFingerprint = Fingerprint(
    definingClass = "Lcom/windscribe/vpn/api/response/LocationData;",
    name = "component5",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
