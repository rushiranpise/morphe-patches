package app.template.patches.automate.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── AutomateService.f(B0, BeginningStatement, Object, Z)Z — flow execution gate ──
// Called before each flow is allowed to run. Returns true = allowed, false = blocked.
//
// Logic:
//   1. If Y1 == 3 (premium verified) → return true immediately
//   2. Query runningStatementCount — if count ≤ 30 (0x1e) → return true (free tier)
//   3. If count > 30 → check premium → if not premium → show PremiumPurchaseActivity
//
// Patch: returnEarly(true) at index 0. Every flow is always allowed.
//
// Fingerprinted by the stable "runningStatementCount" string inside the method,
// on a class that also contains "checkPremiumAllow" (unique to AutomateService).
object AutomatePremiumGateFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/llamalab/automate/B0;",
        "Lcom/llamalab/automate/BeginningStatement;",
        "Ljava/lang/Object;",
        "Z"
    ),
    filters = listOf(
        string("runningStatementCount"),
        string("checkPremiumAllow")
    )
)

// ── AutomateService.onQueryPremiumCompleted(Purchase, Throwable)V — premium state setter ──
// Called when Play Billing returns a purchase query result.
// Sets Y1 = 3 (premium) if purchase is valid and acknowledged, else Y1 = 1 (not premium).
//
// Patch: addInstructions(0, iput 3 to Y1 field) so premium state is always set before
// the billing result is processed — ensures UI also reflects premium status.
//
// Stable fingerprint: non-obfuscated method name on AutomateService (F3/b$i interface impl),
// anchored by the "onQueryPremiumCompleted failed" log string.
object AutomatePremiumQueryFingerprint : Fingerprint(
    definingClass = "Lcom/llamalab/automate/AutomateService;",
    name = "onQueryPremiumCompleted",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/android/billingclient/api/Purchase;", "Ljava/lang/Throwable;"),
    filters = listOf(
        string("onQueryPremiumCompleted failed")
    )
)
