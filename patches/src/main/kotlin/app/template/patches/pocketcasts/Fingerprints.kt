package app.template.patches.pocketcasts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags

// Verified against Pocket Casts 8.16 (versionCode 9441).

private const val SUBSCRIPTION_TIER = "Lau/com/shiftyjelly/pocketcasts/payment/SubscriptionTier;"

// Membership.<obfuscated method>()<obfuscated enum type> — computes a coarse
// "Paid"/"Unsigned"/"Free"/"Unknown" status from the account's Subscription.
// The method name ("a"), its return type, and that return type's own class
// name all change every build (e.g. the return type was `Ld5e;` in 8.14 and
// is `Ldae;` in 8.16). What's stable is that the method body directly
// compares the current SubscriptionTier against the real, non-obfuscated
// `SubscriptionTier.Plus` and `SubscriptionTier.Patron` constants -- so those
// two field reads, in the order they appear in the method body, anchor the
// match instead of any obfuscated name.
val MembershipStatusFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(definingClass = SUBSCRIPTION_TIER, name = "Plus", type = SUBSCRIPTION_TIER),
        fieldAccess(definingClass = SUBSCRIPTION_TIER, name = "Patron", type = SUBSCRIPTION_TIER),
    ),
)

// Locates whichever enum constant class currently represents the "Paid"
// membership status, by the one thing about it that never changes: its
// Kotlin enum constant name, passed as a string literal to
// Enum.<init>(String, int) in its own generated constructor. The holder
// enum's class name and this constant's field name on that holder are both
// obfuscated and unstable (see UnlockPlusPatch.kt for how the holder type
// and ordinal are resolved dynamically from this fingerprint at patch time).
val PaidMembershipTierConstructorFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    name = "<init>",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("Paid"),
)

// Membership.<obfuscated method>(<obfuscated param>)Z — checks whether a
// specific feature is included in the account's membership. Deliberately
// does not match on the parameter type (`Lze7;` in 8.14, `Ldh7;` in 8.16 --
// an internal, obfuscated feature-flag type). The single-obfuscated-param
// boolean method is otherwise unique on this class.
val MembershipHasFeatureFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    returnType = "Z",
    parameters = listOf("L"),
)

// Subscription.<obfuscated method>()Z — "is this a legacy lifetime
// subscription". Unique no-arg boolean method on the class; name ("a") is
// obfuscated but has no better anchor available (no strings or stable type
// references in its body to key on), and is verified unique each update.
val SubscriptionLifetimeFingerprint = Fingerprint(
    definingClass = "Lau/com/shiftyjelly/pocketcasts/models/type/Subscription;",
    name = "a",
    returnType = "Z",
    parameters = emptyList(),
)

// Locates whichever enum constant class currently represents the "Android"
// subscription platform. Unlike the membership-tier enum above, every
// constant on this enum keeps its own stable field name (Android, iOS, Web,
// Gift, Unknown) even though the holder class itself is obfuscated and
// renamed every build (`Lapc;` in 8.14, `Letc;` in 8.16) -- so this is
// matched purely structurally, by the shape of the enum's own static field
// table, and the holder type is then referenced by name directly.
val SubscriptionPlatformEnumFingerprint = Fingerprint(
    name = "<clinit>",
    custom = { _, classDef ->
        classDef.staticFields.any { it.name == "Android" && it.type == classDef.type } &&
            classDef.staticFields.any { it.name == "iOS" && it.type == classDef.type }
    },
)

// The mapper that turns a server SubscriptionStatusResponse into a local
// Membership. Deliberately does not match on `definingClass`: this is a
// Kotlin compiler-generated top-level-function holder class with no stable
// name (`Lby5;` in 8.14, `Loz5;` in 8.16). The method name ("M") plus the
// fully-qualified, non-obfuscated return/parameter types are already unique
// across the whole app.
val SubscriptionStatusMapperFingerprint = Fingerprint(
    name = "M",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Lau/com/shiftyjelly/pocketcasts/models/type/Membership;",
    parameters = listOf("Lau/com/shiftyjelly/pocketcasts/servers/sync/SubscriptionStatusResponse;"),
)
