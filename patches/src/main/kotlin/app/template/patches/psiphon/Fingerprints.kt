package app.template.patches.psiphon

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── SubscriptionState (LB2/j0) / SubscriptionStateImpl (LB2/c) ──────────────

/**
 * SubscriptionStateImpl.getStatus():LB2/j0$a; (LB2/c.h) — the single source of
 * truth for subscription status. LB2/j0.hasValidPurchase() (c) is computed
 * FROM this value via virtual dispatch, so patching h() also fixes c().
 * Several UI call sites (com/psiphon3/j — upgrade button, rate-limit banner)
 * read h() directly instead of going through c(), so h() must be the patch
 * target, not c(), or those UI elements stay visible.
 */
val GetSubscriptionStatusFingerprint = Fingerprint(
    definingClass = "LB2/c;",
    name = "h",
    returnType = "LB2/j0\$a;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)

/**
 * SubscriptionStateImpl.getPurchase():Lcom/android/billingclient/api/Purchase; (LB2/c.g) —
 * reads a (real, often null) Purchase field. Several call sites (com/psiphon3/j, com/psiphon3
 * /PaymentChooserActivity, LB2/g0) assume a non-null Purchase whenever the status looks
 * subscribed and dereference it unsafely (.get(0) on getProducts(), .e() for orderId), so
 * once h() is forced to report a subscription, g() must return a real, well-formed Purchase
 * or those call sites NPE/IndexOutOfBounds at runtime.
 */
val GetPurchaseFingerprint = Fingerprint(
    definingClass = "LB2/c;",
    name = "g",
    returnType = "Lcom/android/billingclient/api/Purchase;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC)
)
