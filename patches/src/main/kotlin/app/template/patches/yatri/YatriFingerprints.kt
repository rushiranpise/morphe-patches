package app.template.patches.yatri

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object UserActivePlanStatusFingerprint : Fingerprint(
    definingClass = "Lcom/yatrirailways/yatri/utils/preference/UserSharedPreference;",
    name = "getUserActivePlanStatus",
    returnType = "Ljava/lang/Boolean;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object ActivePlanDaoFingerprint : Fingerprint(
    definingClass = "Lcom/yatrirailways/yatri/db/dao/ActivePlansDao_Impl;",
    name = "getActivePlan",
    returnType = "Lcom/yatrirailways/yatri/db/data_models/ActivePlanModel;",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
