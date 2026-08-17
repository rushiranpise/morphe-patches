package app.template.patches.superfile

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SUPER_FILE_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Super File (com.esuper.file.explorer) v1.5.6.3
//
// CHANGED FROM PREVIOUS VERSION:
//   Old: two-fingerprint strategy (IsSubscribedFingerprint + SubscriptionTokenCheckFingerprint),
//        both using obfuscated class names (SubscriptionManager.m, ih7.q) — both broke when
//        ih7 was renamed to rh7 and m()Z was replaced with a private helper.
//   New: single fingerprint on SubscriptionManager.p()Z — the sole public isPremium gate,
//        anchored purely on the non-obfuscated definingClass. returnEarly(true) is safe
//        (no monitor, no try/catch). rh7.u()Z is only called through p(), so one patch suffices.

@Suppress("unused")
val superFileUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium and lifetime features in Super File.",
    default = true,
) {
    compatibleWith(SUPER_FILE_COMPATIBILITY)

    execute {
        IsSubscribedFingerprint.method.returnEarly(true)
    }
}
