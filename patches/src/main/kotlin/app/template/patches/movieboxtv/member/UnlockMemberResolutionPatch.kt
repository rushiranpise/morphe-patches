package app.template.patches.movieboxtv.member

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOXTV_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone  (com.community.oneroom) v3.0.16.0703.03
// ═══════════════════════════════════════════════════════════════════
//
// GATE 1 — MemberCheckResult.isPassed() (server response gate, main paywall)
//   Patch: always return Boolean.TRUE
//
// GATE 2 — MemberProvider.a()Z  (MemberDetail.getMemberInfo().isActive() wrapper)
//   Patch: always return true
//
// GATE 3 — MemberProvider.e()Z  MMKV "kv_is_pay_enable_member"  (was c()Z in v3.0.15)
//   Patch: always return true
//
// GATE 4 — MemberProvider.f()Z  MMKV "kv_is_skip_ad"            (was e()Z in v3.0.15)
//   Patch: always return true
//
// GATE 5 — MemberInfo.isActive()Z
//   Patch: always return true
//
// GATE 6 — MemberInfo.getDaysLeft()  →  also patch getExpiryDate()
//   getDaysLeft() returns Integer; the UI shows "Valid Until <expiryDate>" via
//   MemberInfo.getExpiryDate() (String). Both are patched:
//     getDaysLeft()  → 3650
//     getExpiryDate() → "2035-12-31"
//
// GATE 7 — MemberProvider.z(F)V  ClaimMemberDialog popup  (was w(F)V in v3.0.15)
//   Patch: return-void
//
// GATE 8 — MemberResolutionBean.isUnlock() / getVipResolutionTip()
//   Patch: TRUE / FALSE
//
// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv) v1.1.1.0702.03
// ═══════════════════════════════════════════════════════════════════
//
// MemberProvider / MemberInfo / MemberCheckResult are ABSENT in this package.
// Patches 1-7 above will NOT be applied to com.community.mbox.tv.
//
// TV GATE 1 — BffSubjectInfo.isVip()Z  (per-episode VIP flag, main content gate)
//   Used in DetailFragment (lines 25171, 25560, 50312, 68221, 68996) to block playback.
//   Patch: always return false  (not VIP-only → content unlocked)
//
// TV GATE 2 — BffGetVipUserInfoData.isVip()Ljava/lang/Boolean;  (account-level VIP status)
//   Used in TvServiceLocator to check whether user account is VIP.
//   Patch: always return Boolean.TRUE
//
// TV GATE 3 — MemberResolutionBean.isUnlock() / getVipResolutionTip()  (resolution unlock)
//   Patch: TRUE / FALSE  (same as phone)

// ─── Phone-only patches ───────────────────────────────────────────

@Suppress("unused")
val unlockMemberCheckResultPatch = bytecodePatch(
    name = "Bypass member rights check",
    description = "Forces MemberCheckResult.isPassed() to always return Boolean.TRUE, " +
        "bypassing the server-side membership gate and preventing the 'Get Premium / 7-day trial' dialog.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY)

    execute {
        val memberCheckResult = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberCheckResult;",
        ) ?: throw PatchException(
            "MovieBox: MemberCheckResult not found — class may have moved. Re-derive.",
        )

        val isPassedMethod = memberCheckResult.methods.firstOrNull {
            it.name == "isPassed" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberCheckResult.isPassed() not found — re-derive.",
        )

        isPassedMethod.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val unlockMemberInfoActivePatch = bytecodePatch(
    name = "Spoof member active status",
    description = "Forces MemberInfo.isActive() to always return true.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY)

    execute {
        val memberInfo = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberInfo;",
        ) ?: throw PatchException(
            "MovieBox: MemberInfo not found — class may have moved. Re-derive.",
        )

        val isActiveMethod = memberInfo.methods.firstOrNull {
            it.name == "isActive" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberInfo.isActive()Z not found — re-derive.",
        )

        isActiveMethod.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val unlockMemberProviderFlagsPatch = bytecodePatch(
    name = "Spoof member provider flags",
    description = "Forces MemberProvider.e() (kv_is_pay_enable_member) and " +
        "MemberProvider.f() (kv_is_skip_ad) to always return true. " +
        "Renamed from c()/e() in v3.0.15 to e()/f() in v3.0.16.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY)

    execute {
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException(
            "MovieBox: MemberProvider not found — class may have moved. Re-derive.",
        )

        // e()Z — MMKV "kv_is_pay_enable_member" (was c()Z in v3.0.15)
        val eMethod = memberProvider.methods.firstOrNull {
            it.name == "e" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberProvider.e()Z (kv_is_pay_enable_member) not found — re-derive.",
        )

        // f()Z — MMKV "kv_is_skip_ad" (was e()Z in v3.0.15)
        val fMethod = memberProvider.methods.firstOrNull {
            it.name == "f" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberProvider.f()Z (kv_is_skip_ad) not found — re-derive.",
        )

        listOf(eMethod, fMethod).forEach { method ->
            method.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            )
        }
    }
}

@Suppress("unused")
val spoofMemberDaysLeftPatch = bytecodePatch(
    name = "Spoof member days left",
    description = "Forces MemberInfo.getDaysLeft() to return 3650 (10 years) and " +
        "MemberInfo.getExpiryDate() to return '2035-12-31'. " +
        "The UI shows 'Valid Until <expiryDate>' — patching getDaysLeft alone " +
        "left the display null because the UI uses getExpiryDate(), not getDaysLeft().",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY)

    execute {
        val memberInfo = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberInfo;",
        ) ?: throw PatchException(
            "MovieBox: MemberInfo not found — class may have moved. Re-derive.",
        )

        val getDaysLeftMethod = memberInfo.methods.firstOrNull {
            it.name == "getDaysLeft" &&
                it.returnType == "Ljava/lang/Integer;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberInfo.getDaysLeft() not found — re-derive.",
        )

        getDaysLeftMethod.addInstructions(
            0,
            """
                const/16 v0, 0xE42
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )

        // The UI string resource "member_date_left" formats as "Valid Until %s"
        // where %s = getExpiryDate(). Without patching this, display shows "Valid Until null".
        val getExpiryDateMethod = memberInfo.methods.firstOrNull {
            it.name == "getExpiryDate" &&
                it.returnType == "Ljava/lang/String;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberInfo.getExpiryDate() not found — re-derive.",
        )

        getExpiryDateMethod.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val suppressClaimMemberDialogPatch = bytecodePatch(
    name = "Suppress newbie bonus dialog",
    description = "Makes MemberProvider.z(F)V return immediately, suppressing the " +
        "'Claim 7-day trial / newbies bonus' ClaimMemberDialog popup. " +
        "Renamed from w(F)V in v3.0.15 to z(F)V in v3.0.16.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY)

    execute {
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException(
            "MovieBox: MemberProvider not found — re-derive.",
        )

        // z(F)V in v3.0.16 (was w(F)V in v3.0.15)
        val zMethod = memberProvider.methods.firstOrNull {
            it.name == "z" &&
                it.returnType == "V" &&
                it.parameterTypes == listOf("F")
        } ?: throw PatchException(
            "MovieBox: MemberProvider.z(F)V (ClaimMemberDialog trigger) not found — re-derive.",
        )

        zMethod.addInstructions(0, "return-void")
    }
}

// ─── Shared patches (phone + TV) ──────────────────────────────────

@Suppress("unused")
val unlockMemberResolutionPatch = bytecodePatch(
    name = "Unlock member resolution",
    description = "Forces MemberResolutionBean.isUnlock() to always return Boolean.TRUE.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException(
            "MovieBox: MemberResolutionBean not found — class may have moved. Re-derive.",
        )

        val isUnlockMethod = memberBean.methods.firstOrNull {
            it.name == "isUnlock" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberResolutionBean.isUnlock() not found — re-derive.",
        )

        isUnlockMethod.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val clearVipResolutionTipPatch = bytecodePatch(
    name = "Clear VIP resolution tip",
    description = "Forces MemberResolutionBean.getVipResolutionTip() to always return Boolean.FALSE.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException(
            "MovieBox: MemberResolutionBean not found — class may have moved. Re-derive.",
        )

        val vipTipMethod = memberBean.methods.firstOrNull {
            it.name in setOf("getVipResolutionTip", "vipResolutionTip") &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: vipResolutionTip getter not found in MemberResolutionBean — re-derive.",
        )

        vipTipMethod.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )
    }
}

// ─── TV-only patches ──────────────────────────────────────────────

@Suppress("unused")
val unlockTvSubjectVipPatch = bytecodePatch(
    name = "Bypass TV episode VIP gate",
    description = "Forces BffSubjectInfo.isVip() to always return false, " +
        "making every episode appear as non-VIP-only so playback is not blocked. " +
        "This is the primary content gate in com.community.mbox.tv — " +
        "checked in DetailFragment before playback is permitted.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val bffSubjectInfo = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffSubjectInfo;",
        ) ?: throw PatchException(
            "MovieBox TV: BffSubjectInfo not found — class may have moved. Re-derive.",
        )

        val isVipMethod = bffSubjectInfo.methods.firstOrNull {
            it.name == "isVip" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: BffSubjectInfo.isVip()Z not found — re-derive.",
        )

        isVipMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val unlockTvUserVipStatusPatch = bytecodePatch(
    name = "Spoof TV account VIP status",
    description = "Forces BffGetVipUserInfoData.isVip() to always return Boolean.TRUE, " +
        "making the app treat the account as a VIP subscriber.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val vipData = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffGetVipUserInfoData;",
        ) ?: throw PatchException(
            "MovieBox TV: BffGetVipUserInfoData not found — class may have moved. Re-derive.",
        )

        val isVipMethod = vipData.methods.firstOrNull {
            it.name == "isVip" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: BffGetVipUserInfoData.isVip() not found — re-derive.",
        )

        isVipMethod.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )
    }
}
