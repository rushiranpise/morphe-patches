package app.template.patches.strava

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.STRAVA_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Strava 475.11 subscription architecture
//
// SubscriptionDetail (com.strava.subscriptions.data.models.SubscriptionDetail)
//   — now fully non-obfuscated (was pb1/f in v474.14)
//   field isPremium:Z  ← primary gate for all premium feature checks
//
//   Private constructor (classes6.dex, registers 14):
//     <init>(JZLjava/lang/Long;SubscriptionState;SubscriptionSubState;
//            Product;String;RecurringPeriod;SubscriptionPlatform;
//            Long;Long;StravaProductAccess;)V
//     ACCESS: private constructor
//     p1+p2 = athleteId:J (wide), p3 = isPremium:Z
//     instruction 0: invoke-direct Object.<init>
//     instruction 1: iput-wide p1 (athleteId)
//     instruction 2: iput-boolean p3 → isPremium  ← inject here
//
//   Three construction paths — all reach this constructor:
//     REST:    SubscriptionDetailRestDataSource → toSubscriptionDetail()
//     GraphQL: SubscriptionDetailGraphQLMapper.toDomain(svi.d) → new SubscriptionDetail(...)
//     DB:      SubscriptionDetailLocalDataSource.toSubscriptionDetail(entity) via JSON deserializer
//
//   Forcing p3=1 at index 0 of the private constructor covers all three paths.
//
// GraphQL adapters for other-athlete subscriptionStatus {isSubscribed}:
//   Used only for UI display of OTHER athletes (challenges, activity detail,
//   followers). Not a gate for the current user's premium features.
//   The v474 obfuscated adapters wt/z0 and a40/e1 no longer exist in v475.
//   No patch needed — SubscriptionDetail.isPremium=true is sufficient.
//
// OTP / password login (classes4.dex):
//   RequestOtpLogInNetworkResponse.getUsePassword()Z — unchanged from v474.
//   Fingerprinted by stable non-obfuscated definingClass + name.
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Strava Premium features. Also re-enables password login after OTP.",
    default = true,
) {
    compatibleWith(STRAVA_COMPATIBILITY)

    execute {

        // ── DOMAIN MODEL: SubscriptionDetail.<init> ───────────────────────────
        //
        // Class is fully non-obfuscated in v475 (was Lpb1/f; in v474).
        // The private constructor is resolved by definingClass + name directly;
        // no Fingerprint needed. p3 is the isPremium boolean (p1+p2 = J athleteId).
        // const/4 p3, 0x1 at index 0 forces isPremium=true for every instance
        // constructed via REST, GraphQL, or DB deserialization paths.
        mutableClassDefBy("Lcom/strava/subscriptions/data/models/SubscriptionDetail;")
            .directMethods
            .first { it.name == "<init>" && it.accessFlags and AccessFlags.PRIVATE.value != 0 }
            .addInstructions(0, "const/4 p3, 0x1")

        // ── OTP / PASSWORD LOGIN ───────────────────────────────────────────────
        //
        // Stable non-obfuscated fingerprint — unchanged across versions.
        // Returns true so the app falls through to password login rather than
        // forcing OTP-only auth flow.
        Fingerprint(
            definingClass = "Lcom/strava/authorization/data/RequestOtpLogInNetworkResponse;",
            name = "getUsePassword",
            returnType = "Z",
            accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
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
