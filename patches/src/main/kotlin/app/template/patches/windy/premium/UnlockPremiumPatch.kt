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
// The bundle path uses a versioned directory (e.g. "50.1.1.mob.4aea") that
// changes each release. The patch locates it dynamically by scanning
// assets/public/v/ for any subdirectory containing mobile.js — no hardcoded
// version path needed.
//
// ── Subscription model ────────────────────────────────────────────────────────
//
// Windy uses a custom Store class (N) backed by SharedPreferences via a
// localStorage adapter (M). The store system has three levels:
//
//   1. In-memory cache (fr: Map)  — fastest, set by setFinally(), cleared on
//      N.set(key, null) via fr.delete(key).
//   2. Persistent storage (M)     — SharedPreferences; read on cache miss.
//   3. Default value (def)        — used when M.get() returns null.
//
//   N.get(key): fr.has(key) ? fr.get(key) : M.get() ?? def
//
// Two functions control subscription state:
//
//   Vl(subscriptionInfo):         "setTier" — called from the server response handler.
//     - If subscriptionInfo.status === 'active': sets subscription store to the tier
//       string (e.g. "premium"), sets subscriptionInfo store, adds body class
//       `subs-${tier}` via Bl(tier).
//     - Otherwise or if null: calls zl().
//
//   zl():                         "clearTier" — called when server says no active sub.
//     - Reads current subscription value e = N.get('subscription')
//     - Sets subscription → null (clears fr cache via fr.delete, writes null to M)
//     - Sets subscriptionInfo → null
//     - Removes body class: document.body.classList.remove(`subs-${e}`)
//     - Emits G('subscription', 'tier/none') for analytics
//
// Three interconnected gates guard premium features:
//
//   subscription store:  def:null → N.get('subscription') == null means no subscription.
//                        Any non-null string (e.g. "premium") means subscribed.
//
//   pr (boolean flag):   pr = !!N.get('subscription')
//                        Used in premiumOnly store getter shortcut:
//                          if (premiumOnly && !pr) return def
//                        Guards zoom levels, tile steps, overlay params, etc.
//
//   Hl() = hasAny():     Hl = () => N.get('subscription') !== null
//                        Used for UI: minifest API ?premium param, tile zoom caps,
//                        1-hour step, premium-calendar body class, paywall component.
//
//   body.subs-{tier}:    CSS class on <body> used by HTML/CSS to hide "Go Premium"
//                        CTAs and show premium-only UI elements. Added by Bl(tier),
//                        removed by zl(). Without this class the Go Premium button
//                        appears even when Hl() returns true.
//
// ── Root cause of popup ───────────────────────────────────────────────────────
//
// Without patches, on app launch when the user is NOT logged in:
//   1. Am() fires → GET account.windy.com/api/info → responds with auth:false
//   2. km(data, true) else-branch → Vl(null) → zl()
//   3. zl() clears subscription store to null → Hl()=false, pr=false
//   4. zl() calls classList.remove('subs-${e}') → body loses the premium class
//   5. The Go Premium button (hidden by CSS body.subs-premium) becomes visible
//   6. P.emit('rqstOpen','subscription') is triggered by premium-only-wrapper
//
// With P1–P3 only (previous approach):
//   - Subscription def is 'premium', Hl() and pr initially true
//   - But zl() still runs when Am() returns auth:false → clears the store
//   - setFinally on null: fr.delete('subscription') → next N.get returns def='premium' ✓
//   - BUT: zl() ALSO removes body.subs-premium → the Go Premium button reappears
//   - setFinally emits ('subscription', def='premium') → reactive listeners fire
//     but NONE of them call Bl() to re-add the body class
//   - Result: premium features work (store returns 'premium') but the UI button shows
//
// ── Patch strategy ───────────────────────────────────────────────────────────
//
// Five same-length in-place byte replacements:
//
//   P1 — subscription store default (99 bytes):
//     def:null → def:`annual`   — annual plan tier string; makes N.get always
//     return 'annual'  when no value is cached (after fr.delete or on first read).
//     allowed:e=>!0 → e=>1 and nativeSync:!0→1 to reclaim exact bytes.
//     subscriptionInfo.def:null → def:0 (falsy — Ul()/getIssue returns null,
//     so no subscription warning popup from Kl()/checkAndRenderSubsIssue).
//
//   P2 — pr premium flag init (63 bytes):
//     pr=!!N.get('subscription') → pr=!0  — forces pr=true immediately at module
//     load, before Am() fires. Trailing spaces pad to 63 bytes. Defense-in-depth
//     against premiumOnly store defaults returning free-tier values.
//
//   P3 — Hl() hasAny gate (35 bytes):
//     Hl=()=>N.get('subscription')!==null → Hl=()=>!0||...
//     Short-circuits the N.get call. Defense-in-depth. Trailing spaces pad to 35.
//
//   P4 — zl() subscription/subscriptionInfo clear (57 bytes):
//     Removes N.set('subscription',null) and N.set('subscriptionInfo',null) from zl().
//     Replaced with void 0 (padded to 57). This prevents zl() from:
//       (a) writing null to SharedPreferences (M.put)
//       (b) triggering setFinally's emit with def value
//     subscription remains 'premium' in fr cache; Hl() and pr stay true
//     even when the server responds with auth:false.
//
//   P5 — zl() body class flip (46 bytes):
//     classList.remove(`subs-${e}`) → classList.add   (`subs-${e}`)
//     When zl() fires, instead of removing the body class it ADDS it.
//     With P4, e = N.get('subscription') = 'premium' (still in fr cache),
//     so classList.add('subs-premium') runs → Go Premium button stays hidden.
//     Harmless if zl() is called multiple times: classList.add is idempotent.
//
// ── Stability notes ──────────────────────────────────────────────────────────
//
// All patterns target:
//   - Store declaration syntax (structural, version-stable)
//   - Variable names (pr, Hl, Bl, Vl, zl) that are stable Windy-internal
//     identifiers referenced in crash reports and non-obfuscated plugin APIs
//   - The N.get/N.set/N.once store API (public Windy store interface)
//   - DOM classList API (always stable)
//
// PatchException messages include the pattern label and action hint for
// easy diagnosis when the bundle changes between Windy releases.
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
    // Original : subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:tr},
    // Replaced : subscription:{def:`annual` ,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:tr},
    // def:`annual`  → N.get always returns 'annual'  on cache miss (after P4 neutralises clear).
    // e=>1 replaces e=>!0 (same truthy); nativeSync:1 saves the byte versus nativeSync:!0.
    // subscriptionInfo.def:0 → Ul()/getIssue sees falsy → returns null → no warning popup.
    JsPatch(
        label        = "subscription store default",
        original     = "subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:tr},",
        replacement  = "subscription:{def:`annual` ,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:tr},",
    ),

    // P2 — pr premium flag initialisation (63 bytes)
    // Original : pr=!!N.get(`subscription`),pr||N.once(`subscription`,e=>pr=!!e)
    // Replaced : pr=!0,!0||N.once(`subscription`,e=>pr=!0)                      (trailing spaces)
    // Forces pr=true immediately at module load before Am() fires.
    // The N.once listener is registered but dead-code (short-circuit !0||).
    JsPatch(
        label        = "pr premium flag init",
        original     = "pr=!!N.get(`subscription`),pr||N.once(`subscription`,e=>pr=!!e)",
        replacement  = "pr=!0,!0||N.once(`subscription`,e=>pr=!0)                      ",
    ),

    // P3 — Hl() hasAny gate (35 bytes)
    // Original : Hl=()=>N.get(`subscription`)!==null
    // Replaced : Hl=()=>!0||N.get(`subscription`)    (trailing spaces)
    // Short-circuits to always return true. N.get call is dead code.
    JsPatch(
        label        = "Hl hasAny gate",
        original     = "Hl=()=>N.get(`subscription`)!==null",
        replacement  = "Hl=()=>!0||N.get(`subscription`)   ",
    ),

    // P4 — zl() subscription store clear (57 bytes)
    // Original : N.set(`subscription`,null),N.set(`subscriptionInfo`,null)
    // Replaced : void 0                                                     (spaces to 57)
    // Prevents zl() from writing null to M (SharedPrefs) and clearing the fr cache.
    // subscription stays 'premium' in fr; Hl() and pr remain true after Vl(null).
    JsPatch(
        label        = "zl subscription store clear",
        original     = "N.set(`subscription`,null),N.set(`subscriptionInfo`,null)",
        replacement  = "void 0                                                   ",
    ),

    // P5 — zl() body class direction (46 bytes)
    // Original : e&&document.body.classList.remove(`subs-${e}`)
    // Replaced : e&&document.body.classList.add   (`subs-${e}`)  (3 spaces after add)
    // Flips classList.remove to classList.add. When zl() fires, e = N.get('subscription')
    // = 'premium' (still in fr cache thanks to P4), so classList.add('subs-premium') runs.
    // This keeps the Go Premium button hidden via CSS. classList.add is idempotent.
    JsPatch(
        label        = "zl body class direction",
        original     = "e&&document.body.classList.remove(`subs-\${e}`)",
        replacement  = "e&&document.body.classList.add   (`subs-\${e}`)",
    ),
)

@Suppress("unused")
val windyUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks Windy Pro features by patching the JS bundle. " +
        "Sets subscription store default to 'annual'  (P1), forces pr=true (P2) " +
        "and hasAny()=true (P3) at module load. Prevents zl() from clearing the " +
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
        // The directory name is versioned (e.g. "50.1.1.mob.4aea") and changes
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
