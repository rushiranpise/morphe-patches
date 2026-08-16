package app.template.patches.serverauditor.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * tm/c.c(String) — the UserType mapper. Maps the server-provided subscription
 * title string (stored in SharedPrefs as "key_account_user_type") to a
 * UserType sealed class instance.
 *
 * All premium feature gates in the app observe the UserType LiveData produced
 * by tm/c.d() → which calls tm/b (LiveData transformer) → which calls
 * tm/c.a/b(tm/c, String) → which calls this method.
 *
 * Mapping logic (simplified):
 *   "Premium"       → UserType$Pro(title, isExpired=false, period)
 *   "Trial"         → UserType$ProTrial or UserType$BusinessTrialOwner
 *   "Starter"/"Free"→ UserType$Starter(title)
 *   ""  (no login)  → UserType$Starter (via fallback)
 *
 * Server Auditor (Termius) uses server-side account validation:
 * AccountResponse.userType is fetched from api.serverauditor.com after login
 * and persisted to SharedPrefs. This method is the single conversion point
 * between the server string and the local UserType object.
 *
 * Smali: classes4/tm/c.smali  .method private final c(Ljava/lang/String;)UserType
 *   .registers 6
 *   iget-object v0, p0, Ltm/c;->e:Lcom/...interactors/a;
 *   invoke-virtual {v0}, La;->b()Ljava/util/List;           ← getAccounts()
 *   invoke-static {v0}, Ljv/v;->n0(Ljava/util/List;)
 *   check-cast v0, LAccountAccessObject;
 *   invoke-virtual {v0}, LAccountAccessObject;->getSubscriptionTitle()  ← filter[0]
 *   invoke-virtual {v0}, LAccountAccessObject;->getSubscriptionPeriod() ← filter[1]
 *   invoke-direct {p0, v2, v1}, Ltm/c;->i(String, SubscriptionPeriod)  ← Enterprise
 *   invoke-direct {p0, p1, v2, v1}, Ltm/c;->h(...)                     ← big switch
 *   return-object p1
 *
 * Patch: inject at index 0 — construct UserType$Pro("Premium", false, null)
 * and return immediately. All feature-gated UI observing the UserType LiveData
 * will see UserType$Pro regardless of server response or login state.
 *
 * Unique: only tm/c.smali has a PRIVATE FINAL (String)→UserType method that
 * calls AccountAccessObject.getSubscriptionTitle(). Verified across all 52,280
 * smali files in 5 DEX shards.
 */
internal object UserTypeMapperFingerprint : Fingerprint(
    returnType = "Lcom/server/auditor/ssh/client/models/UserType;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/server/auditor/ssh/client/models/account/AccountAccessObject;",
            name = "getSubscriptionTitle",
            returnType = "Ljava/lang/String;",
            parameters = listOf(),
        ),
        methodCall(
            definingClass = "Lcom/server/auditor/ssh/client/models/account/AccountAccessObject;",
            name = "getSubscriptionPeriod",
            returnType = "Lcom/server/auditor/ssh/client/models/account/AccountSubscriptionPeriod;",
            parameters = listOf(),
        ),
    ),
)

/**
 * UserType$Pro.isExpired() — returns whether the Pro subscription has expired.
 * Used in AccountStartScreenPresenter and EndOfTrialScreenPresenter to decide
 * whether to show the "Your subscription has expired" warning and block
 * premium features.
 *
 * Even when UserTypeMapperFingerprint forces UserType$Pro, the isExpired flag
 * could still return true if the server previously wrote an expired state
 * to SharedPrefs. Patching isExpired() → false ensures the "Pro" account
 * always appears active.
 *
 * Smali: classes3/com/server/auditor/ssh/client/models/UserType$Pro.smali
 *   .method public final isExpired()Z
 *     iget-boolean v0, p0, LUserType$Pro;->isExpired:Z
 *     return v0
 *
 * Patch: returnEarly(false) — subscription never appears expired.
 *
 * definingClass + name + return type form a globally unique fingerprint.
 * The non-obfuscated class name (com.server.auditor.ssh.client.models.UserType$Pro)
 * is stable across versions.
 */
internal object BusinessSubscriptionExpiredFingerprint : Fingerprint(
    definingClass = "Lcom/server/auditor/ssh/client/models/UserType\$BusinessTeamOwner;",
    name = "isExpired",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)
