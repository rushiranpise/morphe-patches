package app.template.patches.movieboxtv.member

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOXTV_COMPATIBILITY

// MovieBox TV (com.community.oneroom) v3.0.15 — gate architecture:
//
// GATE 1 — MemberCheckResult.isPassed() (server response gate, main paywall):
//   CheckMemberRightsLoadingDialog receives MemberCheckResult from server API.
//   isPassed() == TRUE → im/a.e() called (allowed/passed).
//   isPassed() != TRUE → interceptType checked: OP_VIP → im/a.a() (shows "Get Premium" dialog).
//   Patch: isPassed() always returns Boolean.TRUE → all checks pass, no paywall shown.
//
// GATE 2 — MemberInfo.isActive() (local membership status):
//   MemberProvider.m()Z reads MemberDetail.getMemberInfo().isActive().
//   Used in MemberFragment.u1() before triggering payment and in MemberProvider.m().
//   Patch: isActive() always returns true.
//
// GATE 3 — MemberProvider.c()Z (MMKV "kv_is_pay_enable_member"):
//   Reads MMKV SharedPrefs key set by server after subscription sync.
//   Patch: c()Z always returns true.
//
// GATE 4 — MemberProvider.e()Z (MMKV "kv_is_skip_ad"):
//   Controls whether ads are skipped (premium perk).
//   Patch: e()Z always returns true.
//
// GATE 5 — MemberResolutionBean.isUnlock() (Room DB per-episode unlock):
//   Per-episode unlock status cached from server. isUnlock=false = episode locked.
//   Patch: isUnlock() always returns Boolean.TRUE.
//
// v1.0.2 (com.community.mbox.tv) compatibility maintained via multi-target in Constants.

@Suppress("unused")
val unlockMemberCheckResultPatch = bytecodePatch(
    name = "Bypass member rights check",
    description = "Forces MemberCheckResult.isPassed() to always return Boolean.TRUE, " +
        "bypassing the server-side membership gate and preventing the 'Get Premium / 7-day trial' dialog.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberCheckResult = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberCheckResult;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberCheckResult not found — class may have moved. Re-derive.",
        )

        val isPassedMethod = memberCheckResult.methods.firstOrNull {
            it.name == "isPassed" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberCheckResult.isPassed() not found — re-derive.",
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
    description = "Forces MemberInfo.isActive() to always return true, " +
        "making the app believe the membership subscription is active.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberInfo = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberInfo;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberInfo not found — class may have moved. Re-derive.",
        )

        val isActiveMethod = memberInfo.methods.firstOrNull {
            it.name == "isActive" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberInfo.isActive()Z not found — re-derive.",
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
    description = "Forces MemberProvider.c() (kv_is_pay_enable_member) and " +
        "MemberProvider.e() (kv_is_skip_ad) to always return true.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        // Pin MemberProvider by unobfuscated class name (it's a named class)
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberProvider not found — class may have moved. Re-derive.",
        )

        // c()Z — reads MMKV "kv_is_pay_enable_member"
        val cMethod = memberProvider.methods.firstOrNull {
            it.name == "c" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberProvider.c()Z (kv_is_pay_enable_member) not found — re-derive.",
        )

        // e()Z — reads MMKV "kv_is_skip_ad"
        val eMethod = memberProvider.methods.firstOrNull {
            it.name == "e" &&
                it.returnType == "Z" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberProvider.e()Z (kv_is_skip_ad) not found — re-derive.",
        )

        listOf(cMethod, eMethod).forEach { method ->
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
val unlockMemberResolutionPatch = bytecodePatch(
    name = "Unlock member resolution",
    description = "Forces MemberResolutionBean.isUnlock() to always return Boolean.TRUE, " +
        "making all locally-cached episodes appear as unlocked to the playback layer.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberResolutionBean not found — class may have moved. Re-derive.",
        )

        val isUnlockMethod = memberBean.methods.firstOrNull {
            it.name == "isUnlock" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberResolutionBean.isUnlock() not found — re-derive.",
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
    description = "Forces MemberResolutionBean.getVipResolutionTip() to always return Boolean.FALSE, " +
        "suppressing any 'VIP only' badge shown on resolution options.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberResolutionBean not found — class may have moved. Re-derive.",
        )

        // v3: getter named getVipResolutionTip(); v1 fallback: vipResolutionTip()
        val vipTipMethod = memberBean.methods.firstOrNull {
            it.name in setOf("getVipResolutionTip", "vipResolutionTip") &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: vipResolutionTip getter not found in MemberResolutionBean — re-derive.",
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

@Suppress("unused")
val spoofMemberDaysLeftPatch = bytecodePatch(
    name = "Spoof member days left",
    description = "Forces MemberInfo.getDaysLeft() to always return 3650 (10 years), " +
        "preventing the '0 days left' display on the premium screen.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        // Pin by unobfuscated class name
        val memberInfo = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberInfo;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberInfo not found — class may have moved. Re-derive.",
        )

        // getDaysLeft()Ljava/lang/Integer; — returns nullable Integer from Room field
        val getDaysLeftMethod = memberInfo.methods.firstOrNull {
            it.name == "getDaysLeft" &&
                it.returnType == "Ljava/lang/Integer;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox TV: MemberInfo.getDaysLeft() not found — re-derive.",
        )

        // Return Integer.valueOf(3650) — 10 years, clearly non-zero
        getDaysLeftMethod.addInstructions(
            0,
            """
                const/16 v0, 0xE42
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )
    }
}

@Suppress("unused")
val suppressClaimMemberDialogPatch = bytecodePatch(
    name = "Suppress newbie bonus dialog",
    description = "Makes MemberProvider.w(F)V return immediately, suppressing the " +
        "'Claim 7-day trial / newbies bonus' ClaimMemberDialog popup.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        // MemberProvider implements im/b — w(F)V is the dialog show method
        // Pin by unobfuscated class name
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException(
            "MovieBox TV: MemberProvider not found — re-derive.",
        )

        // w(F)V — public, 1 float param, void return — shows ClaimMemberDialog
        val wMethod = memberProvider.methods.firstOrNull {
            it.name == "w" &&
                it.returnType == "V" &&
                it.parameterTypes == listOf("F")
        } ?: throw PatchException(
            "MovieBox TV: MemberProvider.w(F)V (ClaimMemberDialog trigger) not found — re-derive.",
        )

        wMethod.addInstructions(0, "return-void")
    }
}
