package app.template.patches.snipd

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// CustomerInfo map mapper - converts CustomerInfo to a Map sent to Flutter
val CustomerInfoMapFingerprint = Fingerprint(
    definingClass = "Lcom/revenuecat/purchases/hybridcommon/mappers/CustomerInfoMapperKt;",
    name = "map",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    returnType = "Ljava/util/Map;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)