package app.template.patches.sociallite.unlock

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.SOCIALLITE_COMPATIBILITY
import app.template.patches.shared.clearBody
import org.w3c.dom.Element

// ── Manifest patch (PairIP) ───────────────────────────────────────────────────
private val socialLiteManifestPatch = resourcePatch(
    name = "SocialLite manifest patch",
    description = "Swaps android:name to SocialLiteApplication, removes LicenseActivity and CHECK_LICENSE.",
    default = false,
) {
    execute {
        document("AndroidManifest.xml").use { document ->
            val app = document.getElementsByTagName("application").item(0) as Element
            app.setAttribute("android:name", "com.sociallite.android.SocialLiteApplication")

            val activities = document.getElementsByTagName("activity")
            for (i in activities.length - 1 downTo 0) {
                val node = activities.item(i) as Element
                if (node.getAttribute("android:name").contains("LicenseActivity"))
                    node.parentNode.removeChild(node)
            }

            val permissions = document.getElementsByTagName("uses-permission")
            for (i in permissions.length - 1 downTo 0) {
                val node = permissions.item(i) as Element
                if (node.getAttribute("android:name").contains("CHECK_LICENSE"))
                    node.parentNode.removeChild(node)
            }
        }
    }
}

// ── Combined unlock patch ─────────────────────────────────────────────────────
//
// Single patch covering both PairIP bypass and full premium unlock.
//
// LAYER 1 — PairIP (manifest + bytecode)
//   Manifest: android:name swapped from com.pairip.application.Application →
//     com.sociallite.android.SocialLiteApplication so attachBaseContext() never
//     calls LicenseClient.checkLicense().
//   Bytecode: LicenseClient.checkLicense() → return-void (belt-and-suspenders).
//
// LAYER 2 — isPremiumActive() → return true
//   n()Z reads SharedPrefs "hasPaid". Returning true satisfies the base gate
//   required by hasProFeatures().
//
// LAYER 3 — getSubscriptionTier() → return "parent"
//   A()String reads SharedPrefs "subscriptionTier" (default "free"). "parent"
//   unlocks all Pro features in hasProFeatures() and matches the family plan
//   RevenueCat entitlement.
//
// LAYER 4 — hasProFeatures() → return true
//   L()Z is the real gate called from every premium screen. Defense-in-depth:
//   even if layers 2+3 somehow fall through, this returns true directly.
//
// LAYER 5 — EntitlementSnapshot → fake active entitlement
//   The server-sync guard in F0 checks: if server says "free" but RC snapshot
//   shows hasPaid=true + willAutoRenew=true → keep Pro state, skip SP writes.
//   Without this, the 24h server sync overwrites hasPaid=false and tier="free"
//   in SharedPrefs, reverting to free on the next cold start.
//
//   The snapshot class is R8-obfuscated (ib.z in v2.0.0.59, d8.x in v2.0.0.45).
//   We resolve it at runtime via method.returnType — no hardcoded obfuscated name.
//
@Suppress("unused")
val socialLiteUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Bypasses PairIP DRM and unlocks all SocialLite Pro features.",
    default = true,
) {
    compatibleWith(SOCIALLITE_COMPATIBILITY)

    dependsOn(socialLiteManifestPatch)

    execute {
        // LAYER 1 — PairIP bytecode bypass
        LicenseCheckFingerprint.method.addInstructions(0, "return-void")

        // LAYER 2 — isPremiumActive() → true
        IsPremiumActiveFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // LAYER 3 — getSubscriptionTier() → "parent"
        SubscriptionTierFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"parent\"\nreturn-object v0")
        }

        // LAYER 4 — hasProFeatures() → true
        HasProFeaturesFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // LAYER 5 — fake EntitlementSnapshot (hasPaid=true, willAutoRenew=true)
        // Resolve the snapshot class name at runtime from the matched method's
        // return type — avoids hardcoding the obfuscated class name (ib/z, d8/x, etc.)
        // which R8 renames every build.
        EntitlementSnapshotFingerprint.method.apply {
            val snapshotType = returnType  // e.g. "Lib/z;" — resolved at patch time
            clearBody()
            addInstructions(
                0,
                """
                new-instance v0, $snapshotType
                const-string v1, "parent"
                const/4 v2, 0x1
                invoke-direct {v0, v1, v2, v2}, ${snapshotType}-><init>(Ljava/lang/String;ZZ)V
                return-object v0
                """.trimIndent(),
            )
        }
    }
}
