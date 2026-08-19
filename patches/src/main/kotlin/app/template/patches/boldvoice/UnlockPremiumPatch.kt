package app.template.patches.boldvoice

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.BOLDVOICE_COMPATIBILITY
import java.security.MessageDigest

/**
 * BoldVoice premium unlock — Hermes bytecode patch (HBC v96).
 *
 * String IDs (v4.6.1, LE-encoded in GetById/PutNewOwnById):
 *   isSubscriber    62792 → 48 F5
 *   isProSubscriber 62659 → C3 F4
 *   subStatus       64375 → 77 FB
 *   subscriptionData 64404 → 94 FB
 *   active          20656 → B0 50  (truthy subStatus value)
 *
 * HBC v96 opcodes:
 *   0x5C = Ret   0x73 = LoadConstString   0x76 = LoadConstUndefined (nop pad)
 *   0x78 = LoadConstTrue   0x79 = LoadConstFalse
 *
 * ENTITLEMENT ARCHITECTURE — why we patch multiple layers:
 *
 *   Layer 1 — Redux state (patches 1-10):
 *     isSubscriber / isProSubscriber / subStatus selectors and the state-update
 *     generator. These cover all standard subscription checks in the UI.
 *
 *   Layer 2 — Superwall SDK (patch 16):
 *     BoldVoice uses Superwall (a paywall SDK) for gating certain features like
 *     AI chat role-play ("Just Speak"). Superwall runs a PARALLEL entitlement
 *     check via useFeature('JUST_SPEAK').value.on — a remote flag from the
 *     Superwall backend, completely independent of Redux state.
 *     Even with isProSubscriber=true in Redux, Superwall still fires its own
 *     paywall if the feature is "on" server-side.
 *     Fix: flip JmpFalse → Jmp at presentSuperwall body+0x002a, so the fallback
 *     closure (which calls onLoad = "access granted") always fires immediately
 *     without going through Superwall.shared.register() → native paywall.
 *
 *   Layer 3 — upgradeToSuper flag (patches 11-12):
 *     A separate boolean computed by useUpgradeToSuper, used to show the
 *     "Upgrade to Super" inline upsell card in role-play lists.
 *     Independent of both Redux and Superwall.
 *   CORE — subscription state (9 sites):
 *     1. isSubscriber selector           → return true
 *     2. isProSubscriber selector        → return true
 *     3. subStatus selector              → return "active"
 *     4-6. ?anon_0_ GetById reads        → force true/true/"active" before dispatch
 *     7. navigation gate (usePaywallActions) → return-void
 *     8. hasActiveEntitlementOrSubscription  → return true
 *     9. hasActiveEntitlements               → return true
 *   DISPLAY — profile "undefined subscription" (1 site):
 *     10. ?anon_0_ subscriptionData read → force truthy (fixes undefined display)
 *   UPSELL — nag screens (3 sites):
 *     11. useUpgradeToSuper              → return false (kills the Upgrade to Super card gate)
 *     12. GenerativeFirstSaleInterstice  → return-void (AI chat / secondary upsell surface)
 *     13. showPaywallScreen action       → return-void (paywall dispatch)
 *     14. showPaywallScreen selector     → return false
 *   FORCE UPDATE — (1 site):
 *     15. ForceUpdateScreen              → return-void (28b window; blanks the screen)
 */
@Suppress("unused")
val boldVoiceUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks BoldVoice premium subscription and removes upsell nags.",
    default = true
) {
    compatibleWith(BOLDVOICE_COMPATIBILITY)

    execute {
        val bundlePath = "assets/index.android.bundle"
        val bundle = get(bundlePath)
        if (!bundle.exists()) throw PatchException("$bundlePath not found.")

        val bytes = bundle.readBytes()
        bytes.requireHermes(bundlePath)

        // ── 1-3. Selector closures ──────────────────────────────────────────
        // LoadParam r0,1 → GetByIdShort 'userState' → GetById '<field>' → Ret
        // Bytes [12..13] are the string_id LE — the only part that changes per update.

        bytes.replaceUnique(
            "isSubscriber selector",
            hex("6C 00 01 36 00 00 01 F7 37 00 00 02 48 F5 5C 00"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        bytes.replaceUnique(
            "isProSubscriber selector",
            hex("6C 00 01 36 00 00 01 F7 37 00 00 02 C3 F4 5C 00"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        bytes.replaceUnique(
            "subStatus selector",
            // returns "active"(20656=B0 50) instead of reading from state
            hex("6C 00 01 36 00 00 01 F7 37 00 00 02 77 FB 5C 00"),
            hex("73 00 B0 50 5C 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 4-6. State update reads in ?anon_0_ (fn#44643) ─────────────────
        // Replaces GetById reads (API response → register) with forced constants
        // before PutNewOwnById writes them into the dispatched state object.
        // All three are consecutive at body+0x16c / +0x172 / +0x178.

        bytes.replaceUnique(
            "state update isSubscriber read",
            hex("37 12 1F 0B 48 F5"),
            hex("78 12 76 00 76 00")
        )

        bytes.replaceUnique(
            "state update isProSubscriber read",
            hex("37 11 1F 0C C3 F4"),
            hex("78 11 76 00 76 00")
        )

        bytes.replaceUnique(
            "state update subStatus read",
            hex("37 10 1F 0D 77 FB"),
            hex("73 10 B0 50 76 00")
        )

        // ── 7. Navigation gate (usePaywallActions / fn#53261) ───────────────
        // First useCallback closure inside usePaywallActions. 24b for uniqueness.

        bytes.replaceUnique(
            "navigation gate",
            hex("29 01 00 2E 03 01 02 29 00 01 2E 04 00 00 2E 02 00 01 6E 00 0C 49 02 02"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 8-9. Named entitlement gates ────────────────────────────────────

        bytes.replaceUnique(
            "hasActiveEntitlementOrSubscription",
            hex("6C 01 01 37 00 01 01 35 5B 36 00 00 02 B3 7A 02"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        bytes.replaceUnique(
            "hasActiveEntitlements",
            hex("30 00 39 02 00 01 17 00 37 01 02 02 11 54 6C 00 01 53 03 01"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 10. Profile "undefined subscription" fix ────────────────────────
        // ?anon_0_ (fn#44643) reads subscriptionData from the API response object
        // at body+0x166 and stores it into the state dispatch. The profile card
        // shows "undefined subscription" when this is null/undefined.
        // Replace the GetById with LoadConstTrue so subscriptionData is truthy,
        // which makes hasSubscriptionData=true and the component shows real text.

        bytes.replaceUnique(
            "state update subscriptionData truthy",
            // GetById r19, r31, cacheIdx=10, 'subscriptionData'(64404=94 FB)
            hex("37 13 1F 0A 94 FB"),
            // LoadConstTrue r19 | nop | nop
            hex("78 13 76 00 76 00")
        )

        // ── 11-12. Upsell nag cards ─────────────────────────────────────────
        // useUpgradeToSuper (fn#62933) computes whether to show the "Upgrade to Super"
        // card. It is called inside NewReviewLobbyScreen which does:
        //   if (upgradeToSuper) jsx(UpsellCard, ...)
        // Making useUpgradeToSuper return false means upgradeToSuper=false everywhere
        // → the JmpFalse at NewReviewLobbyScreen body+0x5e9 always skips the card.
        // This is cleaner than patching the render closure itself (fn#69025)
        // since React throws on components that return undefined.

        bytes.replaceUnique(
            "useUpgradeToSuper return false",
            hex("32 00 6C 05 02 2A 00 00 05 29 04 00 2E 08 04 00"),
            hex("79 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // GenerativeUpsellInterstice (fn#62953): the "Upgrade to Super" inline card
        // rendered between items in the AI Chat > Role-Play and Topic lists.
        // Receives openUpsellModal prop from GenerativeAiScenarioListScreen.
        // Return-void makes the component render nothing.
        // Note: GenerativeFirstSaleInterstice (fn#62925) is a different, separate surface.

        bytes.replaceUnique(
            "GenerativeUpsellInterstice upsell card",
            hex("32 0C 6C 01 01 37 02 01 01 31 63 2A 0C 00 02 37"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 13-14. Paywall modal ────────────────────────────────────────────
        // showPaywallScreen fn#44443: dispatches the SET_SHOW_PAYWALL action.
        // No-op the action function → paywall modal never triggered.
        // showPaywallScreen selector: read from userState; return false so any
        // isVisible check in the modal container stays false.

        bytes.replaceUnique(
            "show paywall screen action",
            hex("32 00 6C 01 01 37 01 01 01 2F 85 2A 00 00 01 64"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        bytes.replaceUnique(
            "showPaywallScreen selector",
            hex("6C 00 01 36 00 00 01 F7 37 00 00 02 F1 E6 5C 00"),
            hex("79 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 15. Force update blocker ────────────────────────────────────────
        // ForceUpdateScreen (fn#77963): the "app is out of date" screen.
        // The server sets state.forceUpdate=true when it detects an old client.
        // No-op the component body → screen renders nothing even if navigated to.
        // 28-byte window required for uniqueness (16/20/24b all hit 3 times).

        bytes.replaceUnique(
            "ForceUpdateScreen no-op",
            hex("29 08 00 2E 00 08 03 36 00 00 01 7A 76 03 51 00 00 03 36 0A 00 02 46 2E 00 08 08 51"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        // ── 16. Superwall paywall bypass ────────────────────────────────────
        // presentSuperwall (fn#53510) is the JS bridge that calls
        // Superwall.shared.register(eventName, ...) on the native layer.
        // This triggers the paywall for gated features (AI chat / Just Speak).
        //
        // Body logic:
        //   +0x025: CreateClosure fallback (calls onLoad + onFallback)
        //   +0x002a: JmpFalse Addr8:18, r5 → if (!eventName) call fallback → return
        //   +0x008b–0x00a2: Superwall.shared.register(...) ← actual paywall trigger
        //
        // Fix: change JmpFalse (92 12 05) → Jmp (8E 12) + nop pad (76 00).
        // The unconditional jump makes fallback() always fire immediately,
        // which calls onLoad(undefined) → the "access granted" callback runs
        // → feature unlocks without the native Superwall paywall appearing.

        bytes.replaceUnique(
            "presentSuperwall bypass",
            // JmpFalse Addr8:18, r5 | GetEnvironment r2 | LoadFromEnvironment r3 ...
            hex("92 12 05 29 02 00 2E 03"),
            // Jmp Addr8:18 | nop | GetEnvironment r2 | LoadFromEnvironment r3 ...
            hex("8E 12 76 00 02 00 2E 03")
        )

        bundle.writeBytes(bytes.withUpdatedSha1())
    }
}

private val hermesMagic = byteArrayOf(0xC6.toByte(), 0x1F, 0xBC.toByte(), 0x03)

private fun ByteArray.requireHermes(path: String) {
    if (size < 12 || !copyOfRange(0, hermesMagic.size).contentEquals(hermesMagic)) {
        throw PatchException("Invalid Hermes bytecode bundle: $path")
    }
}

private fun ByteArray.withUpdatedSha1(): ByteArray {
    val content = dropLast(20).toByteArray()
    return content + MessageDigest.getInstance("SHA-1").digest(content)
}

private fun ByteArray.replaceUnique(name: String, pattern: ByteArray, replacement: ByteArray) {
    if (pattern.size != replacement.size) {
        throw PatchException("BoldVoice $name replacement size mismatch.")
    }
    val offset = indexOf(pattern)
    if (offset < 0) throw PatchException("BoldVoice $name pattern not found.")
    if (indexOf(pattern, offset + 1) >= 0) throw PatchException("BoldVoice $name pattern is not unique.")
    replacement.copyInto(this, offset)
}

private fun ByteArray.indexOf(pattern: ByteArray, start: Int = 0): Int {
    if (pattern.isEmpty() || pattern.size > size) return -1
    val last = size - pattern.size
    outer@ for (offset in start..last) {
        for (index in pattern.indices) {
            if (this[offset + index] != pattern[index]) continue@outer
        }
        return offset
    }
    return -1
}

private fun hex(value: String): ByteArray =
    value.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        .map { it.toInt(16).toByte() }
        .toByteArray()
