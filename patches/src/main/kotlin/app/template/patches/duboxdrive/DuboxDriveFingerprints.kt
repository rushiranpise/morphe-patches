package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val VIP_INFO = "Lcom/dubox/drive/vip/model/VipInfo;"
private const val MEMBER_INFO = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;"
private const val VOLUME_MEMBER_INFO = "Lcom/dubox/drive/vip/domain/job/server/response/VolumeMemberInfo;"

// VipInfo.isVip()Z
object VipInfoIsVipFingerprint : Fingerprint(
    definingClass = VIP_INFO,
    name = "isVip",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VipInfo.getVipLevel()I — 1 = VIP, 2 = SVIP/Premium+
object VipInfoGetVipLevelFingerprint : Fingerprint(
    definingClass = VIP_INFO,
    name = "getVipLevel",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VipInfo.getVipIdentity()I
object VipInfoGetVipIdentityFingerprint : Fingerprint(
    definingClass = VIP_INFO,
    name = "getVipIdentity",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.isVip()I
object MemberInfoIsVipFingerprint : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "isVip",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.getVipLevel()I
object MemberInfoGetVipLevelFingerprint : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "getVipLevel",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VolumeMemberInfo.isVip()I
object VolumeMemberInfoIsVipFingerprint : Fingerprint(
    definingClass = VOLUME_MEMBER_INFO,
    name = "isVip",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

private const val VIP_RIGHTS_MANAGER = "Lcom/dubox/drive/vip/manager/VipRightsManager;"

// VipRightsManager.i(String)Z — generic privilege gate (e.g. speedUpload)
object VipRightsI : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "i",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsJ : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "j",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsK : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "k",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsL : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "l",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsZ : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "z",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsA : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "A",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsB : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "B",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

object VipRightsC : Fingerprint(
    definingClass = VIP_RIGHTS_MANAGER,
    name = "C",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VipInfo.getExpireTimeSeconds()J
object VipInfoGetExpireTimeSeconds : Fingerprint(
    definingClass = VIP_INFO,
    name = "getExpireTimeSeconds",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VipInfo.getVipEndTimeWithoutGrace()J
object VipInfoGetVipEndTimeWithoutGrace : Fingerprint(
    definingClass = VIP_INFO,
    name = "getVipEndTimeWithoutGrace",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.getVipEndTime()J
object MemberInfoGetVipEndTime : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "getVipEndTime",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.getVipEndTimeWithoutGrace()J
object MemberInfoGetVipEndTimeWithoutGrace : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "getVipEndTimeWithoutGrace",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.getVipLeftTime()J
object MemberInfoGetVipLeftTime : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "getVipLeftTime",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// gm0.t.m0()Z — global cached-VipInfo isVip gate (used for ads/mediation badges)
object Gm0TM0Fingerprint : Fingerprint(
    definingClass = "Lgm0/t;",
    name = "m0",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// VipInfo.getRenewTime()J
object VipInfoGetRenewTime : Fingerprint(
    definingClass = VIP_INFO,
    name = "getRenewTime",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo.getRenewTime()J
object MemberInfoGetRenewTime : Fingerprint(
    definingClass = MEMBER_INFO,
    name = "getRenewTime",
    returnType = "J",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
