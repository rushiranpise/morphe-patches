package app.template.patches.camscanner

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// AccountPreference.o8()Z — reads qp3sdjd30renew02sd pref key, returns true if value == 1
// Smali: classes15 — stable: definingClass non-obfuscated, string key, Z return, no params
object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    strings = listOf("qp3sdjd30renew02sd")
)

// AccountPreference.〇O888o0o()J — reads qp3sdjd79xhdas02sd pref key, returns long status code
// Smali: classes15 — stable: definingClass non-obfuscated, string key, J return, no params
object GetStatusCodeFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "J",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    strings = listOf("qp3sdjd79xhdas02sd")
)

// VipUtil.〇o00〇〇Oo(Context)Z — checks SharedPreferences for 5c65f9ecd002f4af+uid key
// Smali: classes14 — stable: definingClass non-obfuscated, string key, Context param, Z return
object IsVipUserFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/util/VipUtil;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("5c65f9ecd002f4af"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getInt"
        )
    )
)

// PirateAppControl.o〇0()Z — private no-param Z, calls AppUtil.o〇O()Z
// Smali: classes11 — fingerprint by definingClass (non-obfuscated) + Z + no params + private
object IsPirateAppFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/business/PirateAppControl;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC),
    parameters = listOf()
)

// AccountPreference.〇o(Context)Z — Flutter pigeon isVip, queries content provider for account_state == 1
// Smali: classes15 — stable: definingClass non-obfuscated, Context param, Z return, string "account_state"
object IsVipContextFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("account_state")
)

// AccountPreference.〇O8o08O()Z — reads KEY_SYNC boolean pref via PreferenceUtil.O8(String,Z)Z
// Smali: classes15 — "KEY_SYNC" is unique in AccountPreference; no SharedPreferences involved here
object IsKeySyncFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    strings = listOf("KEY_SYNC")
)

// SyncUtil.isNetworkAvailable(Context)Z — stable non-obfuscated class + connectivity string
object IsNetworkAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/tsapp/sync/SyncUtil;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("connectivity")
)

// SyncThread.Oo(Z)V — sends LocalBroadcast on server 401; private non-static, Z param, void return
// Smali: classes14 — stable string anchor + LocalBroadcastManager send call
object SendReLoginBroadcastFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/tsapp/sync/SyncThread;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Z"),
    strings = listOf("ReLoginSyncThread:login error, need relogin, isPwdWrong = ")
)

// MainHomeLifecycleObserver.o〇O8〇〇o(MainActivity,Z)V — launches ReLoginDialogActivity
// Smali: classes13 — stable: definingClass non-obfuscated, MainActivity+Z params, is_pwd_wrong string
object LaunchReLoginDialogFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/mainmenu/mainactivity/MainHomeLifecycleObserver;",
    returnType = "V",
    parameters = listOf("Lcom/intsig/camscanner/mainmenu/mainactivity/MainActivity;", "Z"),
    strings = listOf("is_pwd_wrong")
)
