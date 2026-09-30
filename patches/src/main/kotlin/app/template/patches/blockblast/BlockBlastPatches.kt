package app.template.patches.blockblast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLOCKBLAST_COMPATIBILITY

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
    name = "Unlock VIP",
    description = "Unlocks VIP, the ad-free subscription. Removes all ads including revive offers.",
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)

    execute {
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
