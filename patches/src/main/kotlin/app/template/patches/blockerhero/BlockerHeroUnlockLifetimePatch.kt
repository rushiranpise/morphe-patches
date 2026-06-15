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
 * 7. E4/b.p()                  → nop     (suppress network/throwable error toasts)
 * 8. p5/f.Q(Context, String)V  → return  (nop ALL toast helper calls)
 *
 * Layer 8 root cause: null Bearer token → server 401 "Unauthenticated." →
 * parsed by T0/Y0.n() pswitch_1 (HTTP error body path) → p5/f.Q() toast.
 * E4/a+b handle a different code path (GenericResponse / Throwable).
 * Nopping Q() silences all paths at once.
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
        // Root cause path: null Bearer → 401 → T0/Y0.n() pswitch_1 → Q() toast.
        // Nopping Q() silences all API error toast paths at once.
        runCatching {
            ToastHelperFingerprint
                .match(classDefBy(ToastHelperFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
