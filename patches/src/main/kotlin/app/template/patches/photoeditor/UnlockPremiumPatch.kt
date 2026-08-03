package app.template.patches.photoeditor

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PHOTOEDITOR_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Photo Editor premium system overview:
// - Premium and ad-free status is governed by a Software License Cache (SLC)
//   class that persists license data in a SharedPreferences file named "license".
// - The gate method reads the "License.no.advertisement" key from an in-memory
//   HashMap (the cache). If the key is present and non-empty, the user is
//   licensed: ads are hidden and premium features unlock.
// - The SLC class name is obfuscated and shifts every update; the method is always
//   public static, takes a Context, and returns boolean.
//
// Patch strategy:
// - LicenseCacheCheckFingerprint locates the gate method using stable string
//   constants ("license", "License.no.advertisement") and the HashMap.get call
//   rather than the volatile class/method name.
// - returnEarly(true) injects const/4 v0, 0x1 + return v0 at offset 0,
//   unconditionally reporting a valid license before any cache or prefs logic runs.

@Suppress("unused")
val photoeditorUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Photo Editor premium features and removes ads by bypassing the software license cache check.",
) {
    compatibleWith(PHOTOEDITOR_COMPATIBILITY)

    execute {
        LicenseCacheCheckFingerprint.method.returnEarly(true)
    }
}
