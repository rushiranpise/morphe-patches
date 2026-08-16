package app.template.patches.trackchecker

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// r43.e() — reads "noads_sub" SharedPref, returns true if subscribed
internal object NoAdsSubFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("noads_sub"),
    custom = { method, classDef ->
        classDef.type == "Lr43;" && method.name == "e"
    },
)

// TC_Application.j() — returns true if ads should show, false if premium/supported
internal object ShouldShowAdsFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("supporter"),
    custom = { method, classDef ->
        classDef.type == "Lcom/metalsoft/trackchecker_mobile/TC_Application;" && method.name == "j"
    },
)
