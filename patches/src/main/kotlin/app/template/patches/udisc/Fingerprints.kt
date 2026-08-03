package app.template.patches.udisc

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Verified against UDisc 24.2.6 (versionCode 9943).

// Kotlin data class carrying the user's account state. Never obfuscated by name
// (R8 can't rename it away since "Trial"/"Pro"/"Basic" tier labels are embedded as
// string constants inside it), so this anchor is stable across updates.
val UserAccountClassFingerprint = Fingerprint(
    strings = listOf("Trial", "Pro", "Basic"),
)

// The two boolean, no-arg gates below live on the class matched by
// UserAccountClassFingerprint. Neither has a name that survives obfuscation
// (both were fully inlined/renamed by R8 for a build with data-class property
// getters -- there is no public Kotlin API name left in the bytecode to anchor
// on), so they're disambiguated structurally instead, scoped to that one class:
//
//  - AccountHasEntitlementFingerprint: the only no-arg boolean method in the
//    class that compares a cached expiry Instant against "now" (IF_LEZ/IF_GTZ).
//    This corresponds to "does the account have a live (trial-or-paid) entitlement".
//  - AccountIsTrialingFingerprint: the only no-arg boolean method in the class
//    that references the Account.Subscription.Status enum at all. It compares
//    the user's subscription status against the TRIALING constant. The Status
//    enum itself is a real (non-obfuscated) Kotlin Multiplatform model type
//    shared with the backend/iOS, so anchoring on "any access to that enum
//    type" is far more durable than anchoring on the single-letter field name
//    the TRIALING constant happens to be compiled to in this build.
//
// Both are asserted unique against the current build in patch execution --
// see requireSingleMatch() in UDiscUnlockProPatch.kt. If a future update adds
// another IF_LEZ/IF_GTZ or Status-referencing boolean method to this class,
// the patch will fail loudly instead of silently patching the wrong gate.
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

val AccountIsTrialingFingerprint = Fingerprint(
    classFingerprint = UserAccountClassFingerprint,
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            type = "Lcom/udisc/kmp/account/Account\$Subscription\$Status;",
            opcode = Opcode.SGET_OBJECT,
        ),
    ),
)

// udisc-wear-library data class -- not obfuscated (it's a standalone library
// module, kept as a stable public surface for the Wear OS companion app to
// consume). "isPro" is the real Kotlin property name.
val WatchAccountProFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/udiscwearlibrary/WatchAccountInfo;",
    name = "<init>",
    filters = listOf(
        fieldAccess(name = "isPro"),
    ),
)

// The application's Application subclass -- name is fixed by AndroidManifest.xml
// (android:name), so R8 can never rename or remove it.
val UDiscApplicationOnCreateFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/application/UDiscApplication;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
)

// Google Play Billing PurchasesUpdatedListener implementation (a Kotlin
// compiler-generated lambda class -- its name and its wrapping interface are
// both obfuscated internal types with no stable public name available).
// Deliberately does NOT match on `parameters`: the first parameter type was
// `Lb70/b;` in 24.2.3 and is `Lnc/c;` in 24.2.6 -- an internal, obfuscated
// type that drifts between builds and adds no real matching precision beyond
// what `strings` and `accessFlags` already provide.
val PlayBillingPurchaseListenerFingerprint = Fingerprint(
    definingClass = "Lcom/udisc/android/billing/b;",
    name = "a",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("acknowledged"),
)

// Synthetic constructor for the Account.Subscription data class. Deliberately
// does NOT match on `definingClass`: this is a Kotlin compiler-generated
// synthetic class with no stable name -- it was `Lvy/o0;` in 24.2.3 and is
// `Lyy/o0;` in 24.2.6. The parameter list alone (an int ordinal plus the two
// non-obfuscated Account.Subscription enum types, shared KMP model types kept
// stable for backend/iOS interop) is unique across the entire app and is a
// far more durable anchor.
val AccountSubscriptionConstructorFingerprint = Fingerprint(
    accessFlags = listOf(
        AccessFlags.PUBLIC,
        AccessFlags.SYNTHETIC,
        AccessFlags.CONSTRUCTOR,
    ),
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "I",
        "Lcom/udisc/kmp/account/Account\$Subscription\$Platform;",
        "Lcom/udisc/kmp/account/Account\$Subscription\$Status;",
        "Ljava/lang/String;",
    ),
)
