package app.template.patches.larkplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

// ── Lark Player billing-info-provider (IBillingInfoProvide) ──────────────────
//
// The interface itself (formerly "Lo/dw2;") and its sole concrete
// implementation (formerly "Lo/b15;") are BOTH fully obfuscated by R8 and have
// already been observed to rotate names between builds carrying the identical
// version string (2026.10.5) — the implementation class was "b15" in the
// previous build and is "a15" in this one, despite no version bump. Hardcoding
// either of those names is exactly the kind of fragile fingerprint that breaks
// on every rebuild, so none of the fingerprints below reference them.
//
// Instead, PurchaseGetterFingerprint anchors on the one genuinely stable
// signal available: the implementation's own purchase getter returns the
// app's own PurchaseBean data class — a real business-domain type whose name
// is never obfuscated (Moshi/Gson-style model classes keep stable names).
// Every other fingerprint below is scoped to "whatever class contains
// PurchaseGetterFingerprint's match" via classFingerprint, so the concrete
// implementation class is discovered fresh on every run instead of being
// hardcoded.

private const val PURCHASE_BEAN = "Lcom/dywx/larkplayer/module/premium/data/PurchaseBean;"

// Anchor: e()Lcom/dywx/larkplayer/module/premium/data/PurchaseBean;
// A concrete, no-arg method returning PurchaseBean is unique across the whole
// app (it's a small, purpose-specific data class) — EXCEPT for the interface
// itself, which also declares an abstract e()PurchaseBean method with the
// exact same signature (return type + zero params). An abstract method has no
// instruction body at all, so if the fingerprint resolved to *that* instead
// of the concrete override, every other fingerprint scoped to it via
// classFingerprint would fail to match (there'd be nothing to scan).
// `custom` filters out any method without a real implementation, guaranteeing
// this always resolves to the concrete implementing class.
internal val PurchaseGetterFingerprint = Fingerprint(
    returnType = PURCHASE_BEAN,
    parameters = emptyList(),
    custom = { method, _ -> method.implementation != null },
)

// hasPurchase(): return true if the getter above returns non-null.
//   invoke-virtual { }, <self>-><purchase-getter>()LPurchaseBean;
//   move-result-object
//   if-eqz ... (branch on null)
// This exact 3-opcode shape (INVOKE_VIRTUAL → MOVE_RESULT_OBJECT → IF_EQZ) is
// checked to be unique among this class's other boolean-returning methods
// (several other Z-methods on the same god-class invoke a single-arg helper
// and return its result directly with no null branch, so they don't collide).
internal val HasPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IF_EQZ),
    ),
    classFingerprint = PurchaseGetterFingerprint,
)

// hasHistoryPurchase(): return true if a cached PurchaseHistoryRecord is
// non-null. PurchaseHistoryRecord is a Google Play Billing Library class —
// stable and never obfuscated — giving this filter a genuine non-obfuscated
// anchor even though the *shape* match (rather than a direct type filter) is
// what's used to locate the method itself.
//   iget-object  (outer holder field)
//   check-cast   (cast to holder type)
//   iget-object  <holder>-><field>:Lcom/android/billingclient/api/PurchaseHistoryRecord;
//   if-eqz ... (branch on null)
internal val HasHistoryPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.CHECK_CAST),
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.IF_EQZ),
    ),
    classFingerprint = PurchaseGetterFingerprint,
)

// isPermanent(): a kotlin.runCatching { ... } block around an internal
// permanent-status lambda, defaulting to false on failure. Anchored on
// kotlin.Result's own stdlib methods (constructor-impl / isFailure-impl /
// exceptionOrNull-impl) — genuinely stable, since they're Kotlin standard
// library API and are never touched by app-level R8 obfuscation.
//
// IMPORTANT (preserved from the previous version of this patch): patching
// hasPurchase/hasHistoryPurchase alone is NOT sufficient. The app's
// premium-status computation treats "has purchase but isPermanent==false" as
// an *expired* subscription (status 3), which immediately closes the
// in-app-purchase screen (PayPremiumFragment.finish()) the instant it opens,
// even though hasPurchase()==true. isPermanent must also be forced true so
// the computed status resolves to "permanent" (status 1) instead.
internal val IsPermanentFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = "Lkotlin/Result;", name = "constructor-impl"),
        methodCall(definingClass = "Lkotlin/Result;", name = "isFailure-impl"),
        methodCall(definingClass = "Lkotlin/Result;", name = "exceptionOrNull-impl"),
    ),
    classFingerprint = PurchaseGetterFingerprint,
)
