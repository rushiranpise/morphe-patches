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

// hm0.t.m0()Z — global cached-VipInfo isVip gate (4.18.6+, renamed from gm0/t)
object Hm0TM0Fingerprint : Fingerprint(
    definingClass = "Lhm0/t;",
    name = "m0",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// Account.T(Context)V — master logout method; sends ACTION_LOGOUT broadcast
// Anchored by "mContext" param null-check string, unique to this method in Account class
object AccountLogoutFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/account/Account;",
    name = "T",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("mContext")
)


// QuotaExtraInfo.isTimeLimitQuotaType()Z — root gate for all expiry dialogs (a3, k0).
// Stable class path, unique string anchor "permanent_30g_temp_994g".
// Patching here covers ww/xw/_____.m() and AboutMeFragment without class-path guessing.
object QuotaIsTimeLimitTypeFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/cloudfile/io/model/QuotaExtraInfo;",
    name = "isTimeLimitQuotaType",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("permanent_30g_temp_994g")
)

// nu/m (4.18.2) / ou/m (4.18.6+) -- strings-only, class path changes each version
object BackupGuideShownAFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("prefix_buckup_guide_")
)

object BackupGuideShownBFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    strings = listOf("prefix_buckup_new_guide_v2")
)

// i0._()Z — "new user subscription guide not shown" MMKV check
// Returns true (not shown) on cold start → f3.g() shows subscription/expired screen
object NewUserSubGuideFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/ui/tutorial/i0;",
    name = "_",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("is_show_new_user_sub_guide_activity")
)

// i0.__(Context)V — launches SubscribeActivity ("account expired/subscribe" screen)
// Stable class path; unique: only V-returning static method with Context param on i0.
object SubscribeActivityLaunchFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/ui/tutorial/i0;",
    name = "__",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// tutorial/k._(Context, String)V — launches PlusSubscribeGuideActivity
// Called from BuckupSettingGuideActivity and f3.g(); noop to suppress.
object PlusSubscribeGuideActivityLaunchFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/ui/tutorial/k;",
    name = "_",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// az/_.a(Activity)Z (4.18.2) / bz/_.a(Activity)Z (4.18.6+) — "Account has expired" dialog
// No const-string in the method itself — use definingClass + signature, dual-version.
// az/_.____() (4.18.2) / bz/_.____() (4.18.6+) — error dispatch method
// packed-switch base -0x181ca routes error -98761 to az/_.a() "Account has expired" dialog.
// Use custom lambda: exact class+name match, no fragile string anchor.
object AccountExpiredDialogA182Fingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, classDef ->
        classDef.type == "Laz/_;" && method.name == "____"
    }
)

object AccountExpiredDialogA186Fingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, classDef ->
        classDef.type == "Lbz/_;" && method.name == "____"
    }
)

// MainActivity.showBuckupSettingGuideActivity()V — launches BuckupSettingGuideActivity on cold start
// Anchored by const-class BuckupSettingGuideActivity — unique to this method.
object ShowBuckupSettingGuideFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/ui/MainActivity;",
    name = "showBuckupSettingGuideActivity",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

// MainActivity.checkStartAutoBackupGuideActivity()V
// Our VipRightsManager.i() returning true triggers BuckupSettingGuideActivity via this method.
// Noop it to always skip to showMainView().
object CheckStartAutoBackupGuideFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/ui/MainActivity;",
    name = "checkStartAutoBackupGuideActivity",
    returnType = "V",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL)
)

// OtherRouter.M0(Context, ce0/__, Z)V — shows a3 expire dialog from FCM push notifications
// (storagewillexpirealert / storagedidexpirealert routes). Noop to block.
object OtherRouterExpireDialogFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/router/router/OtherRouter;",
    name = "M0",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Lce0/__;", "Z"),
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    strings = listOf("getSupportFragmentManager(...)")
)

// az/g.____(I)Z (4.18.2) / bz/g.____(I)Z (4.18.6+) — errno 132 (0x84) checker
// Server returns errno 132 on session/token expiry → triggers "Account has expired" dialog.
// Return false so errno 132 is never treated as an expiry error.
object Errno132CheckerA182Fingerprint : Fingerprint(
    definingClass = "Laz/g;",
    name = "____",
    returnType = "Z",
    parameters = listOf("I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

object Errno132CheckerA186Fingerprint : Fingerprint(
    definingClass = "Lbz/g;",
    name = "____",
    returnType = "Z",
    parameters = listOf("I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

// VipInfo additional boolean getters needed for full Premium+
object VipInfoGetCurrentLoginCountryEnableVip : Fingerprint(
    definingClass = VIP_INFO, name = "getCurrentLoginCountryEnableVip", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
object VipInfoGetRegisterCountryEnableVip : Fingerprint(
    definingClass = VIP_INFO, name = "getRegisterCountryEnableVip", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
object VipInfoGetVipHasSpacePri : Fingerprint(
    definingClass = VIP_INFO, name = "getVipHasSpacePri", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
object VipInfoIsSub : Fingerprint(
    definingClass = VIP_INFO, name = "isSub", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
object VipInfoIsSubSpaceProduct : Fingerprint(
    definingClass = VIP_INFO, name = "isSubSpaceProduct", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// MemberInfo additional boolean getters
object MemberInfoGetHasSpacePri : Fingerprint(
    definingClass = MEMBER_INFO, name = "getHasSpacePri", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
object MemberInfoGetHasIapRecord : Fingerprint(
    definingClass = MEMBER_INFO, name = "getHasIapRecord", returnType = "Z",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// Passport SDK MemberInfo.isVip()I
object PassportMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/domain/model/MemberInfo;",
    name = "isVip", returnType = "I",
    parameters = emptyList(), accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// VipRightsManager.i(String)Z — main privilege gate, anchored by "type" null-check string
object VipRightsGateIFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("type")
)

// VipRightsManager.j()Z — NA_STUDIO_CREATE gate
object VipRightsGateJFingerprint : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("NA_STUDIO_CREATE")
)

// az/_._____(Activity, RemoteExceptionInfo)Z (4.18.2) — handles ServerBanInfo
// Server sends banCode=-98761 to trigger "Account has expired" dialog.
// Noop returns false immediately before dialog shows.
object ServerBanHandlerA182Fingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "Lcom/dubox/drive/kernel/architecture/net/exception/RemoteExceptionInfo;"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, classDef ->
        classDef.type == "Laz/_;" && method.name == "_____"
    }
)

// bz/_._____(Activity, RemoteExceptionInfo)Z (4.18.6+)
object ServerBanHandlerA186Fingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "Lcom/dubox/drive/kernel/architecture/net/exception/RemoteExceptionInfo;"),
    accessFlags = listOf(AccessFlags.PUBLIC),
    custom = { method, classDef ->
        classDef.type == "Lbz/_;" && method.name == "_____"
    }
)
