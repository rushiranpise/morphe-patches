package app.template.patches.proxyman

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROXYMAN_COMPATIBILITY

/**
 * Unlocks Lifetime in Proxyman — Network Debugger (com.proxyman.proxymanandroid).
 *
 * ## Subscription model
 * - Monthly / Quarterly / Yearly subscription via Google Play Billing
 * - Lifetime one-time purchase via Google Play Billing
 * - Desktop license key (separate — not patched here)
 *
 * Subscription state is held in r9/b (UserSubscription data class):
 *   a:Z  = isPro          — master boolean read by all feature gates
 *   b:r9/d = planType     — MONTHLY / QUARTERLY / YEARLY / LIFETIME
 *   e:r9/c = state        — NONE / ACTIVE_SUBSCRIPTION / ACTIVE_LIFETIME / …
 *
 * The state wrapper q9/c1.c holds the current r9/b instance, updated on every
 * billing query result via q9/u0.h(Purchase)r9/b.
 *
 * ## Patch strategy
 *
 * ### Layer 1 — p9/f.a(p9/a)Z — feature access gate
 * The single entry point for all feature-locked checks (SSL proxying cap,
 * Safe Lock, Custom Filter, Pin Domain, Block List, Body Editor, Compose
 * entries, Proxyman Widget). Reads q9/c1.c.a (isPro) then does a packed-switch
 * on the feature ordinal. Always returning true grants access to every feature
 * regardless of subscription state.
 *
 * ### Layer 2 — p9/f.b(p9/a)I — usage count gate
 * Returns the allowed usage count per feature. Pro users receive
 * Integer.MAX_VALUE (0x7fffffff); free users get 1–5. Always returning
 * MAX_VALUE gives unlimited usage counts for all features.
 *
 * ### Layer 3 — q9/b.c(q9/c1, J)V — auto-paywall suppression
 * Triggered on every MainActivity.onResume(). Tracks foreground count and
 * timestamps in SharedPreferences; shows the subscription paywall sheet once
 * conditions are met (≥2 sessions, ≥24 h since install, cooldown elapsed)
 * — unless r9/b.isPro is true. Making this method a no-op prevents the
 * paywall from ever appearing.
 *
 * ### Layer 4 — LicenseClient.checkLicense(Context)V — PairIP bypass
 * PairIP anti-tamper SDK called in Application.attachBaseContext(). Connects
 * to PairIP's external licensing service and on failure launches
 * LicenseActivity (PAYWALL or ERROR), which blocks the entire app UI.
 * Making this a no-op prevents the service connection entirely.
 *
 * ### Layer 5 — LicenseActivity.onStart()V — PairIP nuclear fallback
 * If LicenseActivity is somehow still launched (cached intent, background
 * restart), calling super.onStart() and returning immediately prevents
 * showPaywallAndCloseApp() and showErrorDialog() from executing.
 */
@Suppress("unused")
val proxymanUnlockLifetimePatch = bytecodePatch(
    name = "Unlock Lifetime",
    description = "Unlocks all Lifetime features in Proxyman.",
    default = true
) {
    compatibleWith(PROXYMAN_COMPATIBILITY)

    execute {
        // ── Layer 1: p9/f.a(p9/a)Z — always return true ──────────────────────
        IsFeatureAllowedFingerprint
            .match(classDefBy(IsFeatureAllowedFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 2: p9/f.b(p9/a)I — always return Integer.MAX_VALUE ─────────
        GetFeatureLimitFingerprint
            .match(classDefBy(GetFeatureLimitFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const v0, 0x7fffffff
                    return v0
                    """.trimIndent()
                )
            }

        // ── Layer 3: q9/b.c(q9/c1,J)V — suppress auto-paywall ────────────────
        AutoPaywallFingerprint
            .match(classDefBy(AutoPaywallFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // ── Layer 4: LicenseClient.checkLicense(Context)V — skip PairIP check ─
        // Called from Application.attachBaseContext(); bypassing stops the whole
        // PairIP flow before it connects to the licensing service.
        PairIPCheckLicenseFingerprint
            .match(classDefBy(PairIPCheckLicenseFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // ── Layer 5: LicenseActivity.onStart()V — nuclear fallback ────────────
        // If PairIP somehow still launches LicenseActivity, return after super()
        // so neither showPaywallAndCloseApp() nor showErrorDialog() is called.
        PairIPLicenseActivityFingerprint
            .match(classDefBy(PairIPLicenseActivityFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    invoke-super {p0}, Landroid/app/Activity;->onStart()V
                    return-void
                    """.trimIndent()
                )
            }

        // ── Layer 6: r9/b.<init>(Z,...) — force Lifetime in UserSubscription ───
        // UI shows "Pro" + "Renewal date unavailable" because r9/b has isPro=true
        // but planType=null, isLifetime=false, state=NONE.
        // ga/b0 reads r9/b.b (planType): null → "Pro", r9/d.d → "Lifetime Pro"
        // and r9/b.b==LIFETIME → "Lifetime access unlocked" (no renewal date).
        // Override all four fields before the constructor iput sequence.
        UserSubscriptionConstructorFingerprint
            .match(classDefBy(UserSubscriptionConstructorFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 p1, 0x1
                    sget-object p2, Lr9/d;->d:Lr9/d;
                    const/4 p4, 0x1
                    sget-object p5, Lr9/c;->c:Lr9/c;
                    """.trimIndent()
                )
            }
    }
}
