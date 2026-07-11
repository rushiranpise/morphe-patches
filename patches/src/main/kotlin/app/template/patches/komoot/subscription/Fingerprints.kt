package app.template.patches.komoot.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Komoot (de.komoot.android) v2026.28.2
//
// Premium architecture:
//   UserV7.k0()Z  — reads field `e` (Boolean) and compares to Boolean.TRUE;
//                   called throughout the codebase to check if the logged-in user
//                   has a premium subscription (used for tour data, map overlays, etc.)
//   AppConfigV3.n()Ljava/lang/Boolean; — returns field `a` annotated @Json("premium");
//                   used by ho0$d to set up the premium user context and by analytics.
//
// Patch strategy:
//   UserV7.k0()   → always return true  (primary premium check across UI & data layers)
//   AppConfigV3.n() → always return Boolean.TRUE  (secondary server-config premium flag)

/**
 * Matches UserV7.k0()Z — compares field `e` (Boolean) to Boolean.TRUE via Kotlin equals.
 * This is the primary premium check called by tour/map/feature gating code.
 */
internal val UserIsPremiumFingerprint = Fingerprint(
    definingClass = "Lde/komoot/android/services/api/model/UserV7;",
    name = "k0",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),   // iget-object p0, p0, UserV7->e (Boolean field)
        opcode(Opcode.SGET_OBJECT),   // sget-object v0, Boolean->TRUE
        opcode(Opcode.INVOKE_STATIC), // Lasg;->e(...) — Kotlin equals comparison
    ),
)

// ── Map packs ─────────────────────────────────────────────────────────────────
//
// Komoot sells individual regions ("map packs") and world packs as IAP.
// Ownership flows through two entry points:
//
//   gpj.H0(Region)Lcin  — builds an OwnedRegion (cin) for a given Region.
//                          Reads gpj.t (ownsWorldPackSnapshot:Z) and gpj.u
//                          (ownedRegionIdsSnapshot:Set) to decide isOwned.
//                          When ownsWorldPackSnapshot==true the branch skips the
//                          Set lookup and sets isOwned=true unconditionally.
//
//   cin.n()Z            — OwnedRegion.isOwned getter (reads field k:Z).
//                          Called by gpj.s1() to route to the owned flow vs
//                          the upsell/paywall flow.
//
// Patch strategy:
//   cin.n()    → always return true   (every OwnedRegion reports it is owned)
//   gpj.H0()   → return early with ownsWorldPack branch taken (const/4 v14, 0x1)
//                by injecting before the cin constructor call

/**
 * Matches cin.n()Z — OwnedRegion.isOwned getter (iget-boolean field k, return).
 * Called by gpj.s1() to choose the owned vs upsell code path.
 */
internal val OwnedRegionIsOwnedFingerprint = Fingerprint(
    definingClass = "Lcin;",
    name = "n",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_BOOLEAN), // iget-boolean p0, p0, Lcin;->k:Z
        opcode(Opcode.RETURN),
    ),
)

/**
 * Matches bqr.g()Z — RegionsData.ownsWorldPack getter (reads field a:Z).
 * Called by gpj$h$a.b() which feeds the result into gpj.p0() to set gpj.t
 * (ownsWorldPackSnapshot). Patching this to return true means the snapshot is
 * always set to true before H0 even runs, covering all code paths.
 */
internal val RegionsDataOwnsWorldPackFingerprint = Fingerprint(
    definingClass = "Lbqr;",
    name = "g",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_BOOLEAN), // iget-boolean p0, p0, Lbqr;->a:Z
        opcode(Opcode.RETURN),
    ),
)

/**
 * Matches AppConfigV3.n()Ljava/lang/Boolean; — returns field `a` annotated @Json("premium").
 * Used by ho0$d to configure the premium user context during app-config processing.
 */
internal val AppConfigPremiumFingerprint = Fingerprint(
    definingClass = "Lde/komoot/android/services/api/model/AppConfigV3;",
    name = "n",
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),    // iget-object p0, p0, AppConfigV3->a
        opcode(Opcode.RETURN_OBJECT),
    ),
)
