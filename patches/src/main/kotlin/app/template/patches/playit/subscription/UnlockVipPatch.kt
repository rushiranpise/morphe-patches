package app.template.patches.playit.subscription

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PLAYIT_COMPATIBILITY

@Suppress("unused")
val playitUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlock VIP subscription in app.",
    default = true
) {
    compatibleWith(PLAYIT_COMPATIBILITY)

    execute {
        // Gate 1: bs/c.j()Z — feature gate used by HomeToolBar, dialogs, skin previews,
        //   ad suppression, download limits etc.
        //   Checks: bs/c.o:Z cache → bs/c.p:Z lifetime_vip GP → SharedPrefs vip expire → bs/o.q() invite days
        PlayitVipGateFingerprint.match().method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // Gate 2: bs/c.g()Z — UI badge check used by MeFragment.updateVipView()
        //   Iterates cached Google Play Purchase objects for "playit_month_2.99",
        //   "playit_year_9.99", "lifetime_vip"; also checks SharedPrefs redeem_code_status.
        //   Without this patch the profile tab still shows "Free" even with Gate 1 patched.
        PlayitVipUiFingerprint.match().method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")
    }
}
