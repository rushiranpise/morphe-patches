package app.template.patches.anexplorer.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ANEXPLORER_COMPATIBILITY

// ══════════════════════════════════════════════════════════════════════════════
// AnExplorer v6.0.6 — Premium Patch
// ══════════════════════════════════════════════════════════════════════════════
//
// Root cause of paywall showing despite o66/h66 patches:
//   The "Unlock Unlimited Access" screen is controlled by ds.g()Z, NOT o66.s().
//   ds.g() reads SharedPreferences "purchased" key directly — bypassing any
//   runtime pro flag we set via o66.p() / o66.s().
//   ProWrapper.b() launches g14 coroutine → g14.invokeSuspend calls ds.g()
//   → if false → shows paywall. Our previous patches never touched ds.g().
//
// PATCH LAYERS:
//
//   1. ds.g() → true  (BOTH variants — same class name in TV and phone builds)
//      The SharedPrefs-backed paywall gate. Patching this hides the
//      "Unlock Unlimited Access" dialog completely.
//
//   2. o66.p(Context) → true  (TV variant only)
//      Seeds DocumentsApplication.y; gates pro features at the data layer.
//
//   3. o66.s() → true  (TV variant only)
//      Combined pro+storage runtime gate consumed by feature checks.
//
//   4. h66.p(Context) → true  (Phone variant only)
//   5. h66.s() → true  (Phone variant only)
//      Same as 2+3 for the phone build's different obfuscated class names.

@Suppress("unused")
val anExplorerPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks AnExplorer Pro: hides paywall and removes all purchase gates.",
    default = true,
) {
    compatibleWith(ANEXPLORER_COMPATIBILITY)

    execute {
        val returnTrue = "const/4 v0, 0x1\nreturn v0"

        // Layer 1: SharedPrefs paywall gate — present in BOTH variants
        DsPurchasedCheckFingerprint.method.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layers 2-3: TV variant (o66) — skipped silently on phone builds
        O66PurchaseCheckFingerprint.methodOrNull?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }
        O66CombinedGateFingerprint.methodOrNull?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }

        // Layers 4-5: Phone variant (h66) — skipped silently on TV builds
        H66PurchaseCheckFingerprint.methodOrNull?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }
        H66CombinedGateFingerprint.methodOrNull?.apply {
            removeInstructions(0, instructions.count())
            addInstructions(0, returnTrue)
        }
    }
}
