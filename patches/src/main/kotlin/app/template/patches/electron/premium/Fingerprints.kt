package app.template.patches.electron.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// cl5.c(CustomerInfo) — the UpdatedCustomerInfoListener that drives all premium state.
//
// Checks entitlements "pro", "electron+", "electron_plus" and product
// "electron_plus_1.99" then writes the OR result into cl5.a (MutableStateFlow<Boolean>).
// AdMob ad-loading in C2130r5 also reads this StateFlow before every ad call.
//
// Patched by injecting Boolean.TRUE directly into cl5.a at instruction 0 and
// returning early — bypassing all null checks and entitlement lookups entirely.
// Fingerprinted by stable RevenueCat SDK parameter type + app entitlement ID strings.
internal object EntitlementObserverFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    filters = listOf(
        string("pro"),
        string("electron+"),
        string("electron_plus"),
        string("electron_plus_1.99"),
    ),
)
