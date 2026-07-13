package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── VipInfo (com.dubox.drive.vip.model.VipInfo) ──────────────────────────────
object VipInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "isVip",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetCountryLogin : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getCurrentLoginCountryEnableVip",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetCountryRegister : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getRegisterCountryEnableVip",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetHasSpacePri : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getVipHasSpacePri",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoIsSub : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "isSub",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoIsSubSpace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "isSubSpaceProduct",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// int getters → 2
object VipInfoGetLevel : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getVipLevel",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetIdentity : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getVipIdentity",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// long getters → 2099 epoch
object VipInfoGetExpireSeconds : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getExpireTimeSeconds",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetEndTimeNoGrace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getVipEndTimeWithoutGrace",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetRenewTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;", name = "getRenewTime",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── MemberInfo (server response) ──────────────────────────────────────────────
object MemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "isVip",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetHasSpacePri : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getHasSpacePri",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetHasIap : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getHasIapRecord",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetLevel : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getVipLevel",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetEndTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getVipEndTime",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetEndTimeNoGrace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getVipEndTimeWithoutGrace",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetLeftTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getVipLeftTime",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetRenewTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;", name = "getRenewTime",
    returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── VolumeMemberInfo ───────────────────────────────────────────────────────────
object VolumeMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/VolumeMemberInfo;", name = "isVip",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── Passport SDK MemberInfo ────────────────────────────────────────────────────
object PassportMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/domain/model/MemberInfo;", name = "isVip",
    returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── VipRightsManager string-anchored gates ─────────────────────────────────────
object VipRightsGateI : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    strings = listOf("type"),
    returnType = "Z", parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipRightsGateJ : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    strings = listOf("NA_STUDIO_CREATE"),
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── Global VipInfo cache gate ──────────────────────────────────────────────────
// 4.18.2: gm0/t.m0()   4.18.6: hm0/t.m0()   4.19.6: ApisKt.J()   4.20.1: ApisKt.L()
object GlobalVipGate4182 : Fingerprint(
    definingClass = "Lgm0/t;", name = "m0", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

object GlobalVipGate4186 : Fingerprint(
    definingClass = "Lhm0/t;", name = "m0", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

object GlobalVipGate4196 : Fingerprint(
    definingClass = "Lcom/dubox/drive/component/ApisKt;", name = "J",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

// 4.20.1: J() renamed to L()
object GlobalVipGate4201 : Fingerprint(
    definingClass = "Lcom/dubox/drive/component/ApisKt;", name = "L",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

// ── Account logout / ban handlers ─────────────────────────────────────────────
object AccountLogout : Fingerprint(
    definingClass = "Lcom/dubox/drive/account/Account;", name = "T",
    parameters = listOf("Landroid/content/Context;"), returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("mContext"))

object AccountBanHandler : Fingerprint(
    definingClass = "Lcom/dubox/drive/util/receiver/BaseResultReceiver;",
    name = "onHandlerAccountBanError",
    parameters = listOf("Ljava/lang/Object;", "I",
        "Lcom/dubox/drive/legacy/ServerBanInfo;", "Landroid/os/Bundle;"),
    returnType = "V")

// ── Account expired gate ────────────────────────────────────────────────────
object AccountExpiredGate : Fingerprint(
    definingClass = "Llq/_;", name = "_____",
    parameters = listOf("Landroid/app/Activity;",
        "Lcom/dubox/drive/kernel/architecture/net/exception/RemoteExceptionInfo;"),
    returnType = "Z", accessFlags = listOf(AccessFlags.PUBLIC))

// ── Passport SDK "invalid signature" error parser ──────────────────────────────
object PassportSignatureErrorParser : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/service/____\$_;",
    name = "__", parameters = listOf("Ljava/lang/String;"),
    returnType = "I", accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL))

// ── Login cracked-APK error display ───────────────────────────────────────────
object AccountFragmentLoginErrorDisplay : Fingerprint(
    definingClass = "Lcom/dubox/drive/login/ui/fragment/AccountFragment\$______;",
    name = "_", parameters = listOf("Ljava/lang/String;", "I", "Ljava/lang/String;"),
    returnType = "V", accessFlags = listOf(AccessFlags.PUBLIC))
