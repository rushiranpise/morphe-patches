package app.template.patches.navitime

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// NAVITIME — Japan Travel & Navigation (com.navitime.inbound.walk) v12.0.1
//
// Anti-tamper: No PairIP detected. Play Integrity only present as SDK stubs with no app-level
// calls to requestIntegrityToken() or StandardIntegrityManager.
//
// Subscription architecture (server-side):
//   App calls /initialcheck API on startup → receives BillingCommonResponse {periodTime, currentTime}
//   periodTime = subscription expiry date (ISO-8601)
//   currentTime = server's current time
//
//   DataManager.saveBillingInfo(BillingCommonResponse):
//     Persists to SharedPreferences:
//       PREF_BILLING_EXPIRED_DATETIME = periodTime
//       PREF_BILLING_TIME_STAMP       = currentTime
//     Then calls updateAccountStatus() → updateBillingStatus(periodTime, currentTime)
//
//   DataManager.updateBillingStatus(periodTime: OffsetDateTime, currentTime: String):
//     isFree = ChronoUnit.SECONDS.between(currentTime, periodTime) <= 0
//             OR periodTime == null OR currentTime empty
//     PreferencesHelper.setBoolean("PREF_BILLING_STATUS_IS_FREE", isFree)
//     → Firebase Analytics event: member_status = "free_user" or "pay_user"
//
//   DataManager.isFree()         → Observable<Boolean> from PREF_BILLING_STATUS_IS_FREE
//   DataManager.isFreeAndNotPromotion() → combines isFree + PREF_IS_IN_PROMOTION
//   Both drive: drawer menu, route options, article detail, collection UI, plan screen, ranking
//
// Patch strategy:
//   Patch DataManager.updateBillingStatus() to always write isFree=false (=premium).
//   Inject const/4 p1, 0x0 right before PreferencesHelper.setBoolean() call, overriding
//   the ChronoUnit.SECONDS.between() result. This persists "paid" status regardless of
//   server response, covering online and offline paths (SharedPreferences is persistent).
//   Firebase analytics event still fires (now reporting "pay_user").

/**
 * Targets DataManager.updateBillingStatus(OffsetDateTime, String)V.
 *
 * This private method computes and persists the isFree boolean.
 * Identified by:
 *   - definingClass: com.navitime.inbound.data.DataManager
 *   - name: "updateBillingStatus" (preserved in d2 metadata)
 *   - ChronoUnit.between() call (subscription expiry comparison)
 *   - PreferencesHelper.setBoolean() call (persists the result)
 *
 * We inject const/4 p1, 0x0 immediately before the setBoolean() call to
 * unconditionally write isFree=false (premium) regardless of date comparison.
 */
internal object UpdateBillingStatusFingerprint : Fingerprint(
    definingClass = "Lcom/navitime/inbound/data/DataManager;",
    name = "updateBillingStatus",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.FINAL),
    parameters = listOf("Ljava/time/OffsetDateTime;", "Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            name = "between",
            definingClass = "Ljava/time/temporal/ChronoUnit;",
            returnType = "J",
        ),
        methodCall(
            name = "setBoolean",
            definingClass = "Lcom/navitime/inbound/data/local/PreferencesHelper;",
            returnType = "V",
        ),
    ),
)
