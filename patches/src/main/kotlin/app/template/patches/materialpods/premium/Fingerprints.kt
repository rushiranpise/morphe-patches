package app.template.patches.materialpods.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string

// o5/a.c()Z — reads SharedPreferences key "PRO_MODE_ENABLED" (default false).
// This is the master premium gate used by the entire app's feature list builder (i5/d).
// Fingerprint: public, returns Z, no params, contains const-string "PRO_MODE_ENABLED".
// The class is obfuscated (Lo5/a;) but the string key is stable.
object ProModeEnabledFingerprint : Fingerprint(
    returnType = "Z",
    filters = listOf(
        string("PRO_MODE_ENABLED"),
    )
)
