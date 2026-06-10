package app.template.patches.twtapp

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val CustomerInfoMapFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/mappers/CustomerInfoMapperKt;",
    name = "map",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    returnType = "Ljava/util/Map;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

val SharedPrefLegacyGetBoolFingerprint = Fingerprint(
    definingClass = "Lu7/F;",
    name = "j",
    parameters = listOf("Ljava/lang/String;", "Lu7/H;"),
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val DataStoreGetBoolFingerprint = Fingerprint(
    definingClass = "Lu7/I;",
    name = "j",
    parameters = listOf("Ljava/lang/String;", "Lu7/H;"),
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

val ProcessResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    parameters = listOf("I", "Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)
