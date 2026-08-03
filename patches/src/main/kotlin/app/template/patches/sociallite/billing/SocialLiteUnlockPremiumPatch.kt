package app.template.patches.sociallite.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOCIALLITE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.sociallite.protection.socialLitePairIpPatch

// ── Subscription model ────────────────────────────────────────────────────────
//
// SocialLite premium state is controlled by TWO independent checks in d8.b1:
//
//   n() — isPremiumActive():
//     Returns true when the user has any active subscription at all.
//     Reads SharedPref "hasPaid" (written by server sync + RevenueCat).
//     Used as the base gate for all premium content.
//
//   y() — getSubscriptionTier():
//     Returns SharedPref "subscriptionTier" string (default: "free").
//     Values: "free", "pro", "parent", "personal".
//     Written by the server sync from UserStateResponse.getSubscriptionTier().
//
//   H() — hasProFeatures():
//     The REAL gate for full Pro plan features. Logic:
//       if forceFreeModeDebug → false
//       if D() (demo account) → true
//       if n() && (C() || y()=="pro" || y()=="parent") → true
//       else → false
//     Many feature screens call H() directly, not n(). With n()=true but
//     y()="free" (SP default), H() still returns false → app shows free UI.
//
// ── Why app was still free ───────────────────────────────────────────────────
//
//   The previous patch only patched n() (isPremiumActive) and the RevenueCat
//   snapshot. But H() (hasProFeatures) is the gate actually called from most
//   premium screens, and it separately calls y() to check the tier string.
//   With y() returning "free" (SP default, never written since no real purchase),
//   H() returned false even with n()=true → app displayed free-tier UI.
//
// ── Patch strategy ───────────────────────────────────────────────────────────
//
//   PATCH 1 — n() → returnEarly(true)
//     isPremiumActive always returns true. Required by H() as a prerequisite.
//     clearBody() removes the try-block/catch table before injection.
//
//   PATCH 2 — y() → return "pro"
//     getSubscriptionTier always returns "pro" — the individual Pro plan tier.
//     This makes H() return true for all Pro-gated features.
//     "pro" "parent" is the family plan tier — unlocks identical features to "pro" in H().
//     Also matches RevenueCat entitlement "parent" (sociallite_monthly5 product).
//     clearBody() used to remove the original SP read + null check.
//
//   PATCH 3 — H() → returnEarly(true)
//     hasProFeatures always returns true directly.
//     Defense-in-depth: even if n() or y() somehow fall through, H() is true.
//     Also future-proofs against new tier checks added to H() in later versions.
//
//   PATCH 4 — EntitlementSnapshot → fake active "pro" entitlement
//     d8.s.b() returns a fake snapshot with hasPaid=true, willAutoRenew=true.
//     This fires the server-sync override guard (F0 in b1.smali):
//       if (!serverHasPaid && snapshot.hasPaid && snapshot.willAutoRenew)
//           → log "keeping Pro state" + skip all SP writes
//     Without this, server sync (24h throttled) would overwrite "hasPaid"=false
//     and "subscriptionTier"="free" in SharedPrefs, and on the NEXT cold start
//     (before next sync throttle expires), y() would return "free" again.
//     With this patch: server sync always activates the guard → no SP downgrades.
//
@Suppress("unused")
val socialLiteUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Family Plan",
    description = "Unlocks SocialLite Pro by: (1) forcing isPremiumActive() to " +
        "return true; (2) forcing getSubscriptionTier() to return 'pro', enabling " +
        "all Pro-tier features gated by hasProFeatures(); (3) forcing hasProFeatures() " +
        "itself to return true as defense-in-depth; (4) faking the RevenueCat " +
        "entitlement snapshot to prevent the server sync from downgrading the " +
        "premium state on subsequent launches.",
    default = true,
) {
    compatibleWith(SOCIALLITE_COMPATIBILITY)

    dependsOn(socialLitePairIpPatch)

    execute {
        // PATCH 1 — isPremiumActive() → always true
        IsPremiumActiveFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }

        // PATCH 2 — getSubscriptionTier() → always "pro"
        // "pro" is the individual paid tier string checked by H() and the server sync.
        SubscriptionTierFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const-string v0, "parent"
                return-object v0
                """.trimIndent(),
            )
        }

        // PATCH 3 — hasProFeatures() → always true (defense-in-depth)
        HasProFeaturesFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                const/4 v0, 0x1
                return v0
                """.trimIndent(),
            )
        }

        // PATCH 4 — EntitlementSnapshot → fake active "pro" entitlement
        // d8.x.<init>(String purchaseToken, Z hasPaid, Z willAutoRenew)
        //   p1 = "parent"  (productIdentifier / purchaseToken)
        //   p2 = 1      (hasPaid = true,       field x.a)
        //   p3 = 1      (willAutoRenew = true,  field x.c)
        // Fires the F0 guard: server says free → RC says active+renewing → keep Pro
        EntitlementSnapshotFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, Ld8/x;
                const-string v1, "parent"
                const/4 v2, 0x1
                invoke-direct {v0, v1, v2, v2}, Ld8/x;-><init>(Ljava/lang/String;ZZ)V
                return-object v0
                """.trimIndent(),
            )
        }
    }
}
