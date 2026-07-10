package app.template.patches.strengthtraining.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Reads sp_premium SharedPreference; main isPremium gate.
object UserPrefsIsPremiumFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("getIsPremium() -> isPaid: "),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    name = "d",
    returnType = "Z",
    parameters = emptyList(),
)

// z0/z3.t0():Z — reads sp_android_subscription; secondary billing gate.
object HasAndroidSubscriptionFingerprint : Fingerprint(
    definingClass = "Lne7;",
    name = "w0",
    returnType = "Z",
    parameters = emptyList(),
)

// z0/z3.q0(Z):V — writes sp_premium = !cancelled on subscription cancel.
// No-op to prevent premium flag being cleared.
object SaveUserStatusFingerprint : Fingerprint(
    definingClass = "Lne7;",
    name = "t0",
    returnType = "V",
    parameters = listOf("Z"),
)

// PaidStatus fingerprints for UI-level subscription display
object IsPaidStatusEmptyFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    name = "isPaidStatusEmpty",
)

object IsPremiumFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    name = "isPremium",
)

object LoginResponseIsPaidFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/LoginResponse;",
    name = "isPaid",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// PaidStatus.getType():String — returns "indi"/"pro"/"business"/"" from server field.
// Profile fragment uses this to display plan name via getCurrentPlan().
// Patch to return "business" so getCurrentPlan() maps it to Business.
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

object GetTraineeLimitFingerprint : Fingerprint(
    definingClass = "Lair/com/musclemotion/data/remote/response/login/PaidStatus;",
    name = "getTraineeLimit",
    returnType = "Ljava/lang/Integer;",
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

// // l/b1.d():Z — reads SharedPreferences("sp_premium").
// // z0/z3.s() delegates to this; home/p.smali calls z0/z3.s() via g2/n interface
// // to determine whether home screen items are paywalled (v18 flag).
// // When false → b7/l.k=true → x6/v adapter shows paywall instead of playing video.
// // Patch to return true so home screen items play directly.
// object UserPrefsIsPremiumFingerprint : Fingerprint(
//     classFingerprint = Fingerprint(
//         strings = listOf("getIsPremium() -> isPaid: "),
//     ),
//     accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
//     returnType = "Z",
//     parameters = emptyList(),
//     name = "d",
// )
