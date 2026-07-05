package app.template.patches.kinestop.pro

import app.morphe.patcher.Fingerprint

private const val KINE_SERVICE_COMPANION = "Lcom/urbandroid/kinestop/KineService\$Companion;"

// KineService.Companion.isTrial(Context) — returns true (trial) when pref "u" != 42.
// When purchased, setTrial(context, false) writes 42 to pref "u".
object IsTrialFingerprint : Fingerprint(
    custom = { method, classDef ->
        classDef.type == KINE_SERVICE_COMPANION && method.name == "isTrial"
    },
)
