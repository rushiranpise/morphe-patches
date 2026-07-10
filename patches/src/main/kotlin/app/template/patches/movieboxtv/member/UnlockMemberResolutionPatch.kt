package app.template.patches.movieboxtv.member

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOX_IN_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOXTV_COMPATIBILITY

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone  (com.community.oneroom)  v3.0.16.0703.03  vc=50020113
//  MovieBox India  (com.community.mbox.in)  v3.0.16.0707.03  vc=50020115
// ═══════════════════════════════════════════════════════════════════
//
// NOTE: v3.0.16.0708.03 (com.community.oneroom) is protected by ByteDance PGL
//   shell packer (libpglarmor.so) — 4-class stub DEX, real payload encrypted
//   in assets. Static DEX patching not possible; target 0703.03 only.
//
// GATE 1 — MemberCheckResult.isPassed()     → Boolean.TRUE
// GATE 2 — MemberProvider.a()Z             → true  [isActive wrapper]
// GATE 3 — MemberProvider.e()Z / IN: d()Z  → true  [kv_is_pay_enable_member]
// GATE 4 — MemberProvider.f()Z             → true  [kv_is_skip_ad]
// GATE 5 — MemberProvider.z(F)V / IN: w()  → return-void  [ClaimMemberDialog]
// GATE 6 — MemberInfo.isActive()Z          → true
// GATE 7 — MemberInfo.getDaysLeft()        → 3650
//           MemberInfo.getExpiryDate()     → "2035-12-31"
//           MemberInfo.getMemberType()I    → 2  (from MovieboxHooker analysis)
//           MemberInfo.getNextRenewDate()  → "2035-12-31"
// GATE 8 — MemberBriefInfo.isActive()Z     → true  (NEW: from Hooker analysis)
//           MemberBriefInfo.getExpiryDate()→ "2035-12-31"
//           MemberBriefInfo.getMemberType()→ 2
// GATE 9 — MemberResolutionBean.isUnlock() → Boolean.TRUE
//           MemberResolutionBean.getVipResolutionTip() → Boolean.FALSE
// GATE 10 — DownloadBean.getRequireMemberType() → Integer(0) [NEW: Hooker]
//
// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv) v1.1.1.0702.03  vc=50040006
// ═══════════════════════════════════════════════════════════════════
//  TV patches: BffSubjectInfo.isVip()Z → false
//              BffGetVipUserInfoData.isVip() → Boolean.TRUE
//              MemberResolutionBean (shared)

// ─── Phone + India patches ────────────────────────────────────────

@Suppress("unused")
val unlockMemberCheckResultPatch = bytecodePatch(
    name = "Bypass member rights check",
    description = "Forces MemberCheckResult.isPassed() to always return Boolean.TRUE.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val memberCheckResult = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberCheckResult;",
        ) ?: throw PatchException("MovieBox: MemberCheckResult not found — re-derive.")

        val isPassedMethod = memberCheckResult.methods.firstOrNull {
            it.name == "isPassed" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException("MovieBox: MemberCheckResult.isPassed() not found — re-derive.")

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
val unlockMemberInfoPatch = bytecodePatch(
    name = "Spoof member info",
    description = "Forces MemberInfo active=true, memberType=2 (VIP), days=3650, " +
        "expiryDate/nextRenewDate='2035-12-31'. " +
        "Previously only patched isActive+getDaysLeft+getExpiryDate; now also patches " +
        "getMemberType()+getNextRenewDate() discovered via MovieboxHooker analysis.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val memberInfo = mutableClassDefByOrNull(
            "Lcom/transsion/memberapi/MemberInfo;",
        ) ?: throw PatchException("MovieBox: MemberInfo not found — re-derive.")

        memberInfo.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            ?: throw PatchException("MovieBox: MemberInfo.isActive()Z not found.")

        // UI shows "Valid Until %s" via getExpiryDate() (not getDaysLeft())
        memberInfo.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2035-12-31\"\nreturn-object v0")
            ?: throw PatchException("MovieBox: MemberInfo.getExpiryDate() not found.")

        memberInfo.methods.firstOrNull {
            it.name == "getDaysLeft" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/16 v0, 0xE42
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberInfo.getDaysLeft() not found.")

        // getMemberType()I — 2 = VIP (from Hooker analysis)
        memberInfo.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")

        // getNextRenewDate() — prevents "renewal expired" display
        memberInfo.methods.firstOrNull {
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2035-12-31\"\nreturn-object v0")
    }
}

@Suppress("unused")
val unlockMemberBriefInfoPatch = bytecodePatch(
    name = "Spoof member brief info",
    description = "Forces MemberBriefInfo.isActive()=true, getMemberType()=2, getExpiryDate()='2035-12-31'. " +
        "MemberBriefInfo is a lightweight summary bean used in home/profile cards — " +
        "discovered via MovieboxHooker (Kero309x) analysis.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val memberBriefInfo = mutableClassDefByOrNull(
            "Lcom/transsion/member/bean/MemberBriefInfo;",
        ) ?: throw PatchException("MovieBox: MemberBriefInfo not found — re-derive.")

        memberBriefInfo.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        memberBriefInfo.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")

        memberBriefInfo.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2035-12-31\"\nreturn-object v0")
    }
}

@Suppress("unused")
val unlockMemberProviderFlagsPatch = bytecodePatch(
    name = "Spoof member provider flags",
    description = "Forces MMKV member gates to true. " +
        "Phone (com.community.oneroom): e()Z=kv_is_pay_enable_member, f()Z=kv_is_skip_ad. " +
        "India (com.community.mbox.in): d()Z=kv_is_pay_enable_member, f()Z=kv_is_skip_ad. " +
        "Both share f()Z for kv_is_skip_ad; primary gate method differs per package.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException("MovieBox: MemberProvider not found — re-derive.")

        // kv_is_skip_ad is always f()Z in both packages
        memberProvider.methods.firstOrNull {
            it.name == "f" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            ?: throw PatchException("MovieBox: MemberProvider.f()Z (kv_is_skip_ad) not found — re-derive.")

        // kv_is_pay_enable_member: e()Z on .oneroom, d()Z on .mbox.in
        val payEnableMethod = memberProvider.methods.firstOrNull { m ->
            (m.name == "e" || m.name == "d") &&
                m.returnType == "Z" &&
                m.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "MovieBox: MemberProvider kv_is_pay_enable_member gate (e/d)()Z not found — re-derive.",
        )
        payEnableMethod.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
    }
}

@Suppress("unused")
val suppressClaimMemberDialogPatch = bytecodePatch(
    name = "Suppress newbie bonus dialog",
    description = "Makes MemberProvider ClaimMemberDialog trigger return-void. " +
        "Phone (com.community.oneroom): z(F)V. India (com.community.mbox.in): w(F)V. " +
        "Both suppressed via name search.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val memberProvider = mutableClassDefByOrNull(
            "Lcom/transsion/member/MemberProvider;",
        ) ?: throw PatchException("MovieBox: MemberProvider not found — re-derive.")

        // z(F)V on .oneroom, w(F)V on .mbox.in
        val dialogMethod = memberProvider.methods.firstOrNull { m ->
            (m.name == "z" || m.name == "w") &&
                m.returnType == "V" &&
                m.parameterTypes == listOf("F")
        } ?: throw PatchException(
            "MovieBox: MemberProvider ClaimMemberDialog trigger (z/w)(F)V not found — re-derive.",
        )
        dialogMethod.addInstructions(0, "return-void")
    }
}

@Suppress("unused")
val unlockDownloadRequirementPatch = bytecodePatch(
    name = "Unlock download member requirement",
    description = "Forces DownloadBean.getRequireMemberType() to always return Integer(0), " +
        "making all download items appear as freely downloadable regardless of VIP tier. " +
        "Discovered via MovieboxHooker (Kero309x) analysis.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
        val downloadBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
        ) ?: throw PatchException("MovieBox: DownloadBean not found — re-derive.")

        val requireMethod = downloadBean.methods.firstOrNull {
            it.name == "getRequireMemberType" &&
                it.returnType == "Ljava/lang/Integer;" &&
                it.parameterTypes.isEmpty()
        } ?: throw PatchException("MovieBox: DownloadBean.getRequireMemberType() not found — re-derive.")

        requireMethod.addInstructions(
            0,
            """
                const/4 v0, 0x0
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """.trimIndent(),
        )
    }
}

// ─── Shared patches (phone + India + TV) ─────────────────────────

@Suppress("unused")
val unlockMemberResolutionPatch = bytecodePatch(
    name = "Unlock member resolution",
    description = "Forces MemberResolutionBean.isUnlock() to always return Boolean.TRUE.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY, MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException("MovieBox: MemberResolutionBean not found — re-derive.")

        memberBean.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberResolutionBean.isUnlock() not found — re-derive.")
    }
}

@Suppress("unused")
val clearVipResolutionTipPatch = bytecodePatch(
    name = "Clear VIP resolution tip",
    description = "Forces MemberResolutionBean.getVipResolutionTip() to always return Boolean.FALSE.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY, MOVIEBOXTV_COMPATIBILITY)

    execute {
        val memberBean = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionBean;",
        ) ?: throw PatchException("MovieBox: MemberResolutionBean not found — re-derive.")

        memberBean.methods.firstOrNull {
            it.name in setOf("getVipResolutionTip", "vipResolutionTip") &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: vipResolutionTip getter not found — re-derive.")
    }
}

// ─── TV-only patches ──────────────────────────────────────────────

@Suppress("unused")
val unlockTvSubjectVipPatch = bytecodePatch(
    name = "Bypass TV episode VIP gate",
    description = "Forces BffSubjectInfo.isVip() to always return false, " +
        "making every episode appear as non-VIP-only so playback is not blocked in DetailFragment.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val bffSubjectInfo = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffSubjectInfo;",
        ) ?: throw PatchException("MovieBox TV: BffSubjectInfo not found — re-derive.")

        bffSubjectInfo.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            ?: throw PatchException("MovieBox TV: BffSubjectInfo.isVip()Z not found — re-derive.")
    }
}

@Suppress("unused")
val unlockTvUserVipStatusPatch = bytecodePatch(
    name = "Spoof TV account VIP status",
    description = "Forces BffGetVipUserInfoData.isVip() to always return Boolean.TRUE.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val vipData = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffGetVipUserInfoData;",
        ) ?: throw PatchException("MovieBox TV: BffGetVipUserInfoData not found — re-derive.")

        vipData.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: BffGetVipUserInfoData.isVip() not found — re-derive.")
    }
}
