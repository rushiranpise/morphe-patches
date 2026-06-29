package app.template.patches.posture.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

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

// l/m0.d():Z — reads SharedPreferences("sp_premium") and drives:
// (1) whether the pricing row is shown in settings list (if-eqz v10 :cond_1a in
//     ProfileAndSettingsFragment.onViewCreated), (2) the "purchased" state badge.
// Patch to always return true = user appears subscribed in settings.
object UserPrefsIsPremiumFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("getIsPremium() -> isPaid: "),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    name = "d",
)

// PaidStatus.getType():String — returns the subscription type label ("pro", "indi", etc.)
// shown in the profile "Current Plan" section. Null by default (no server data).
// Patch to return "pro" so the profile shows a valid subscription plan.
object PaidStatusGetTypeFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    name = "getType",
)

// PaidStatus.getBilling():String — returns the billing period label ("Monthly", "Yearly").
// Shown alongside getType() in the profile subscription info section.
// Patch to return "Yearly" so the profile shows a valid billing period.
object PaidStatusGetBillingFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("Monthly", "Yearly", "3 Month", "3 Years"),
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/String;",
    parameters = emptyList(),
    name = "getBilling",
)
