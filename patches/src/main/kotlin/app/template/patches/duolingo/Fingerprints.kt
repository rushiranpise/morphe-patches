package app.template.patches.duolingo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Duolingo 6.90.3 fingerprint stability notes
//
// STABLE (non-obfuscated class/method paths, unchanged across versions):
//   com/duolingo/data/plus/SubscriptionInfo   — data class, <init> signature stable
//   com/duolingo/data/user/User               — data class
//   com/duolingo/data/user/SubscriberLevel    — enum: FREE, GOLD, PREMIUM, LITE, UNKNOWN
//   com/duolingo/core/subscription/models/SubscriptionFeatures — enum, values stable
//
// OBFUSCATED (change each release, fingerprinted via toString strings):
//   ef20                — LoggedIn state wrapper (was previously a different class)
//   x9m                 — MaxHooksUserData wrapper
//   n89                 — DebugMenuProvider (holds isDebug field via Lie3;->a:Z)
//
// REMOVED in 6.90.3:
//   VideoCallTabCtaButtonState  — class gone; VideoCallTabCtaButtonStateToStringFingerprint removed
//   User.isPaid:Z               — field gone; UserIsPaidFieldUsageFingerprint removed
//   User.hasGold:Z              — replaced by User.P0:Z (read in MaxHooksUserData constructor)
//
// RENAMED in 6.90.3:
//   User.r()SubscriptionInfo → User.s()SubscriptionInfo (subscription getter)
// ─────────────────────────────────────────────────────────────────────────────

// Targets SubscriptionInfo constructor for full subscription object replacement.
// Signature stable since 6.88: (String, J, Z, I, I, String, String, Z, String) → V
// Fields written in order: a(currency), b(expiry J), c(trial Z), d(periodLength I),
// e(periodUnit I), f(productId), g(provider), h(active Z), i(vendorPurchaseId)
// j(expiryMillis J) is computed from b via TimeUnit.SECONDS.toMillis.
object SubscriptionInfoConstructorFingerprint : Fingerprint(
    definingClass = "Lcom/duolingo/data/plus/SubscriptionInfo;",
    name = "<init>",
    returnType = "V",
    parameters = listOf(
        "Ljava/lang/String;",
        "J",
        "Z",
        "I",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "Z",
        "Ljava/lang/String;",
    ),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
)

// Targets User.s() — the SubscriptionInfo getter on the User data class.
// 6.88: name was "r". 6.90.3: renamed to "s". Stable definingClass.
object UserSubscriptionInfoFingerprint : Fingerprint(
    definingClass = "Lcom/duolingo/data/user/User;",
    name = "s",
    returnType = "Lcom/duolingo/data/plus/SubscriptionInfo;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// Targets ef20 (LoggedIn state wrapper).
// toString prints "LoggedIn(user=…)" — stable across obfuscation.
// Constructor: <init>(User) — stores User as field a.
// We inject User field writes before return-void to apply premium state.
object LoggedInStateFingerprint : Fingerprint(
    strings = listOf("LoggedIn(user=", ")"),
)

// Targets com/duolingo/data/user/User toString method.
// Used to locate hasPlus (y:Z) and subscriberLevel (A0:SubscriberLevel) fields
// by their stable toString label strings.
object UserFingerprint : Fingerprint(
    strings = listOf("User(adsConfig=", ", id=", ", betaStatus="),
)

// Targets x9m (MaxHooksUserData) to locate User.P0:Z (hasGold/MAX indicator).
// toString strings stable across obfuscation.
// The constructor reads User.P0:Z → stored in x9m.b:Z (hasMax field).
// UserHasGoldFieldUsageFingerprint then finds User.P0:Z via fieldAccess filter.
object MaxHooksUserDataFingerprint : Fingerprint(
    strings = listOf(
        "MaxHooksUserData(isAdmin=",
        ", hasMax=",
        ", plusSubscriptionInventoryItem=",
    ),
)

// Locates User.P0:Z (hasMax/hasGold field) via the MaxHooksUserData constructor
// which reads it with iget-boolean … User;->P0:Z.
// classFingerprint pins to x9m; fieldAccess(User, Z) finds the first User boolean field.
object UserHasGoldFieldUsageFingerprint : Fingerprint(
    classFingerprint = Fingerprint(
        strings = listOf("MaxHooksUserData(isAdmin="),
    ),
    filters = listOf(
        fieldAccess(
            definingClass = "Lcom/duolingo/data/user/User;",
            type = "Z",
        ),
    ),
)

// Targets n89 (DebugMenuProvider) for debug menu enabling.
// Reads Lie3;->a:Z as isDebug, branches on "debug"/"release" for BUILD_TARGET.
// Opcode filter: IGET_OBJECT (read ie3 ref) → IGET_BOOLEAN (ie3.a:Z) → IF_EQZ (branch)
object BuildTargetFieldFingerprint : Fingerprint(
    strings = listOf("BUILD_TARGET", "debug", "release"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.IGET_BOOLEAN,
        Opcode.IF_EQZ,
    ),
)
