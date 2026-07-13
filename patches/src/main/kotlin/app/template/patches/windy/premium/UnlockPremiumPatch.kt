package app.template.patches.windy.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.WINDY_COMPATIBILITY

// Architecture: Windy is a Capacitor web-hybrid. All subscription logic lives in
// assets/public/v/<version>/mobile.js (plain UTF-8 JS, not Hermes bytecode).
//
// Three patches in mobile.js:
//
// 1. Store default — subscription:{def:null,...}
//    N.get('subscription') returns def (null) when localStorage has no stored value.
//    The Svelte `$subscription` store in plugins reads this directly (not via Hl()),
//    so the rhpane/desktop premium icon checks `$subscription` and shows when null.
//    Fix: change def to `premium` so N.get('subscription') always returns 'premium'.
//    Also set subscriptionInfo.def to 0 (falsy, same effect as null) to reclaim bytes.
//    Side effect: pr=!!N.get('subscription') naturally becomes true, so patch 2 is
//    redundant but kept for clarity and defense-in-depth.
//
// 2. Premium state (`pr`) init:
//    pr=!!N.get(`subscription`) — will now be !!`premium` = true (from patch 1).
//    Patched additionally so pr=!0 is explicit and the N.once listener is neutralised.
//
// 3. hasAny() — Hl=()=>N.get(`subscription`)!==null
//    Used by premium-only-wrapper Svelte component (D()=hasAny()) to gate UI:
//      D() || emit('rqstOpen','subscription')   ← paywall trigger
//    Also gates minifest URL params, tile zoom levels, API step params.
//    With patch 1, N.get('subscription')='premium'≠null so this is naturally true.
//    Patched additionally for explicit defense-in-depth.
//
// All replacements are same-length byte swaps; no recompression needed.

@Suppress("unused")
val windyUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks Windy Pro features.",
    default = true,
) {
    compatibleWith(WINDY_COMPATIBILITY)

    execute {
        // Locate versioned JS bundle: assets/public/v/<version>/mobile.js
        val vDir = get("assets/public/v")
        val bundleFile = (vDir.listFiles() ?: emptyArray())
            .asSequence()
            .filter { it.isDirectory }
            .map { it.resolve("mobile.js") }
            .firstOrNull { it.exists() }
            ?: get("assets/public/v/50.1.1.mob.4aea/mobile.js").takeIf { it.exists() }
            ?: throw PatchException(
                "Windy: mobile.js not found under assets/public/v/<version>/mobile.js.",
            )

        val ba = bundleFile.readBytes().also {
            if (it.isEmpty()) throw PatchException("Windy: mobile.js is empty.")
        }.copyOf()

        var patched = 0

        fun patch(orig: String, repl: String, label: String) {
            val ob = orig.toByteArray(Charsets.UTF_8)
            val rb = repl.toByteArray(Charsets.UTF_8)
            check(ob.size == rb.size) { "Size mismatch for $label: ${ob.size} vs ${rb.size}" }
            val idx = run {
                outer@ for (i in 0..ba.size - ob.size) {
                    for (j in ob.indices) if (ba[i + j] != ob[j]) continue@outer
                    return@run i
                }
                -1
            }
            if (idx == -1) throw PatchException("Windy: '$label' pattern not found in ${bundleFile.name}.")
            rb.copyInto(ba, idx)
            patched++
        }

        // ── Patch 1: Store default (99 bytes) ────────────────────────────────
        // Original:    subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:tr},
        // Replacement: subscription:{def:`premium`,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:tr},
        // Effect: N.get('subscription') returns 'premium' instead of null.
        //         $subscription Svelte store = 'premium' → premium icon hidden.
        //         subscriptionInfo.def = 0 (falsy) — same effect as null for getIssue().
        patch(
            "subscription:{def:null,allowed:e=>!0,save:!0,nativeSync:!0},subscriptionInfo:{def:null,allowed:tr},",
            "subscription:{def:`premium`,allowed:e=>1,save:!0,nativeSync:1},subscriptionInfo:{def:0,allowed:tr},",
            "subscription store default",
        )

        // ── Patch 2: Premium state init (63 bytes) ───────────────────────────
        // Original:    pr=!!N.get(`subscription`),pr||N.once(`subscription`,e=>pr=!!e)
        // Replacement: pr=!0,!0||N.once(`subscription`,e=>pr=!0)                      
        // Effect: pr=true explicitly; N.once listener neutralised.
        //         (pr would already be true from patch 1, but kept for clarity.)
        patch(
            "pr=!!N.get(`subscription`),pr||N.once(`subscription`,e=>pr=!!e)",
            "pr=!0,!0||N.once(`subscription`,e=>pr=!0)                      ",
            "pr premium state init",
        )

        // ── Patch 3: hasAny() gate (35 bytes) ────────────────────────────────
        // Original:    Hl=()=>N.get(`subscription`)!==null
        // Replacement: Hl=()=>!0||N.get(`subscription`)   
        // Effect: Hl() always returns true → premium-only-wrapper allows all UI.
        //         (Would already be true from patch 1, but kept for clarity.)
        patch(
            "Hl=()=>N.get(`subscription`)!==null",
            "Hl=()=>!0||N.get(`subscription`)   ",
            "Hl hasAny subscription gate",
        )

        if (patched != 3) throw PatchException("Windy: expected 3 patches, applied $patched.")
        bundleFile.writeBytes(ba)
    }
}
