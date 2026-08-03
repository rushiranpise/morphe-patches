package app.template.patches.boxbox

import app.morphe.patcher.Fingerprint

// ── Box Box subscription state (RevenueCat + DataStore persistence) ──────────
//
// Subscription state flow: a RevenueCat listener coroutine writes the app's
// Free/Pro plan enum into a Preferences DataStore ("plans_data_store" file,
// "plan_type" key). Separately, a FirebaseMessagingService coroutine reads
// that same DataStore value back out to decide whether to unlock Pro features
// or show the paywall. Patching only the write side is not sufficient: the
// read side may already have a stale Free value persisted from before the
// device was ever patched, so both the write and read paths must be forced.
//
// Every class name involved here is obfuscated and has already been observed
// to shift between versions carrying no functional change of their own —
// e.g. the plan-writer class was "pd6" in 5.4.13 and is "ag6" in 5.4.15, and
// the short name "rb8" (5.4.13's Free/Pro enum) was silently reassigned by R8
// to an entirely unrelated enum in 5.4.15 rather than simply disappearing.
// Reusing a previously-observed obfuscated name — even opportunistically —
// is therefore actively unsafe here, not just fragile. None are hardcoded
// below; everything is discovered structurally, either via genuine
// human-authored string literals or via classFingerprint chaining.

// Locates the DataStore preference-key holder class purely by the two
// literal keys it defines in its own <clinit> — "plans_data_store" (the
// DataStore file name) and "plan_type" (the preference key name). Both are
// real config identifiers with no reason for R8 to rename their *contents*
// (only the surrounding class name is obfuscated and rotates).
internal object PlanDataStoreHolderFingerprint : Fingerprint(
    strings = listOf("plans_data_store", "plan_type"),
)

// <plan-enum-writer>(<FreeProEnum>, <continuation>)Ljava/lang/Object; — the
// DataStore plan writer, invoked whenever the app persists a Free/Pro
// decision. It's the only Object-returning method in the class located
// above (the others are <clinit>/<init>/a Flow getter), so return type +
// classFingerprint scoping alone is enough to resolve it uniquely — no need
// to name either of its (one obfuscated, one stdlib-coroutine) parameter
// types.
internal object PlanDataStoreWriterFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    classFingerprint = PlanDataStoreHolderFingerprint,
)

// invokeSuspend(Object)Object — the FirebaseMessagingService coroutine that
// reads the persisted plan back out of DataStore and decides whether to
// unlock Pro features or show the paywall. Anchored on a literal analytics
// event-name string, "is eligible to get LA", that sits a few instructions
// after the DataStore-read result is unwrapped and cast to the plan enum —
// a human-authored event name, not touched by obfuscation, and confirmed
// unique across the entire app.
//
// The exact check-cast (to the plan enum) and the plan enum's Pro constant
// are both located dynamically at patch-time in UnlockProPatch.kt, rather
// than being encoded as fingerprint filters here, since the enum's own type
// name is one of the values being discovered.
internal object PlanDataStoreReaderFingerprint : Fingerprint(
    strings = listOf("is eligible to get LA"),
)
