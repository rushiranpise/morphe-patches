package app.template.patches.drivvo.billing

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DRIVVO_COMPATIBILITY
import app.template.patches.shared.clearBody

// ── Subscription model ────────────────────────────────────────────────────────
//
// Drivvo premium is controlled by four static methods in class y9:
//
//   y9.a(Context)Z  — isPremium()
//     Reads expiry date from SharedPrefs via w62.k() wrapper, compares to Date().
//     Written by server sync (gq.M(WsPlanoDTO)) when the Drivvo backend responds.
//
//   y9.d(Context)String  — getPlanType()
//     Returns "pro"/"individual"/"frota*"/null from SharedPrefs "PlanoDrivvoServerPlano".
//     "pro" → PlanosNovoActivity exits immediately (no upgrade CTA).
//
//   y9.j(Activity)Z  — checkVehicleLimit()
//     The primary hard lock. Even when y9.a()=true, if the plan's vehicle limit
//     (SP "PlanoDrivvoServerVeiculos") is 0/null (never written for non-paying users),
//     ALL active vehicles exceed the limit → PlanosNovoActivity.t0() is called and
//     feature screens are blocked. Returns false = blocked, true = allowed.
//
//   y9.k(Activity)Z  — checkPremiumScreen()
//     Simpler gate used on non-vehicle feature screens. If y9.a()=false → shows
//     PlanosNovoActivity, returns false. If true → returns true.
//
// ── Why a() and d() alone were not enough ────────────────────────────────────
//
//   y9.a() patched → true: isPremium passes everywhere.
//   y9.d() patched → "pro": plan screen shows "Pro" and exits cleanly.
//   BUT y9.j() independently reads SP("PlanoDrivvoServerVeiculos") — the server-set
//   vehicle limit. For a free user this is null → treated as 0 → any vehicle count
//   exceeds the limit → j() returns false → feature screens are blocked with an
//   upsell dialog, regardless of y9.a()=true.
//
// ── Patch strategy ───────────────────────────────────────────────────────────
//
//   All four patches use clearBody() + a 2-instruction replacement.
//   No secondary patches needed: y9 is the sole gatekeeper class.
//   fq0.b() (coroutine that adds home-screen banner cards) independently checks
//   y9.a() + auto-renew flag — its banner cards are cosmetic and dismissable;
//   not patched here as it is a complex coroutine state machine.
//
@Suppress("unused")
val drivvoUnlockPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Drivvo Pro by patching all four gateway methods in the y9 " +
        "subscription class: isPremium() → true, getPlanType() → 'pro', " +
        "checkVehicleLimit() → true (no vehicle cap), " +
        "checkPremiumScreen() → true (no screen lock).",
    default = true,
) {
    compatibleWith(DRIVVO_COMPATIBILITY)

    execute {
        // PATCH 1 — y9.a() → always true
        IsPremiumFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // PATCH 2 — y9.d() → always "pro"
        PlanTypeFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"pro\"\nreturn-object v0")
        }

        // PATCH 3 — y9.j() → always true (no vehicle limit)
        // Eliminates the "PlanoDrivvoServerVeiculos=0" vehicle cap that blocks
        // feature screens even when y9.a()=true.
        VehicleLimitFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // PATCH 4 — y9.k() → always true (no screen gate)
        PremiumScreenGateFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
    }
}
