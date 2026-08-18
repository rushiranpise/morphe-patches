package app.template.patches.monet.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MONET_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Monet Launcher (com.klevico.monet) — premium model overview
//
// Google Play Billing, single SKU: "premium_unlock" (one-time inapp purchase)
//
// The BillingManager (obfuscated class, La/yo; in 1.0.73) owns all premium state:
//
//   Field c:Z  = is_premium_cached  (read from SharedPrefs "billing_prefs" at startup)
//   Field d:Z  = has_license_blob   (non-null "license_blob_v1" in "billing_prefs")
//   Field e:Z  = billingQueryDone   (set true after first queryPurchases returns)
//   Field f:Z  = wasEverGranted     (set true once a PURCHASED state is observed)
//
//   isPremium = d || (c && (f || !e))
//
//   The MutableStateFlow<Boolean> (field g:La/sb4;) is updated by i()V and observed
//   by all feature-gate ViewModels across the app.
//
// Two patch layers cover all code paths:
//
//   Layer 1 — BillingCallbackFingerprint → l(Z)V:
//     Force p1=true at entry. Every billing refresh (PURCHASED or NOT_PURCHASED)
//     becomes a "grant" call. Persists true to SharedPrefs and emits true to StateFlow.
//
//   Layer 2 — BillingManagerConstructorFingerprint → <init>(Context, ?):
//     After startup reads of is_premium_cached and license_blob_v1, inject
//     iput-boolean true into both field c and field d. Covers cold-start and
//     cleared-data scenarios before any billing query returns.
//
// No obfuscated names appear in either fingerprint.
// All anchors are: stable string literals or stable Android SDK method calls.

@Suppress("unused")
val monetUnlockPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all Monet Launcher premium features by forcing the billing cache to always report premium as active.",
    default = true,
) {
    compatibleWith(MONET_COMPATIBILITY)

    execute {
        // Layer 1: force billing callback to always report premium = true.
        // Injects const/4 p1, 0x1 as the very first instruction so every call
        // to setPremiumCached() — from queryPurchases or the purchase-verify
        // coroutine — persists true and emits true into the premium StateFlow.
        BillingCallbackFingerprint.method.addInstructions(
            0,
            "const/4 p1, 0x1",
        )

        // Layer 2: force is_premium_cached = true and has_license = true in the
        // BillingManager constructor, covering the startup window before the first
        // billing query returns.
        //
        // Strategy: scan the constructor's instructions for the two iput-boolean
        // writes that follow the SharedPrefs reads. We identify them by the field
        // reference (type Z, on the same defining class), then record which fields
        // they write. Finally we inject const/true + both iput-boolean overrides
        // immediately before return-void.
        val ctor = BillingManagerConstructorFingerprint.method

        // Collect the two iput-boolean instructions that write Z fields on `this`.
        // In 1.0.73: idx 10 → c:Z (is_premium_cached), idx 22 → d:Z (has_license).
        val thisType = BillingManagerConstructorFingerprint.classDef.type
        data class FieldWrite(val index: Int, val fieldRef: FieldReference)

        val premiumFieldWrites = ctor.instructionsOrNull
            ?.mapIndexedNotNull { index, insn ->
                if (insn.opcode != Opcode.IPUT_BOOLEAN) return@mapIndexedNotNull null
                val ref = (insn as? ReferenceInstruction)?.reference as? FieldReference
                    ?: return@mapIndexedNotNull null
                if (ref.definingClass != thisType || ref.type != "Z") return@mapIndexedNotNull null
                // Skip field c (register A == p0 destination register 2-reg insn)
                FieldWrite(index, ref)
            }
            ?: emptyList()

        if (premiumFieldWrites.size < 2) {
            throw PatchException(
                "BillingManager constructor: expected ≥2 iput-boolean Z writes on $thisType, " +
                    "found ${premiumFieldWrites.size}. Fingerprint may be stale."
            )
        }

        // Find return-void (last one in the constructor).
        val returnVoidIndex = ctor.instructionsOrNull
            ?.indexOfLast { it.opcode == Opcode.RETURN_VOID }
            ?.takeIf { it >= 0 }
            ?: throw PatchException("Could not find RETURN_VOID in BillingManager constructor.")

        // Determine destination register: the iput-boolean uses a TwoRegisterInstruction
        // where registerB = p0 (this). We use v0 as scratch (free at end of constructor).
        val cField = premiumFieldWrites[0].fieldRef   // first Z write = is_premium_cached
        val dField = premiumFieldWrites[1].fieldRef   // second Z write = has_license_blob

        // Inject before return-void: v0=1, then write both fields.
        // Must insert in reverse order so that index for dField override is applied
        // first, then cField override — both reference the same `thisType`.
        ctor.addInstructions(
            returnVoidIndex,
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, $thisType->${cField.name}:Z
                iput-boolean v0, p0, $thisType->${dField.name}:Z
            """.trimIndent(),
        )
    }
}
