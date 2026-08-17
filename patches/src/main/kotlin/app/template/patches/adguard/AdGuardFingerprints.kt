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
 * Anchored on the retry-log string — a developer-written semantic message tied
 * to the retry logic inside PlusManager. Decouples all child fingerprints from
 * the obfuscated class name, which changes every release.
 *
 * Verified stable across:
 *   v4.14.0 phone : D0/b
 *   v4.13.1 phone : F0/b   ← NEW
 *   v4.13.0 TV    : F0/b
 */
private val PlusManagerClassFingerprint = Fingerprint(
    strings = listOf("Failed to get state from backend. Remaining retry count: "),
)

/**
 * PlusState.PaidLicense class anchor.
 *
 * Anchored on the Kotlin data-class compiler-generated toString prefix.
 * This string is emitted by kotlinc unconditionally and survives R8 shaking.
 *
 * Verified stable across:
 *   v4.14.0 phone : G0/i$n
 *   v4.13.1 phone : I0/i$n   ← NEW
 *   v4.13.0 TV    : I0/i$n
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
 * Reads the in-memory cached PlusState field (IGET_OBJECT). On cache miss,
 * fetches from storage, then writes the result back (IPUT_OBJECT). No params,
 * returns a PlusState object. Verified unique: only this method in PlusManager
 * has no params, returns L, and contains both IGET_OBJECT and IPUT_OBJECT.
 *
 * Verified method names across versions:
 *   v4.14.0 phone : T6()LG0/i
 *   v4.13.1 phone : P6()LI0/i   ← NEW (same smali shape)
 *   v4.13.0 TV    : P6()LI0/i
 *
 * Smali pattern (all versions):
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
 * Writes the new PlusState into the in-memory cache field (IPUT_OBJECT),
 * persists it to storage, then notifies all registered observers via an
 * interface call (INVOKE_INTERFACE). One PlusState param, void return.
 *
 * ⚠️ False-positive hazard: PlusManager has other void-1-param methods with
 * INVOKE_INTERFACE (e.g. the activate-key method). Using INVOKE_INTERFACE alone
 * can match them and cause a VerifyError crash. IPUT_OBJECT prefix is the
 * discriminator — only the set-state method performs a cache write-back before
 * the interface notify. Verified exhaustively across all three target versions.
 *
 * Verified method names across versions:
 *   v4.14.0 phone : Z6(LG0/i)V
 *   v4.13.1 phone : V6(LI0/i)V   ← NEW (same smali shape)
 *   v4.13.0 TV    : V6(LI0/i)V
 *
 * Smali pattern (all versions):
 *   iput-object p1, p0, LXX/b;->l:LYY/i;          ← cache write ← anchor
 *   invoke-virtual {p0, p1}, LXX/b;->persist(...)  ← storage persist
 *   iget-object v1, p0, LXX/b;->d:LYY/g;
 *   invoke-interface {v1, p1}, LYY/g;->b(...)      ← notify observers ← anchor
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
 * Dispatches a coroutine to fetch PlusState from the backend. Drives the
 * AboutLicenseViewModel → license screen StateFlow → UI. Uniquely identified by
 * INSTANCE_OF on the first parameter followed by INVOKE_VIRTUAL. The only
 * 2-param, object-returning method in PlusManager with this shape.
 *
 * Verified method names across versions:
 *   v4.14.0 phone : U6(LD0/a$a;LD0/a$e;)LG0/i
 *   v4.13.1 phone : Q6(LF0/a$a;LF0/a$e;)LI0/i   ← NEW (same smali shape)
 *   v4.13.0 TV    : Q6(LF0/a$a;LF0/a$e;)LI0/i
 *
 * Smali pattern (all versions):
 *   instance-of v0, p1, LXX/a$a$b;                            ← forceRefresh check ← anchor
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
 * Dispatches a coroutine for the promo/check-license dialog. Free/Unknown result
 * triggers a "Check license" dialog opening the purchase URL in Chrome. Anchored
 * on Kotlin Intrinsics param-name null-check strings at the method top.
 *
 * classFingerprint scopes to PlusManager implementation (excludes the F0/a
 * interface which has the same strings only in annotation metadata).
 *
 * Verified method names across versions:
 *   v4.14.0 phone : D5(LD0/a$a;LD0/a$e;)LG0/i
 *   v4.13.1 phone : K2(LF0/a$a;LF0/a$e;)LI0/i   ← NEW
 *   v4.13.0 TV    : L2(LF0/a$a;LF0/a$e;)LI0/i
 *
 * Smali pattern (all versions):
 *   const-string v0, "cacheStrategy"                                    ← anchor
 *   invoke-static {p1, v0}, Lkotlin/jvm/internal/p;->g(...)V
 *   const-string v0, "retryStrategy"                                    ← anchor
 *   invoke-static {p2, v0}, Lkotlin/jvm/internal/p;->g(...)V
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
 *   v4.13.1 phone : I0/i$n  (String, I0/i$m, I0/i$l, I, I, String)   ← NEW
 *   v4.13.0 TV    : I0/i$n  (String, I0/i$m, I0/i$l, I, I, String)
 */
internal val PaidLicenseFingerprint = Fingerprint(
    classFingerprint = PaidLicenseClassFingerprint,
    name = "<init>",
)

/**
 * LicenseDuration.Lifetime singleton — toString() method.
 *
 * Used at patch time to resolve the singleton static field via
 * classDef.staticFields.first(). Anchored on string("Lifetime").
 *
 * Verified class names:
 *   v4.14.0 phone : G0/i$l$a
 *   v4.13.1 phone : I0/i$l$a   ← NEW
 *   v4.13.0 TV    : I0/i$l$a
 */
internal val LifetimeDurationFingerprint = Fingerprint(
    name = "toString",
    filters = listOf(
        string("Lifetime"),
    ),
)

/**
 * PlusManager.activateLicenseKey(String) — backend re-verification path.
 *
 * Called on license screen open to re-verify any stored key with the backend.
 * Without a real key this fails and the UI falls back to "License activated"
 * text instead of displaying the "Lifetime" duration label from our synthetic
 * PaidLicense. returnEarly() prevents the backend call entirely.
 *
 * Previously documented as TV-only (L6 in TV v4.13.0). Verified that L6 is
 * also present in Android v4.13.1 with an identical smali shape — making this
 * fingerprint applicable to both builds. Applied via methodOrNull in the unified
 * patch so it gracefully no-ops on versions where it doesn't match.
 *
 * Verified method names:
 *   v4.13.1 phone : L6(Ljava/lang/String;)V   ← present; same shape
 *   v4.13.0 TV    : L6(Ljava/lang/String;)V
 *   v4.14.0 phone : (verify before applying)
 *
 * Smali pattern (all verified versions):
 *   if-eqz p1, :cond_c
 *   iget-object v0, p0, LXX/b;->c:LYY/e;
 *   invoke-interface {v0, p1}, LYY/e;->l(Ljava/lang/String;)LYY/d;  ← backend call ← anchor
 *   ...
 *   :cond_c
 *   return-void
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
