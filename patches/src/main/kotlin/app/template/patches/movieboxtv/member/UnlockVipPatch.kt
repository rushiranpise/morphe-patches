package app.template.patches.movieboxtv.member

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOXTV_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone  (com.community.oneroom)
//  MovieBox India  (com.community.mbox.in)
// ═══════════════════════════════════════════════════════════════════
//
//  Both apps are Ijiami-packed (stub DEX contains only 4 classes: s/h/e/l/l/*).
//  Morphe patches the DEX as-is at build time — the encrypted payload inside
//  classes.dex (assets/IJMDal.Data) is NOT decrypted during patching.
//  All com.transsion.* classes are inside the encrypted blob and are invisible
//  to mutableClassDefByOrNull. Bytecode patching is NOT possible for these variants.
//  A runtime DEX dump would be required.

// ═══════════════════════════════════════════════════════════════════
//  MovieBox TV  (com.community.mbox.tv)  v1.1.7.0813.03  vc=50040012
// ═══════════════════════════════════════════════════════════════════
//
//  r8-compiled, 4 DEX files. Standard bytecode patching.
//
//  ALL class and method names below are NON-OBFUSCATED (Kotlin data-class
//  property accessors and named interface methods that survive r8 renaming).
//  No single-letter r8-assigned method names are used.
//
//  ── VIP DISPLAY CHAIN ─────────────────────────────────────────────
//  SettingsFragment reads:
//    SettingsMembershipViewModel.l(..., BffUserInfoData, ..., String vipLevel)
//      → builds SettingsMembershipViewModel$b(vipLevel = BffUserInfoData.getVipLevel())
//    $b.h() → returns this.g (vipLevel String)
//    SettingsFragment uses:
//      "1" → ic_vip_tag_basic  (Basic badge)
//      "2" → ic_vip_tag_premium (Premium badge)
//      else → ic_vip_tag_free  (Free badge — default when getVipLevel()=null/"0")
//    BffUserInfoData.isVip() also drives bg colour separately.
//
//  → Patch: getVipLevel() → "2", isVip() → true on both BffUserInfoData
//    and BffGetVipUserInfoData.
//
//  ── VIP SINGLETON CHAIN (changes each minor version) ──────────────
//  v1.1.4: TvServiceLocator.V()Z
//  v1.1.6: TvServiceLocator.Z()Z  → com.transsion.tvdata.x.a()Z
//  v1.1.7: TvServiceLocator.e0()Z → com.transsion.tvdata.z.a()Z
//  Strategy: patch the singleton class (z, with x and TvServiceLocator fallbacks).
//
//  ── CONTENT GATE ──────────────────────────────────────────────────
//  BffSubjectInfo.isVip()Z  — true = VIP-only content, false = free to watch.
//  Patch → false so all content is accessible without account.
//
//  ── HD RESOLUTION LOCK ────────────────────────────────────────────
//  MemberResolutionBean.isUnlock()Boolean → true  (HD available)
//  MemberResolutionBean.getVipResolutionTip()Boolean → false (hide "Unlock HD" banner)
//
//  ── DOWNLOAD PAYWALL ──────────────────────────────────────────────
//  DownloadBean.getRequireMemberType()Integer → 0 (all downloads free tier)
//
//  ── REGION GATE ───────────────────────────────────────────────────
//  BffVisitorLoginData.getRegionBlock()Boolean → false (no region block)
//  AppLifeStatusInterceptor removed in v1.1.7 (was noop'd in v1.1.6).
//
//  ── LIVE STREAM BUG FIX ───────────────────────────────────────────
//  LiveDetailViewModel.L()V: when VIP=true the method emits only stream ID (no URL).
//  Fix: inject const/4 v1, 0x0 before first if-eqz so it always takes the URL path.

@Suppress("unused")
val unlockTvVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks VIP features in MovieBox TV.",
) {
    compatibleWith(MOVIEBOXTV_COMPATIBILITY)

    execute {
        val returnTrue = """
            sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
            return-object v0
        """.trimIndent()

        val returnFalse = """
            sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v0
        """.trimIndent()

        // ── BffUserInfoData ──────────────────────────────────────────
        // isVip()Boolean drives bg colour; getVipLevel()String drives badge icon.
        // Must patch both — isVip() alone leaves "Free" badge because the icon
        // branch reads getVipLevel(), not isVip().
        var cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffUserInfoData;")
            ?: throw PatchException("TV: BffUserInfoData not found")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("TV: BffUserInfoData.isVip() not found")

        // "2" = Premium tier — displays ic_vip_tag_premium badge in SettingsFragment
        cls.methods.firstOrNull {
            it.name == "getVipLevel" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2\"\nreturn-object v0")
            ?: throw PatchException("TV: BffUserInfoData.getVipLevel() not found")

        // ── BffGetVipUserInfoData ────────────────────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffGetVipUserInfoData;")
            ?: throw PatchException("TV: BffGetVipUserInfoData not found")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("TV: BffGetVipUserInfoData.isVip() not found")

        cls.methods.firstOrNull {
            it.name == "getVipLevel" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2\"\nreturn-object v0")

        // ── BffSubjectInfo ───────────────────────────────────────────
        // isVip()Z — true = VIP-only content gate; false = free to watch.
        cls = mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffSubjectInfo;")
            ?: throw PatchException("TV: BffSubjectInfo not found")

        cls.methods.firstOrNull {
            it.name == "isVip" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            ?: throw PatchException("TV: BffSubjectInfo.isVip()Z not found")

        // ── MemberResolutionBean — HD resolution lock ────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("TV: MemberResolutionBean not found")

        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("TV: MemberResolutionBean.isUnlock() not found")

        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnFalse)
            ?: throw PatchException("TV: MemberResolutionBean.getVipResolutionTip() not found")

        // ── DownloadBean — download paywall ──────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/baselib/db/download/DownloadBean;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" &&
                it.returnType == "Ljava/lang/Integer;" &&
                it.parameterTypes.isEmpty()
            }?.addInstructions(0, """
                const/4 v0, 0x0
                invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object v0
                return-object v0
            """.trimIndent())
            ?: throw PatchException("TV: DownloadBean.getRequireMemberType() not found")

        // ── BffVisitorLoginData — region gate ────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/tvdata/bean/BffVisitorLoginData;")
            ?.methods?.firstOrNull {
                it.name == "getRegionBlock" &&
                it.returnType == "Ljava/lang/Boolean;" &&
                it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnFalse)

        // ── VIP singleton — z.a()Z (v1.1.7) ─────────────────────────
        // Fallback chain handles rename across minor versions.
        val zSingleton = mutableClassDefByOrNull("Lcom/transsion/tvdata/z;")
        val xSingleton = mutableClassDefByOrNull("Lcom/transsion/tvdata/x;")
        val tvServiceLocator = mutableClassDefByOrNull("Lcom/transsion/tvdata/TvServiceLocator;")

        when {
            zSingleton != null -> {
                zSingleton.methods.firstOrNull {
                    it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    ?: throw PatchException("TV: com.transsion.tvdata.z.a()Z not found")
            }
            xSingleton != null -> {
                // v1.1.6 fallback
                xSingleton.methods.firstOrNull {
                    it.name == "a" && it.returnType == "Z" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    ?: throw PatchException("TV: com.transsion.tvdata.x.a()Z not found")
            }
            tvServiceLocator != null -> {
                // v1.1.4 fallback — VIP bool lived directly on TvServiceLocator
                tvServiceLocator.methods.firstOrNull {
                    it.returnType == "Z" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    ?: throw PatchException("TV: TvServiceLocator ()Z VIP accessor not found")
            }
            else -> throw PatchException("TV: VIP singleton class not found — re-derive")
        }

        // ── LiveDetailViewModel.L()V — live stream URL fix ───────────
        cls = mutableClassDefByOrNull("Lcom/transsion/tvui/viewmodel/LiveDetailViewModel;")
            ?: throw PatchException("TV: LiveDetailViewModel not found")

        val liveMethod = cls.methods.firstOrNull {
            it.name == "L" && it.returnType == "V" && it.parameterTypes.isEmpty()
        } ?: throw PatchException("TV: LiveDetailViewModel.L()V not found")

        val ifEqzIndex = liveMethod.implementation!!.instructions.toList()
            .indexOfFirst { it.opcode == Opcode.IF_EQZ }
        if (ifEqzIndex == -1) throw PatchException("TV: LiveDetailViewModel.L()V if-eqz not found")

        liveMethod.addInstructions(ifEqzIndex, "const/4 v1, 0x0")
    }
}
