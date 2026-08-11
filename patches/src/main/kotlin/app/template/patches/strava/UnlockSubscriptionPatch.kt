package app.template.patches.strava

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.template.patches.shared.Constants.STRAVA_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Strava 474.14 subscription architecture
//
// pb1/f = SubscriptionDetail domain model (obfuscated, stable in 474.14)
//   field a:J  = athleteId
//   field b:Z  = isPremium  ← primary gate (confirmed via toString)
//   constructor <init>(J, Z[=p3 isPremium], Long?, pb1/i, pb1/j, Product,
//                       String, pb1/c, pb1/h, Long?, Long?, pb1/e)
//   instruction 3: iput-boolean p3, p0, Lpb1/f;->b:Z  (own-class write)
//
// Three independent construction paths — all pass isPremium as p3:
//   1. REST    SubscriptionDetailResponseKt.toSubscriptionDetail(Response)
//   2. GraphQL SubscriptionDetailGraphQLMapper.toDomain(mb1/c$d)
//   3. DB      SubscriptionDetailLocalDataSource.toSubscriptionDetail(Entity)
// Forcing p3=1 in <init> covers all three paths in one instruction.
//
// GraphQL SubscriptionStatus adapters (AdpRepository / in-activity gating):
//   wt/z0.b(qf/f, mf/o)  → vt/b$z0(isSubscribed:Z)
//   a40/e1.b(qf/f, mf/o) → y30/z$e1(isSubscribed:Z)
//
// OTP: RequestOtpLogInNetworkResponse.getUsePassword() [stable, unchanged ✓]
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Strava Premium features. Also re-enables password login after OTP.",
    default = true,
) {
    compatibleWith(STRAVA_COMPATIBILITY)

    execute {

        // ── DOMAIN MODEL: pb1/f.<init> ─────────────────────────────────────────
        //
        // mutableClassDefBy resolves pb1/f directly by descriptor — the class name
        // is stable in this version and known from static analysis; no Fingerprint
        // needed. The single constructor takes isPremium as p3 (J is wide → p1+p2,
        // so p3 = the Z boolean). const/4 p3, 0x1 at index 0 makes every pb1/f
        // instance constructed via any path have isPremium=true.
        // Own-class write: legal even with the FINAL access flag on field b.
        mutableClassDefBy("Lpb1/f;")
            .directMethods
            .first { it.name == "<init>" }
            .addInstructions(0, "const/4 p3, 0x1")

        // ── GRAPHQL SUBSCRIPTION STATUS ADAPTERS ──────────────────────────────
        //
        // Fingerprint (shared happy-path shape for both adapters):
        //   parameters = listOf("Lqf/f;", "Lmf/o;")    ← exact b() signature
        //   methodCall(mf/b$b, "b")     = Apollo next-token reader
        //   methodCall(Boolean, "booleanValue") = unbox JSON boolean
        // "isSubscribed" appears only in the error branch — not a valid filter.
        // custom filter pins each fingerprint to its specific adapter class.
        for ((adapterClass, statusType) in listOf(
            "Lwt/z0;" to "Lvt/b\$z0;",
            "La40/e1;" to "Ly30/z\$e1;",
        )) {
            Fingerprint(
                returnType = "Ljava/lang/Object;",
                accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
                parameters = listOf("Lqf/f;", "Lmf/o;"),
                filters = listOf(
                    methodCall(definingClass = "Lmf/b\$b;", name = "b"),
                    methodCall(definingClass = "Ljava/lang/Boolean;", name = "booleanValue"),
                ),
                custom = { _, classDef -> classDef.type == adapterClass },
            ).method.addInstructions(
                0,
                """
                    new-instance v0, $statusType
                    const/4 v1, 0x1
                    invoke-direct { v0, v1 }, $statusType-><init>(Z)V
                    return-object v0
                """.trimIndent(),
            )
        }

        // ── OTP / PASSWORD LOGIN ───────────────────────────────────────────────
        Fingerprint(
            definingClass = "Lcom/strava/authorization/data/RequestOtpLogInNetworkResponse;",
            name = "getUsePassword",
            returnType = "Z",
            parameters = emptyList(),
        ).method.addInstructions(
            0,
            """
                const/4 v0, 0x1
                return v0
            """.trimIndent(),
        )
    }
}
