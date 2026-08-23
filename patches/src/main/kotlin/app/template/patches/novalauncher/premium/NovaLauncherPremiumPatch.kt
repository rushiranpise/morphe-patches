package app.template.patches.novalauncher.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.NOVA_LAUNCHER_COMPATIBILITY
import app.template.patches.shared.getReference
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

// Nova Launcher 8.8.8 Premium Patch
//
// Architecture:
//   Nova Prime state flows from two independent sources that both must be patched:
//
//   A) wu/v0.b()Z — the static isPrime() gate (8 direct callers)
//      ORs together three prime sources:
//        - oy/h2.h:Z         (runtime SharedPreferences state)
//        - wu/v0.b:Z         (static flag set by billing callback)
//        - lv/f0.a(Context)Z (time-limited "prime grant" from prefs)
//      → returnEarly(true)
//
//   B) oy/h2.a(SharedPreferences)V — the runtime prime state initialiser
//      Called once from Application.onCreate(). Contains 3 iput-boolean writes
//      to oy/h2.h:Z depending on purchase state read from SharedPreferences.
//      oy/h2.h:Z is read DIRECTLY by 45+ classes across all 5 DEX shards,
//      bypassing wu/v0.b() entirely. The h:Z field must be forced to true at
//      the source.
//      Strategy: scan all iput-boolean instructions on Loy/h2;->h:Z in reverse
//      order and inject const/4 vREG, 0x1 before each write to override the value
//      register regardless of the billing-derived result.
//
//   C) lv/c0.w(Context)Z — subscription-active check (6 callers in billing path)
//      Reads "nova_billing" SharedPreferences: checks subscription_active boolean
//      and calls lv/c0.y() to verify the last_verified timestamp is < 48h old.
//      Called by wu/v0.c() and 5 other billing/feature-gate classes.
//      → returnEarly(true)
//
// Protections: no Pairip, no signature check, no SSL pinning.
// Anti-debug/anti-VM checks (APKiD) are in billing-related third-party SDKs
// (AdMob, Meta Audience Network) — not in the prime check path.

@Suppress("unused")
val novaLauncherPremiumPatch = bytecodePatch(
    name = "Unlock Prime",
    description = "Unlocks Nova Launcher Prime by bypassing the static isPrime gate, forcing the runtime prime state to true on startup, and bypassing the subscription-active verification check.",
    default = true,
) {
    compatibleWith(NOVA_LAUNCHER_COMPATIBILITY)

    execute {

        // ── Patch A: wu/v0.b()Z — static isPrime gate ─────────────────────────
        // Returning true unconditionally short-circuits all 8 call sites without
        // touching the billing callback or SharedPreferences state.
        IsPrimeStaticFingerprint.method.returnEarly(true)

        // ── Patch B: oy/h2.a(SharedPreferences) — runtime prime state init ────
        // 45+ classes read oy/h2.h:Z directly (bypassing wu/v0.b()). This method
        // has 3 iput-boolean writes to h:Z; the last one (:L16) produces the
        // definitive runtime value. We force all 3 registers to 1 in reverse
        // instruction order so the final written value is always true.
        //
        // iput-boolean vREG, p0, Loy/h2;->h:Z   ← target opcode
        // Register layout: registerA = value (v2 or v3), registerB = object (p0)
        val primeInitMethod = PrimeStateInitFingerprint.method

        // Collect indices of all iput-boolean on h:Z in reverse order (safe to modify)
        val hFieldDesc = "Loy/h2;->h:Z"
        val hPutIndices = primeInitMethod.instructions
            .mapIndexedNotNull { index, insn ->
                if (insn.opcode != Opcode.IPUT_BOOLEAN) return@mapIndexedNotNull null
                val ref = insn.getReference<FieldReference>() ?: return@mapIndexedNotNull null
                if ("${ref.definingClass}->${ref.name}:${ref.type}" != hFieldDesc) return@mapIndexedNotNull null
                index
            }
            .reversed()

        if (hPutIndices.isEmpty()) throw PatchException(
            "Nova Launcher: no iput-boolean on $hFieldDesc found in PrimeStateInitFingerprint method."
        )

        // Inject const/4 vREG, 0x1 before each iput-boolean to override value register
        hPutIndices.forEach { idx ->
            val valueReg = (primeInitMethod.instructions.elementAt(idx) as TwoRegisterInstruction).registerA
            primeInitMethod.addInstructions(idx, "const/4 v$valueReg, 0x1")
        }

        // ── Patch C: lv/c0.w(Context)Z — subscription-active gate ─────────────
        // Returning true makes all 6 subscription-path callers believe an active,
        // non-expired subscription is present, preventing billing re-queries and
        // suppressing the upgrade prompts triggered by a false return.
        SubscriptionActiveFingerprint.method.returnEarly(true)
    }
}
