package app.template.patches.adguard

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Class anchors
// ─────────────────────────────────────────────────────────────────────────────

/**
 * PlusManager class anchor.
 *
 * Obfuscated class names across verified versions:
 *   v4.14.0 phone : D0/b
 *   v4.13.0 TV    : F0/b
 *
 * Anchored on the retry-log string — a developer-written semantic message tied
 * to the retry logic inside PlusManager. Decouples all child fingerprints from
 * the obfuscated class name.
 */
private val PlusManagerClassFingerprint = Fingerprint(
    strings = listOf("Failed to get state from backend. Remaining retry count: "),
)

/**
 * PlusState.PaidLicense class anchor.
 *
 * Obfuscated class names across verified versions:
 *   v4.14.0 phone : G0/i$n
 *   v4.13.0 TV    : I0/i$n
 *
 * Anchored on the Kotlin data-class compiler-generated toString prefix.
 */
private val PaidLicenseClassFingerprint = Fingerprint(
    strings = listOf("PaidLicense(licenseKey="),
)

// ─────────────────────────────────────────────────────────────────────────────
// Method fingerprints
// ─────────────────────────────────────────────────────────────────────────────

/**
 * PlusManager.getCachedPlusState() — cache-aside getter.
 *
 * Verified method names:
 *   v4.14.0 phone : T6()LG0/i
 *   v4.13.0 TV    : P6()LI0/i
 *
 * Reads the in-memory cached PlusState field (IGET_OBJECT). On cache miss,
 * fetches from storage, then writes the result back (IPUT_OBJECT). No params,
 * returns a PlusState object.
 *
 * Uniqueness verified exhaustively against all no-param, object-returning
 * methods in PlusManager: only T6/P6 contain both IGET_OBJECT and IPUT_OBJECT.
 *   R6()  : iget=2, iput=0  ← no write-back → excluded
 *   T6/P6 : iget=1, iput=1  ← cache read + write-back → unique match ✓
 *   r6()  : iget=0, iput=0  ← excluded
 *   x0()/t0() : iget=1, iput=0 ← no write-back → excluded
 *
 * Smali pattern (both versions):
 *   iget-object v0, p0, LXX/b;->l:LYY/i;   ← cache read  ← anchor
 *   if-nez v0, :cond_a
 *   invoke-virtual {p0}, LXX/b;->N6()LYY/i; ← storage fallback
 *   move-result-object v0
 *   iput-object v0, p0, LXX/b;->l:LYY/i;   ← write-back  ← anchor
 *   :cond_a
 *   return-object v0
 */
internal val GetPlusStateFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.IPUT_OBJECT),
    ),
    custom = { method, _ -> method.parameterTypes.isEmpty() },
)

/**
 * PlusManager.setPlusState(PlusState) — persist + notify.
 *
 * Verified method names:
 *   v4.14.0 phone : Z6(LG0/i)V
 *   v4.13.0 TV    : V6(LI0/i)V
 *
 * Writes the new PlusState into the in-memory cache field (IPUT_OBJECT),
 * persists it to storage, then notifies all registered observers via an
 * interface call (the PlusManagerNotificationAssistant observer, INVOKE_INTERFACE).
 *
 * ⚠️ False-positive hazard (crash root cause):
 * PlusManager contains other void-1-param methods with INVOKE_INTERFACE:
 *   P6(String)V   : interface=1, iput=0  — license-key activate method
 *   y4(D0/a$d)V   : interface=1, iput=0  — settings listener method
 * Using INVOKE_INTERFACE alone matched P6(String)V, injecting G0.i into a
 * String register → VerifyError crash at startup.
 *
 * Fix: prefix with IPUT_OBJECT — the cache write-back that only Z6/V6 perform
 * before the interface call. Verified exhaustively: no other void-1-param method
 * in PlusManager has both IPUT_OBJECT and INVOKE_INTERFACE.
 *
 * Smali pattern (both versions):
 *   iput-object p1, p0, LXX/b;->l:LYY/i;           ← cache write ← anchor
 *   invoke-virtual {p0, p1}, LXX/b;->X6(LYY/i;)V   ← persist to storage
 *   iget-object v1, p0, LXX/b;->d:LYY/g;
 *   invoke-interface {v1, p1}, LYY/g;->b(LYY/i;)V  ← notify observers ← anchor
 */
internal val SetPlusStateFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "V",
    filters = listOf(
        opcode(Opcode.IPUT_OBJECT),
        opcode(Opcode.INVOKE_INTERFACE),
    ),
    custom = { method, _ -> method.parameterTypes.size == 1 },
)

/**
 * PlusManager.fetchAndUpdatePlusState(cacheStrategy, retryStrategy) — license screen path.
 *
 * Verified method names:
 *   v4.14.0 phone : U6(LD0/a$a;LD0/a$e;)LG0/i
 *   v4.13.0 TV    : Q6(LF0/a$a;LF0/a$e;)LI0/i
 *
 * Dispatches a coroutine to fetch PlusState from the backend. The result feeds
 * the AboutLicenseViewModel → license screen StateFlow → UI.
 *
 * Uniquely identified by INSTANCE_OF on the first parameter (forceRefresh check)
 * followed by INVOKE_VIRTUAL (the dispatch). The only 2-param, object-returning
 * method in PlusManager with this shape.
 *
 * Smali pattern (both versions):
 *   instance-of v0, p1, LXX/a$a$b;                           ← forceRefresh check ← anchor
 *   invoke-virtual {p0, v0, p2}, LXX/b;->S6(ZLX/a$e;)LYY/i; ← dispatch ← anchor
 */
internal val StateFlowResolverFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    filters = listOf(
        opcode(Opcode.INSTANCE_OF),
        opcode(Opcode.INVOKE_VIRTUAL),
    ),
    custom = { method, _ -> method.parameterTypes.size == 2 },
)

/**
 * PlusManager.fetchPlusStateForPromo(cacheStrategy, retryStrategy) — promo dialog path.
 *
 * Verified method names:
 *   v4.14.0 phone : D5(LD0/a$a;LD0/a$e;)LG0/i
 *   v4.13.0 TV    : L2(LF0/a$a;LF0/a$e;)LI0/i
 *
 * Dispatches a coroutine for the promo/check-license dialog. Result drives
 * needShowCheckLicenseDialog via MutableLiveData.postValue(). When Free or
 * Unknown, AdGuard shows a "Check license" dialog that opens the purchase URL.
 *
 * Anchored on "cacheStrategy" then "retryStrategy" — Kotlin Intrinsics param-name
 * null-check strings at the top of the method. classFingerprint scopes to the
 * PlusManager implementation class, excluding the interface (D0/a / F0/a) which
 * has the same strings only in annotation metadata.
 *
 * Smali pattern (both versions):
 *   const-string v0, "cacheStrategy"                                     ← anchor
 *   invoke-static {p1, v0}, kotlin/jvm/internal/p;->g(Object;String;)V
 *   const-string v0, "retryStrategy"                                     ← anchor
 *   invoke-static {p2, v0}, kotlin/jvm/internal/p;->g(Object;String;)V
 */
internal val PromoStateFlowResolverFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "L",
    filters = listOf(
        string("cacheStrategy"),
        string("retryStrategy"),
    ),
)

/**
 * PlusState.PaidLicense constructor.
 *
 * Verified class / signature:
 *   v4.14.0 phone : G0/i$n  (String, G0/i$m, G0/i$l, I, I, String)
 *   v4.13.0 TV    : I0/i$n  (String, I0/i$m, I0/i$l, I, I, String)
 */
internal val PaidLicenseFingerprint = Fingerprint(
    classFingerprint = PaidLicenseClassFingerprint,
    name = "<init>",
)

/**
 * LicenseDuration.Lifetime singleton — toString() method.
 *
 * Verified class names:
 *   v4.14.0 phone : G0/i$l$a
 *   v4.13.0 TV    : I0/i$l$a
 *
 * Used at patch time to resolve the singleton static field via
 * classDef.staticFields.first(). Anchored on string("Lifetime") in the
 * filter rather than name="toString" alone to prevent accidental matches
 * in unrelated classes if class resolution ever broadens.
 */
internal val LifetimeDurationFingerprint = Fingerprint(
    name = "toString",
    filters = listOf(
        string("Lifetime"),
    ),
)

// ─────────────────────────────────────────────────────────────────────────────
// TV-specific fingerprints
// ─────────────────────────────────────────────────────────────────────────────

/**
 * PlusManager.activateLicenseKey(String) — TV-only license key activation path.
 *
 * Verified method name:
 *   v4.13.0 TV : L6(Ljava/lang/String;)V
 *
 * Called from the TV license screen (TvAboutLicenseViewModel via synthetic
 * accessor I1). When the license screen opens on TV, it reads any stored
 * license key from storage and calls this method to re-verify it with the
 * backend (via LI0/e interface). Since there is no real key in a patched
 * build, this call fails and the UI falls back to "License activated" text
 * instead of displaying the "Lifetime" duration label from our fake PaidLicense.
 *
 * Patching → returnEarly() prevents the re-verification entirely. Combined with
 * getCachedPlusState() returning our PaidLicense, the license screen reads
 * the correct Lifetime state and displays it properly.
 *
 * Note: L6 already no-ops when called with a null param (if-eqz p1 → return-void).
 * We returnEarly() unconditionally to also cover the case where a stale non-null
 * key string is stored from a previous real activation attempt.
 *
 * NOT present in the phone version (phone uses P6(String)V for the same purpose,
 * but the phone license screen does not call it on open — only on explicit user
 * action). Scoped to TV patch only via ADGUARD_TV_COMPATIBILITY.
 *
 * Smali (F0/b.smali, L6(Ljava/lang/String;)V):
 *   if-eqz p1, :cond_c
 *   iget-object v0, p0, LF0/b;->c:LI0/e;
 *   invoke-interface {v0, p1}, LI0/e;->l(Ljava/lang/String;)LI0/d;  ← backend call
 *   iget-object p1, p0, LF0/b;->k:Lc3/a;
 *   invoke-virtual {p1}, Lc3/a;->g()V
 *   :cond_c
 *   return-void
 *
 * Anchored on the PlusManager class + method name "L6" (stable within TV v4.13.0).
 * As an additional guard, the INVOKE_INTERFACE filter confirms the backend
 * activation call is present — preventing a match on other short void-String methods.
 */
internal val LicenseKeyActivateFingerprint = Fingerprint(
    classFingerprint = PlusManagerClassFingerprint,
    returnType = "V",
    filters = listOf(
        opcode(Opcode.INVOKE_INTERFACE),
    ),
    custom = { method, _ ->
        method.parameterTypes.size == 1 &&
            method.parameterTypes[0] == "Ljava/lang/String;"
    },
)
