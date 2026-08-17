package app.template.patches.windy.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.WINDY_COMPATIBILITY

// ── Architecture ─────────────────────────────────────────────────────────────
//
// Windy (com.windyty.android) is a Capacitor web-hybrid app. All subscription
// logic lives entirely in a single minified JS bundle:
//
//   assets/public/v/<version>/mobile.js
//
// The bundle path uses a versioned directory (e.g. "51.0.1.mob.0f9e") that
// changes each release. The patch locates it dynamically by scanning
// assets/public/v/ for any subdirectory containing mobile.js — no hardcoded
// version path needed.
//
// ── Subscription model ────────────────────────────────────────────────────────
//
// Windy uses a custom Store object (P in v51, was N in v50) backed by
// SharedPreferences via a localStorage adapter. The store system has three
// levels:
//
//   1. In-memory cache (fr: Map)  — fastest, set by setFinally(), cleared on
//      P.set(key, null) via fr.delete(key).
//   2. Persistent storage         — SharedPreferences; read on cache miss.
//   3. Default value (def)        — used when storage.get() returns null.
//
//   P.get(key): fr.has(key) ? fr.get(key) : storage.get() ?? def
//
// Two functions control subscription state:
//
//   uu(subscriptionInfo):         "setTier" — called from the server response.
//     - If subscriptionInfo.status === 'active': sets subscription store to the
//       tier string (e.g. "premium"), calls lu(tier) to add body class
//       `subs-${tier}`.
//     - Otherwise: calls cu().
//
//   cu():                         "clearTier" — called when no active sub.
//     - Reads current subscription value e = P.get('subscription')
//     - Clears subscription and subscriptionInfo stores to null
//     - Removes body class: document.body.classList.remove(`subs-${e}`)
//     - Emits analytics: ru('subscription', 'tier/none')
//
// Three interconnected gates guard premium features:
//
//   subscription store:  def:null → P.get('subscription') === null means free.
//                        Any non-null string (e.g. "premium") means subscribed.
//
//   gr (boolean flag):   gr = !!P.get('subscription')
//                        Used in premiumOnly store getter shortcut:
//                          if (premiumOnly && !gr) return def
//                        Guards zoom levels, tile steps, overlay params, etc.
//
//   du() = hasAny():     du = () => P.get('subscription') !== null
//                        Used for UI: minifest API ?premium param, tile zoom
//                        caps, 1-hour step, premium-calendar body class,
//                        paywall component.
//
//   body.subs-{tier}:    CSS class on <body> used by HTML/CSS to hide "Go
//                        Premium" CTAs and show premium-only UI elements.
//                        Added by lu(tier), removed by cu().
//
// ── Root cause of popup ───────────────────────────────────────────────────────
//
// Without patches, on app launch (not logged in):
//   1. Account API call → responds with auth:false
//   2. km(data, true) else-branch → uu(null) → cu()
//   3. cu() clears subscription store to null → du()=false, gr=false
//   4. cu() calls classList.remove('subs-${e}') → body loses the premium class
//   5. The Go Premium button (hidden by CSS body.subs-premium) becomes visible
//   6. P.emit('rqstOpen','subscription') triggered by premium-only-wrapper
//
// ── Patch strategy ───────────────────────────────────────────────────────────
//
// Five same-length in-place byte replacements, all targeting v51.0.1:
//
//   P1 — subscription store default (99 bytes):
//     def:null → def:`premium`  — sets store default to the 'premium' tier.
//     This ensures P.get('subscription') always returns 'premium' on a cache
//     miss (i.e. after cu() nulls the fr cache entry).
//     subscriptionInfo.def:null → def:0 — falsy, so fu()/getIssue returns
//     null → no subscription warning popup.
//     Byte savings: e=>1 vs e=>!0, nativeSync:1 vs nativeSync:!0.
//
//   P2 — gr premium flag initialisation (63 bytes):
//     gr=!!P.get(`subscription`) → gr=!0
//     Forces gr=true at module load, before the account API call fires.
//     The N.once listener is preserved but dead-coded via !0||short-circuit.
//
//   P3 — du() hasAny gate (35 bytes):
//     du=()=>P.get(`subscription`)!==null → du=()=>!0||P.get(`subscription`)
//     Short-circuits to always return true. P.get call becomes dead code.
//
//   P4 — cu() subscription store clear (57 bytes):
//     Removes P.set(`subscription`,null),P.set(`subscriptionInfo`,null)
//     from cu(), replaced with void 0. Prevents cu() from:
//       (a) writing null to SharedPreferences
//       (b) evicting the fr cache entry (so P.get returns def='premium')
//
//   P5 — cu() body class direction (46 bytes):
//     classList.remove(`subs-${e}`) → classList.add(`subs-${e}`)
//     When cu() fires, e = P.get('subscription') = 'premium' (from P1 def,
//     since P4 keeps the fr entry intact). classList.add('subs-premium') runs
//     → Go Premium button stays hidden via CSS. Idempotent.
//
// ── Stability notes ──────────────────────────────────────────────────────────
//
// All patterns target Windy-internal identifiers that are stable across minor
// releases:
//   - Store declaration syntax (structural, version-stable)
//   - N.get/N.set/N.once store API (public Windy store interface)
//   - DOM classList API (always stable)
//
// Variable renames (N→P, pr→gr, Hl→du, Vl→uu, zl→cu) are detected by
// hunting the semantic patterns, not hardcoded identifiers.
//
// PatchException messages include the pattern label and a hint for diagnosis.
//

private data class JsPatch(val label: String, val original: String, val replacement: String) {
    init {
        val ob = original.toByteArray(Charsets.UTF_8)
        val rb = replacement.toByteArray(Charsets.UTF_8)
        require(ob.size == rb.size) {
            "JsPatch '$label' byte-length mismatch: original=${ob.size} replacement=${rb.size}. " +
                "Padding must keep lengths equal."
        }
    }
    val originalBytes: ByteArray get() = original.toByteArray(Charsets.UTF_8)
    val replacementBytes: ByteArray get() = replacement.toByteArray(Charsets.UTF_8)
}

private val PATCHES = listOf(

    // P1 — subscription store default (99 bytes)
    //
    // Original : subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:ir},
    // Replaced : subscription:{def:`premium`,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:ir},
    //
    // def:`premium` → P.get always returns 'premium' on cache miss (after P4
    //   neutralises the fr entry eviction). 'premium' matches the subs-premium
    //   CSS class that hides the Go Premium button.
    // allowed:e=>1 replaces e=>!0 (same truthy, saves 1 byte).
    // nativeSync:1 replaces nativeSync:!0 (same truthy, saves 1 byte).
    // subscriptionInfo.def:0 → fu()/getIssue receives falsy → returns null →
    //   no subscription-issue warning popup.
    // Note: `allowed:ir` (v51) vs `allowed:tr` (v50) — updated for v51.
    JsPatch(
        label        = "subscription store default",
        original     = "subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:ir},",
        replacement  = "subscription:{def:`premium`,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:ir},",
    ),

    // P2 — gr premium flag initialisation (63 bytes)
    //
    // Original : gr=!!P.get(`subscription`),gr||P.once(`subscription`,e=>gr=!!e)
    // Replaced : gr=!0,!0||P.once(`subscription`,e=>gr=!0)                      (trailing spaces)
    //
    // Forces gr=true immediately at module load before the account API fires.
    // The P.once listener is registered but never runs (short-circuit !0||).
    // Note: variable renamed pr→gr in v51. Pattern updated accordingly.
    JsPatch(
        label        = "gr premium flag init",
        original     = "gr=!!P.get(`subscription`),gr||P.once(`subscription`,e=>gr=!!e)",
        replacement  = "gr=!0,!0||P.once(`subscription`,e=>gr=!0)                      ",
    ),

    // P3 — du() hasAny gate (35 bytes)
    //
    // Original : du=()=>P.get(`subscription`)!==null
    // Replaced : du=()=>!0||P.get(`subscription`)    (trailing spaces)
    //
    // Short-circuits to always return true. P.get call is unreachable.
    // Note: function renamed Hl→du in v51. Pattern updated accordingly.
    JsPatch(
        label        = "du hasAny gate",
        original     = "du=()=>P.get(`subscription`)!==null",
        replacement  = "du=()=>!0||P.get(`subscription`)   ",
    ),

    // P4 — cu() subscription store clear (57 bytes)
    //
    // Original : P.set(`subscription`,null),P.set(`subscriptionInfo`,null)
    // Replaced : void 0                                                     (spaces to 57)
    //
    // Prevents cu() from writing null to SharedPreferences and evicting the
    // fr in-memory cache. The subscription store continues to return 'premium'
    // via def even after cu() runs. Keeps du() and gr true post-auth-fail.
    // Note: store object renamed N→P in v51. Pattern updated accordingly.
    JsPatch(
        label        = "cu subscription store clear",
        original     = "P.set(`subscription`,null),P.set(`subscriptionInfo`,null)",
        replacement  = "void 0                                                   ",
    ),

    // P5 — cu() body class direction (46 bytes)
    //
    // Original : e&&document.body.classList.remove(`subs-${e}`)
    // Replaced : e&&document.body.classList.add   (`subs-${e}`)  (3 spaces after add)
    //
    // Flips classList.remove → classList.add. When cu() fires:
    //   e = P.get('subscription') = 'premium' (from P1 def, fr intact via P4)
    //   classList.add('subs-premium') → Go Premium button stays hidden.
    // classList.add is idempotent — safe to call multiple times.
    JsPatch(
        label        = "cu body class direction",
        original     = "e&&document.body.classList.remove(`subs-\${e}`)",
        replacement  = "e&&document.body.classList.add   (`subs-\${e}`)",
    ),
)

@Suppress("unused")
val windyUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks Windy Pro features by patching the versioned JS bundle. " +
        "Sets subscription store default to 'premium' (P1), forces gr=true (P2) " +
        "and hasAny()=true (P3) at module load. Prevents cu() from clearing the " +
        "subscription store (P4) and flips its body class call from remove to add (P5), " +
        "ensuring the 'subs-premium' CSS class persists on <body> even when the server " +
        "reports no active subscription. Unlocks higher tile zoom, 1-hour forecast steps, " +
        "premium minifest API params, the premium calendar view, and all premium UI.",
    default = true,
) {
    compatibleWith(WINDY_COMPATIBILITY)

    execute {
        // ── Locate versioned JS bundle ────────────────────────────────────────
        // Scan assets/public/v/ for any subdirectory containing mobile.js.
        // The directory name is versioned (e.g. "51.0.1.mob.0f9e") and changes
        // each release — never hardcode it.
        val bundleFile = get("assets/public/v")
            .listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isDirectory }
            .map { it.resolve("mobile.js") }
            .firstOrNull { it.exists() }
            ?: throw PatchException(
                "Windy: mobile.js not found under assets/public/v/<version>/mobile.js. " +
                    "The versioned directory structure may have changed.",
            )

        val original = bundleFile.readBytes()
        if (original.isEmpty()) throw PatchException("Windy: mobile.js is empty.")
        val ba = original.copyOf()

        // ── Apply patches ─────────────────────────────────────────────────────
        for (patch in PATCHES) {
            val ob = patch.originalBytes
            val rb = patch.replacementBytes

            val idx = (0..ba.size - ob.size).firstOrNull { i ->
                ob.indices.all { j -> ba[i + j] == ob[j] }
            } ?: throw PatchException(
                "Windy: '${patch.label}' pattern not found in ${bundleFile.name}. " +
                    "The JS bundle may have changed — update the patch string.",
            )

            // Guard: detect double-apply
            if (rb.indices.all { j -> ba[idx + j] == rb[j] }) {
                throw PatchException(
                    "Windy: '${patch.label}' already patched at offset $idx. " +
                        "Patch was applied twice — check the patch pipeline.",
                )
            }

            rb.copyInto(ba, idx)
        }

        // ── Write result ──────────────────────────────────────────────────────
        bundleFile.writeBytes(ba)
    }
}
