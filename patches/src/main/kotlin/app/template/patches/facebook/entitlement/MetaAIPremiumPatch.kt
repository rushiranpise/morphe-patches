package app.template.patches.facebook.entitlement

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FACEBOOK_COMPATIBILITY
import app.template.patches.shared.returnEarly

// Unlocks the full Facebook Plus benefit suite and Meta AI premium features.
//
// ─── Benefit system (reverse-engineered from MetaPlusXposed) ─────────────────
//
// Facebook's premium features are gated by a server-driven benefit entitlement
// system. The server pushes a Set<String> of active benefit keys into LX/84K.A01
// on login. UI components check Set.contains("CUSTOM_APP_ICON") etc. via either:
//
//   • LX/9Re.A03(String)Z   — the named dispatcher (normalises aliases, delegates to 84K)
//   • LX/84K.A08(String)Z   — the raw Set.contains check (called directly by some UI)
//
// A CGN listener (LX/U0t) is registered to receive server benefit-set updates and
// can RE-LOCK features when the server pushes an empty or reduced set.
//
// Patch chain:
//   1. BenefitCheckerFingerprint  → returnEarly(true)  — 9Re.A03 always returns true
//   2. BenefitSetCheckFingerprint → returnEarly(true)  — 84K.A08 always returns true
//   3. BenefitRelockListenerFingerprint → returnEarly() — U0t.Cqz never re-locks
//
// Facebook Plus benefit keys unlocked (from LX/AZX enum, 569.0.0.42.72):
//   CUSTOM_APP_ICON, CUSTOM_APP_THEME, CUSTOM_PROFILE_BIO_FONT, STORY_EXTEND,
//   STORY_PREVIEW, STORY_SUPERLIKES, STORY_REWATCH, SEARCH_STORY_VIEWERS,
//   BIZ_LINKS_IN_REELS, ENHANCED_CONTENT_SCHEDULING, ENHANCED_CONTENT_PROTECTION,
//   AI_CREDITS, AI_VOICE, IMAGE_GENERATION, IMAGINE_VIDEO, ADVANCED_REASONING,
//   FB_LINKS_IN_POSTS, STORY_FONTS, STORY_SPOTLIGHT, RANKING_BOOST, STARS,
//   PREMIUM_INSIGHTS, PREMIUM_VIDEO, EXCLUSIVE_STICKERS, BRANDED_THREADS,
//   NEXT_GEN_FB_BENEFIT, MV_FEED_AND_REELS, and 100+ more.
//
// ─── Meta AI premium gates (LX/2kD, classes13) ───────────────────────────────
//
// Three MobileConfig aggregator methods are forced to return true:
//   MetaOnePremiumRowFingerprint  → ThreadSettingsMetaOnePremiumRow.A00
//   MetaAIAvailableFingerprint    → 2kD.A05 (MetaAI availability aggregator)
//   MetaAIPremiumEnabledFingerprint → 2kD.A06 (MetaAI premium tier aggregator)
//
// Note: Server-side features (actual AI response generation, verified badge display,
// cloud storage quotas) still depend on the server. This patch unlocks the client-side
// gates that SHOW these features, not the server-side processing behind them.
//
// Verified against com.facebook.katana 569.0.0.42.72:
//   classes18/X/9Re.smali, classes17/X/84K.smali, classes5/X/U0t.smali,
//   classes5/com/facebook/.../ThreadSettingsMetaOnePremiumRow.smali,
//   classes13/X/2kD.smali
@Suppress("unused")
val facebookMetaAIPremiumPatch = bytecodePatch(
    name = "Unlock Meta AI premium features",
    description = "Unlocks all Facebook Plus benefits and Meta AI premium tier by bypassing the benefit entitlement system and MobileConfig remote flag checks.",
) {
    compatibleWith(FACEBOOK_COMPATIBILITY)

    execute {
        // ── Benefit system ────────────────────────────────────────────────────

        // 9Re.A03(String)Z — named benefit dispatcher → always return true.
        // Covers all UI components that call the dispatcher by feature name.
        BenefitCheckerFingerprint.method.returnEarly(true)

        // 84K.A08(String)Z — raw Set.contains check → always return true.
        // Covers UI components that call the benefit set directly, bypassing 9Re.
        BenefitSetCheckFingerprint.method.returnEarly(true)

        // U0t.Cqz(Set)V — CGN listener that re-locks app icons on server sync.
        // return-void prevents the re-lock runnable from being posted to the handler.
        BenefitRelockListenerFingerprint.method.returnEarly()

        // ── Meta AI / MetaOne premium gates ──────────────────────────────────

        // ThreadSettingsMetaOnePremiumRow.A00 → always show MetaOne premium row.
        MetaOnePremiumRowFingerprint.method.returnEarly(true)

        // 2kD.A05 → Meta AI marked as available for this session.
        MetaAIAvailableFingerprint.method.returnEarly(true)

        // 2kD.A06 → Meta AI premium tier marked as enabled.
        MetaAIPremiumEnabledFingerprint.method.returnEarly(true)
    }
}
