package app.template.patches.slopes.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Targets AccessController.getPassExpiration()Ljava/time/Instant;  (.registers 4)
// We inject a far-future Instant (year 2100) at index 0 so getPremiumStatus() receives
// a real non-null Instant and computes a proper date string for the UI.
// Stable: non-obfuscated class + method, plus Instant.plusSeconds call as filter.
object GetPassExpirationFingerprint : Fingerprint(
    definingClass = "Lcom/consumedbycode/slopes/access/AccessController;",
    name = "getPassExpiration",
    returnType = "Ljava/time/Instant;",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/time/Instant;",
            name = "plusSeconds"
        ),
    )
)

// Targets AccessController.getAutoRenewing()Z  (.registers 2)
// Returns lastOrNull(passRanges)?.getAutoRenewing() — false when no purchase on file.
// returnEarly(true) sends getPremiumStatus() into the PassStatus$Subscribed branch
// (Unlimited Plan) rather than PassStatus$Active.
object GetAutoRenewingFingerprint : Fingerprint(
    definingClass = "Lcom/consumedbycode/slopes/access/AccessController;",
    name = "getAutoRenewing",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/consumedbycode/slopes/vo/PassRange;",
            name = "getAutoRenewing"
        ),
    )
)

// Targets AccessController.isSubscribed()Z
// Checked in getPremiumStatus() to gate entry into the Subscribed/Expiring branches.
object IsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/consumedbycode/slopes/access/AccessController;",
    name = "isSubscribed",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lcom/consumedbycode/slopes/vo/SubscriptionOrigin;",
            name = "NONE"
        ),
    )
)
