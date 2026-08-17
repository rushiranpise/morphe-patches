package app.template.patches.tiktok_lite.reuse

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Aweme.getDuetSetting()I -- returns 0=allowed, 1=blocked, 3=blocked.
// Source: Toki installReusePermissionPatches. Smali verified: PUBLIC, stable method name.
internal object AwemeDuetSettingFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "getDuetSetting",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "I",
    parameters = emptyList(),
)

// Aweme.getStitchSetting()I -- same semantics as duetSetting, returns 0=allowed.
// Source: Toki installReusePermissionPatches. Smali verified.
internal object AwemeStitchSettingFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;",
    name = "getStitchSetting",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "I",
    parameters = emptyList(),
)
