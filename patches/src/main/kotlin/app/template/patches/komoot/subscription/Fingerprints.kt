package app.template.patches.komoot.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Architecture overview (verified against 2026.31.3) ──────────────────────
//
// Premium user check:
//   UserV7.k0()Z
//     Reads field `e:Boolean`, compares to Boolean.TRUE via Kotlin equals.
//
// Server-config premium flag:
//   AppConfigV3.n()Boolean
//     Returns field `a:Boolean` (@Json("premium") — first field in the class).
//
// Region / map-pack ownership:
//
//   c2t.g()Z  [ownsWorldPack source — was bqr.g() in 2026.29.0]
//     Observed by gwk$h$a.b() which calls gwk.p0(gwk,Z) to set gwk.t=true.
//     When gwk.t=true, gwk.H0(Region) marks every region isOwned=true at
//     construction, so all subsequent vqo objects have n()=true naturally.
//
//   vqo.n()Z  [OwnedRegion.isOwned per-object getter — was cin.n() in 2026.29.0]
//     Field k:Z. Covers vqo objects already built before gwk.t is set true.
//     Located via OwnedRegionIsOwnedFingerprint.matchAll()[3]:
//       vqo has exactly 4 PUBLIC FINAL Z-returning no-param methods (f,l,m,n).
//       R8 sorts methods alphabetically → declaration order in smali is f,l,m,n.
//       matchAll() with classFingerprint scoped to vqo returns them in that order.
//       Index [3] = n()Z reliably without using the obfuscated method name.
//     OwnedRegionClassFingerprint identifies vqo by its unique constructor:
//       (String, String, D, Z, <obf>, String, Z, Z, <obf>, Geometry, Z, Long)
//       — the only constructor in the entire APK containing both Geometry
//         (org.maplibre) and Long (java.lang) in that position.
//
// ── DexGuard note ────────────────────────────────────────────────────────────
//
// APKiD reports DexGuard. All filters use opcode-level matching or references
// to non-obfuscated SDK classes only. No string() filters are used.

private const val USER_V7    = "Lde/komoot/android/services/api/model/UserV7;"
private const val APP_CONFIG = "Lde/komoot/android/services/api/model/AppConfigV3;"

// ── UserV7.k0()Z ─────────────────────────────────────────────────────────────

/**
 * Matches UserV7.k0()Z — primary premium check.
 * Stable: non-obfuscated definingClass; filter on java.lang.Boolean.TRUE.
 */
internal val UserIsPremiumFingerprint = Fingerprint(
    definingClass = USER_V7,
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            smali = "Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;",
            opcode = Opcode.SGET_OBJECT,
        ),
    ),
)

// ── AppConfigV3.n()Boolean ───────────────────────────────────────────────────

/**
 * Matches AppConfigV3.n()Boolean — server-config premium flag.
 * Stable: non-obfuscated definingClass; field `a` = first declared field = @Json("premium").
 */
internal val AppConfigPremiumFingerprint = Fingerprint(
    definingClass = APP_CONFIG,
    returnType = "Ljava/lang/Boolean;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            smali = "${APP_CONFIG}->a:Ljava/lang/Boolean;",
            opcode = Opcode.IGET_OBJECT,
        ),
    ),
)

// ── Region ownership ─────────────────────────────────────────────────────────

/**
 * Identifies the c2t class (ownsWorldPack data model) by its unique constructor.
 * Constructor: (Z, Map, List, <obf>, Map, Map, Set) — only one in the APK with this shape.
 * "L" placeholder for the one obfuscated parameter (cel).
 */
private val OwnsWorldPackClassFingerprint = Fingerprint(
    name = "<init>",
    parameters = listOf(
        "Z",
        "Ljava/util/Map;",
        "Ljava/util/List;",
        "L",
        "Ljava/util/Map;",
        "Ljava/util/Map;",
        "Ljava/util/Set;",
    ),
)

/**
 * Matches c2t.g()Z — ownsWorldPack getter.
 * When patched to return true, gwk.t is set true and gwk.H0() builds every
 * subsequent OwnedRegion with isOwned=true.
 */
internal val OwnsWorldPackFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    classFingerprint = OwnsWorldPackClassFingerprint,
    filters = listOf(
        opcode(Opcode.IGET_BOOLEAN),
        opcode(Opcode.RETURN),
    ),
)

/**
 * Identifies the vqo class (OwnedRegion) by its unique constructor signature.
 * The constructor takes (String, String, D, Z, <obf>, String, Z, Z, <obf>, Geometry, Z, Long)
 * — the only constructor in the APK containing both Geometry (non-obfuscated MapLibre
 * type) and Long (java.lang). "L" placeholder for the two obfuscated types.
 */
private val OwnedRegionClassFingerprint = Fingerprint(
    name = "<init>",
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "D",
        "Z",
        "L",
        "Ljava/lang/String;",
        "Z",
        "Z",
        "L",
        "Lorg/maplibre/geojson/Geometry;",
        "Z",
        "Ljava/lang/Long;",
    ),
)

/**
 * Matches all PUBLIC FINAL Z-returning no-param methods within the vqo class.
 *
 * vqo has exactly four such methods: f, l, m, n (R8 alphabetical sort order).
 * Patch code uses matchAll()[3] to select n()Z — the isOwned getter (reads field k:Z).
 * This avoids using the obfuscated method name "n" or the obfuscated class name "vqo".
 *
 * Stability: R8 always sorts methods alphabetically within a class, so the
 * 4th match (index 3) is deterministically n()Z across obfuscation reruns.
 */
internal val OwnedRegionIsOwnedFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    classFingerprint = OwnedRegionClassFingerprint,
    filters = listOf(
        opcode(Opcode.IGET_BOOLEAN),
        opcode(Opcode.RETURN),
    ),
)
