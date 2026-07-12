package app.template.patches.playit.subscription

import app.morphe.patcher.Fingerprint

// bs/c.j()Z — central VIP feature-gate used by HomeToolBar, dialogs, adapters, etc.
// Checks bs/c.o:Z (in-memory cache), bs/c.p:Z (lifetime_vip GP flag),
// SharedPreferences "vip" expire time, and bs/o.q() (invite VIP days).
// All checks are 100% local — no network calls.
val PlayitVipGateFingerprint = Fingerprint(
    returnType = "Z",
    strings = listOf("_expire_time", "vip"),
    custom = { method, classDef ->
        classDef.type == "Lbs/c;" && method.name == "j"
    }
)

// bs/c.g()Z — VIP status check used by MeFragment.updateVipView() to render the UI badge.
// Iterates Google Play cached Purchase objects looking for "playit_month_2.99",
// "playit_year_9.99", or "lifetime_vip". Also checks SharedPrefs "redeem_code_status".
// All local. Unique strings: "playit_month_2.99" and "lifetime_vip" in same method.
val PlayitVipUiFingerprint = Fingerprint(
    returnType = "Z",
    strings = listOf("playit_month_2.99", "lifetime_vip"),
    custom = { method, classDef ->
        classDef.type == "Lbs/c;" && method.name == "g"
    }
)
