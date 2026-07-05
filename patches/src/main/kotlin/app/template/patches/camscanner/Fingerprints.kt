package app.template.patches.camscanner

import app.morphe.patcher.Fingerprint

object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "Z",
    strings = listOf("qp3sdjd30renew02sd")
)

object GetStatusCodeFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "J",
    strings = listOf("qp3sdjd79xhdas02sd")
)

object IsVipUserFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/util/VipUtil;",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    strings = listOf("5c65f9ecd002f4af")
)

object IsPirateAppFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/business/PirateAppControl;",
    returnType = "Z",
    parameters = listOf()
)

// Flutter pigeon isVip: queries DB for account_state == 1
object IsVipContextFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    strings = listOf("account_state")
)

object IsKeySyncFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/comm/account_data/AccountPreference;",
    returnType = "Z",
    strings = listOf("KEY_SYNC")
)

object IsSkipLoggingFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/log/LogAgentHelper;",
    parameters = listOf(),
    returnType = "Z"
)

object IsNetworkAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/tsapp/sync/SyncUtil;",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z",
    strings = listOf("connectivity")
)

// Sends relogin broadcast when server returns 401 (fake UID causes this)
object SendReLoginBroadcastFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/tsapp/sync/SyncThread;",
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("ReLoginSyncThread:login error, need relogin, isPwdWrong = ")
)

// Launches ReLoginDialogActivity
object LaunchReLoginDialogFingerprint : Fingerprint(
    definingClass = "Lcom/intsig/camscanner/mainmenu/mainactivity/MainHomeLifecycleObserver;",
    returnType = "V",
    strings = listOf("is_pwd_wrong")
)
