package app.template.patches.netmonster

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NETMONSTER_COMPATIBILITY

@Suppress("unused")
val netMonsterUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features.",
    default = true
) {
    compatibleWith(NETMONSTER_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {
        // ── Layer 1: isPremium flow → always emit true ────────────────────────
        // er/o$a$a.a(List, Continuation) maps Adapty subscriptions → Boolean.
        // Replacing the body emits true to er/o.f (isPremium StateFlow).
        runCatching {
            SubscriptionActiveFlowFingerprint
                .match(classDefBy(SubscriptionActiveFlowFingerprint.definingClass!!))
                .method
        }.getOrNull()?.apply {
            if (implementation == null) return@apply
            removeInstructions(0, instructions.count())
            addInstructions(
                0,
                """
                iget-object p2, p0, Ler/o${'$'}a${'$'}a;->X:Ler/o;
                iget-object p2, p2, Ler/o;->f:Lkv/j0;
                const/4 v0, 0x1
                invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v0
                invoke-interface {p2, v0}, Lkv/i0;->b(Ljava/lang/Object;)Z
                sget-object p1, Llt/p2;->a:Llt/p2;
                return-object p1
                """.trimIndent()
            )
        }

        // ── Layer 2: SIM permission gate → no-op ─────────────────────────────
        // er/o.n() emits FALSE to er/o.g when READ_PHONE_STATE is denied.
        // return-void at the top prevents that early FALSE, keeping SIM features unlocked.
        // Note: using definingClass + name only (no strings filter) for reliable match.
        runCatching {
            SubscriptionRepositoryPermissionGateFingerprint
                .match(classDefBy(SubscriptionRepositoryPermissionGateFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(0, "return-void")

        // ── Layer 3: Settings "Active/Inactive" display ───────────────────────
        // uq/l$a$a.a(OffsetDateTime, Continuation) CAS-writes uq/k(date) into uq/l.Z.
        // MoreFragment shows "Inactive" when uq/k.a == null.
        // If p1 is null, inject OffsetDateTime.now(UTC).plusYears(50) to always show active.
        runCatching {
            SubscriptionExpiryEmitFingerprint
                .match(classDefBy(SubscriptionExpiryEmitFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            """
            if-nez p1, :skip_inject
            sget-object p1, Ljava/time/ZoneOffset;->UTC:Ljava/time/ZoneOffset;
            invoke-static {p1}, Ljava/time/OffsetDateTime;->now(Ljava/time/ZoneId;)Ljava/time/OffsetDateTime;
            move-result-object p1
            const-wide/16 v0, 0x32
            invoke-virtual {p1, v0, v1}, Ljava/time/OffsetDateTime;->plusYears(J)Ljava/time/OffsetDateTime;
            move-result-object p1
            :skip_inject
            """.trimIndent()
        )

        // ── Layer 4: Google Maps cert/API key spoof ───────────────────────────
        // After patching, the APK cert changes → Google Maps SDK cert mismatch.
        // NetMonsterHelper.init() installs a PackageManager proxy that returns the
        // original cert and API key (com.google.android.geo.API_KEY) for this package.
        runCatching {
            NetMonsterAppOnCreateFingerprint
                .match(classDefBy(NetMonsterAppOnCreateFingerprint.definingClass!!))
                .method
        }.getOrNull()?.addInstructions(
            0,
            "invoke-static {}, Lapp/template/extension/extension/NetMonsterHelper;->init()V"
        )
    }
}
