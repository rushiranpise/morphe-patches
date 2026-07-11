package app.template.patches.pixelhabittracker.pro

import app.morphe.patcher.Fingerprint

/**
 * hh2 — PurchaseRepository constructor
 *
 * Single constructor: <init>(Context)V
 * Reads "pro_purchased" from SharedPreferences named "billing_prefs".
 * Stores result in field c:Z and wraps in MutableStateFlow<Boolean> (field d:Lw43;).
 *
 * Note: "habit_tracker_pro" SKU string is in method c(), NOT the constructor.
 * Use "billing_prefs" + "pro_purchased" to uniquely match the constructor.
 */
object PurchaseRepositoryConstructorFingerprint : Fingerprint(
    definingClass = "Lhh2;",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;"),
    strings = listOf("billing_prefs", "pro_purchased"),
    custom = { method, _ -> method.name == "<init>" }
)

/**
 * hh2.f(Z)V — pro-state setter
 *
 * Writes boolean arg to SharedPreferences("pro_purchased") then emits to
 * the w43 MutableStateFlow via w43.i(null, Boolean.valueOf(c)).
 * compareAndSet(null, newValue) — first arg is always null (the UNSET sentinel).
 */
object ProStateSetterFingerprint : Fingerprint(
    definingClass = "Lhh2;",
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("pro_purchased"),
    custom = { method, _ -> method.name == "f" }
)
