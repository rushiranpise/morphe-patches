package app.template.patches.blockblast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLOCKBLAST_COMPATIBILITY

private const val UNLOCK_VIP = "Unlock VIP"
private const val REMOVE_ADS = "Remove ads"

// VIP hides every ad placement including rewarded revives, while "Remove ads"
// fakes rewarded ads as watched, so the two conflict. The patcher has no
// incompatibility API: the first patch to execute claims the slot, the second
// fails. Reset in finalize because the bundle may be reused for another run.
private var appliedAdPatch: String? = null

private fun claimAdPatch(name: String) {
    val other = appliedAdPatch
    if (other != null && other != name) {
        appliedAdPatch = null
        throw PatchException("\"$UNLOCK_VIP\" and \"$REMOVE_ADS\" are mutually exclusive, select only one.")
    }
    appliedAdPatch = name
}

/**
 * Unlock VIP — grants permanent VIP (ad-free subscription) status in Block Blast.
 *
 * ── Architecture ─────────────────────────────────────────────────────────────
 * Block Blast is Cocos2d-JS. Game logic runs in V8 JS (.jsc bytecode). VIP state
 * lives in SPStore, a C++ singleton in libcocos2djs.so accessible via JNI.
 * JS reads VIP directly from the native SPStore — not via any Java method.
 *
 * Since 10.8.1 the org.cocos2dx.javascript classes (SPStore, JsCallJava) are
 * encrypted in assets/nedata.db (NetEase Yidun, libnesec.so) and loaded at
 * runtime, so their signatures can only be read from call sites in the dex.
 *
 * ── VIP state write/notify chain ────────────────────────────────────────────
 *
 * 1. cg/j.e(String) — subscription state processor (called by billing & cg/m)
 *    Reads SPStore.p()I (sign_status) and cg/j.d(productId)Z (fill/verify check).
 *    If it's a real purchase result AND sign_status != 1:
 *      → calls JsCallJava.notifySubStateUpdate() to tell JS to re-read SPStore
 *    If it's a fill/verify check: → calls cg/j.f() to trigger network query
 *
 * 2. cg/g.o(JSONObject, cg/g$b) — server query response parser
 *    Reads expire_in_seconds / expire_time from /user/subscription response.
 *    Computes isVip = (expire_in_seconds > 0).
 *    Calls SPStore.L(isVip) to persist VIP state.
 *    Returns sign_status (I) → caller passes to cg/g$b.b(I) callback.
 *
 * 3. cg/m.q() — in-app purchase handler
 *    Same expire_in_seconds check → SPStore.L(isVip).
 *
 * ── Why previous patches failed ──────────────────────────────────────────────
 * cg/g.o() returned sign_status=1 immediately after setting SPStore.L(true).
 * BUT cg/j.e() only calls notifySubStateUpdate() when sign_status != 1 —
 * so JS was never told to re-read SPStore, and never saw VIP=true.
 *
 * cg/m.q() returned early but is never called without an actual purchase,
 * so it never fired for sideloaded apps.
 *
 * ── Patch strategy ───────────────────────────────────────────────────────────
 * Patch THREE methods:
 *
 * ① cg/j.e(String) — the notify hub:
 *   Set SPStore.L(true), then call JsCallJava.notifySubStateUpdate() immediately.
 *   This fires whenever billing has any subscription state data AND tells JS right away.
 *
 * ② cg/g.o(JSONObject, cg/g$b) — server response parser:
 *   Set SPStore.L(true), return 0 (sign_status=0 != 1) so the caller's
 *   cg/j.e() path falls into the notifySubStateUpdate() branch.
 *
 * ③ cg/m.q() — purchase handler (fallback for purchase path):
 *   Set SPStore.L(true), call notifySubStateUpdate() directly, return-void.
 */
@Suppress("unused")
val blockBlastUnlockVipPatch = bytecodePatch(
    name = UNLOCK_VIP,
    description = "Unlocks VIP, the ad-free subscription. Removes all ads including revive offers. " +
        "Mutually exclusive with \"$REMOVE_ADS\".",
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)

    finalize { appliedAdPatch = null }

    execute {
        claimAdPatch(UNLOCK_VIP)

        val setVipAndNotify = """
            invoke-static {}, Lorg/cocos2dx/javascript/pay/SPStore;->k()Lorg/cocos2dx/javascript/pay/SPStore;
            move-result-object v0
            const/4 v1, 0x1
            invoke-virtual {v0, v1}, Lorg/cocos2dx/javascript/pay/SPStore;->L(Z)V
            invoke-static {}, Lorg/cocos2dx/javascript/JsCallJava;->notifySubStateUpdate()V
        """

        // ① cg/j.e(String) — subscription state hub → set VIP + notify JS + return
        mutableClassDefBy("Lcg/j;")
            .methods.first { it.name == "e" && it.returnType == "V" }
            .addInstructions(0, "$setVipAndNotify\n            return-void")

        // ② cg/g.o(JSONObject, cg/g$b) → returns I
        //    Return 0 (sign_status != 1) so cg/j.e() will call notifySubStateUpdate()
        mutableClassDefBy("Lcg/g;")
            .methods.first { it.name == "o" && it.returnType == "I" }
            .addInstructions(0, """
                invoke-static {}, Lorg/cocos2dx/javascript/pay/SPStore;->k()Lorg/cocos2dx/javascript/pay/SPStore;
                move-result-object v0
                const/4 v1, 0x1
                invoke-virtual {v0, v1}, Lorg/cocos2dx/javascript/pay/SPStore;->L(Z)V
                const/4 v0, 0x0
                return v0
            """)

        // ③ cg/m.q() — purchase handler fallback
        mutableClassDefBy("Lcg/m;")
            .methods.first { it.name == "q" && it.returnType == "V" }
            .addInstructions(0, "$setVipAndNotify\n            return-void")
    }
}

/**
 * Remove ads — blocks banner and interstitial ads and grants rewarded-ad rewards
 * (revive, extra moves, …) without showing the ad.
 *
 * Block Blast shows ads through several independent layers; each show entry point
 * is replaced with the callback sequence its SDK adapter emits after a finished ad.
 *
 * A. React Native ad module, MAX part (y1/i "HuAL"):
 *   z1/d.n(Activity, WAdConfig, r1/f) rewarded: displayed (r1/f.d), rewarded
 *   (r1/e.a on the load listener z1/d.d), hidden with rewarded=true (r1/f.c).
 *   z1/d.h() isReady → true, so revive is offered even with no fill.
 *   z1/c.n(Activity, r1/d) interstitial: displayed (r1/d.c), hidden (r1/d.a).
 *   z1/d.n has no local registers and z1/c.n only one, so the no longer needed
 *   parameter registers (and `this` in z1/d.n) are reused as scratch.
 *
 * B. React Native ad module, direct "hot" channels (Meta, Moloco, Pangle, BigO, …):
 *   Every channel ad is held in a g4/b (rewarded) or f4/b (interstitial) wrapper whose
 *   show i/j wraps the listener in g4/b$b / f4/b$b, stamping channel, keyId and adUnitId.
 *   The fake builds that same wrapper listener and reports impression (g.b, g.c),
 *   for rewarded isEarned=true + rewarded (g4/d.a), then closed (g.f).
 *
 * C. Channel manager m3/b (AdChannelManager):
 *   N(…, w) showRewardAd: impression (p.a), rewarded (w.e), closed earned=true (w.b).
 *   M(…, v) showIntersAd: impression (p.a), dismissed (v.d).
 *
 * D. Cocos mediation facade g5/c (PAdSDKContext), reached via g2/a and j3, i3, s2:
 *   Interstitial D/E/F/G(String, Activity, d2/l): displayed (a), hidden (d).
 *   Rewarded S/T/U/V(String, Activity, e2/l): displayed (b), rewarded (a), hidden (d, TRUE).
 *   Rewarded isLoading/isReady M()/N() → true. Its banners end in MAX/AdMob views (E).
 *
 * E. Banners at SDK level: MaxAdView.loadAd/startAutoRefresh and AdMob
 *   BaseAdView.loadAd → return-void, so no banner is ever fetched.
 */
@Suppress("unused")
val blockBlastRemoveAdsPatch = bytecodePatch(
    name = REMOVE_ADS,
    description = "Removes banner and interstitial ads and grants rewarded ad rewards like revive without watching. " +
        "Mutually exclusive with \"$UNLOCK_VIP\".",
    default = false,
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)

    finalize { appliedAdPatch = null }

    execute {
        claimAdPatch(REMOVE_ADS)

        fun method(type: String, name: String, vararg parameters: String) =
            mutableClassDefBy(type).methods.first { method ->
                method.name == name && method.parameterTypes.map { it.toString() } == parameters.toList()
            }

        val activity = "Landroid/app/Activity;"
        val config = "Lcom/block/juggle/ad/almax/api/WAdConfig;"
        val track = "Lcom/block/juggle/ad/almax/api/RctAdTrackConfig;"
        val trackBuilder = "Lcom/block/juggle/ad/almax/api/RctAdTrackConfig\$Builder;"
        fun buildTrack(adType: String, target: String, scratch: String) = """
            invoke-static {}, $trackBuilder->newBuilder()$trackBuilder
            move-result-object $target
            const/4 $scratch, $adType
            invoke-virtual {$target, $scratch}, $trackBuilder->setAdType(I)$trackBuilder
            move-result-object $target
            invoke-virtual {$target}, $trackBuilder->build()$track
            move-result-object $target
        """

        // No local registers here (v0 is p0), so read the load listener before `this` is reused.
        method("Lz1/d;", "n", activity, config, "Lr1/f;").addInstructions(
            0,
            """
                if-eqz p3, :done
                iget-object p2, p0, Lz1/d;->d:Lr1/e;
                ${buildTrack("0x3", "p0", "p1")}
                invoke-interface {p3, p0}, Lr1/f;->d($track)V
                if-eqz p2, :hidden
                invoke-interface {p2, p0}, Lr1/e;->a($track)V
                :hidden
                const/4 p1, 0x1
                invoke-interface {p3, p0, p1}, Lr1/f;->c(${track}Z)V
                :done
                return-void
            """,
        )
        method("Lz1/d;", "h").addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """,
        )
        method("Lz1/c;", "n", activity, "Lr1/d;").addInstructions(
            0,
            """
                if-eqz p2, :done
                ${buildTrack("0x2", "v0", "p1")}
                invoke-interface {p2, v0}, Lr1/d;->c($track)V
                invoke-interface {p2, v0}, Lr1/d;->a($track)V
                :done
                return-void
            """,
        )

        val adContext = "Lg5/c;"
        val loadConfig = "iget-object v0, p0, $adContext->a:$config"

        listOf("D", "E", "F", "G").forEach {
            method(adContext, it, "Ljava/lang/String;", activity, "Ld2/l;").addInstructions(
                0,
                """
                    if-eqz p3, :done
                    $loadConfig
                    invoke-interface {p3, v0}, Ld2/l;->a($config)V
                    invoke-interface {p3, v0}, Ld2/l;->d($config)V
                    :done
                    return-void
                """,
            )
        }

        listOf("S", "T", "U", "V").forEach {
            method(adContext, it, "Ljava/lang/String;", activity, "Le2/l;").addInstructions(
                0,
                """
                    if-eqz p3, :done
                    $loadConfig
                    invoke-interface {p3, v0}, Le2/l;->b($config)V
                    invoke-interface {p3, v0}, Le2/l;->a($config)V
                    sget-object v1, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                    invoke-interface {p3, v0, v1}, Le2/l;->d(${config}Ljava/lang/Boolean;)V
                    :done
                    return-void
                """,
            )
        }

        listOf("M", "N").forEach {
            method(adContext, it).addInstructions(
                0,
                """
                    const/4 v0, 0x1
                    return v0
                """,
            )
        }

        val directTrack = "Lcom/block/juggle/ad/channels/hot/base/HuDirectAdTrackConfig;"
        val directTrackBuilder = "Lcom/block/juggle/ad/channels/hot/base/HuDirectAdTrackConfig\$Builder;"
        val directListener = "Lcom/block/juggle/ad/channels/hot/base/g;"
        // The wrapper listener stamps channel, keyId and adUnitId the game uses to match the reward.
        fun directShow(wrapper: String, show: String, listener: String, earn: String) =
            method(wrapper, show, activity, directTrack, listener).addInstructions(
                0,
                """
                    if-eqz p3, :done
                    invoke-virtual {p0}, $wrapper->c()Ljava/lang/String;
                    move-result-object v0
                    iget-object v1, p0, $wrapper->c:Ljava/lang/String;
                    new-instance v2, ${wrapper.dropLast(1)}${'$'}b;
                    invoke-direct {v2, p0, v0, v1, p3}, ${wrapper.dropLast(1)}${'$'}b;-><init>(${wrapper}Ljava/lang/String;Ljava/lang/String;$listener)V
                    if-nez p2, :track
                    invoke-static {}, $directTrack->newBuilder()$directTrackBuilder
                    move-result-object p2
                    invoke-virtual {p2}, $directTrackBuilder->build()$directTrack
                    move-result-object p2
                    :track
                    invoke-interface {v2, p2}, $directListener->b($directTrack)V
                    invoke-interface {v2, p2}, $directListener->c($directTrack)V
                    $earn
                    invoke-interface {v2, p2}, $directListener->f($directTrack)V
                    :done
                    return-void
                """,
            )
        directShow(
            "Lg4/b;",
            "i",
            "Lg4/d;",
            """
                const/4 v0, 0x1
                iput-boolean v0, p2, $directTrack->isEarned:Z
                invoke-interface {v2, p2}, Lg4/d;->a($directTrack)V
            """,
        )
        directShow("Lf4/b;", "j", "Lf4/d;", "")

        val channel = "Lcom/block/juggle/ad/channels/base/a;"
        val channelAd = "Lcom/block/juggle/ad/channels/base/d;"
        val adFormat = "Lcom/block/juggle/ad/channels/base/b;"
        val channelListener = "Lcom/block/juggle/ad/channels/base/p;"
        fun channelInfo(format: String) = """
            sget-object v0, $adFormat->$format:$adFormat
            invoke-virtual {p0, p1, v0, p3}, Lm3/b;->e($channel${adFormat}Ljava/lang/String;)$channelAd
            move-result-object v0
            invoke-interface {p5, v0}, $channelListener->a($channelAd)V
        """
        method("Lm3/b;", "M", channel, activity, "Ljava/lang/String;", channelAd, "Lcom/block/juggle/ad/channels/base/v;")
            .addInstructions(
                0,
                """
                    if-eqz p5, :done
                    ${channelInfo("interstitialAd")}
                    invoke-interface {p5, v0}, Lcom/block/juggle/ad/channels/base/v;->d($channelAd)V
                    :done
                    return-void
                """,
            )
        method("Lm3/b;", "N", channel, activity, "Ljava/lang/String;", channelAd, "Lcom/block/juggle/ad/channels/base/w;")
            .addInstructions(
                0,
                """
                    if-eqz p5, :done
                    ${channelInfo("rewardAd")}
                    invoke-interface {p5, v0}, Lcom/block/juggle/ad/channels/base/w;->e($channelAd)V
                    const/4 v1, 0x1
                    invoke-interface {p5, v0, v1}, Lcom/block/juggle/ad/channels/base/w;->b(${channelAd}Z)V
                    :done
                    return-void
                """,
            )

        method("Lcom/applovin/mediation/ads/MaxAdView;", "loadAd").addInstructions(0, "return-void")
        method("Lcom/applovin/mediation/ads/MaxAdView;", "startAutoRefresh").addInstructions(0, "return-void")
        method("Lcom/google/android/gms/ads/BaseAdView;", "loadAd", "Lcom/google/android/gms/ads/AdRequest;")
            .addInstructions(0, "return-void")
    }
}
