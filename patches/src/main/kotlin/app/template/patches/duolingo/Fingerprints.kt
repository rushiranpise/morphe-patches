package app.template.patches.duolingo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
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

object LoggedInStateFingerprint : Fingerprint(
    strings = listOf("LoggedIn(user=", ")"),
)

object UserFingerprint : Fingerprint(
    strings = listOf("User(adsConfig=", ", id=", ", betaStatus="),
)

object UserIsPaidFieldUsageFingerprint : Fingerprint(
    parameters = listOf(
        "Lcom/duolingo/data/user/User;",
        "Lcom/duolingo/data/home/CoursePathInfo;",
        "Z",
        "Lcom/duolingo/core/experiments/ExperimentsRepository\$TreatmentRecord;",
    ),
    returnType = "Z",
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/duolingo/data/user/User;",
            type = "Z",
        ),
    ),
)

object HasMaxUserInfoConstructorFingerprint : Fingerprint(
    definingClass = "Ldre;",
    name = "<init>",
    returnType = "V",
    parameters = listOf("Z", "J"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

object UserHasGoldFieldUsageFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("MaxHooksUserData(isAdmin="),
    ),
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/duolingo/data/user/User;",
            type = "Z",
        ),
    ),
)

object VideoCallTabCtaButtonStateToStringFingerprint : Fingerprint(
    strings = listOf("VideoCallTabCtaButtonState(userHasMax="),
    filters = listOf(
        string(", isEligibleForSecondaryUpsell="),
        OpcodesFilter.opcodesToFilters(Opcode.IGET_BOOLEAN).first(),
    ),
)

object BuildTargetFieldFingerprint : Fingerprint(
    strings = listOf("BUILD_TARGET", "debug", "release"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    ),
)
