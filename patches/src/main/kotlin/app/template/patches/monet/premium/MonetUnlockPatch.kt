package app.template.patches.monet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MONET_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Monet Launcher (com.klevico.monet) — premium model overview
//
// Google Play Billing, single SKU: "premium_unlock" (one-time inapp purchase)
//
// The BillingManager (R8-obfuscated class; La/hp; in 1.0.76, La/km; in 1.0.67)
// owns all premium state via these fields (field names stable across builds):
//
//   Field c:Z  = is_premium_cached  (read from SharedPrefs "billing_prefs" at startup)
//   Field d:Z  = has_license_blob   (non-null "license_blob_v1" in "billing_prefs")
//   Field e:Z  = billingQueryDone   (set true after first queryPurchases returns)
//   Field f:Z  = wasEverGranted     (set true once a PURCHASED state is observed)
//
//   isPremium = d || (c && (f || !e))
//
//   The MutableStateFlow<Boolean> (field g) is updated by i()V and observed
//   by all feature-gate ViewModels across the app.
//
// Two patch layers cover all code paths:
//
//   Layer 1 — BillingCallbackFingerprint → l(Z)V:
//     Force p1=true at entry so every billing refresh (PURCHASED or NOT_PURCHASED)
//     is treated as a "grant" call. Persists true to SharedPrefs and emits true
//     into the premium StateFlow via i()V.
//
//   Layer 2 — BillingManagerConstructorFingerprint → <init>(Context, ?)V:
//     Inject iput-boolean true into both field c:Z and field d:Z immediately
//     before return-void. Covers the cold-start / cleared-data window before
//     the first billing query returns, preventing a flash of locked UI.
//
// No obfuscated names appear in either fingerprint or in the patch body.
// All anchors are stable string literals or stable Android SDK method calls.
// Field names are discovered dynamically from smali at patch time.

@Suppress("unused")
val monetUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Monet Launcher premium features by forcing the billing cache to always report premium as active.",
    default = true,
) {
    compatibleWith(MONET_COMPATIBILITY)

    execute {
        // ── Layer 1 ──────────────────────────────────────────────────────────
        // Force the billing write-back method to always receive p1=true.
        // Injecting at index 0 means every call-site — queryPurchases callback
        // or the license-blob coroutine — will persist true and emit true into
        // the premium StateFlow before any original logic runs.
        BillingCallbackFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )

        // ── Layer 2 ──────────────────────────────────────────────────────────
        // Force is_premium_cached (c:Z) and has_license_blob (d:Z) to true
        // in the BillingManager constructor, covering cold-start and cleared-data.
        //
        // Strategy:
        //   1. Collect all iput-boolean instructions that write a Z field on `this`
        //      (the BillingManager instance). These are the field assignments for
        //      c:Z (from getBoolean "is_premium_cached") and d:Z (from license blob
        //      null-check). We record the field references dynamically so the patch
        //      works even if R8 renames the class or reorders fields.
        //   2. Locate the final return-void in the constructor.
        //   3. Inject const/4 v0, 0x1 + iput-boolean for both fields before return-void,
        //      overriding whatever the SharedPrefs reads produced.
        val ctor = BillingManagerConstructorFingerprint.method
        val thisType = BillingManagerConstructorFingerprint.classDef.type

        data class FieldWrite(val index: Int, val fieldRef: FieldReference)

        val premiumFieldWrites = ctor.instructionsOrNull
            ?.mapIndexedNotNull { index, insn ->
                if (insn.opcode != Opcode.IPUT_BOOLEAN) return@mapIndexedNotNull null
                val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
                    ?: return@mapIndexedNotNull null
                // Only target Z fields on `this` (the BillingManager), not on other objects
                if (ref.definingClass != thisType || ref.type != "Z") return@mapIndexedNotNull null
                FieldWrite(index, ref)
            }
            ?: emptyList()

        if (premiumFieldWrites.size < 2) {
            throw PatchException(
                "BillingManager constructor: expected ≥2 iput-boolean Z writes on $thisType, " +
                    "found ${premiumFieldWrites.size}. Fingerprint or field layout may have changed.",
            )
        }

        // First Z write = is_premium_cached (field c), second = has_license_blob (field d).
        val cField = premiumFieldWrites[0].fieldRef
        val dField = premiumFieldWrites[1].fieldRef

        // Find the last return-void in the constructor (there is exactly one).
        val returnVoidIndex = ctor.instructionsOrNull
            ?.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            ?.takeIf { it >= 0 }
            ?: throw PatchException("Could not find RETURN_VOID in BillingManager constructor.")

        // Inject before return-void: set v0=1, write c:Z=true, write d:Z=true,
        // then call i()V to re-emit the isPremium formula into the MutableStateFlow (field g).
        //
        // WHY i()V is required:
        //   The constructor builds the initial StateFlow value from c/d/e/f at line ~178
        //   (absolute), well before return-void at line ~442. On a fresh install, c=false
        //   and d=false at that point, so g is seeded with isPremium=false. Without calling
        //   i()V here, the StateFlow stays false even after we write c=d=true, and the UI
        //   will show the paywall when the user first taps a premium feature (before the
        //   first billing query returns and l(Z)V fires).
        //
        //   i()V reads c, d, e, f, recomputes isPremium, and pushes the result into g via
        //   hi4->l(). All coroutines observing hp->h (the exposed StateFlow) will see true.
        //
        //   g (La/hi4; MutableStateFlow) is initialized at line ~178 of the constructor,
        //   so it is safe to call i()V at return-void (line ~442).
        //
        // v0 is free at end of constructor (all locals have been consumed by prior iput-object).
        ctor.addInstructions(
            returnVoidIndex,
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, $thisType->${cField.name}:Z
                iput-boolean v0, p0, $thisType->${dField.name}:Z
                invoke-virtual {p0}, $thisType->i()V
            """.trimIndent(),
        )
    }
}
