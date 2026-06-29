package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

private const val VIP_INFO = "Lcom/dubox/drive/vip/model/VipInfo;"
private const val MEMBER_INFO = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;"
private const val VOLUME_MEMBER_INFO = "Lcom/dubox/drive/vip/domain/job/server/response/VolumeMemberInfo;"
private const val VIP_RIGHTS = "Lcom/dubox/drive/vip/manager/VipRightsManager;"
private const val BASE_RECEIVER = "Lcom/dubox/drive/util/receiver/BaseResultReceiver;"

// ── VipInfo getters ──────────────────────────────────────────────────────────
object VipInfoIsVip : Fingerprint(definingClass = VIP_INFO, name = "isVip",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetLevel : Fingerprint(definingClass = VIP_INFO, name = "getVipLevel",
    returnType = "I", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetIdentity : Fingerprint(definingClass = VIP_INFO, name = "getVipIdentity",
    returnType = "I", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetExpireSeconds : Fingerprint(definingClass = VIP_INFO, name = "getExpireTimeSeconds",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetEndTimeNoGrace : Fingerprint(definingClass = VIP_INFO, name = "getVipEndTimeWithoutGrace",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetRenewTime : Fingerprint(definingClass = VIP_INFO, name = "getRenewTime",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetCountryLogin : Fingerprint(definingClass = VIP_INFO, name = "getCurrentLoginCountryEnableVip",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetCountryRegister : Fingerprint(definingClass = VIP_INFO, name = "getRegisterCountryEnableVip",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoGetHasSpacePri : Fingerprint(definingClass = VIP_INFO, name = "getVipHasSpacePri",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoIsSub : Fingerprint(definingClass = VIP_INFO, name = "isSub",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object VipInfoIsSubSpace : Fingerprint(definingClass = VIP_INFO, name = "isSubSpaceProduct",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── MemberInfo getters ───────────────────────────────────────────────────────
object MemberInfoIsVip : Fingerprint(definingClass = MEMBER_INFO, name = "isVip",
    returnType = "I", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetLevel : Fingerprint(definingClass = MEMBER_INFO, name = "getVipLevel",
    returnType = "I", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetEndTime : Fingerprint(definingClass = MEMBER_INFO, name = "getVipEndTime",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetEndTimeNoGrace : Fingerprint(definingClass = MEMBER_INFO, name = "getVipEndTimeWithoutGrace",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetLeftTime : Fingerprint(definingClass = MEMBER_INFO, name = "getVipLeftTime",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetRenewTime : Fingerprint(definingClass = MEMBER_INFO, name = "getRenewTime",
    returnType = "J", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetHasSpacePri : Fingerprint(definingClass = MEMBER_INFO, name = "getHasSpacePri",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
object MemberInfoGetHasIap : Fingerprint(definingClass = MEMBER_INFO, name = "getHasIapRecord",
    returnType = "Z", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── VolumeMemberInfo ─────────────────────────────────────────────────────────
object VolumeMemberInfoIsVip : Fingerprint(definingClass = VOLUME_MEMBER_INFO, name = "isVip",
    returnType = "I", parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── Passport SDK MemberInfo ──────────────────────────────────────────────────
object PassportMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/domain/model/MemberInfo;",
    name = "isVip", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ── VipRightsManager gates ───────────────────────────────────────────────────
// i(String)Z — main privilege gate, "type" = param null-check anchor (stable)
object VipRightsGateI : Fingerprint(definingClass = VIP_RIGHTS, returnType = "Z",
    parameters = listOf("Ljava/lang/String;"), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("type"))
// j()Z — NA_STUDIO_CREATE privilege
object VipRightsGateJ : Fingerprint(definingClass = VIP_RIGHTS, returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("NA_STUDIO_CREATE"))

// ── Global VipInfo cache gate (class path changes each version) ───────────────
// 4.18.2: gm0/t.m0()   4.18.6: hm0/t.m0()   4.19.6+: ApisKt.J()
object GlobalVipGate4182 : Fingerprint(definingClass = "Lgm0/t;", name = "m0",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))
object GlobalVipGate4186 : Fingerprint(definingClass = "Lhm0/t;", name = "m0",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))
// 4.19.6+: moved to ApisKt top-level file, stable class name
object GlobalVipGate4196 : Fingerprint(
    definingClass = "Lcom/dubox/drive/component/ApisKt;", name = "J",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

// ── Account.T(Context)V — master logout ──────────────────────────────────────
// "mContext" is the param null-check string, unique to this method in Account class.
object AccountLogout : Fingerprint(
    definingClass = "Lcom/dubox/drive/account/Account;", name = "T",
    returnType = "V", parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("mContext"))

// ── BaseResultReceiver.onHandlerAccountBanError — server ban dialog trigger ──
// Stable class+method name. Server sends banCode=-98761 → this shows "Account has expired".
// "ACCOUNT_BAN_ERROR" is a unique string in this method via ErrorType enum sget.
object AccountBanHandler : Fingerprint(
    definingClass = BASE_RECEIVER,
    name = "onHandlerAccountBanError",
    returnType = "V",
    parameters = listOf("Ljava/lang/Object;", "I",
        "Lcom/dubox/drive/legacy/ServerBanInfo;", "Landroid/os/Bundle;"),
    accessFlags = listOf(AccessFlags.PRIVATE))

// ── Account expired gate — lq/_._____(Activity, RemoteExceptionInfo)Z ──────────
// Called by all API receivers when server returns ServerBanInfo after login.
// Returns Z=false = "not handled" so caller skips the expired dialog.
object AccountExpiredGate : Fingerprint(
    definingClass = "Llq/_;",
    name = "_____",
    parameters = listOf(
        "Landroid/app/Activity;",
        "Lcom/dubox/drive/kernel/architecture/net/exception/RemoteExceptionInfo;",
    ),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
)

// ── Cracked APK login block — "current version carries a risk" ─────────────────
// Passport SDK parses server response body for "invalid signature" → returns 0x970ff7
// → OnThirdLoginResultListener → AccountFragment$______._() → shows cracked dialog

// Root fix: passport SDK error parser (private, called via synthetic wrapper)
object PassportSignatureErrorParser : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/service/____\$_;",
    name = "__",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "I",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

// Safety net: login error display method in AccountFragment
object AccountFragmentLoginErrorDisplay : Fingerprint(
    definingClass = "Lcom/dubox/drive/login/ui/fragment/AccountFragment\$______;",
    name = "_",
    parameters = listOf("Ljava/lang/String;", "I", "Ljava/lang/String;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
