package app.template.patches.httpmock

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.HTTPMOCK_COMPATIBILITY

@Suppress("unused")
val unlockVipPatch = bytecodePatch(
    name = "Unlock VIP (Lifetime)",
    description = "Forces permanent professional VIP tier, removes ads and upgrade popups, bypasses PairIP.",
) {
    compatibleWith(HTTPMOCK_COMPATIBILITY)

    execute {

        // ── Layer 1: VipConfigModel — explicit match(classDefBy(...)) ─────────
        // This is the pattern confirmed working in commit 57cd199.

        val vipConfigClass = classDefBy("Lcom/mock/sample/respository/model/VipConfigModel;")

        listOf(IsVipValidFingerprint, IsPermanentVipValidFingerprint, IsVipUserFingerprint).forEach { fp ->
            runCatching { fp.match(vipConfigClass).method }.getOrNull()?.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // getVipType() → PERMANENT
        runCatching { GetVipTypeFingerprint.match(vipConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                "sget-object v0, Lcom/mock/sample/respository/model/VipType;->PERMANENT:Lcom/mock/sample/respository/model/VipType;\nreturn-object v0"
            )
        }

        // getVipStatusDescription() reads isVipUser/vipType as RAW FIELDS — getter patches don't affect it.
        // Return hardcoded string directly, bypassing all field reads.
        runCatching { GetVipStatusDescriptionFingerprint.match(vipConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                const-string v0, "Permanent VIP"
                return-object v0
                """.trimIndent()
            )
        }

        // ── Layer 2: Ir0 (VipTierChecker) — classDefBy direct ────────────────
        // Fixes upgrade popup (AEHjFh), ad gate (ABZjeC), tier derivation (ABJdtR)

        val ir0Class = classDefBy("Lo/Ir0;")

        // ABJdtR():VipTier → PROFESSIONAL
        runCatching {
            ir0Class.methods.first {
                it.name == "ABJdtR" && it.returnType == "Lcom/mock/sample/respository/model/VipTier;"
            }.toMutable()
        }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                "sget-object v0, Lcom/mock/sample/respository/model/VipTier;->PROFESSIONAL:Lcom/mock/sample/respository/model/VipTier;\nreturn-object v0"
            )
        }

        // ABZjeC():Z → true (hasAdFreePermission)
        runCatching { ir0Class.methods.first { it.name == "ABZjeC" }.toMutable() }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // AEHjFh():Z → true (hasProfessionalPermission — blocks upgrade popup)
        runCatching { ir0Class.methods.first { it.name == "AEHjFh" }.toMutable() }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Layer 3: ADConfigModel ────────────────────────────────────────────

        val adConfigClass = classDefBy("Lcom/mock/sample/respository/model/ADConfigModel;")

        runCatching { IfShowRewardsViewFingerprint.match(adConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        runCatching { GetMNeedTipsRewardsAdFingerprint.match(adConfigClass).method }.getOrNull()?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Layer 4: PairIP — no-op static entry point ────────────────────────

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
