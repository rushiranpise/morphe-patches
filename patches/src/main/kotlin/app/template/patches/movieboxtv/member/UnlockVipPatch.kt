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
//  MovieBox Phone  (com.community.oneroom)  v3.0.16.0709.03  vc=50020116
//  MovieBox India  (com.community.mbox.in)  v3.0.16.0707.03  vc=50020115
// ═══════════════════════════════════════════════════════════════════
//  MemberProvider:
//    d()Z  — MMKV "kv_is_pay_enable_member"
//    f()Z  — MMKV "kv_is_skip_ad"
//    w(F)V — ClaimMemberDialog trigger
//    A(Function0)V — checkShowAdState: server → writes MMKV + calls li.b.c(isPassed)
//  AppLifeStatusInterceptor.j/k now private static final (same sig, patch matches)
//
//  *** AD GATE CHAIN (fully traced) ***
//  MemberProvider.A() → checkShowAdState$1.d() calls:
//    MemberCheckResult.isPassed()       → MMKV kv_is_skip_ad
//    MemberCheckResult.getVipEnable()   → MMKV kv_is_enable_member
//    MemberCheckResult.getVipPayEnable()→ MMKV kv_is_pay_enable_member
//    li.b.c(isPassed)                   → li.e.c(true) → MMKV "j376W52LrKvau6r8" = true
//  scene.c.g(String)Z reads li.e.a() → key "j376W52LrKvau6r8" → if true = skip ad
//  ObserveLoginAction.onLogout() resets kv_is_skip_ad → false on every logout
//  !! A(Function0)V must NOT be noop'd — it's the pipeline that writes li.e key=true !!
//
//  *** DOWNLOAD PAYWALL CHAIN ***
//  IPremiumApi.g() → PremiumProvider$checkAccess$8$1 → PremiumV2CheckAccessDto(server)
//  DownloadReDetectorGroupMainFragment calls PremiumV2CheckAccessDto.getHasAccess()
//  If false → ITaskCenterApi.d() → TaskCenterProvider.d() → TreasureStyleADialog shown
//
// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv) v1.1.4.0710.03  vc=50040009
// ═══════════════════════════════════════════════════════════════════
//  TvServiceLocator VIP gate: T()Z → V()Z (renamed in 1.1.4.0710.03)
//
//  TV LIVE STREAM BUG (V()Z=true breaks live TV load):
//  LiveDetailViewModel.L()V: if V()Z=true → emits only integer stream ID to StateFlow,
//    does NOT call kl.a.b(url, id) → player gets ID but no URL → stream fails silently.
//  if V()Z=false → calls kl.a.b(url, id) = correct full load.
//  Fix: replace if-eqz on V()Z result in L()V with goto :cond_0 (always non-VIP path).
//  V()Z=true kept for: VOD resolution (TvResolutionManager), settings (SettingsMembership),
//    fullscreen UI (LiveFullScreenFragment), DetailFragment, DetailPlayerViewModel.

// ─── Phone + India: membership gates ─────────────────────────────

//  REGION BYPASS (doc-derived):
//  NationalInformationManager.d() returns sp_code from MMKV → fallback to real SIM MCC.
//  Doc confirms: X-Client-Info sp_code="90101" (Transsion test MCC) causes BFF to return
//    {isPassed:true, vipEnable:true} from /vip/member/rights-check.
//  AppLifeStatusInterceptor.j/k noop stops the UI redirect (already patched).
//  NationalInformationManager.d() spoof prevents ad-strategy region gating upstream.
//  Note: live sports are WebView-partner-controlled (sportslivetoday.com, sportsnow.top) —
//    those ads cannot be patched at smali level.

@Suppress("unused")
val unlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP features in app.",
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOX_IN_COMPATIBILITY)

    execute {
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
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: MemberInfo.getExpiryDate() not found.")

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
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const-string v0, "2035-12-31"
                return-object v0
            """.trimIndent(),
        )

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

        cls = mutableClassDefByOrNull("Lcom/transsion/member/MemberProvider;")
            ?: throw PatchException("MovieBox: MemberProvider not found.")

        cls.methods.firstOrNull {
            it.name == "w" && it.returnType == "V" && it.parameterTypes == listOf("F")
        }?.addInstructions(0, "return-void")
            ?: throw PatchException("MovieBox: MemberProvider.w(F)V not found.")

// ─── Phone + India: ad-check server override suppression ──────────
// NOTE: A(Function0)V (checkShowAdState) must NOT be noop'd — it is the pipeline that
// calls li.b.c(isPassed) → li.e.c(true) which sets MMKV key "j376W52LrKvau6r8"=true.
// That key is what scene.c.g(String)Z reads to decide "current user is member, skip ad".
// Nooping A() leaves the key false → ad SDK still fires. Instead we patch the source
// getters so when A() runs the server result isPassed/getVipEnable/getVipPayEnable all
// return true → MMKV key is written true → ads are suppressed by the SDK itself.
// Belt+suspenders: also patch li.e.a()Z directly so even if A() hasn't run yet ads skip.

        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberCheckResult;")
            ?: throw PatchException("MovieBox: MemberCheckResult not found.")

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

        cls = mutableClassDefByOrNull("Lcom/transsion/member/ObserveLoginAction;")
            ?: throw PatchException("MovieBox: ObserveLoginAction not found.")

        cls.methods.firstOrNull {
            it.name == "onLogout" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply {
            clearBody()
            addInstructions(0, "return-void")
        } ?: throw PatchException("MovieBox: ObserveLoginAction.onLogout()V not found.")

        cls = mutableClassDefByOrNull("Lli/e;")
            ?: throw PatchException("MovieBox: li.e (AdSdkSkipState) not found.")

        cls.methods.firstOrNull {
            it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: li.e.a()Z not found.")

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
// ─── Phone + India: player entitlement gates ──────────────────────

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
// ─── Shared: download unlock (phone + India + TV) ─────────────────

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

        // DownloadResolutionItem returns primitive int, not Integer
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

        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MovieBox: MemberResolutionBean not found.")

        cls.methods.firstOrNull {
            it.name in setOf("getVipResolutionTip", "vipResolutionTip") &&
                it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: vipResolutionTip getter not found.")

// ─── Shared: region bypass (phone + India + TV) ───────────────────

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

        // TV: BffVisitorLoginData.getRegionBlock() → Boolean.FALSE
        // Patching SplashActivity.n1()V (the coroutine launcher) caused a stuck splash screen
        // because n1() returns-void before the coroutine ever calls k1() (the proceed method).
        // Correct fix: let the checkRegionBlock coroutine run normally but always report
        // regionBlock=false so it always takes the k1() branch instead of /app/not_available_tv.
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
            throw PatchException("MovieBox: No region gate class found — re-derive.")
        }
    }
}

// ─── TV──────────────

@Suppress("unused")
val unlockTvVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP Features in app.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {

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

        // DownloadResolutionItem returns primitive int, not Integer
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

        var cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
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

        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MovieBox: MemberResolutionBean not found.")

        cls.methods.firstOrNull {
            it.name in setOf("getVipResolutionTip", "vipResolutionTip") &&
                it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        ) ?: throw PatchException("MovieBox: vipResolutionTip getter not found.")

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

        // TV: BffVisitorLoginData.getRegionBlock() → Boolean.FALSE
        // Patching SplashActivity.n1()V (the coroutine launcher) caused a stuck splash screen
        // because n1() returns-void before the coroutine ever calls k1() (the proceed method).
        // Correct fix: let the checkRegionBlock coroutine run normally but always report
        // regionBlock=false so it always takes the k1() branch instead of /app/not_available_tv.
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
            throw PatchException("MovieBox: No region gate class found — re-derive.")
        }
        
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

        cls = mutableClassDefByOrNull("Lcom/transsion/tvui/viewmodel/LiveDetailViewModel;")
            ?: throw PatchException("MovieBox TV: LiveDetailViewModel not found.")

        val method = cls.methods.firstOrNull {
            it.name == "L" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("MovieBox TV: LiveDetailViewModel.L()V not found.")

        // Find the if-eqz that gates on V()Z result (v1) and force the non-VIP stream path.
        // Instruction layout in L()V:
        //   [4] invoke-virtual V()Z
        //   [5] move-result v1
        //   [6] const-wide/16 v2, 0x0
        //   [7] if-eqz v1, :cond_0   ← replace this with "goto :cond_0"
        val instructions = method.implementation!!.instructions.toList()
        val ifEqzIndex = instructions.indexOfFirst { instr ->
            instr.opcode == Opcode.IF_EQZ
        }
        if (ifEqzIndex == -1) throw PatchException("MovieBox TV: L()V if-eqz not found.")

        method.addInstructions(ifEqzIndex, "const/4 v1, 0x0")

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
