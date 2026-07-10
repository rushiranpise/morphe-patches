package app.template.patches.trackchecker

import app.morphe.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// r43.e() — reads "noads_sub" SharedPref, returns true if subscribed
internal val noAdsSubFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
    returns("Z")
    parameters()
    strings("noads_sub")
    custom { methodDef, classDef ->
        classDef.type == "Lr43;" && methodDef.name == "e"
    }
}

// TC_Application.j() — returns true if ads should show, false if premium/supported
internal val shouldShowAdsFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("Z")
    parameters()
    strings("supporter")
    custom { methodDef, classDef ->
        classDef.type == "Lcom/metalsoft/trackchecker_mobile/TC_Application;" && methodDef.name == "j"
    }
}
