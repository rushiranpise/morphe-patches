package app.template.patches.blockblast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.BLOCKBLAST_COMPATIBILITY

/**
 * Unlock VIP — grants permanent VIP (ad-free subscription) status in Block Blast.
 *
 * ── Architecture ─────────────────────────────────────────────────────────────
 * Block Blast is Cocos2d-JS. Game logic runs in V8 JS (.jsc bytecode). VIP state
 * lives in SPStore, a C++ singleton in libcocos2djs.so accessible via JNI.
 * JS reads VIP directly from the native SPStore — not via any Java method.
 *
 * ── VIP state write/notify chain ────────────────────────────────────────────
 *
 * 1. wf/j.e(String) — subscription state processor (called by billing & wf/m)
 *    Reads SPStore.p()I (sign_status) and wf/j.d(productId)Z (fill/verify check).
 *    If it's a real purchase result AND sign_status != 1:
 *      → calls JsCallJava.notifySubStateUpdate() to tell JS to re-read SPStore
 *    If it's a fill/verify check: → calls wf/j.f() to trigger network query
 *
 * 2. wf/g.o(JSONObject, wf/g$b) — server query response parser
 *    Reads expire_in_seconds / expire_time from /user/subscription response.
 *    Computes isVip = (expire_in_seconds > 0).
 *    Calls SPStore.N(isVip) to persist VIP state.
 *    Returns sign_status (I) → caller passes to wf/g$b.b(I) callback.
 *
 * 3. wf/m.q() — in-app purchase handler
 *    Same expire_in_seconds check → SPStore.N(isVip).
 *
 * ── Why previous patches failed ──────────────────────────────────────────────
 * wf/g.o() returned sign_status=1 immediately after setting SPStore.N(true).
 * BUT wf/j.e() only calls notifySubStateUpdate() when sign_status != 1 —
 * so JS was never told to re-read SPStore, and never saw VIP=true.
 *
 * wf/m.q() returned early but is never called without an actual purchase,
 * so it never fired for sideloaded apps.
 *
 * ── Patch strategy ───────────────────────────────────────────────────────────
 * Patch THREE methods:
 *
 * ① wf/j.e(String) — the notify hub:
 *   Set SPStore.N(true), then call JsCallJava.notifySubStateUpdate() immediately.
 *   This fires whenever billing has any subscription state data AND tells JS right away.
 *
 * ② wf/g.o(JSONObject, wf/g$b) — server response parser:
 *   Set SPStore.N(true), return 0 (sign_status=0 != 1) so the caller's
 *   wf/j.e() path falls into the notifySubStateUpdate() branch.
 *
 * ③ wf/m.q() — purchase handler (fallback for purchase path):
 *   Set SPStore.N(true), call notifySubStateUpdate() directly, return-void.
 */
@Suppress("unused")
val blockBlastUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlock ViP features in app.",
) {
    compatibleWith(BLOCKBLAST_COMPATIBILITY)

    execute {
        val setVipAndNotify = """
            invoke-static {}, Lorg/cocos2dx/javascript/pay/SPStore;->k()Lorg/cocos2dx/javascript/pay/SPStore;
            move-result-object v0
            const/4 v1, 0x1
            invoke-virtual {v0, v1}, Lorg/cocos2dx/javascript/pay/SPStore;->N(Z)V
            invoke-static {}, Lorg/cocos2dx/javascript/JsCallJava;->notifySubStateUpdate()V
        """

        // ① wf/j.e(String) — subscription state hub → set VIP + notify JS + return
        classDefBy("Lwf/j;")
            .methods.first { it.name == "e" && it.returnType == "V" }
            .toMutable()
            .addInstructions(0, "$setVipAndNotify\n            return-void")

        // ② wf/g.o(JSONObject, wf/g$b) → returns I
        //    Return 0 (sign_status != 1) so wf/j.e() will call notifySubStateUpdate()
        classDefBy("Lwf/g;")
            .methods.first { it.name == "o" && it.returnType == "I" }
            .toMutable()
            .addInstructions(0, """
                invoke-static {}, Lorg/cocos2dx/javascript/pay/SPStore;->k()Lorg/cocos2dx/javascript/pay/SPStore;
                move-result-object v0
                const/4 v1, 0x1
                invoke-virtual {v0, v1}, Lorg/cocos2dx/javascript/pay/SPStore;->N(Z)V
                const/4 v0, 0x0
                return v0
            """)

        // ③ wf/m.q() — purchase handler fallback
        classDefBy("Lwf/m;")
            .methods.first { it.name == "q" && it.returnType == "V" }
            .toMutable()
            .addInstructions(0, "$setVipAndNotify\n            return-void")
    }
}
