package app.template.patches.anexplorer.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

// ══════════════════════════════════════════════════════════════════════════════
// AnExplorer v6.0.6 — Two variants, different obfuscated class names
//
// TV variant    (versionCode 60604, 2arch): purchase gate via o66 / rz8
// Phone variant (versionCode 60601, 4arch): purchase gate via h66 / hz8
//
// BOTH variants share the same ds.g()Z method — this is the ACTUAL gate
// that controls paywall visibility in ProWrapper. The o66/h66 methods only
// seed DocumentsApplication.y which is a separate (secondary) gate.
// ══════════════════════════════════════════════════════════════════════════════

// ── SharedPrefs paywall gate (BOTH variants — same class name) ────────────────

// ds.g()Z — present in both TV and phone builds (classes.dex)
// Reads SharedPreferences.getBoolean("purchased", false) and
// oy3.n("PURCHASE_ALT_STATUS", false) to decide whether to show the paywall.
// Called by g14.invokeSuspend (ProWrapper coroutine) to gate the purchase screen.
// THIS is what controls the "Unlock Unlimited Access" dialog.
//
// Logic: return false if both "purchased"=false AND "PURCHASE_ALT_STATUS"=false
//        return true  if either is true
// Patching → true hides the paywall entirely.
//
// Filter: string("purchased") is the SharedPrefs key read — stable and unique.
object DsPurchasedCheckFingerprint : Fingerprint(
    definingClass = "Lds;",
    name = "g",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        string("purchased"),
    ),
)

// ── TV variant (o66) ─────────────────────────────────────────────────────────

// o66.p(Context)Z — root purchase check, seeds DocumentsApplication.y
// Filter: methodCall(rz8, p0) = hasSystemFeature wrapper.
object O66PurchaseCheckFingerprint : Fingerprint(
    definingClass = "Lo66;",
    name = "p",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lrz8;", name = "p0"),
    ),
)

// o66.s()Z — combined pro+storage runtime gate
// Filter: methodCall(o66, t) = fallback permission check.
object O66CombinedGateFingerprint : Fingerprint(
    definingClass = "Lo66;",
    name = "s",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Lo66;", name = "t"),
    ),
)

// ── Phone variant (h66) ───────────────────────────────────────────────────────

// h66.p(Context)Z — root purchase check, phone variant
// Filter: methodCall(hz8, p0) = hasSystemFeature wrapper.
object H66PurchaseCheckFingerprint : Fingerprint(
    definingClass = "Lh66;",
    name = "p",
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(definingClass = "Lhz8;", name = "p0"),
    ),
)

// h66.s()Z — combined pro+storage runtime gate, phone variant
// Filter: methodCall(h66, t) = fallback permission check.
object H66CombinedGateFingerprint : Fingerprint(
    definingClass = "Lh66;",
    name = "s",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Lh66;", name = "t"),
    ),
)
