package app.template.patches.serverauditor.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SERVER_AUDITOR_COMPATIBILITY
import app.template.patches.shared.returnEarly

private const val USER_TYPE_BUSINESS =
    "Lcom/server/auditor/ssh/client/models/UserType\$BusinessTeamOwner;"

/**
 * Unlocks Pro subscription in Server Auditor — SSH Client (Termius).
 *
 * ## Account architecture
 *
 * Server Auditor uses server-side account validation. After login, the app
 * fetches account data from api.serverauditor.com. The response includes
 * `userType` (serialised as `pro_mode`), which is a subscription tier string:
 *   "Premium"  → Pro subscription active
 *   "Trial"    → Free trial active
 *   "Starter"  → Free tier
 *   "Free"     → Free tier
 *   ""         → Not logged in / no cached account
 *
 * This string is stored in SharedPreferences under key `key_account_user_type`
 * by the sync service (vh/z.smali). The UserTypeRepository (tm/c) maps it to
 * a sealed UserType class instance via tm/c.c(String), and exposes it as a
 * LiveData<UserType> via tm/c.d().
 *
 * All UI feature gates (AccountStartScreenPresenter, EndOfTrialScreenPresenter,
 * ProFeaturesListViewModel, OnboardingActivity) observe this LiveData and
 * branch on the UserType sealed class via instance-of checks.
 *
 * No Pairip, no local billing decision — the DEX layer is the only patchable
 * surface. Google Play Billing is present but only triggers a server sync
 * (via SyncServiceHelper.startProfileAndBulkSync) after purchase.
 *
 * ## Two-layer patch
 *
 * ### Layer 1 — UserTypeMapperFingerprint on tm/c.c(String) → inject Pro
 *
 * Injects at index 0 of the UserType mapper:
 *   const-string v0, "Premium"
 *   const/4 v1, 0x0                 ← isExpired = false
 *   const/4 v2, 0x0                 ← subscriptionPeriod = null
 *   new-instance v3, UserType$Pro
 *   invoke-direct {v3, v0, v1, v2}  ← UserType$Pro(title, isExpired, period)
 *   return-object v3
 *
 * This forces every call to tm/c.c(String) — regardless of what the server
 * returned or whether the user is logged in — to produce UserType$Pro.
 * The LiveData observed by all UI components emits UserType$Pro immediately.
 *
 * UserType$Pro constructor: (String title, boolean isExpired, SubscriptionPeriod)
 * The SubscriptionPeriod param is nullable — passing null is safe and prevents
 * any subscription expiry date from being displayed in the UI.
 *
 * ### Layer 2 — ProSubscriptionExpiredFingerprint on UserType$Pro.isExpired()
 *
 * returnEarly(false) — ensures isExpired always returns false even for any
 * UserType$Pro instances that bypass Layer 1 (e.g. constructed directly by
 * the billing acknowledgement path). Prevents "subscription expired" UI state.
 */
@Suppress("unused")
val serverAuditorPremiumPatch = bytecodePatch(
    name = "Unlock Business Premium",
    description = "Unlocks Business tier by forcing the UserType mapper to always return UserType\$BusinessTeamOwner.",
    default = true,
) {
    compatibleWith(SERVER_AUDITOR_COMPATIBILITY)

    execute {
        // ── Layer 1: Force UserType mapper to always return UserType$BusinessTeamOwner ──
        // Constructor: BusinessTeamOwner(title: String, isExpired: Boolean,
        //   teamSubscriptionPeriod: SubscriptionPeriod?, teamName: String)
        // v0 = title "Business", v1 = isExpired false, v2 = subscriptionPeriod null,
        // v3 = teamName "Business" (non-null required — crashes if null)
        UserTypeMapperFingerprint.method.addInstructions(
            0,
            """
            const-string v0, "Business"
            const/4 v1, 0x0
            const/4 v2, 0x0
            new-instance v3, $USER_TYPE_BUSINESS
            invoke-direct {v3, v0, v1, v2, v0}, $USER_TYPE_BUSINESS-><init>(Ljava/lang/String;ZLcom/server/auditor/ssh/client/models/SubscriptionPeriod;Ljava/lang/String;)V
            return-object v3
            """,
        )

        // ── Layer 2: Force Business subscription expiry → false ──
        BusinessSubscriptionExpiredFingerprint.method.returnEarly(false)
    }
}
