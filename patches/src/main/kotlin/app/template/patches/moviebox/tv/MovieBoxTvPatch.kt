package app.template.patches.moviebox.tv

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_TV_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode

// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv)  v1.1.8.0814.03
// ═══════════════════════════════════════════════════════════════════
//
// CLASSES ABSENT IN THIS VERSION (removed v1.1.7+):
//   AppLifeStatusInterceptor, VipInfo, DownloadItem, DownloadResolutionItem
//
// REGION GATE (v1.1.8):
//   BffVisitorLoginData.getRegionBlock() → false
//   NotAvailableActivity.initView() + NotAvailableTvActivity.initView() → finish()
//   No AppLifeStatusInterceptor. Country code spoof not applicable (TV has no
//   NationalInformationManager). Region is server-side only; bean patch is the fix.
//
// VIP SINGLETON CHAIN:
//   v1.1.4: TvServiceLocator.V()Z
//   v1.1.6: TvServiceLocator.Z()Z → com.transsion.tvdata.x.a()Z
//   v1.1.7+: TvServiceLocator.e0()Z → com.transsion.tvdata.z.a()Z
//   NOTE: com.transsion.tvdata.x in this build is a coroutine lambda, NOT the old VIP singleton
//
// LIVE STREAM BUG: when z.a()=true, LiveDetailViewModel.L()V emits only stream
//   ID (no URL) → player fails silently. Fix: inject const/4 v1,0x0 before first if-eqz.
//
// RENEW/UPSELL SUPPRESSION (TV):
//   BffUserInfoData.getVipLevel() → "2" (premium badge, hides upsell CTAs)
//   BffVisitorLoginData.getRegionBlock() → false (prevents paywall on region devices)
//   BffSubjectInfo.isVip()Z → false (all content freely watchable, no upgrade prompts)
//   DownloadBean.getRequireMemberType() → 0 (no upgrade required for downloads)
//   MemberResolutionBean.getVipResolutionTip() → false (hides "Unlock HD" banner)

@Suppress("unused")
val movieBoxTvPatch = bytecodePatch(
    name = "All-In-One",
    description = "Unlocks TV premium, removes ads and upsells, bypasses region lock and force update."
) {
    compatibleWith(MOVIEBOX_TV_COMPATIBILITY)

    execute {

        val returnBoxedTrue = "sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnBoxedFalse = "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnZeroBoxed = """
            const/4 v0, 0x0
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()

        // ─── BffUserInfoData — VIP display in Settings/profile ────────
        // isVip()Boolean drives bg colour
        // getVipLevel()String drives badge: "1"=basic, "2"=premium, else=free
        // "2" = premium badge + hides upgrade CTAs in SettingsFragment
        var cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffUserInfoData;")
            ?: throw PatchException("TV: BffUserInfoData not found")
        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("TV: BffUserInfoData.isVip() not found")
        cls.methods.firstOrNull {
            it.name == "getVipLevel" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2\"\nreturn-object v0")
            ?: throw PatchException("TV: BffUserInfoData.getVipLevel() not found")

        // ─── BffGetVipUserInfoData ────────────────────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffGetVipUserInfoData;")
            ?: throw PatchException("TV: BffGetVipUserInfoData not found")
        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("TV: BffGetVipUserInfoData.isVip() not found")
        cls.methods.firstOrNull {
            it.name == "getVipLevel" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2\"\nreturn-object v0")

        // ─── BffSubjectInfo — content VIP gate ───────────────────────
        // isVip()Z (primitive) true = VIP-only. false = freely watchable.
        // Returning false removes all "upgrade to watch" prompts on content.
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffSubjectInfo;")
            ?: throw PatchException("TV: BffSubjectInfo not found")
        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            ?: throw PatchException("TV: BffSubjectInfo.isVip()Z not found")

        // ─── BffVisitorLoginData — region gate ───────────────────────
        // Only region gate remaining in v1.1.7+. false = no region block.
        mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffVisitorLoginData;")
            ?.methods?.firstOrNull {
                it.name == "getRegionBlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnBoxedFalse)

        // ─── NotAvailableActivity / NotAvailableTvActivity ───────────
        // Both finish() immediately in initView before any UI renders.
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/NotAvailableActivity;")
            ?.methods?.firstOrNull {
                it.name == "initView" && it.returnType == "V" && it.parameterTypes == listOf("Landroid/os/Bundle;")
            }?.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")

        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/NotAvailableTvActivity;")
            ?.methods?.firstOrNull {
                it.name == "initView" && it.returnType == "V" && it.parameterTypes == listOf("Landroid/os/Bundle;")
            }?.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")

        // ─── MemberResolutionBean — HD resolution lock ────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("TV: MemberResolutionBean not found")
        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("TV: MemberResolutionBean.isUnlock() not found")
        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedFalse)
            ?: throw PatchException("TV: MemberResolutionBean.getVipResolutionTip() not found")

        // ─── DownloadBean — download paywall ─────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/baselib/db/download/DownloadBean;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnZeroBoxed)
            ?: throw PatchException("TV: DownloadBean.getRequireMemberType() not found")

        // ─── Force update bypass ──────────────────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/version/update/RemoteVersionInfo;")
            ?.let { rv ->
                rv.methods.firstOrNull { it.name == "getForceUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                rv.methods.firstOrNull { it.name == "getHasUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // ─── VIP singleton: z.a()Z ────────────────────────────────────
        // v1.1.7+: TvServiceLocator.e0()Z → z.a()Z (StateFlow<Boolean> reader)
        // x is a coroutine lambda in this build — NOT the VIP singleton
        // Fallback: any ()Z on TvServiceLocator itself
        val zSingleton = mutableClassDefByOrNull("Lcom/transsion/tvdata/z;")
        if (zSingleton != null) {
            zSingleton.methods.firstOrNull {
                it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                ?: throw PatchException("TV: com.transsion.tvdata.z.a()Z not found")
        } else {
            val tvsl = mutableClassDefByOrNull("Lcom/transsion/tvdata/TvServiceLocator;")
                ?: throw PatchException("TV: VIP singleton not found (z absent, TvServiceLocator absent)")
            tvsl.methods.firstOrNull { it.returnType == "Z" && it.parameterTypes.isEmpty() }
                ?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                ?: throw PatchException("TV: TvServiceLocator ()Z VIP accessor not found")
        }

        // ─── LiveDetailViewModel.L()V — live stream URL fix ───────────
        // When z.a()=true, L()V emits only stream ID (no URL) → player fails.
        // Inject const/4 v1,0x0 before first if-eqz so it always takes
        // the full-URL non-VIP path regardless of VIP state.
        cls = mutableClassDefByOrNull("Lcom/transsion/tvui/viewmodel/LiveDetailViewModel;")
            ?: throw PatchException("TV: LiveDetailViewModel not found")
        val liveMethod = cls.methods.firstOrNull {
            it.name == "L" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("TV: LiveDetailViewModel.L()V not found")
        val ifEqzIndex = liveMethod.implementation!!.instructions.toList()
            .indexOfFirst { it.opcode == Opcode.IF_EQZ }
        if (ifEqzIndex == -1) throw PatchException("TV: LiveDetailViewModel.L()V if-eqz not found")
        liveMethod.addInstructions(ifEqzIndex, "const/4 v1, 0x0")

        // ─── Scene ad removal — SceneInterceptManager ─────────────────
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
