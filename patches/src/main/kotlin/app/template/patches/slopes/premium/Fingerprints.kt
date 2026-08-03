package app.template.patches.slopes.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.Opcode

// ── Slopes 2026.15 premium gate ───────────────────────────────────────────────
//
// As of 2026.15, the old `com.consumedbycode.slopes.access.AccessController`
// class (getPassExpiration / getAutoRenewing / isSubscribed) no longer exists —
// the whole `access` package was removed and the pass-status logic was moved
// into a class whose name IS now obfuscated by R8 (currently `Lh3/e;`, but that
// name will change again on the next build). Because of that we do NOT anchor
// on the containing class name or the (single-letter, obfuscated) method names.
//
// Instead every fingerprint below is anchored purely on calls into stable,
// non-obfuscated SDK/data classes (java.time.Instant, and the app's own Moshi
// data classes AccountPurchases / UnusedPass, whose class names survive
// obfuscation because they're referenced by string in Moshi's generated
// adapters). This keeps the fingerprints resilient to the method/class rename
// churn that broke the old AccessController-based fingerprints on this update.
//
// The three methods patched are unchanged in *purpose* from the old
// AccessController, just renamed and relocated:
//   i()Ljava/time/Instant;  — pass/membership expiration instant (was getPassExpiration)
//   j()Z                    — "has an active, non-expired pass" gate (was isSubscribed)
//   h()Z                    — "has any pass/membership on file" gate (was closest to getAutoRenewing)

// Targets the method returning the pass/membership expiration Instant.
// Two static code paths both end in this shape:
//   - debug/simulated path: Instant.now().plusSeconds(...)
//   - real path: reads AccountPurchases' Instant field directly
// Both call sequences appear in program order regardless of which branch
// executes at runtime, so both are usable as ordered filters.
internal object GetPassExpirationFingerprint : Fingerprint(
    returnType = "Ljava/time/Instant;",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/time/Instant;",
            name = "now",
        ),
        methodCall(
            definingClass = "Ljava/time/Instant;",
            name = "plusSeconds",
        ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/consumedbycode/slopes/data/AccountPurchases;",
            type = "Ljava/time/Instant;",
        ),
    ),
)

// Targets the "has an active, non-expired pass" boolean gate. Internally calls
// the expiration-Instant method above and compares it against Instant.now()
// via isAfter. Constrained to the same class as GetPassExpirationFingerprint
// so it can't accidentally match an unrelated isAfter() check elsewhere in the
// app.
internal object IsSubscribedFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/time/Instant;",
            name = "now",
        ),
        methodCall(
            definingClass = "Ljava/time/Instant;",
            name = "isAfter",
        ),
    ),
    classFingerprint = GetPassExpirationFingerprint,
)

// Targets the "has any pass/membership on file" boolean gate. Reads
// AccountPurchases' membership-ranges list, then iterates the app's own
// UnusedPass data class checking its isActive-equivalent boolean field.
// Field names on both classes are obfuscated (Moshi only needs stable class
// names, not stable field names), so filters intentionally omit `name` and
// match on definingClass + type only.
internal object HasAnyPassFingerprint : Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lcom/consumedbycode/slopes/data/AccountPurchases;",
            type = "Ljava/util/List;",
        ),
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Lcom/consumedbycode/slopes/vo/UnusedPass;",
            type = "Z",
        ),
    ),
    classFingerprint = GetPassExpirationFingerprint,
)
