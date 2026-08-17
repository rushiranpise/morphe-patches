package app.template.patches.httpmock

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HTTPMOCK_COMPATIBILITY

// ── Billing architecture ──────────────────────────────────────────────────────
//
// All VIP state flows through one Kotlin data class:
//   Lcom/mock/sample/respository/model/VipConfigModel;
//
// Fields set at construction from billing backend:
//   isVipUser:Z, vipType:VipType, vipExpireTime:J, purchaseTime:J, ...
//
// Gate methods (all delegate back to the same fields):
//   isVipUser()Z         — field read, plain gate
//   isVipValid()Z        — isVipUser && vipType != NONE && (PERMANENT || expiry > now)
//   isPermanentVipValid()Z — vipType == PERMANENT
//   getVipType()VipType  — field read
//   getVipStatusDescription()String — reads fields directly (bypasses getters)
//
// Layer 2 (zu4 in 2.3.5, Ir0 in 2.11.4) is a thin dispatcher that calls
// VipManager.AwaMin() → VipConfigModel.isVipValid(). Patching VipConfigModel
// methods at Layer 1 is sufficient — the dispatcher inherits the patched result.
//
// Layer 2 class name is obfuscated (o/Ir0 → o/zu4 across versions) and MUST NOT
// be hardcoded. It is intentionally omitted here.
//
// DexGuard note: string filters in Fingerprints.kt are limited to the two
// unencrypted PairIP strings. All other fingerprints use definingClass + name.
//
@Suppress("unused")
val unlockVipPatch = bytecodePatch(
    name = "Unlock VIP (Lifetime)",
    description = "Forces permanent professional VIP tier, removes ads and upgrade popups, bypasses PairIP.",
) {
    compatibleWith(HTTPMOCK_COMPATIBILITY)

    execute {

        // ── Layer 1: VipConfigModel — stable non-obfuscated class ─────────────
        val vipConfigClass = classDefBy("Lcom/mock/sample/respository/model/VipConfigModel;")

        // isVipValid / isPermanentVipValid / isVipUser → always true
        listOf(IsVipValidFingerprint, IsPermanentVipValidFingerprint, IsVipUserFingerprint).forEach { fp ->
            runCatching { fp.match(vipConfigClass).method }.getOrNull()?.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // getVipType() → PERMANENT (stable enum field, never obfuscated)
        runCatching { GetVipTypeFingerprint.match(vipConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                "sget-object v0, Lcom/mock/sample/respository/model/VipType;->PERMANENT:Lcom/mock/sample/respository/model/VipType;\nreturn-object v0"
            )
        }

        // getVipStatusDescription() reads raw fields, bypasses getters — patch directly
        runCatching { GetVipStatusDescriptionFingerprint.match(vipConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const-string v0, \"Permanent VIP\"\nreturn-object v0")
        }

        // ── Layer 2: ADConfigModel — suppress ads ─────────────────────────────
        val adConfigClass = classDefBy("Lcom/mock/sample/respository/model/ADConfigModel;")

        // ifShowRewardsView → false (no rewards ads)
        runCatching { IfShowRewardsViewFingerprint.match(adConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // getMNeedTipsRewardsAd → false (no tips/rewards ad banner)
        runCatching { GetMNeedTipsRewardsAdFingerprint.match(adConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Layer 3: PairIP — no-op entry point ───────────────────────────────
        // Anchored on literal strings that survive DexGuard encryption.

        runCatching { CheckLicenseFingerprint.method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "return-void")
        }

        runCatching { VipVerifyCheckFingerprint.method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
