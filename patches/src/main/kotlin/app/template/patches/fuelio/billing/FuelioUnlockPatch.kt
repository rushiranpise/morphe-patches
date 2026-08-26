package app.template.patches.fuelio.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FUELIO_COMPATIBILITY
import app.template.patches.shared.clearBody

// ── Architecture (v10.3.3) ────────────────────────────────────────────────────
//
// Five premium/promo paths. All coroutine state machines MUST NOT be clearBody'd —
// the packed-switch dispatch table and register frames make clearBody + inject
// produce VerifyError ("check-cast on non-reference") and the ART lock verifier
// warning ("max register v15") at startup. Only non-coroutine methods may use clearBody.
//
// Safe clearBody methods (no packed-switch, small register frame):
//   el5.b()Z      — .registers 2, no packed-switch
//   PromoEnabled, PromoHomeEnabled — .registers small, no packed-switch
//
// Coroutine state machines (addInstructions(0,...) only — short-circuits before switch):
//   ve0.h()       — .registers 15, packed-switch at instruction 8
//   el5.a(Lk41;)  — .registers 6,  packed-switch in state machine
//
// Coroutine state machine (replaceInstruction only — must not disturb register frame):
//   r7.p()        — .registers 43, packed-switch dispatcher
//
@Suppress("unused")
val fuelioUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Fuelio Premium, restores Google Maps, and suppresses " +
        "the Limited Promo paywall banner for unlocked users.",
    default = true,
) {
    compatibleWith(FUELIO_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {

        // ── Path A: el5.b()Z → always true ───────────────────────────────────
        // Non-coroutine, .registers 2. clearBody safe.
        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Path B: ve0.h(Object, Li41;)Object → always return Boolean.TRUE ──
        // Coroutine state machine (.registers 15, packed-switch at instruction 8).
        // DO NOT clearBody — destroys packed-switch data → VerifyError.
        // addInstructions(0,...) short-circuits before the switch is ever reached.
        // v0 is safe at index 0: not yet assigned (first use is iget v0, p0, q:I).
        SubscriptionEmitFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // ── Path C: r7.p(Object)Object → always post PREMIUM_RENEWABLE_PROFILE ─
        // Coroutine state machine (.registers 43, packed-switch dispatcher).
        // DO NOT clearBody.
        //
        // The pswitch_8b4 dispatch block at ~line 4366:
        //   [+0]  iget-object v1, Lr7;->x        (LiveData holder)
        //   [+1]  check-cast v1, Llc0;
        //   [+2]  iget-object v1, Llc0;->x:Lmp4; (the Lmp4; LiveData)
        //   [+3]  iget-object v0, Lr7;->w        (BuyState)
        //   [+4]  check-cast v0, Lgc0;
        //   [+5]  invoke-static/range Lhq9;->c()
        //   [+6]  iget-object v2, gc0;->a        (hasRenewablePremium)
        //   [+7]  sget-object v3, Boolean;->TRUE
        //   [+8]  invoke-static Llo3;->f()        (Boolean equality check)
        //   [+9]  move-result v2
        //   [+10] if-eqz v2, :cond_8dc           ← REPLACE WITH nop
        //   [+11] sget-object v0, Lid7;->a (analytics log — harmless)
        //   ...
        //   [+16] sget-object v0, Lhc0;->s       ← filter[0] match
        //   [+17] invoke-virtual {v1,v0}, La74;->i() ← filter[1] match
        //
        // Replacing if-eqz at (filter[0].index - 6) with nop forces execution to
        // always fall through to the hc0.s post, skipping cond_8dc (hc0.t/hc0.q).
        // v1 already holds the valid Lmp4; LiveData — no register pollution.
        val sgetIndex = DestinationScreenFingerprint.instructionMatches[0].index
        DestinationScreenFingerprint.method.replaceInstruction(
            sgetIndex - 6,
            "nop",
        )

        // ── Path D: el5.a(Lk41;)Object → always return Boolean.TRUE ──────────
        // Coroutine state machine (.registers 6).
        // DO NOT clearBody — the state machine setup and Deferred.await are needed
        // by the coroutine runtime for bookkeeping on second resume calls.
        // addInstructions(0,...) short-circuits on first entry before any state check.
        // v0 is safe at index 0: first use is instance-of v0, p1, Ldl5;
        AwaitAccessFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
            """.trimIndent(),
        )

        // ── Path E: Google Maps cert + API key spoof ──────────────────────────
        FuelioApplicationOnCreateFingerprint.method.addInstructions(
            0,
            "invoke-static { }, Lapp/template/extension/extension/FuelioHelper;->init()V",
        )

        // ── Path F: Suppress "Limited Promo" / "30% OFF" banner ──────────────
        // Non-coroutine, small register frame. clearBody safe.
        PromoEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        PromoHomeEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
    }
}
