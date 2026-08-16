package app.template.patches.automate.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── AutomateService.f(F0, BeginningStatement, Object, Z)Z — flow execution gate ──
// Called before each flow is allowed to run. Returns true = allowed, false = blocked.
//
// Logic:
//   1. If Z1 == 3 (premium verified) → return true immediately   [was Y1 in v1.51.1]
//   2. Query runningStatementCount — if count ≤ 30 (0x1e) → return true (free tier)
//   3. If count > 30 → check premium → if not premium → show PremiumPurchaseActivity
//
// Patch: returnEarly(true) at index 0. Every flow is always allowed.
//
// Fingerprinted by the stable "runningStatementCount" + "checkPremiumAllow" strings.
// The first parameter class was renamed B0→F0 in v1.53.1; both names are obfuscated
// so we use "L" as a placeholder — the stable strings uniquely identify the method.
object AutomatePremiumGateFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "L",  // obfuscated flow context class (was B0 in v1.51.1, F0 in v1.53.1)
        "Lcom/llamalab/automate/BeginningStatement;",
        "Ljava/lang/Object;",
        "Z"
    ),
    filters = listOf(
        string("runningStatementCount"),
        string("checkPremiumAllow")
    )
)

// ── AutomateService.onQueryPremiumCompleted — premium state setter ────────────
// Called when Play Billing returns a purchase query result.
// Sets Z1 = 3 (premium) if purchase is valid and acknowledged, else Z1 = 1.
// Field was renamed Y1:I → Z1:I in v1.53.1.
//
// Patch: addInstructions(0, iput 3 to Z1 field) so premium state is always set
// before the billing result is processed.
//
// Fingerprint: non-obfuscated class + method name (F3/b$i interface impl),
// anchored by the stable "onQueryPremiumCompleted failed" log string.
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

// ── MainFragment.onQueryPremiumCompleted — settings UI premium label ──────────
// Called when billing query returns. Only calls setPremiumPurchase() if a valid
// purchase exists — leaving "Buy premium" text on fresh installs.
// Patching to always call setPremiumPurchase() at the top forces the
// "You got Premium" / "View order information" strings regardless.
//
// Fingerprint: non-obfuscated class + method name. "MainSettingsFragment" log
// tag distinguishes this from AutomateService's copy of the same method name.
object AutomateMainFragmentPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/llamalab/automate/prefs/MainFragment;",
    name = "onQueryPremiumCompleted",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("Lcom/android/billingclient/api/Purchase;", "Ljava/lang/Throwable;"),
    filters = listOf(
        string("MainSettingsFragment")
    )
)
