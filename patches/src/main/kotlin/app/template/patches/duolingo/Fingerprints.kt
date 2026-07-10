package app.template.patches.duolingo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.AccessFlags

object SubscriptionInfoConstructorFingerprint : Fingerprint(
    definingClass = "Lcom/duolingo/data/plus/SubscriptionInfo;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "J",
        "Z",
        "I",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    strings = listOf(),
)

object UserSubscriptionInfoFingerprint : Fingerprint(
    definingClass = "Lcom/duolingo/data/user/User;",
    name = "r",
    returnType = "Lcom/duolingo/data/plus/SubscriptionInfo;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

object HasMaxUserInfoConstructorFingerprint : Fingerprint(
    definingClass = "Ldre;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Z", "J"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

object BuildTargetFieldFingerprint : Fingerprint(
    strings = listOf("BUILD_TARGET", "debug", "release"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    ),
)
