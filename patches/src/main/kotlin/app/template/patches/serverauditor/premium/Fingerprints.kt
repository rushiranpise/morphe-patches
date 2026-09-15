package app.template.patches.serverauditor.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * cn/c.c(String) — the UserType mapper (formerly tm/c in earlier versions;
 * obfuscated class renamed each release but fingerprint resolves via stable
 * method call filters on non-obfuscated AccountAccessObject methods).
 *
 * Smali: classes4/cn/c.smali  .method private final c(Ljava/lang/String;)UserType
 *   invoke-virtual {v0}, LAccountAccessObject;->getSubscriptionTitle()  <- filter[0]
 *   invoke-virtual {v0}, LAccountAccessObject;->getSubscriptionPeriod() <- filter[1]
 *
 * Patch: inject at index 0 — construct UserType$Pro("Premium", false, null)
 * and return immediately. All feature-gated UI observing the UserType LiveData
 * will see UserType$Pro regardless of server response or login state.
 *
 * UserType$Pro ctor: (String title, boolean isExpired, SubscriptionPeriod? period)
 * SubscriptionPeriod is nullable — passing null is safe.
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
 * UserType$Pro.isExpired() — returns whether the personal Pro subscription
 * has expired. Patching to false ensures the injected Pro account always
 * appears active regardless of any cached expiry state from a prior sync.
 *
 * Smali: classes3/com/server/auditor/ssh/client/models/UserType$Pro.smali
 *   .method public final isExpired()Z
 *     iget-boolean v0, p0, LUserType$Pro;->isExpired:Z
 *     return v0
 */
internal object ProSubscriptionExpiredFingerprint : Fingerprint(
    definingClass = "Lcom/server/auditor/ssh/client/models/UserType\$Pro;",
    name = "isExpired",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)
