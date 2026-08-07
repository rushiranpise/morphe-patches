package app.template.patches.park4night

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.template.patches.shared.Constants.PARK4NIGHT_COMPATIBILITY

// ══════════════════════════════════════════════════════════════════════════════
// Park4Night v7.1.60 — Premium Analysis
// ══════════════════════════════════════════════════════════════════════════════
//
// Framework: Native Kotlin/Java, Google Play Billing + server-side validation
// APK: APKS split, 4 DEX files, r8/proguard obfuscated
//
// PREMIUM GATE ARCHITECTURE:
//
// Layer 1 — User model (J1/n) — runtime user object
//   isPro()          → subscriptionEndDate != null && isFutureDate(subscriptionEndDate)
//   isAmbassador()   → status == p.AMBASSADOR
//   isAdmin()        → status == p.ADMIN || p.SUPER_ADMIN
//   canAccessPro()   → isPro() || isAmbassador() || isAdmin()  ← PRIMARY GATE
//
//   SessionManager.userCanAccessPro() → connectedUser.canAccessPro()
//   Called from: UI components, R1/a, fr/tramb/park4night/ui/**
//   fr/tramb/park4night/services/pro/a.ensureProOrShowLanding() gates all
//   premium features behind userCanAccessPro().
//
// Layer 2 — ProStatePreference (persisted DataStore)
//   Serialized via kotlinx.serialization into a DataStore file.
//   Fields: isMonthlySubscribed, isYearlySubscribed, isPro, isPub, isPubDetail
//   Read directly by UI components that don't go through SessionManager.
//   isPub / isPubDetail: additional publisher-tier flags (higher tier than isPro).
//
// PATCH STRATEGY:
//   J1/n.canAccessPro() → returnEarly(true)
//     Covers all SessionManager.userCanAccessPro() call sites.
//     Fingerprint: PUBLIC FINAL, return Z, no params, calls isPro() + isAmbassador() + isAdmin()
//
//   J1/n.isPro() → returnEarly(true)
//     Covers direct isPro() calls and equality checks.
//     Fingerprint: PUBLIC FINAL, return Z, no params, reads subscriptionEndDate field via
//     string("guest") anchor from isFutureDate which checks username first.
//
//   ProStatePreference.isPro() / isPub() / isPubDetail()
//     / isMonthlySubscribed() / isYearlySubscribed() → returnEarly(true)
//     Covers persisted subscription state reads.
//     Non-obfuscated class name: stable across updates.

private const val USER_CLASS = "LJ1/n;"
private const val PRO_STATE_PREF_CLASS =
    "Lcom/park4night/p4nsharedlayers/data/datasources/local/dto/ProStatePreference;"

// J1/n.canAccessPro() — root premium gate
// Calls isPro() then isAmbassador() then isAdmin() internally.
object CanAccessProFingerprint : Fingerprint(
    definingClass = USER_CLASS,
    name = "canAccessPro",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = USER_CLASS, name = "isPro"),
        methodCall(definingClass = USER_CLASS, name = "isAmbassador"),
        methodCall(definingClass = USER_CLASS, name = "isAdmin"),
    ),
)

// J1/n.isPro() — date-based subscription check
// Reads subscriptionEndDate field, calls isFutureDate().
// Stable anchor: isFutureDate() calls String.compareTo on an ISO date.
object IsProFingerprint : Fingerprint(
    definingClass = USER_CLASS,
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(definingClass = USER_CLASS, name = "isFutureDate"),
    ),
)

// ProStatePreference getters — persisted subscription flags
object ProStatePrefIsProFingerprint : Fingerprint(
    definingClass = PRO_STATE_PREF_CLASS,
    name = "isPro",
    returnType = "Z",
    parameters = emptyList(),
)

object ProStatePrefIsPubFingerprint : Fingerprint(
    definingClass = PRO_STATE_PREF_CLASS,
    name = "isPub",
    returnType = "Z",
    parameters = emptyList(),
)

object ProStatePrefIsPubDetailFingerprint : Fingerprint(
    definingClass = PRO_STATE_PREF_CLASS,
    name = "isPubDetail",
    returnType = "Z",
    parameters = emptyList(),
)

object ProStatePrefIsMonthlyFingerprint : Fingerprint(
    definingClass = PRO_STATE_PREF_CLASS,
    name = "isMonthlySubscribed",
    returnType = "Z",
    parameters = emptyList(),
)

object ProStatePrefIsYearlyFingerprint : Fingerprint(
    definingClass = PRO_STATE_PREF_CLASS,
    name = "isYearlySubscribed",
    returnType = "Z",
    parameters = emptyList(),
)

@Suppress("unused")
val park4NightUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Park4Night premium (Pro) access, removing all feature gates " +
        "for offline maps, advanced filters, and publisher content.",
    default = true,
) {
    compatibleWith(PARK4NIGHT_COMPATIBILITY)

    execute {
        val trueInstructions = "const/4 v0, 0x1\nreturn v0"

        // Layer 1 — User model runtime gates
        for (fingerprint in listOf(CanAccessProFingerprint, IsProFingerprint)) {
            fingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, trueInstructions)
            }
        }

        // Layer 2 — Persisted ProStatePreference gates
        for (fingerprint in listOf(
            ProStatePrefIsProFingerprint,
            ProStatePrefIsPubFingerprint,
            ProStatePrefIsPubDetailFingerprint,
            ProStatePrefIsMonthlyFingerprint,
            ProStatePrefIsYearlyFingerprint,
        )) {
            fingerprint.method.apply {
                removeInstructions(0, instructions.count())
                addInstructions(0, trueInstructions)
            }
        }
    }
}
