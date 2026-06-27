package app.template.patches.strengthtraining.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// PaidStatus — user-level subscription model from login API.
// isPaidStatusEmpty():Z → true when type+billing both blank (no subscription).
// Gate used by PricingFragment (b8/r) and profile screens.
// Patch to always return false = "not empty" = subscribed.
object IsPaidStatusEmptyFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    name = "isPaidStatusEmpty",
)

// PaidStatus.isPremium():Z — server-set premium flag.
// Read by analytics, profile badge, FAQ subscription check.
// Patch to return true.
object IsPremiumFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    name = "isPremium",
)

// LoginResponse.isPaid():Z — user-level isPaid from login API.
// true = user has active subscription. Patch to return true.
object LoginResponseIsPaidFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/LoginResponse;",
    name = "isPaid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// PaidStatus.getType():String — returns "indi"/"pro"/"business"/"" from server field.
// Profile fragment uses this to display plan name via getCurrentPlan().
// Patch to return "indi" so getCurrentPlan() maps it to "Individual".
object GetTypeFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/PaidStatus;",
    name = "getType",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// PaidStatus.getBilling():String — returns "Monthly"/"Yearly"/"3 Month"/"3 Years"/"".
// Profile fragment uses this to display billing period.
// Patch to return "Yearly".
object GetBillingFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/PaidStatus;",
    name = "getBilling",
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// LoginResponse.getExpiryDate():Long? — null when no subscription.
// Profile fragment shows this as expiry date; null causes "empty" display.
// Patch to return a far-future timestamp (Jan 1 2099 = 4070908800000L).
object GetExpiryDateFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/LoginResponse;",
    name = "getExpiryDate",
    returnType = "Ljava/lang/Long;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
