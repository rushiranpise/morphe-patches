package app.template.patches.sociallite.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOCIALLITE_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.morphe.patcher.patch.resourcePatch
import org.w3c.dom.Element

// ── Pairip scope in SocialLite ────────────────────────────────────────────────
//
// SocialLite bundles a lightweight Pairip integration — no VM, no native lib:
//
//   com.pairip.application.Application   (extends SocialLiteApplication)
//     └── attachBaseContext(Context)
//           └── LicenseClient.checkLicense(context)   ← the only enforcement point
//
//   com.pairip.licensecheck.LicenseClient     — Play LVL check + LicenseActivity start
//   com.pairip.licensecheck.LicenseActivity   — blocks UI / exits if not licensed
//   com.pairip.licensecheck.LicenseResponseHelper — RSA verification of LVL response
//   com.pairip.licensecheck.LicenseContentProvider — IPC with Play Store licensing service
//
//   NO VMRunner, NO StartupLauncher, NO libpairipcore.so, NO VmDecryptor.
//   The arm64 split only contains libandroidx.graphics.path.so.
//
// ── Root cause ────────────────────────────────────────────────────────────────
//
//   AndroidManifest.xml sets android:name="com.pairip.application.Application".
//   On every app start, Android instantiates this class and calls
//   attachBaseContext(), which unconditionally calls LicenseClient.checkLicense().
//   After Morphe re-signs the APK, the signature hash differs from the one
//   embedded in the Pairip SDK → the check fails → LicenseActivity starts and
//   blocks the UI (or calls System.exit() after timeout).
//
//   SocialLiteApplication (the real app Application subclass) has NO Pairip calls.
//   Its attachBaseContext() only applies a dark-mode configuration override.
//
// ── Fix — two parts ───────────────────────────────────────────────────────────
//
//   PART 1 (manifest): Swap android:name from
//     "com.pairip.application.Application" → "com.sociallite.android.SocialLiteApplication"
//
//     com.pairip.application.Application is never constructed, so
//     LicenseClient.checkLicense() is never called. SocialLiteApplication's
//     clean attachBaseContext() runs instead (dark-mode override only).
//     Also removes LicenseActivity from the manifest and CHECK_LICENSE permission.
//
//   PART 2 (bytecode): no-op LicenseClient.checkLicense(Context)
//
//     Belt-and-suspenders: if checkLicense() is ever reached through a path
//     other than the Application constructor (e.g. a service or broadcast),
//     the method immediately returns without performing any check or starting
//     LicenseActivity.
//
private val socialLitePairIpManifestPatch = resourcePatch(
    name = "SocialLite Pairip manifest patch",
    description = "Swaps android:name from com.pairip.application.Application to " +
        "com.sociallite.android.SocialLiteApplication, removing the Pairip " +
        "attachBaseContext call. Also removes LicenseActivity and CHECK_LICENSE.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->

            // Swap application class — Pairip's attachBaseContext never runs
            val applicationElement =
                document.getElementsByTagName("application").item(0) as Element
            applicationElement.setAttribute(
                "android:name",
                "com.sociallite.android.SocialLiteApplication",
            )

            // Remove LicenseActivity — paywall screen can no longer start
            val activities = document.getElementsByTagName("activity")
            for (i in activities.length - 1 downTo 0) {
                val activity = activities.item(i) as Element
                if (activity.getAttribute("android:name")
                        .contains("LicenseActivity")) {
                    activity.parentNode.removeChild(activity)
                }
            }

            // Remove CHECK_LICENSE permission — no longer needed
            val permissions = document.getElementsByTagName("uses-permission")
            for (i in permissions.length - 1 downTo 0) {
                val permission = permissions.item(i) as Element
                if (permission.getAttribute("android:name")
                        .contains("CHECK_LICENSE")) {
                    permission.parentNode.removeChild(permission)
                }
            }
        }
    }
}

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

    dependsOn(socialLitePairIpManifestPatch)

    execute {
        // Belt-and-suspenders: no-op checkLicense() at the bytecode level
        LicenseCheckFingerprint.method.addInstructions(0, "return-void")
        
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
