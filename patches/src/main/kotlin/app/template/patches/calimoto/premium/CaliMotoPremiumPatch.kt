package app.template.patches.calimoto.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CALIMOTO_COMPATIBILITY

// Calimoto uses a custom Parse backend + Google Play Billing for subscriptions.
// The entire app premium state flows through one static method b.H() which
// returns an m7/a (Membership) enum value. Every feature gate in the app calls
// b.H() and compares the result against the desired tier.
//
// Membership enum values (m7/a, classes3):
//   d = NONE       e = WEEKLY      f = SEASON_PASS
//   i = YEARLY     q = LIFETIME    r = TRIAL    s = NAVIGATION_PACKAGE
//
// Strategy: inject at the top of b.H() to return m7/a.q (LIFETIME) immediately.
// This covers all 13+ call sites simultaneously — no per-feature patching needed.
//
// The LIFETIME sget-object is:
//   sget-object p0, Lm7/a;->q:Lm7/a;
//   return-object p0
// Using p0 since b.H() is static (.locals 4, p0 is the first available register
// but since it's static there is no "this", so v0 is fine — using v0).

@Suppress("unused")
val calimotoPremiumPatch = bytecodePatch(
    name = "Calimoto Premium",
    description = "Unlocks Calimoto LIFETIME membership by spoofing the membership getter.",
) {
    compatibleWith(CALIMOTO_COMPATIBILITY)

    execute {
        // Inject at index 0: load LIFETIME enum constant and return it.
        // b.H() declares .locals 4, so v0–v3 are available.
        MembershipGetterFingerprint.method.addInstructions(
            0,
            """
                sget-object v0, Lm7/a;->q:Lm7/a;
                return-object v0
            """,
        )
    }
}
