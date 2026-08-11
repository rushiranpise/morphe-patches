package app.template.patches.movieboxtv.member

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOX_IN_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOXTV_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone  (com.community.oneroom)   v3.0.16.0708.03  vc=50020115
//  MovieBox India  (com.community.mbox.in)   v3.0.16.0804.03  vc=50020116
// ═══════════════════════════════════════════════════════════════════
//
//  *** MEMBERSHIP GATE CHAIN ***
//  MemberProvider.d()Z  — MMKV "kv_is_pay_enable_member"  → patch: return true
//  MemberProvider.f()Z  — MMKV "kv_is_skip_ad"            → patch: return true
//  MemberProvider.w(F)V — triggers ClaimMemberDialog       → patch: noop
//
//  *** AD GATE CHAIN ***
//  MemberProvider.A(Function0)V → checkShowAdState$1 calls:
//    MemberCheckResult.isPassed()        → MMKV kv_is_skip_ad
//    MemberCheckResult.getVipEnable()    → MMKV kv_is_enable_member
//    MemberCheckResult.getVipPayEnable() → MMKV kv_is_pay_enable_member
//    li.b.c(isPassed) → li.e.c(true) → writes MMKV key "j376W52LrKvau6r8" = true
//  scene.c.g(String)Z reads li.e.a() → if true = skip ad
//  ObserveLoginAction.onLogout() resets kv_is_skip_ad → false on every logout
//  !! A(Function0)V must NOT be noop'd: it is the pipeline that sets the MMKV skip-ad key.
//  Instead patch the source getters so isPassed/getVipEnable/getVipPayEnable always return
//  true → the MMKV key is written true → the SDK suppresses ads itself.
//  Belt+suspenders: also patch li.e.a()Z so ads skip even before A() has run.
//
//  li.b vs li.e (verified smali, v3.0.16.0804.03):
//    li/b — does NOT have a()Z; has c(Z)V, d(Z)V (state writers)
//    li/e — HAS a()Z (the MMKV reader the SDK consults)
//  Correct priority: try li/e first (confirmed carrier), li/b as fallback.
//
//  *** HD OVERLAY CHAIN (during video playback) ***
//  Server → updateVipResolutionTipOrCreate (MemberResolutionDao.b via DefaultImpls.b)
//    writes vipResolutionTip=true to Room DB column.
//  ResolutionMemberManager reads MemberResolutionBean from DB:
//    if getVipResolutionTip()=true → show LongVodResolutionMemberTipView ("Unlock HD" banner)
//    if isUnlock()=false           → show LongVodMemberNoFreeResolutionView (HD overlay)
//  Three-layer fix:
//    (1) MemberResolutionBean.isUnlock()          → return Boolean.TRUE
//    (2) MemberResolutionBean.getVipResolutionTip()→ return Boolean.FALSE
//    (3) MemberResolutionDao$DefaultImpls.b()     → noop (prevents server writing true to DB)
//        Identified via stable class name MemberResolutionDao$updateVipResolutionTipOrCreate$1
//        in EnclosingMethod annotation → DefaultImpls.b(DAO,String,I,I,Z,Continuation)
//
//  *** DOWNLOAD PAYWALL CHAIN ***
//  IPremiumApi.g() → PremiumProvider$checkAccess$8$1 → PremiumV2CheckAccessDto (server)
//  DownloadReDetectorGroupMainFragment reads PremiumV2CheckAccessDto.getHasAccess()
//  If false → ITaskCenterApi.d() → TaskCenterProvider.d() → shows TreasureStyleADialog
//
//  *** REGION BYPASS ***
//  NationalInformationManager.d() returns sp_code from MMKV (fallback: real SIM MCC).
//  sp_code="90101" (Transsion test MCC) causes BFF to return {isPassed:true, vipEnable:true}.
//  AppLifeStatusInterceptor.j/k noop stops the UI redirect on region-blocked response.
//  Note: live sports ads (sportslivetoday.com, sportsnow.top) are WebView-partner-controlled
//  and cannot be intercepted at smali level.
//
// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv)  v1.1.6.0723.03  vc=50040011
// ═══════════════════════════════════════════════════════════════════
//  TvServiceLocator VIP gate history:
//    v1.1.4.0710.03: TvServiceLocator.V()Z was the live boolean VIP check
//    v1.1.6.0723.03: V()Z now returns cl.w (VIP repository object, not boolean).
//      The boolean check moved to TvServiceLocator.Z()Z, delegating to singleton
//      com.transsion.tvdata.x.a()Z which reads a MutableStateFlow<Boolean> (default false)
//      set by refreshVipStateFromServer.
//  Patch the singleton directly — stable even if TvServiceLocator's accessor letter changes.
//
//  TV LIVE STREAM BUG (if VIP=true breaks live TV load):
//  LiveDetailViewModel.L()V: if V()Z=true → emits only integer stream ID to StateFlow,
//    does NOT call kl.a.b(url, id) → player gets ID but no URL → stream fails silently.
//  Fix: prepend const/4 v1, 0x0 before the if-eqz that gates on V()Z so it always takes
//  the non-VIP (full URL) path regardless of the VIP flag.

// ─── Phone + India ────────────────────────────────────────────────

@Suppress("unused")
val unlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP features in MovieBox and MovieBox India.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {

        // ── MemberCheckResult: server membership response bean ──────
        var cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberCheckResult;")
            ?: throw PatchException("MovieBox: MemberCheckResult not found.")

        cls.methods.firstOrNull {
            it.name == "isPassed" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberCheckResult.isPassed() not found.")

        for (name in listOf("getVipEnable", "getVipPayEnable")) {
            cls.methods.firstOrNull {
                it.name == name && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    return-object v0
                """.trimIndent(),
            ) ?: throw PatchException("MovieBox: MemberCheckResult.$name() not found.")
        }

        // ── MemberInfo: full member detail bean ─────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberInfo;")
            ?: throw PatchException("MovieBox: MemberInfo not found.")

        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberInfo.isActive()Z not found.")

        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """.trimIndent(),
        )

        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberInfo.getExpiryDate() not found.")

        cls.methods.firstOrNull {
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        )

        cls.methods.firstOrNull {
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

        // ── MemberBriefInfo: lightweight member summary bean ─────────
        cls = mutableClassDefByOrNull("Lcom/transsion/member/bean/MemberBriefInfo;")
            ?: throw PatchException("MovieBox: MemberBriefInfo not found.")

        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )

        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x2
                return v0
            """.trimIndent(),
        )

        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        )

        // ── MemberProvider: runtime MMKV membership flag cache ───────
        // d()Z reads "kv_is_pay_enable_member" → gating paywall features
        // f()Z reads "kv_is_skip_ad"           → gating ad suppression
        // w(F)V triggers ClaimMemberDialog (the VIP upsell popup)
        cls = mutableClassDefByOrNull("Lcom/transsion/member/MemberProvider;")
            ?: throw PatchException("MovieBox: MemberProvider not found.")

        cls.methods.firstOrNull {
            it.name == "d" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberProvider.d()Z not found.")

        cls.methods.firstOrNull {
            it.name == "f" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberProvider.f()Z not found.")

        cls.methods.firstOrNull {
            it.name == "w" && it.returnType == "V" && it.parameterTypes == listOf("F")
        }?.addInstructions(0, "return-void")
            ?: throw PatchException("MovieBox: MemberProvider.w(F)V not found.")

        // ── NationalInformationManager: region/country code spoof ────
        // d() returns sp_code from MMKV; "90101" = Transsion test MCC which makes
        // the BFF return {isPassed:true, vipEnable:true} from /vip/member/rights-check.
        cls = mutableClassDefByOrNull("Lcom/transsion/ad/strategy/NationalInformationManager;")
            ?: throw PatchException("MovieBox: NationalInformationManager not found.")

        cls.methods.firstOrNull {
            it.name == "d" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "90101"
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: NationalInformationManager.d() not found.")

        // ── ObserveLoginAction: prevent logout from resetting skip-ad flag ──
        // onLogout() writes kv_is_skip_ad=false; nooping it preserves our patched state.
        cls = mutableClassDefByOrNull("Lcom/transsion/member/ObserveLoginAction;")
            ?: throw PatchException("MovieBox: ObserveLoginAction not found.")

        cls.methods.firstOrNull {
            it.name == "onLogout" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply {
            clearBody()
            addInstructions(0, "return-void")
        } ?: throw PatchException("MovieBox: ObserveLoginAction.onLogout()V not found.")

        // ── Ad-skip state reader (li/e.a()Z) ────────────────────────
        // li/e.a()Z reads MMKV key "j376W52LrKvau6r8" — the boolean scene.c.g() consults
        // to decide whether to suppress ads for the current session.
        // Patching getVipEnable/getVipPayEnable/isPassed causes A() to write true to that
        // key. This patch is belt+suspenders: ensures skip=true even before A() has run
        // (e.g. first launch before the server response arrives).
        //
        // Verified smali v3.0.16.0804.03:
        //   li/e has a()Z  ← confirmed, the MMKV reader
        //   li/b lacks a()Z (it has c/d state writers only)
        // Try li/e first; fall back to li/b for older builds where the roles were swapped.
        val adSkipCls = mutableClassDefByOrNull("Lli/e;")
            ?: mutableClassDefByOrNull("Lli/b;")

        adSkipCls?.methods?.firstOrNull {
            it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )

        // ── PremiumV2CheckAccessDto: download access server response ─
        // getHasAccess()=true prevents the "TreasureStyleADialog" download paywall.
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/PremiumV2CheckAccessDto;")
            ?: throw PatchException("MovieBox: PremiumV2CheckAccessDto not found.")

        cls.methods.firstOrNull {
            it.name == "getHasAccess" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: PremiumV2CheckAccessDto.getHasAccess() not found.")

        // ── PremiumProvider: player entitlement quota gates ──────────
        // j()I and k()I return remaining quota counts (episodes, time, etc.).
        // Returning Int.MAX_VALUE (0x7fff0000 via const/high16) makes the player
        // treat the user as having unlimited quota.
        cls = mutableClassDefByOrNull("Lcom/transsion/member/premium/PremiumProvider;")
            ?: throw PatchException("MovieBox: PremiumProvider not found.")

        cls.methods.firstOrNull {
            it.name == "j" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/high16 v0, 0x7fff0000
                return v0
            """.trimIndent(),
        )

        cls.methods.firstOrNull {
            it.name == "k" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/high16 v0, 0x7fff0000
                return v0
            """.trimIndent(),
        )

        // ── MemberResolutionBean: per-episode HD resolution lock ─────
        // isUnlock()=true   → player shows HD quality selector without restriction.
        // getVipResolutionTip()=false → hides "Unlock HD" banner overlay in the player.
        // See also the DAO noop below which prevents the server from writing true to the DB.
        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MovieBox: MemberResolutionBean not found.")

        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberResolutionBean.isUnlock() not found.")

        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberResolutionBean.getVipResolutionTip() not found.")

        // ── MemberResolutionDao.updateVipResolutionTipOrCreate noop ──
        // The server response triggers a Room DB write of vipResolutionTip=true via:
        //   MemberResolutionDao (interface) → g.smali.c() → lambda f → g.j() → g.r()
        //   which executes: UPDATE member_resolution SET vipResolutionTip = ? ...
        // The DAO interface method 'b' in DefaultImpls is the stable entry point,
        // identified via the non-obfuscated coroutine class name in its EnclosingMethod.
        // Nooping it prevents the server from ever persisting vipResolutionTip=true,
        // so the MemberResolutionBean getter patches above never see a stale DB value.
        val resolutionDaoImpls = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/db/member/MemberResolutionDao\$DefaultImpls;",
        )
        resolutionDaoImpls?.methods?.firstOrNull {
            // DefaultImpls.b() = updateVipResolutionTipOrCreate, confirmed via EnclosingMethod
            // annotation on MemberResolutionDao$updateVipResolutionTipOrCreate$1
            it.name == "b" &&
                it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes == listOf(
                    "Lcom/transsion/baselib/db/member/MemberResolutionDao;",
                    "Ljava/lang/String;",
                    "I",
                    "I",
                    "Z",
                    "Lkotlin/coroutines/Continuation;",
                )
        }?.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
                    return-object v0
                """.trimIndent(),
            )
        }

        // ── Download paywall: per-content VIP tier requirement ───────
        // getRequireMemberType()=0 signals "no membership required" to the download manager.
        for (className in listOf(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
            "Lcom/transsion/baselib/db/download/VipInfo;",
            "Lcom/transsion/moviedetailapi/DownloadItem;",
            "Lcom/transsion/shorttv/bean/DownloadItem;",
            "Lcom/transsion/shorttv_pugc/bean/DownloadItem;",
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getRequireMemberType" &&
                        it.returnType == "Ljava/lang/Integer;" &&
                        it.parameterTypes.isEmpty()
                }?.addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                        move-result-object v0
                        return-object v0
                    """.trimIndent(),
                )
        }

        // DownloadResolutionItem uses a primitive int, not boxed Integer
        mutableClassDefByOrNull("Lcom/transsion/moviedetailapi/bean/DownloadResolutionItem;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """.trimIndent(),
            )

        mutableClassDefByOrNull("Lcom/transsion/baselib/db/download/DownloadBean;")
            ?: throw PatchException("MovieBox: DownloadBean not found — re-derive.")

        // ── AppLifeStatusInterceptor: region-block UI redirect noop ──
        // j(String,String)V and k(String)V redirect the UI to a region-blocked screen.
        // Note: changed from public to private static final in v3.0.16.0804.03;
        // matched by name+signature without access flag constraint so still found.
        val interceptorClass = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/net/AppLifeStatusInterceptor;",
        )

        if (interceptorClass != null) {
            interceptorClass.methods.firstOrNull {
                it.name == "j" && it.returnType == "V" &&
                    it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
            }?.apply {
                clearBody()
                addInstructions(0, "return-void")
            } ?: throw PatchException("MovieBox: AppLifeStatusInterceptor.j(String,String)V not found.")

            interceptorClass.methods.firstOrNull {
                it.name == "k" && it.returnType == "V" &&
                    it.parameterTypes == listOf("Ljava/lang/String;")
            }?.apply {
                clearBody()
                addInstructions(0, "return-void")
            }
        }

        // TV region gate (BffVisitorLoginData) is absent in Phone/India builds, so
        // the null check below is the correct guard — no exception needed here.
        val visitorLoginData = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffVisitorLoginData;",
        )
        visitorLoginData?.methods?.firstOrNull {
            it.name == "getRegionBlock" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        if (interceptorClass == null && visitorLoginData == null) {
            throw PatchException("MovieBox: No region gate class found — re-derive.")
        }
    }
}

// ─── TV ───────────────────────────────────────────────────────────

@Suppress("unused")
val unlockTvVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP features in MovieBox TV.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {

        // ── Download paywall (shared library, same as Phone/India) ───
        for (className in listOf(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
            "Lcom/transsion/baselib/db/download/VipInfo;",
            "Lcom/transsion/moviedetailapi/DownloadItem;",
            "Lcom/transsion/shorttv/bean/DownloadItem;",
            "Lcom/transsion/shorttv_pugc/bean/DownloadItem;",
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getRequireMemberType" &&
                        it.returnType == "Ljava/lang/Integer;" &&
                        it.parameterTypes.isEmpty()
                }?.addInstructions(
                    0,
                    """
                        const/4 v0, 0x0
                        invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                        move-result-object v0
                        return-object v0
                    """.trimIndent(),
                )
        }

        mutableClassDefByOrNull("Lcom/transsion/moviedetailapi/bean/DownloadResolutionItem;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return v0
                """.trimIndent(),
            )

        mutableClassDefByOrNull("Lcom/transsion/baselib/db/download/DownloadBean;")
            ?: throw PatchException("MovieBox TV: DownloadBean not found — re-derive.")

        // ── MemberResolutionBean: HD lock (same as Phone/India) ─────
        var cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MovieBox TV: MemberResolutionBean not found.")

        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: MemberResolutionBean.isUnlock() not found.")

        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: MemberResolutionBean.getVipResolutionTip() not found.")

        // ── AppLifeStatusInterceptor: region-block redirect noop ─────
        val interceptorClass = mutableClassDefByOrNull(
            "Lcom/transsion/baselib/net/AppLifeStatusInterceptor;",
        )

        if (interceptorClass != null) {
            interceptorClass.methods.firstOrNull {
                it.name == "j" && it.returnType == "V" &&
                    it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
            }?.apply {
                clearBody()
                addInstructions(0, "return-void")
            } ?: throw PatchException("MovieBox TV: AppLifeStatusInterceptor.j(String,String)V not found.")

            interceptorClass.methods.firstOrNull {
                it.name == "k" && it.returnType == "V" &&
                    it.parameterTypes == listOf("Ljava/lang/String;")
            }?.apply {
                clearBody()
                addInstructions(0, "return-void")
            }
        }

        // ── BffVisitorLoginData.getRegionBlock() → false ─────────────
        // Correct fix for TV region gate: let the checkRegionBlock coroutine run
        // normally but always report regionBlock=false so it proceeds instead of
        // redirecting to /app/not_available_tv.
        // (Nooping SplashActivity.n1()V caused a stuck splash — documented above.)
        val visitorLoginData = mutableClassDefByOrNull(
            "Lcom/transsion/tvdata/bean/BffVisitorLoginData;",
        )

        if (visitorLoginData != null) {
            visitorLoginData.methods.firstOrNull {
                it.name == "getRegionBlock" &&
                    it.returnType == "Ljava/lang/Boolean;" &&
                    it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                    return-object v0
                """.trimIndent(),
            )
        }

        if (interceptorClass == null && visitorLoginData == null) {
            throw PatchException("MovieBox TV: No region gate class found — re-derive.")
        }

        // ── BffUserInfoData.isVip() → true ───────────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffUserInfoData;")
            ?: throw PatchException("MovieBox TV: BffUserInfoData not found.")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: BffUserInfoData.isVip() not found.")

        // ── VIP singleton (com.transsion.tvdata.x.a()Z) ──────────────
        // v1.1.6.0723.03: TvServiceLocator.Z()Z delegates to this singleton which
        // reads a MutableStateFlow<Boolean> set by refreshVipStateFromServer.
        // Patching the singleton is more stable than patching TvServiceLocator's
        // accessor letter (which changed from V to Z between minor versions).
        // Fallback: older builds where the boolean lived directly on TvServiceLocator.V()Z.
        val xSingleton = mutableClassDefByOrNull("Lcom/transsion/tvdata/x;")
        if (xSingleton != null) {
            xSingleton.methods.firstOrNull {
                it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            ) ?: throw PatchException("MovieBox TV: com.transsion.tvdata.x.a()Z not found.")
        } else {
            cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/TvServiceLocator;")
                ?: throw PatchException("MovieBox TV: TvServiceLocator not found.")

            cls.methods.firstOrNull {
                it.name == "V" && it.returnType == "Z" && it.parameterTypes.isEmpty()
            }?.addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """.trimIndent(),
            ) ?: throw PatchException("MovieBox TV: TvServiceLocator.V()Z not found.")
        }

        // ── BffSubjectInfo.isVip()Z → false ──────────────────────────
        // Content is marked isVip=true when it requires VIP to watch.
        // Returning false tells the player this content is freely accessible.
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffSubjectInfo;")
            ?: throw PatchException("MovieBox TV: BffSubjectInfo not found.")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x0
                return v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: BffSubjectInfo.isVip()Z not found.")

        // ── LiveDetailViewModel.L()V: force non-VIP live stream path ─
        // When the VIP singleton returns true, L()V emits only an integer stream ID
        // (no URL) to the StateFlow, causing the player to fail silently on live TV.
        // Fix: prepend const/4 v1, 0x0 before the if-eqz that gates on V()Z result,
        // overriding the register to always take the non-VIP (full URL load) branch.
        cls = mutableClassDefByOrNull("Lcom/transsion/tvui/viewmodel/LiveDetailViewModel;")
            ?: throw PatchException("MovieBox TV: LiveDetailViewModel not found.")

        val liveMethod = cls.methods.firstOrNull {
            it.name == "L" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("MovieBox TV: LiveDetailViewModel.L()V not found.")

        val ifEqzIndex = liveMethod.implementation!!.instructions.toList()
            .indexOfFirst { it.opcode == Opcode.IF_EQZ }
        if (ifEqzIndex == -1) throw PatchException("MovieBox TV: L()V if-eqz not found.")

        liveMethod.addInstructions(ifEqzIndex, "const/4 v1, 0x0")

        // ── BffGetVipUserInfoData.isVip() → true ─────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffGetVipUserInfoData;")
            ?: throw PatchException("MovieBox TV: BffGetVipUserInfoData not found.")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox TV: BffGetVipUserInfoData.isVip() not found.")
    }
}
