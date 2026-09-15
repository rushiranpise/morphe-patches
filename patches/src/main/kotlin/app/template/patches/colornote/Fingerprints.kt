package app.template.patches.colornote

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── STABILITY CONTRACT ─────────────────────────────────────────────────────────
// Previous version used definingClass = "Lsm/c5/y;" — BROKE when package path
// changed from c5 to d5 in v4.8.8. ALL obfuscated class/package paths removed.
// Now uses ONLY:
//   - Named app classes with stable methods (ColorNote.c)
//   - Named billing datatype classes (@Keep, never renamed)
//   - Java/Android SDK methods (never obfuscated)
//   - Stable string literals from billing/account init code
// ───────────────────────────────────────────────────────────────────────────────

// Locate sm.F4.a (LicensePurchased) via its constructor's stable parameter:
// InAppPurchaseData is in com.socialnmobile.commons.inapppurchase.billing.datatypes
// — a named package with @Keep annotation; R8 CANNOT rename it.
private val LicensePurchasedClassFingerprint = Fingerprint(
    parameters = listOf(
        "L",  // sm.w5.f (obfuscated — use placeholder)
        "Ljava/lang/String;",
        "J",
        "J",
        "J",
        "Lcom/socialnmobile/commons/inapppurchase/billing/datatypes/InAppPurchaseData;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Integer;",
        "L",  // sm.w5.d (obfuscated)
        "L",  // sm.F4.n (obfuscated)
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

// sm.F4.a.i()Z — license validity check (root of isPremium chain).
// Filter: System.currentTimeMillis() — first call in i()Z; stable Java SDK.
// classFingerprint locates the class without using its obfuscated name.
// Smali verified: classes.dex, versionCode 2104880.
internal val IsPremiumFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    classFingerprint = LicensePurchasedClassFingerprint,
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/System;",
            name = "currentTimeMillis",
        ),
    ),
)

// sm.d5.y.i()Z — isSubscribed() on the SubscriptionStatus object.
// Belt-and-suspenders: covers null-check short-circuit in y.g() that bypasses F4.a.i().
// classFingerprint locates y via stable log string + ColorNote.c(String) call.
// Smali verified: classes.dex, versionCode 2104880.
private val SubscriptionStatusClassFingerprint = Fingerprint(
    filters = listOf(
        methodCall(
            definingClass = "Lcom/socialnmobile/colornote/ColorNote;",
            name = "c",
        ),
    ),
    strings = listOf("ActiveAccountTracker: lazy initialized with AccountDataLoader"),
)

internal val IsSubscribedFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    classFingerprint = SubscriptionStatusClassFingerprint,
)
