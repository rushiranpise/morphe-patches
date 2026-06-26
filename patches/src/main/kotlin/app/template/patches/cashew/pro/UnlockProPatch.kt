package app.template.patches.cashew.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants

// Cashew is a Flutter app (Dart AOT, arm64-v8a). Premium is gated by hidePremiumPopup(), a
// top-level bool function in premiumPage.dart:
//
//   bool hidePremiumPopup() {
//     return premiumPopupEnabled == false ||
//         appStateSettings["purchaseID"] != null ||
//         appStateSettings["previewDemo"] == true;
//   }
//
// Every paywall entry-point (premiumPopupBudgets, premiumPopupObjectives, premiumPopupPastBudgets,
// premiumPopupAddTransaction, premiumPopupPushRoute) calls hidePremiumPopup() first and returns
// immediately if it returns true; PremiumPage and PremiumPopup widgets also skip rendering the
// paywall when it returns true. Forcing it to always return true therefore suppresses all paywalls
// and unlocks every in-app feature gate without touching purchaseID or the store.
//
// The function also prevents the store from resetting purchaseID: initializeStoreAndPurchases()
// gates its entire body (including the `updateSettings("purchaseID", null, ...)` reset) behind
// `if (tryStoreEnabled == true)`. When no store is contacted the subscription stream is never
// set up and purchaseID is never cleared.
//
// Dart AOT (arm64-v8a) encodes true/false returns as:
//   add x0, x22, #0x20  →  0x910082c0  (tagged true,  x22 = Dart null/heap-base register)
//   add x0, x22, #0x30  →  0x9100c2c0  (tagged false)
//   ret                  →  0xd65f03c0
//
// hidePremiumPopup() is identified by its unique 16-byte prologue (4 sequential field loads
// from the settings map via `ldr w1, [x2, #offset]` + `add x1, x1, x28` + `cmp w1, w22` +
// `b.ne <true>`) and its exact call count of 9 in the snapshot. The patch overwrites the first
// 8 bytes with `add x0, x22, #0x20` + `ret`, making the function a one-instruction stub that
// always returns true; the remaining dead instructions after `ret` are left in place.
@Suppress("unused")
val unlockProPatch = rawResourcePatch(
    name = "Unlock Pro",
    description = "Unlock Pro Features in Cashew App",
) {
    compatibleWith(Constants.CASHEW_COMPATIBILITY)

    execute {
        val libPath = "lib/arm64-v8a/libapp.so"
        val lib = get(libPath)
        if (!lib.exists()) {
            throw PatchException(
                "$libPath not found. Apply this patch to a merged universal APK that includes " +
                    "the arm64-v8a native split (merge the .apks with APKEditor or apktools first).",
            )
        }

        val bytes = lib.readBytes()

        // Unique 16-byte prologue of hidePremiumPopup() in the arm64-v8a Dart snapshot.
        // Derived from libapp.so of Cashew 6.5.9 (versionCode 497).
        //   ldr  w1, [x2, #240]   b840f041  — load settings map field (premiumPopupEnabled slot)
        //   add  x1, x1, x28      8b1c8021  — re-tag pointer with heap base (x28)
        //   cmp  w1, w22          6b16003f  — compare to null (x22 = Dart null sentinel)
        //   b.ne <true>           a1010054  — if non-null → already true, short-circuit
        val signature = byteArrayOf(
            0x41, 0xf0.toByte(), 0x40, 0xb8.toByte(),  // ldr w1, [x2, #240]
            0x21, 0x80.toByte(), 0x1c, 0x8b.toByte(),  // add x1, x1, x28
            0x3f, 0x00, 0x16, 0x6b,                    // cmp w1, w22
            0xa1.toByte(), 0x01, 0x00, 0x54,            // b.ne +0x34 (true branch)
        )

        val match = bytes.findUnique(signature)
            ?: throw PatchException(
                "hidePremiumPopup() signature not found in $libPath. This patch targets Cashew " +
                    "6.5.9 (versionCode 497); the Dart snapshot changed in this build and the " +
                    "signature must be re-derived.",
            )

        // Overwrite prologue with: add x0, x22, #0x20 (true) + ret
        // Dart tagged true  = add x0, x22, #0x20 → 0x910082c0
        // AArch64 ret       = 0xd65f03c0
        val patch = byteArrayOf(
            0xc0.toByte(), 0x82.toByte(), 0x00, 0x91.toByte(), // add x0, x22, #0x20 (true)
            0xc0.toByte(), 0x03, 0x5f, 0xd6.toByte(),          // ret
        )
        patch.copyInto(bytes, match)
        lib.writeBytes(bytes)
    }
}

private fun ByteArray.findUnique(pattern: ByteArray): Int? {
    var found: Int? = null
    val last = size - pattern.size
    outer@ for (i in 0..last) {
        for (j in pattern.indices) if (this[i + j] != pattern[j]) continue@outer
        if (found != null) throw PatchException(
            "hidePremiumPopup() signature matched more than once in libapp.so — too ambiguous to patch safely.",
        )
        found = i
    }
    return found
}
