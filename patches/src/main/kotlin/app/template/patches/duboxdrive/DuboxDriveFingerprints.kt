package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ══ VipInfo (com.dubox.drive.vip.model.VipInfo) ══════════════════════════════
// Kotlin data class used throughout the app to represent VIP status.
// All getters are stable non-obfuscated property accessors — safe in fingerprints.
// DEX: classes12  (v4.22.1)

// Boolean getters → returnEarly(true)
object VipInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "isVip", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetCountryLogin : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getCurrentLoginCountryEnableVip", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetCountryRegister : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getRegisterCountryEnableVip", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetHasSpacePri : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getVipHasSpacePri", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoIsSub : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "isSub", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoIsSubSpace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "isSubSpaceProduct", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// New in v4.22.1: additional boolean getters on VipInfo
object VipInfoGetHasIapRecord : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getHasIapRecord", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetCanTrial : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getCanTrial", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// Int getters → 2 (SVIP level)
object VipInfoGetLevel : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getVipLevel", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetIdentity : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getVipIdentity", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// Long getters → 2099 epoch (seconds for VipInfo, millis for MemberInfo)
object VipInfoGetExpireSeconds : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getExpireTimeSeconds", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetEndTimeNoGrace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getVipEndTimeWithoutGrace", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object VipInfoGetRenewTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/model/VipInfo;",
    name = "getRenewTime", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ══ MemberInfo (server response model) ════════════════════════════════════════
// DEX: classes12

object MemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "isVip", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetHasSpacePri : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getHasSpacePri", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetHasIap : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getHasIapRecord", returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetLevel : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getVipLevel", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetEndTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getVipEndTime", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetEndTimeNoGrace : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getVipEndTimeWithoutGrace", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetLeftTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getVipLeftTime", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

object MemberInfoGetRenewTime : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;",
    name = "getRenewTime", returnType = "J", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ══ VolumeMemberInfo ══════════════════════════════════════════════════════════
// DEX: classes12
object VolumeMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/domain/job/server/response/VolumeMemberInfo;",
    name = "isVip", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ══ Passport SDK MemberInfo ═══════════════════════════════════════════════════
// DEX: classes2  (never obfuscated — third-party SDK)
object PassportMemberInfoIsVip : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/domain/model/MemberInfo;",
    name = "isVip", returnType = "I", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ══ VipRightsManager string-anchored gates ════════════════════════════════════
// DEX: classes12
// Method names (I, j) are obfuscated but the string anchors are developer-defined
// feature-flag keys — stable across updates. The catch-all scanner in the patch
// body handles any newly added ()Z methods without needing explicit fingerprints.

// I(String)Z — the general rights gate that checks a privilege "type" key
object VipRightsGateByType : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    strings = listOf("type"),
    returnType = "Z", parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// j()Z — the NA_STUDIO_CREATE feature gate
object VipRightsGateNaStudio : Fingerprint(
    definingClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;",
    strings = listOf("NA_STUDIO_CREATE"),
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))

// ══ Global VipInfo cache gate (ApisKt) ════════════════════════════════════════
// This method name changes nearly every release. Using definingClass+name only —
// the class is non-obfuscated so definingClass is stable.
// History: 4.18.2: gm0/t.m0  4.18.6: hm0/t.m0  4.19.6: ApisKt.J  4.20.1: ApisKt.L
// 4.22.1: ApisKt.L (unchanged — verified from classes10/ApisKt.smali)
object GlobalVipGate : Fingerprint(
    definingClass = "Lcom/dubox/drive/component/ApisKt;",
    name = "L",
    returnType = "Z", parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL))

// ══ Account logout handler ════════════════════════════════════════════════════
// Account.V(Context)V — sends "com.dubox.drive.ACTION_LOGOUT" broadcast.
// Method name changed: T(Context)V in v4.20.1 → V(Context)V in v4.22.1.
// Anchored on string "mContext" (Kotlin null-check) + "com.dubox.drive.ACTION_LOGOUT"
// broadcast action — both stable developer strings.
// DEX: classes9
object AccountLogout : Fingerprint(
    definingClass = "Lcom/dubox/drive/account/Account;",
    name = "V",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("mContext"))

// ══ Account ban handler ═══════════════════════════════════════════════════════
// BaseResultReceiver.onHandlerAccountBanError — suppress ban dialog.
// Non-obfuscated method name in a named class — stable.
// DEX: classes12
object AccountBanHandler : Fingerprint(
    definingClass = "Lcom/dubox/drive/util/receiver/BaseResultReceiver;",
    name = "onHandlerAccountBanError",
    parameters = listOf(
        "Ljava/lang/Object;", "I",
        "Lcom/dubox/drive/legacy/ServerBanInfo;", "Landroid/os/Bundle;"),
    returnType = "V")

// ══ Passport SDK error parser ═════════════════════════════════════════════════
// ____$_.__(String)I — maps "invalid signature" server string → 0x970ff5
// which the login UI shows as "current version carries a risk" blocking login.
// Class name (____$_) and method name (__) are obfuscated, but the class path
// is stable (passport SDK, never relocates) and the companion-object inner-class
// pattern is consistent across versions.
// Verified in v4.22.1: classes2/com/mars/united/international/passport/service/____$_.smali
object PassportSignatureErrorParser : Fingerprint(
    definingClass = "Lcom/mars/united/international/passport/service/____\$_;",
    name = "__",
    parameters = listOf("Ljava/lang/String;"),
    returnType = "I",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL))

// ══ Account common error handler ══════════════════════════════════════════════
// BaseResultReceiver.onHandlerAccountCommonError(Object, I, Bundle)V —
// triggered by server error codes -6 and -25 (isAccountCommonError).
// These are generic auth-failure codes; the server sends the display message
// (including "account has expired") in the Bundle key
// "com.dubox.drive.server_alert_message". The handler shows a dialog with that
// message and then calls onFailed() which routes to the login/setup screen.
//
// Fix: return-void — suppresses the expired-account dialog and the routing.
// The non-obfuscated method name in a named class makes this fingerprint stable.
// DEX: classes12
object AccountCommonError : Fingerprint(
    definingClass = "Lcom/dubox/drive/util/receiver/BaseResultReceiver;",
    name = "onHandlerAccountCommonError",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Ljava/lang/Object;", "I", "Landroid/os/Bundle;"),
    returnType = "V")

// ══ OnLoginCallBack failure handler ══════════════════════════════════════════
// AccountStartup$__._() — the OnLoginCallBack._(int type) implementation that
// fires when autoLogin() fails at cold start (errorCode=2 = not licensed).
//
// What _() does on failure:
//   1. ___.e(context)             — starts login/setup Activity (the routing)
//   2. rm0/______.___()           — clears account key from secure store
//   3. AccountStartup.g(context)  — launches loginAsync background coroutine
//   4. AccountStartup.______()/_____() + postDelayed — background sync tasks
//
// return-void at index 0 kills all of the above in one patch.
//
// Stable anchor: implements OnLoginCallBack; single _(I)V method in class.
// definingClass path is compiler-generated from AccountStartup single anon
// OnLoginCallBack — stable across minor updates.
// DEX: classes10
object OnLoginCallBackFailure : Fingerprint(
    definingClass = "Lcom/dubox/drive/initialize/AccountStartup\$__;",
    name = "_",
    parameters = listOf("I"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL))
