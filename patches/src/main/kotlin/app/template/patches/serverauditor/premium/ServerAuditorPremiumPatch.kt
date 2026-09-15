package app.template.patches.serverauditor.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SERVER_AUDITOR_COMPATIBILITY
import app.template.patches.shared.returnEarly

private const val USER_TYPE_PRO =
    "Lcom/server/auditor/ssh/client/models/UserType\$Pro;"

/**
 * Unlocks Pro subscription in Server Auditor (Termius).
 *
 * ## Architecture
 *
 * The app fetches account data from api.serverauditor.com after login.
 * The response includes planType / userType which maps to a sealed UserType
 * class via cn/c.c(String) (obfuscated; was tm/c in prior versions).
 * All UI feature gates observe the resulting LiveData<UserType> and branch
 * on UserType subtypes via instance-of checks.
 *
 * No Pairip, no local billing decision — the only patchable surface is the
 * UserType mapper in DEX.
 *
 * ## Root cause of "shows Business branding but features still locked"
 *
 * The prior patch returned UserType$BusinessTeamOwner. That type is a team
 * account owner, not a personal Pro subscriber. The app gates many local
 * features (port forwarding, custom snippets, etc.) on instance-of
 * UserType$Pro specifically, not BusinessTeamOwner. Returning BusinessTeamOwner
 * passes team-owner gates but fails personal-Pro gates, leaving those features
 * locked behind upgrade prompts.
 *
 * ## Two-layer patch
 *
 * ### Layer 1 - UserTypeMapperFingerprint on cn/c.c(String)
 *
 * Injects at index 0:
 *   new-instance v0, UserType$Pro
 *   invoke-direct {v0, "Premium", false, null}  <- (String, Z, SubscriptionPeriod?)
 *   return-object v0
 *
 * Forces every call to the mapper to produce UserType$Pro regardless of
 * server response or login state. All UI gates observing the LiveData see Pro.
 *
 * ### Layer 2 - ProSubscriptionExpiredFingerprint on UserType$Pro.isExpired()
 *
 * returnEarly(false) — ensures isExpired always returns false even for any
 * UserType$Pro instances constructed outside Layer 1 (e.g. billing ack path).
 * Prevents "subscription expired" UI state from ever appearing.
 */
@Suppress("unused")
val serverAuditorPremiumPatch = bytecodePatch(
    name = "Server Auditor Premium",
    description = "Unlocks Pro features by forcing the UserType mapper to always return UserType\$Pro.",
    default = true,
) {
    compatibleWith(SERVER_AUDITOR_COMPATIBILITY)

    execute {
        // Layer 1: Force UserType mapper to always return UserType$Pro
        // Constructor: UserType$Pro(title: String, isExpired: Boolean, subscriptionPeriod: SubscriptionPeriod?)
        // v0 = new Pro instance, p1 = title string, v1 = false (isExpired), v2 = null (period)
        UserTypeMapperFingerprint.method.addInstructions(
            0,
            """
            const-string p1, "Premium"
            const/4 v1, 0x0
            const/4 v2, 0x0
            new-instance v0, $USER_TYPE_PRO
            invoke-direct {v0, p1, v1, v2}, $USER_TYPE_PRO-><init>(Ljava/lang/String;ZLcom/server/auditor/ssh/client/models/SubscriptionPeriod;)V
            return-object v0
            """,
        )

        // Layer 2: Force Pro subscription expiry -> false
        ProSubscriptionExpiredFingerprint.method.returnEarly(false)
    }
}
