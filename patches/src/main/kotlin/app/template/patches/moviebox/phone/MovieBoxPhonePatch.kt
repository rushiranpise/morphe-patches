package app.template.patches.moviebox.phone

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOXIN_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone  (com.community.oneroom)  v4.0.01.0813.02
// ═══════════════════════════════════════════════════════════════════
//
// All non-obfuscated stable names verified from smali.
// Obfuscated names (R8-assigned, pinned by class+return+params):
//   MemberProvider.c()Z          — isActive wrapper
//   MemberProvider.f()Z          — kv_is_pay_enable_member
//   MemberProvider.g()Z          — kv_is_skip_ad
//   MemberProvider.x(F)V         — showMemberDialog / upsell popup
//   MemberProvider.D()I          — kv_parallel_download_task_num
//   NationalInformationManager.e() — sp_code reader (country code)
//   AppLifeStatusInterceptor.i(S,S)V — dialog-style region popup
//   AppLifeStatusInterceptor.j(S,S)V — TheRouter page_not_available route
//   AppLifeStatusInterceptor.k(S)V   — TheRouter redirect
//   AppLifeStatusInterceptor.n(Chain)Z — OkHttp freeze flag setter
//   MemberResolutionDao$DefaultImpls.b() — updateVipResolutionTipOrCreate
//   PremiumProvider.c/k/u/o/f/i/t/w/x — entitlement methods
//   ShortTVItem.getNeedPaid()I    — live stream paywall gate (stable name)
//   com.transsion.shorttv.bean.Subject.getNeedPaid()I — same

@Suppress("unused")
val movieBoxPhonePatch = bytecodePatch(
    name = "All-In-One",
    description = "Unlocks VIP premium, removes ads and upsells, bypasses region lock " +
        "and force update, unlocks HD and downloads, enables 5 parallel downloads."
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY,MOVIEBOXIN_COMPATIBILITY)

    execute {

        val returnTrue = "const/4 v0, 0x1\nreturn v0"
        val returnFalse = "const/4 v0, 0x0\nreturn v0"
        val returnIntMax = "const/high16 v0, 0x7fff0000\nreturn v0"
        val returnBoxedTrue = "sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnBoxedFalse = "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnInt9999 = """
            const/16 v0, 0x270f
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()
        val returnZeroBoxed = """
            const/4 v0, 0x0
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()

        // ─── MemberCheckResult — server membership response ───────────
        var cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberCheckResult;")
            ?: throw PatchException("MemberCheckResult not found")
        for (name in listOf("isPassed", "getVipEnable", "getVipPayEnable")) {
            cls.methods.firstOrNull {
                it.name == name && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnBoxedTrue)
                ?: throw PatchException("MemberCheckResult.$name() not found")
        }

        // ─── MemberInfo — full member detail bean ────────────────────
        // isActive()Z (primitive), getDaysLeft/getExpiryDate suppress Renew button.
        // daysLeft=9999 > 7 (threshold) → no Renew in MemberGuideBannerView or f0.B().
        // getVipLevel is NOT on MemberInfo — it has getMemberType()I.
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberInfo;")
            ?: throw PatchException("MemberInfo not found")
        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberInfo.isActive()Z not found")
        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        cls.methods.firstOrNull {
            it.name == "getDaysLeft" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnInt9999)
            ?: throw PatchException("MemberInfo.getDaysLeft() not found")
        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")
            ?: throw PatchException("MemberInfo.getExpiryDate() not found")
        cls.methods.firstOrNull {
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberBriefInfo — lightweight member summary bean ────────
        cls = mutableClassDefByOrNull("Lcom/transsion/member/bean/MemberBriefInfo;")
            ?: throw PatchException("MemberBriefInfo not found")
        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberProvider — MMKV membership flag cache ─────────────
        // c()Z = isActive wrapper
        // f()Z = kv_is_pay_enable_member
        // g()Z = kv_is_skip_ad
        // x(F)V = showMemberDialog upsell popup → noop
        // D()I = kv_parallel_download_task_num → 5
        cls = mutableClassDefByOrNull("Lcom/transsion/member/MemberProvider;")
            ?: throw PatchException("MemberProvider not found")
        cls.methods.firstOrNull { it.name == "c" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue) ?: throw PatchException("MemberProvider.c()Z not found")
        cls.methods.firstOrNull { it.name == "f" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue) ?: throw PatchException("MemberProvider.f()Z not found")
        cls.methods.firstOrNull { it.name == "g" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue) ?: throw PatchException("MemberProvider.g()Z not found")
        cls.methods.firstOrNull { it.name == "x" && it.returnType == "V" && it.parameterTypes == listOf("F") }
            ?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("MemberProvider.x(F)V not found")
        cls.methods.firstOrNull { it.name == "D" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")

        // ─── PremiumProvider — entitlement quota and feature gates ───
        // c()Z  isActive wrapper → true
        // k()Z  isVip (vipLevel==1) → true
        // u()Z  isSVip (vipLevel==2) → true (drives "TV Pro" gold label via f0.B())
        // o()   daysLeft backup for player paths → 9999
        // f()I  free_download_count → Int.MAX_VALUE
        // i()I  per_download_resource_count → 5
        // t()I  max_resolution → Int.MAX_VALUE
        // w()I  preview_seconds → Int.MAX_VALUE
        // x()I  free_hd_preview_count → Int.MAX_VALUE
        cls = mutableClassDefByOrNull("Lcom/transsion/member/premium/PremiumProvider;")
            ?: throw PatchException("PremiumProvider not found")
        for (name in listOf("c", "k", "u")) {
            cls.methods.firstOrNull { it.name == name && it.returnType == "Z" && it.parameterTypes.isEmpty() }
                ?.addInstructions(0, returnTrue)
        }
        cls.methods.firstOrNull { it.name == "o" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnInt9999)
        cls.methods.firstOrNull { it.name == "f" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
        cls.methods.firstOrNull { it.name == "i" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")
        for (name in listOf("t", "w", "x")) {
            cls.methods.firstOrNull { it.name == name && it.returnType == "I" && it.parameterTypes.isEmpty() }
                ?.addInstructions(0, returnIntMax)
        }

        // ─── NationalInformationManager — country code spoof ─────────
        // e()Ljava/lang/String; reads MMKV "sp_code" (SIM MCC).
        // "90101" = Transsion test MCC → BFF returns {isPassed:true, vipEnable:true}
        // → no 471/472 responses → no region-block redirects → app stays responsive.
        cls = mutableClassDefByOrNull("Lcom/transsion/ad/strategy/NationalInformationManager;")
            ?: throw PatchException("NationalInformationManager not found")
        cls.methods.firstOrNull {
            it.name == "e" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"90101\"\nreturn-object v0")
            ?: throw PatchException("NationalInformationManager.e() not found")

        // ─── ObserveLoginAction — prevent logout resetting skip-ad ───
        cls = mutableClassDefByOrNull("Lcom/transsion/member/ObserveLoginAction;")
            ?: throw PatchException("ObserveLoginAction not found")
        cls.methods.firstOrNull {
            it.name == "onLogout" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("ObserveLoginAction.onLogout()V not found")

        // ─── PremiumV2CheckAccessDto — download access server response
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/PremiumV2CheckAccessDto;")
            ?: throw PatchException("PremiumV2CheckAccessDto not found")
        cls.methods.firstOrNull {
            it.name == "getHasAccess" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("PremiumV2CheckAccessDto.getHasAccess() not found")

        // ─── MemberResolutionBean — per-episode HD resolution lock ───
        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MemberResolutionBean not found")
        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("MemberResolutionBean.isUnlock() not found")
        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedFalse)
            ?: throw PatchException("MemberResolutionBean.getVipResolutionTip() not found")

        // ─── MemberResolutionDao$DefaultImpls — prevent DB vipTip write
        mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionDao\$DefaultImpls;")
            ?.methods?.firstOrNull { it.name == "b" && it.returnType == "Ljava/lang/Object;" && it.parameterTypes.size == 6 }
            ?.apply {
                clearBody()
                addInstructions(0, "sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;\nreturn-object v0")
            }

        // ─── Download paywall — all getRequireMemberType → 0 ─────────
        for (className in listOf(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
            "Lcom/transsion/baselib/db/download/VipInfo;",
            "Lcom/transsion/moviedetailapi/DownloadItem;",
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getRequireMemberType" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, returnZeroBoxed)
        }
        mutableClassDefByOrNull("Lcom/transsion/moviedetailapi/bean/DownloadResolutionItem;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")

        // ─── ShortTV live stream paywall — getNeedPaid()I → 0 ────────
        // ShortTVItem.needPaid: nonzero = paid content → starts 10-min countdown
        // then calls watchAdToUnlock → SceneInterceptManager("ShortTvPlayerUnlockPlayScene")
        // Returning 0 marks all live/short-TV content as free → no countdown starts.
        for (className in listOf(
            "Lcom/transsion/shorttv/bean/ShortTVItem;",
            "Lcom/transsion/shorttv/bean/Subject;",
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getNeedPaid" && it.returnType == "I" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ─── AppLifeStatusInterceptor — region bypass ─────────────────
        // i(String,String)V — dialog popup via interface g (code 472)
        // j(String,String)V — TheRouter route to /main/page_not_available (code 471)
        // k(String)V        — TheRouter redirect (code 403)
        // n(Chain)Z         — freeze flag setter → false (prevents AtomicBoolean freeze)
        // Combined with NationalInformationManager.e()="90101": BFF returns success
        // so 471/472/403 never fire. These noops are belt-and-suspenders.
        val interceptor = mutableClassDefByOrNull("Lcom/transsion/baselib/net/AppLifeStatusInterceptor;")
            ?: throw PatchException("AppLifeStatusInterceptor not found")
        interceptor.methods.firstOrNull {
            it.name == "i" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
        interceptor.methods.firstOrNull {
            it.name == "j" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("AppLifeStatusInterceptor.j(String,String)V not found")
        interceptor.methods.firstOrNull {
            it.name == "k" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
        interceptor.methods.firstOrNull {
            it.name == "n" && it.returnType == "Z" && it.parameterTypes == listOf("Lokhttp3/Interceptor\$Chain;")
        }?.addInstructions(0, returnFalse)
            ?: throw PatchException("AppLifeStatusInterceptor.n(Chain)Z not found")

        // ─── NotAvailableActivity — region-lock wall ──────────────────
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/NotAvailableActivity;")
            ?.methods?.firstOrNull {
                it.name == "initView" && it.returnType == "V" && it.parameterTypes == listOf("Landroid/os/Bundle;")
            }?.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")

        // ─── Force update bypass ──────────────────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/version/update/RemoteVersionInfo;")
            ?.let { rv ->
                rv.methods.firstOrNull { it.name == "getForceUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
                rv.methods.firstOrNull { it.name == "getHasUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
            }

        // ─── Scene ad removal — SceneInterceptManager ─────────────────
        // Suspend gate for all scene ads including ShortTvPlayerUnlockPlayScene
        // (the live stream "go premium" overlay). Returns Pair(true,"no ads").
        mutableClassDefByOrNull("Lcom/transsion/ad/scene/SceneInterceptManager;")
            ?.methods?.firstOrNull {
                it.name == "a" && it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes == listOf("Ljava/lang/String;", "Lkotlin/coroutines/Continuation;")
            }?.addInstructions(0, """
                new-instance v0, Lkotlin/Pair;
                const/4 v1, 0x1
                invoke-static {v1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v1
                const-string v2, "no ads"
                invoke-direct {v0, v1, v2}, Lkotlin/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
                return-object v0
            """.trimIndent())

        // ─── Splash ad redirect ────────────────────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/SplashActivity;")
            ?.methods?.firstOrNull { m ->
                m.returnType == "V" && m.implementation?.instructions?.any {
                    it.toString().contains("startSplashAdLoad\$1")
                } == true
            }?.addInstructions(0, """
                const/4 v0, 0x0
                invoke-direct {p0, v0}, Lcom/transsion/subroom/activity/SplashActivity;->e0(Z)V
                return-void
            """.trimIndent())

        // ─── Mintegral ad executor kill points ────────────────────────
        for ((cls2, method) in listOf(
            "Lcom/hisavana/mintegral/executer/MintegralVideo;" to "initVideo",
            "Lcom/hisavana/mintegral/executer/MintegralBanner;" to "showBanner",
            "Lcom/hisavana/mintegral/executer/MintegralNative;" to "initNative",
            "Lcom/hisavana/mintegral/executer/MintegralInterstitial;" to "initInterstitial",
            "Lcom/hisavana/mintegral/executer/MintegralSplash;" to "onSplashStartLoad",
        )) {
            mutableClassDefByOrNull(cls2)
                ?.methods?.firstOrNull { it.name == method && it.returnType == "V" }
                ?.addInstructions(0, "return-void")
        }
    }
}
