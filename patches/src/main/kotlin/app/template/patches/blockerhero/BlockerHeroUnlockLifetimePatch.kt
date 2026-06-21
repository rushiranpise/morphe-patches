package app.template.patches.blockerhero

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLOCKERHERO_COMPATIBILITY

/**
 * Unlocks Lifetime in BlockerHero — App Blocker & Focus Timer (com.blockerhero).
 *
 * ## Layer summary
 * 1. isLifetime()              → true    (lifetime UI branch)
 * 2. isTakenFromGooglePlay()   → true    (subscription page manage-sub UI)
 * 3. n5/a.h()                  → fake lifetime UserSubscription (paywall gate)
 * 4. Y3/b.l()                  → true    (logged-in gate bypass)
 * 5. Y3/b.j()                  → 1       (premium user ID for k() check)
 * 6. E4/a.p()                  → nop     (suppress GenericResponse API error toasts)
 * 7. E4/b.p()                  → nop     (suppress Throwable error toasts)
 * 8. p5/f.Q(Context,String)V   → nop     (silence all unauthenticated toasts)
 * 9. j4/v.p() index 30         → inject Y3/b.s("KEY_ACCOUNTABILITY_PARTNER", v1)
 *                                 before API call — persists FRIEND selection locally
 * 10. k4/j.a() index 0         → inject Y3/b.s("KEY_ACCOUNTABILITY_PARTNER", "")
 *                                 at start of remove flow — clears partner locally
 *                                 before API call so remove works on 401
 */
@Suppress("unused")
val blockerHeroUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime",
    description = "Unlocks lifetime subscription features in BlockerHero.",
    default = true
) {
    compatibleWith(BLOCKERHERO_COMPATIBILITY)

    execute {
        // ── Layer 1: isLifetime → true ────────────────────────────────────────
        IsLifetimeFingerprint
            .match(classDefBy(IsLifetimeFingerprint.definingClass!!))
            .method.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // ── Layer 2: isTakenFromGooglePlay → true ─────────────────────────────
        runCatching {
            IsTakenFromGooglePlayFingerprint
                .match(classDefBy(IsTakenFromGooglePlayFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Layer 3: n5/a.h → fake lifetime UserSubscription ──────────────────
        runCatching {
            GetActiveSubscriptionFingerprint.match().method
        }.getOrNull()?.apply {
            ensureRegisters(16)
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, Lcom/blockerhero/data/db/entities/UserSubscription;
                const-string v1, "morphe_order"
                const-string v2, "blockerhero_lifetime"
                const-wide/16 v3, 0x0
                const-wide v5, 0x7fffffffffffffffL
                sget-object v7, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                const-string v8, "US"
                const-string v9, "inapp"
                const-wide/16 v10, 0x0
                const-string v12, "morphe_token"
                invoke-direct/range {v0 .. v12}, Lcom/blockerhero/data/db/entities/UserSubscription;-><init>(Ljava/lang/String;Ljava/lang/String;JJLjava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;JLjava/lang/String;)V
                return-object v0
                """.trimIndent()
            )
        }

        // ── Layer 4: Y3/b.l() → true (logged-in gate) ────────────────────────
        runCatching {
            IsLoggedInFingerprint
                .match(classDefBy(IsLoggedInFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Layer 5: Y3/b.j() → 1 (premium user ID) ──────────────────────────
        runCatching {
            GetUserIdFingerprint
                .match(classDefBy(GetUserIdFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Layer 6: E4/a.p() → nop (GenericResponse API error toast) ─────────
        runCatching {
            ApiErrorToastFingerprint
                .match(classDefBy(ApiErrorToastFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "sget-object v0, LA7/B;->a:LA7/B;\nreturn-object v0")
        }

        // ── Layer 7: E4/b.p() → nop (Throwable network error toast) ──────────
        runCatching {
            NetworkErrorToastFingerprint
                .match(classDefBy(NetworkErrorToastFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "sget-object v0, LA7/B;->a:LA7/B;\nreturn-object v0")
        }

        // ── Layer 8: p5/f.Q() → return-void (nop all error toasts) ───────────
        runCatching {
            ToastHelperFingerprint
                .match(classDefBy(ToastHelperFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "return-void")
        }

        // ── Layer 9: j4/v.p() → write FRIEND pref locally before API ──────────
        // At instr 30: v1=partner type, v3=j4/w, v3.h=Y3/b. v6/v7 free here.
        runCatching {
            SetAccountabilityPartnerFingerprint
                .match(classDefBy(SetAccountabilityPartnerFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            addInstructions(
                30,
                """
                iget-object v6, v3, Lj4/w;->h:LY3/b;
                const-string v7, "KEY_ACCOUNTABILITY_PARTNER"
                invoke-virtual {v6, v7, v1}, LY3/b;->s(Ljava/lang/String;Ljava/lang/String;)V
                """.trimIndent()
            )
        }

        // ── Layer 10: (Reset Timer — no partner pref change needed) ────────────
        // j4/e.a() reset timer coroutine only resets the day counter, NOT the
        // partner type. KEY_ACCOUNTABILITY_PARTNER intentionally kept intact so
        // the TIME_DELAY partner persists after a timer reset.

        // ── Layer 11: T3/a.c() → clear partner pref on TIME_DELAY cancel ─────
        // T3/a.c() confirm handler: instr 385 sets v8=Y3/b, instr 386 calls
        // Y3/b.o() to remove _PARTNER_REQ_TIME_MILLIS. Inject at 387 to also
        // clear KEY_ACCOUNTABILITY_PARTNER using v8=Y3/b, v4/v5 as scratch.
        runCatching {
            CancelTimeDelayFingerprint.match().method
        }.getOrNull()?.apply {
            addInstructions(
                387,
                """
                const-string v4, "KEY_ACCOUNTABILITY_PARTNER"
                const-string v5, ""
                invoke-virtual {v8, v4, v5}, LY3/b;->s(Ljava/lang/String;Ljava/lang/String;)V
                """.trimIndent()
            )
        }
        // ── Layer 12: k4/j.<clinit>() → remove FRIEND from partner type list ────
        // k4/j.a = List built from all 3 k4/a enum values (FRIEND, MYSELF, TIME_DELAY).
        // Replace with a 2-element list of [MYSELF, TIME_DELAY] to hide FRIEND option.
        // Email can't be sent from patched APK so FRIEND is non-functional.
        // clearBody() + addInstructions replaces the static init with minimal code.
        runCatching {
            PartnerTypeListFingerprint
                .match(classDefBy(PartnerTypeListFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            ensureRegisters(2)
            clearBody()
            addInstructions(
                0,
                """
                sget-object v0, Lk4/a;->w:Lk4/a;
                sget-object v1, Lk4/a;->x:Lk4/a;
                filled-new-array {v0, v1}, [Ljava/lang/Object;
                move-result-object v0
                invoke-static {v0}, LB7/n;->a0([Ljava/lang/Object;)Ljava/util/List;
                move-result-object v0
                sput-object v0, Lk4/j;->a:Ljava/util/List;
                return-void
                """.trimIndent()
            )
        }
    }
}
