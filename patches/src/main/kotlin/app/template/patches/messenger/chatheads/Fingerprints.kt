package app.template.patches.messenger.chatheads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── Chat bubbles eligibility gate — classes2/X/2NA.A00()Z ──────────────────
// 2NA.A00()Z is the method that decides whether chat bubbles (chat heads) are
// supported on the current device. It returns true only if:
//   • API level >= 30 (Android 11), AND
//   • device is NOT low-RAM
//
// Returning false causes the "Open as chat head" option to disappear from
// thread settings and the notification panel, and prevents all bubble entry
// points from showing.
//
// Method body (verified v573, classes2/X/2NA.smali):
//   sget  v1, Landroid/os/Build$VERSION;->SDK_INT:I     ← anchor 1
//   const v0, 0x1e          (API 30)
//   if-lt v1, v0, :false
//   ... ActivityManager.isLowRamDevice() ...             ← anchor 2
//   if-nez → :false
//   const/4 v0, 0x1 / return v0   (true)
//   :false → const/4 v0, 0x0 / return v0  (false)
//
// Fingerprint: PUBLIC FINAL, no params, returns Z, in classes2.
// Filters on both stable SDK references (never obfuscated by R8):
//   • fieldAccess on Landroid/os/Build$VERSION;->SDK_INT:I
//   • methodCall on Landroid/app/ActivityManager;->isLowRamDevice()Z
//
// Verified: classes2/X/2NA.smali — unique match, one file in classes2.
// Verified against com.facebook.orca 573.0.0.44.88.
//
// Ported from NeonOrbit/ChatHeadEnabler (Xposed → static bytecode patch).
// ChatHeadEnabler finds this method at runtime via DexFetcher searching for
// classes referencing SDK_INT field AND isLowRamDevice method. We anchor on the
// same two stable SDK references in compile-time fingerprint form.
internal val BubblesEligibilityGateFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET,
            definingClass = "Landroid/os/Build\$VERSION;",
            name = "SDK_INT",
        ),
        methodCall(
            definingClass = "Landroid/app/ActivityManager;",
            name = "isLowRamDevice",
        ),
    ),
)
