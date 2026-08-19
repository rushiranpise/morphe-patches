package app.template.patches.driverlicense.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.DRIVERLICENSE_COMPATIBILITY

// Driver's Ed - License Test (com.driverlicenseapp) v4.5.78 (versionCode 102278)
//
// Architecture: Flutter (Dart 3.11.1 / libapp.so ARM64). Premium logic lives in
// libapp.so (AOT snapshot); the DEX side is a thin Flutter shell with no
// patchable premium gating in Java bytecode.
//
// Billing: dual-layer
//   1. RevenueCat (purchases_flutter SDK) — EntitlementInfo.isActive fed via
//      MethodChannel from the Dart side.
//   2. Custom backend (api_subs) — FriggySubscription.active stored in a
//      BehaviorSubject stream; StripeSubscription checked for CDL users.
//
// Gate 1 (subscription): SubRepo.isUserSubscribed is the choke-point called by
// SubState constructor, PaywallBloc, every feature controller, and
// VideoLessonController. It calls:
//   → _doesUserHaveProperSubscription: reads StripeSubscription.active via
//     AsyncField.data (non-CDL path) or delegates to hasAnySub() (US-CDL path).
//   → hasAnySub: reads BehaviorSubject<CustomerInfo?>.value?.active.
//
// Gate 2 (per-lesson entitlement): the roadmap padlocks are NOT driven by
// isUserSubscribed. LearnState.isLessonAvailable returns whether a lesson node
// is unlocked, and the roadmap item builder (RoadMapItem::_createItemsFromState)
// stores that result as each node's availability. The decision is:
//   if (reverseTrialInfo.isRunning) return true;
//   return availabilityMap[lessonId].isAvailable;
// where availabilityMap comes from AvailabilityApi.load →
// GET /v4/learning/lessons/availability/ (server-authoritative). Because there
// is no real server-side subscription, that endpoint reports lessons 2+ as
// is_available=false, so they stay padlocked even with Gate 1 bypassed.
//
// Dart AOT bool return convention (IMPORTANT): Dart booleans are the VM's
// true/false singletons addressed relative to the NULL register (x22):
//   true  = x22 + 0x20
//   false = x22 + 0x30
// Returning a raw 0/1 does NOT work — callers compare against the singleton.
//
// Patch strategy — four ARM64 function-entry overwrites in libapp.so:
//   isUserSubscribed                @ 0x7de03c → add x0, x22, #0x20; ret
//   _doesUserHaveProperSubscription @ 0x7de148 → add x0, x22, #0x20; ret
//   hasAnySub                       @ 0x7de220 → add x0, x22, #0x20; ret
//   LearnState.isLessonAvailable    @ 0x9ee378 → add x0, x22, #0x20; ret
//
// Each overwrite replaces the 8-byte function prologue (stp fp,lr; mov fp,sp)
// with add x0,x22,#0x20 (C0 82 00 91) + ret (C0 03 5F D6). Remaining original
// bytes are unreachable after ret.
//
// Addresses are virtual addresses == file offsets (all PT_LOAD segments have
// p_offset == p_vaddr, verified). Regenerated with Blutter for v4.5.78
// (Dart 3.11.1, snapshot 78da37fed6bf…).

private val ADD_NULL_TRUE = byteArrayOf(0xC0.toByte(), 0x82.toByte(), 0x00, 0x91.toByte())  // add x0, x22, #0x20 (Dart 'true')
private val RET           = byteArrayOf(0xC0.toByte(), 0x03, 0x5F, 0xD6.toByte())  // ret

private data class HexSite(
    val name: String,
    val fileOffset: Int,
    val expectedBytes: ByteArray,  // 8-byte prologue for verification
)

private val PATCH_SITES = listOf(
    HexSite(
        name = "SubRepo.isUserSubscribed",
        fileOffset = 0x7de03c,
        expectedBytes = byteArrayOf(
            0xFD.toByte(), 0x79, 0xBF.toByte(), 0xA9.toByte(),  // stp  fp, lr, [sp, #-0x10]!
            0xFD.toByte(), 0x03, 0x0F, 0xAA.toByte(),           // mov  fp, sp
        ),
    ),
    HexSite(
        name = "SubRepo._doesUserHaveProperSubscription",
        fileOffset = 0x7de148,
        expectedBytes = byteArrayOf(
            0xFD.toByte(), 0x79, 0xBF.toByte(), 0xA9.toByte(),  // stp  fp, lr, [sp, #-0x10]!
            0xFD.toByte(), 0x03, 0x0F, 0xAA.toByte(),           // mov  fp, sp
        ),
    ),
    HexSite(
        name = "SubRepo.hasAnySub",
        fileOffset = 0x7de220,
        expectedBytes = byteArrayOf(
            0xFD.toByte(), 0x79, 0xBF.toByte(), 0xA9.toByte(),  // stp  fp, lr, [sp, #-0x10]!
            0xFD.toByte(), 0x03, 0x0F, 0xAA.toByte(),           // mov  fp, sp
        ),
    ),
    HexSite(
        name = "LearnState.isLessonAvailable",
        fileOffset = 0x9ee378,
        expectedBytes = byteArrayOf(
            0xFD.toByte(), 0x79, 0xBF.toByte(), 0xA9.toByte(),  // stp  fp, lr, [sp, #-0x10]!
            0xFD.toByte(), 0x03, 0x0F, 0xAA.toByte(),           // mov  fp, sp
        ),
    ),
)

private val libappPatch = rawResourcePatch {
    execute {
        val lib = get("lib/arm64-v8a/libapp.so")
            ?: throw PatchException(
                "lib/arm64-v8a/libapp.so not found — patch requires merged base + arm64-v8a split.",
            )

        val bytes = lib.readBytes().toMutableList()

        for (site in PATCH_SITES) {
            val off = site.fileOffset
            // Verify expected prologue bytes (catches version mismatch early)
            val actual = bytes.subList(off, off + 8).map { it }
            val expected = site.expectedBytes.toList()
            if (actual != expected) {
                throw PatchException(
                    "${site.name}: unexpected bytes at ${Integer.toHexString(off)} — " +
                        "expected ${expected.joinToString(" ") { "%02x".format(it) }}, " +
                        "got ${actual.joinToString(" ") { "%02x".format(it) }}. " +
                        "App version changed?",
                )
            }
            // Overwrite: add x0, x22, #0x20 (Dart true); ret
            val patch = ADD_NULL_TRUE + RET
            patch.forEachIndexed { i, b -> bytes[off + i] = b }
        }

        lib.writeBytes(bytes.toByteArray())
    }
}

@Suppress("unused")
val driverLicensePremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Driver's Ed premium features by patching the Flutter subscription gate.",
) {
    compatibleWith(DRIVERLICENSE_COMPATIBILITY)
    dependsOn(libappPatch)

    execute {
        // No DEX-side bytecode to patch: all premium logic is in libapp.so (Flutter).
        // The libappPatch dependency handles all ARM64 overwrites.
    }
}
