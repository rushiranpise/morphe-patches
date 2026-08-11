package app.template.patches.shareit.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.SHAREIT_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ── Background ────────────────────────────────────────────────────────────────
//
// libXXX.so contains a two-arm signature tamper check:
//
//   Arm 1 (Java/DEX, com.base.core.x.d.c):
//     Reads expected cert fingerprints from obfuscated asset, computes
//     installed APK signing cert MD5, calls ArrayList.contains() to compare.
//     On mismatch: reflective startActivity(kill Intent) + sleep(20000).
//     → Bypassed by TamperCheckFingerprint.method.returnEarly() in SHAREitPremiumPatch.
//
//   Arm 2 (native, libXXX.so):
//     JNI_OnLoad → x_check() → MD5 cert check.
//     On mismatch: calls Java XNative.onFail() via JNI (writes salva_config/enable=false),
//     then spawns a thread that sleeps 20s and calls exit().
//     exit() triggers the app's signal handler → SIGSEGV in HWUI CommonPool thread.
//     → This patch neutralises the native kill path.
//
// Patch targets (file offsets in lib/arm64-v8a/libXXX.so):
//
//   0x231e0: BL  CallVoidMethod (JNI onFail callback to Java)
//     84 f2 00 94  →  1f 20 03 d5  (NOP)
//     Suppresses salva_config SharedPrefs write + prevents any future JNI call
//     into the kill chain from this thread.
//
//   0x23238: BL  exit()
//     72 f2 00 94  →  1f 20 03 d5  (NOP)
//     The kill thread wakes after sleep(20), hits this NOP, falls through to
//     __stack_chk_fail (which itself becomes unreachable), and returns cleanly.
//
// Verification:
//   sleep(20) call at 0x231e8 is preserved — the thread still parks for 20s
//   but does nothing harmful on wakeup.
//   ARM64 NOP: d5 03 20 1f (little-endian: 1f 20 03 d5)

private val ORIGINAL_ONFAIL_JNI = byteArrayOf(0x84.toByte(), 0xf2.toByte(), 0x00.toByte(), 0x94.toByte())
private val ORIGINAL_EXIT_CALL  = byteArrayOf(0x72.toByte(), 0xf2.toByte(), 0x00.toByte(), 0x94.toByte())
private val NOP_ARM64           = byteArrayOf(0x1f.toByte(), 0x20.toByte(), 0x03.toByte(), 0xd5.toByte())

private val shareitNativeTamperResourcePatch = rawResourcePatch {
    execute {
        val libFile = get("lib/arm64-v8a/libXXX.so")
        val bytes = libFile.readBytes().toMutableList()

        // Helper: find and patch a 4-byte pattern at a known expected offset.
        // Falls back to a linear scan if the bytes were re-ordered (e.g. by a
        // future recompile), but logs a warning so failures are visible.
        fun patchAt(expectedOffset: Int, pattern: ByteArray, replacement: ByteArray, desc: String) {
            val b = bytes
            val slice = b.subList(expectedOffset, expectedOffset + 4)
            if (slice == pattern.toList()) {
                replacement.forEachIndexed { i, byte -> b[expectedOffset + i] = byte }
                return
            }
            // Linear scan fallback
            outer@ for (i in b.indices - 3) {
                for (j in pattern.indices) {
                    if (b[i + j] != pattern[j]) continue@outer
                }
                replacement.forEachIndexed { j, byte -> b[i + j] = byte }
                return
            }
            throw RuntimeException("libXXX.so: pattern not found for '$desc' (expected at 0x${expectedOffset.toString(16)})")
        }

        patchAt(0x231e0, ORIGINAL_ONFAIL_JNI, NOP_ARM64, "BL CallVoidMethod/onFail")
        patchAt(0x23238, ORIGINAL_EXIT_CALL,  NOP_ARM64, "BL exit()")

        libFile.writeBytes(bytes.toByteArray())
    }
}


/**
 * SHAREit Premium + Tamper Bypass Patch
 *
 * Two independent fixes:
 *
 * 1. VIP unlock — SHAREit Premium uses Google Play Billing with a custom subscription
 *    service registered via WMRouter at "/subscription/service/subs". The yg interface
 *    declares isVip() implemented by anr; all feature gates consult it via:
 *      com.ushareit.component.subscription.a.a() → yg.isVip() → anr.isVip()
 *        → hepler.b.a().d() → MutableLiveData<Boolean> (GP Billing result)
 *    returnEarly(true) cascades to all 5 subscription gates.
 *
 * 2. Tamper check bypass — com.base.core.x.d.c() is a signature integrity check
 *    triggered 5s after launch (via d.a() thread) from jp.smali → c.a():
 *      d() reads expected cert fingerprints from an obfuscated asset file
 *      e() computes the installed APK's signing cert MD5 via reflection
 *      ArrayList.contains() compares them
 *      On mismatch: reflective startActivity(kill Intent) → sleep(20000ms) → process death
 *    The post-death HWUI FORTIFY crash (pthread_mutex_lock on destroyed mutex) is a
 *    symptom of this kill. returnEarly() skips the entire cert read + kill path.
 *
 *    Note: libXXX.so nativeCheck (the other arm) writes to XNative.sResult but
 *    sResult is not read from smali — the Java cert check in d.c() is the kill trigger.
 */
@Suppress("unused")
val shareitPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks SHAREit Premium and bypasses APK signature tamper check.",
    default = true
) {
    compatibleWith(SHAREIT_COMPATIBILITY)
    dependsOn(shareitNativeTamperResourcePatch)

    execute {
        // 1. Bypass VIP gate — all subscription limits cascade from this
        IsVipFingerprint.method.returnEarly(true)

        // 2. Bypass Java cert tamper check (one-time, 5s after launch)
        TamperCheckFingerprint.method.returnEarly()

        // 3. Bypass native tamper check entry — fires on every activity lifecycle event.
        //    Each call spawns a native kill thread (sleep→exit). NOP at the Java gate
        //    so nativeCheck() is never called regardless of how many screens are opened.
        XNativeCheckFingerprint.method.returnEarly()

        // 4. Seed subscription state as active (1 = active) — drives UI badge + LiveData init
        SubStateFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """.trimIndent())

        // 5. Mark "ever_vip" as true — enables lifetime/pro IAP display in isOpenIAPForMe()
        EverVipFingerprint.method.addInstructions(0, """
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object v0
            return-object v0
        """.trimIndent())
    }
}
