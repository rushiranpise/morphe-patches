package app.template.patches.udisc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags

val UserAccountClassFingerprint = Fingerprint(
    strings = listOf("Trial", "Pro", "Basic"),
)

val WatchAccountProFingerprint = Fingerprint(
    definingClass = "WatchAccountInfo",
    name = "<init>",
    filters = listOf(
        fieldAccess(name = "isPro"),
    ),
)

val UDiscApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/application/UDiscApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
)

val PlayBillingPurchaseListenerFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/billing/b;",
    name = "a",
    returnType = "V",
    parameters = listOf("La70/b;", "Ljava/util/List;"),
    strings = listOf("acknowledged"),
)

val AccountSubscriptionConstructorFingerprint = Fingerprint(
    accessFlags = listOf(
        AccessFlags.PUBLIC,
        AccessFlags.SYNTHETIC,
        AccessFlags.CONSTRUCTOR,
    ),
    definingClass = "Lvy/n0;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "I",
        "Lcom/udisc/kmp/account/Account\$Subscription\$Platform;",
        "Lcom/udisc/kmp/account/Account\$Subscription\$Status;",
        "Ljava/lang/String;",
    ),
)
