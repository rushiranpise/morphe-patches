package app.template.patches.blurwall.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Lŕ/ؠ;->onReceived(Lcom/revenuecat/purchases/CustomerInfo;)V
// Implements ReceiveCustomerInfoCallback. Reads getActiveSubscriptions() and
// getNonSubscriptionTransactions(), XORs the isEmpty result, stores to SharedPrefs
// key "start_blur". Patching to immediately store true bypasses the subscription check.
object OnReceivedFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf("Lcom/revenuecat/purchases/CustomerInfo;"),
    filters = listOf(
        string("customerInfo"),
        string("start_blur"),
    ),
    custom = { method, _ -> method.name == "onReceived" },
)
