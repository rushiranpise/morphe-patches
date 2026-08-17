package app.template.patches.udisc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Verified against UDisc 24.2.8 (versionCode 20235).

// ── STABLE: UserAccountClassFingerprint ──────────────────────────────────────
// Kotlin data class carrying the user's account state. R8 can't rename it
// because "Trial"/"Pro"/"Basic" are literal string constants emitted by the
// compiler for the Kotlin enum-like tier logic inside the class.
// These three strings have been present since at least 24.2.3 and will remain
// as long as the free/trial/pro tier model exists.
val UserAccountClassFingerprint = Fingerprint(
    strings = listOf("Trial", "Pro", "Basic"),
)

// ── STABLE: AccountHasEntitlementFingerprint ──────────────────────────────────
// The only no-arg boolean method in UserAccount that compares an Instant field
// against the current time using IF_LEZ. This maps to "does the account have a
// live (trial-or-paid) entitlement right now?"
// Structural anchor: IF_LEZ / IF_GTZ on a time comparison is an R8-invariant
// pattern — the compiler always emits exactly one conditional branch per
// compareTo() call, and no other ()Z method in this class compares an Instant.
val AccountHasEntitlementFingerprint = Fingerprint(
    classFingerprint = UserAccountClassFingerprint,
    returnType = "Z",
    parameters = emptyList(),
    custom = { method, _ ->
        method.implementation?.instructions?.any {
            it.opcode == Opcode.IF_LEZ || it.opcode == Opcode.IF_GTZ
        } == true
    },
)

// ── STABLE: AccountIsTrialingFingerprint ──────────────────────────────────────
// The only no-arg boolean method in UserAccount that calls
// kotlin.jvm.internal.Intrinsics.areEqual() — i.e. uses Kotlin structural
// equality on an object field. Concretely: it compares the Subscription.status
// field against the Trialing singleton.
//
// WHY THIS ANCHOR:
// Previous versions used fieldAccess(type="Lcom/udisc/kmp/account/Account$Subscription$Status;")
// That KMP type descriptor was present as a SGET_OBJECT target in 24.2.3/24.2.6 but
// disappeared in 24.2.8 when R8 fully inlined the enum into the enclosing package.
// kotlin.jvm.internal.g (compiled form of Intrinsics) is a Kotlin stdlib class —
// guaranteed stable across all Kotlin versions UDisc will ever ship. And k()Z is
// the *only* no-arg boolean method in this class that uses it (verified 24.2.8).
// requireSingleMatch() in the patch enforces this uniqueness at runtime.
val AccountIsTrialingFingerprint = Fingerprint(
    classFingerprint = UserAccountClassFingerprint,
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Lkotlin/jvm/internal/g;",
            name = "c",
        ),
    ),
)

// ── STABLE: SubscriptionClassFingerprint ─────────────────────────────────────
// The Subscription data class. Identified by its toString() prefix string —
// a stable, human-readable string constant the Kotlin compiler emits that
// R8 cannot remove because it appears in production logging/serialisation paths.
// Used as the classFingerprint scope for AccountSubscriptionConstructorFingerprint.
val SubscriptionClassFingerprint = Fingerprint(
    strings = listOf("Subscription(platform="),
)

// ── STABLE: AccountSubscriptionConstructorFingerprint ─────────────────────────
// Kotlin-generated synthetic constructor for the Subscription data class.
// Scoped to SubscriptionClassFingerprint so we don't need to name the obfuscated
// class directly.
//
// WHY NOT MATCH ON PARAMETERS:
// In 24.2.3 params were (I, Platform_old, Status_old, String).
// In 24.2.6 they were (I, Lvy/o0, Lyy/o0, String) — fully obfuscated.
// In 24.2.8 they are (I, Lyy/b2, Lyy/k2, String) — obfuscated again, different letters.
// The obfuscated type descriptors drift every build; anchoring on them guarantees breakage.
// SYNTHETIC + CONSTRUCTOR + 4-param shape + class scope is unique within this class.
val AccountSubscriptionConstructorFingerprint = Fingerprint(
    classFingerprint = SubscriptionClassFingerprint,
    name = "<init>",
    returnType = "V",
    accessFlags = listOf(
        AccessFlags.PUBLIC,
        AccessFlags.SYNTHETIC,
        AccessFlags.CONSTRUCTOR,
    ),
)

// ── STABLE: GooglePlayStorePlatformFingerprint ────────────────────────────────
// The GooglePlayStore Platform singleton. "google-play-store" is the canonical
// serialisation key for the Play billing platform — it's shared with the backend
// API contract and will not change without a breaking API version bump.
// The class has exactly one static-final field (the singleton instance).
val GooglePlayStorePlatformFingerprint = Fingerprint(
    strings = listOf("google-play-store"),
)

// ── STABLE: SubscribedStatusFingerprint ───────────────────────────────────────
// The Subscribed Status singleton. "subscribed" is the canonical serialisation
// key for an active paid subscription — same reasoning as GooglePlayStore above.
// The class has exactly one static-final field (the singleton instance).
val SubscribedStatusFingerprint = Fingerprint(
    strings = listOf("subscribed"),
)

// ── STABLE: WatchAccountProFingerprint ───────────────────────────────────────
// udisc-wear-library data class — module name and public API surface kept stable
// for the Wear OS companion. "isPro" is the real Kotlin property name.
// definingClass is non-obfuscated (library boundary, AndroidManifest-referenced).
val WatchAccountProFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/udiscwearlibrary/WatchAccountInfo;",
    name = "<init>",
    filters = listOf(
        fieldAccess(name = "isPro"),
    ),
)

// ── STABLE: UDiscApplicationOnCreateFingerprint ───────────────────────────────
// Application subclass fixed by android:name in AndroidManifest.xml — R8 can
// never rename or remove it. Same for onCreate().
val UDiscApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/application/UDiscApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
)

// ── STABLE: PlayBillingPurchaseListenerFingerprint ────────────────────────────
// Google Play Billing PurchasesUpdatedListener. The listener class
// (com.udisc.android.billing.b) is non-obfuscated at the package level;
// "acknowledged" is the literal key used in purchase verification logging —
// stable as long as the Play Billing API uses that acknowledgement term.
//
// Deliberately no `parameters`: the first param type drifted build-over-build
// (Lb70/b in 24.2.3, Lnc/c in 24.2.6, Lnc/e in 24.2.8). Omitting it trades
// a tiny amount of match precision for guaranteed update stability.
val PlayBillingPurchaseListenerFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/billing/b;",
    name = "a",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("acknowledged"),
)
