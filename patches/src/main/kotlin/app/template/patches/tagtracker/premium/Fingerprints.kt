package app.template.patches.tagtracker.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets the subscription state writer (dx3.a(Z, qj0)).
 *
 * Billing flow:
 *   BillingClient.queryPurchasesAsync()
 *   → nz3 callback: purchaseList.isEmpty() XOR 1 = isSubscribed
 *   → dx3.a(isSubscribed, coroutineContinuation)
 *   → ra3 coroutine: DataStore.write("is_full_version", isSubscribed)
 *   → kz3 reducer: reads DataStore Boolean → updates jz3.isUpgraded in SettingsUiState
 *   → UI (t6, bz3): jz3.a:Z gates all PRO features
 *
 * dx3.a() is the single tap-point where the subscription boolean (p1:Z) flows from
 * the billing result into the DataStore write. Injecting const/4 p1, 1 at index 0
 * makes every billing callback write true to "is_full_version".
 *
 * Pin: strings "is_paid_user" and "is_lucky_user" are Firebase Analytics event
 * parameter names logged only in dx3.a() — globally unique across the entire DEX.
 * Combined with accessFlags [PUBLIC, FINAL] and returnType Object this is unambiguous.
 */
object SubscriptionStateWriterFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("is_paid_user"),
        string("is_lucky_user"),
    )
)

/**
 * Targets PairIP LicenseClient.initializeLicenseCheck()V.
 * Standard SDK method — name is never obfuscated.
 */
object PairIPLicenseCheckFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "initializeLicenseCheck",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
)

/**
 * Targets PairIP LicenseClient.scheduleAppShutdown()V.
 * Standard SDK method — name is never obfuscated.
 */
object PairIPScheduleShutdownFingerprint : Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "scheduleAppShutdown",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
)
