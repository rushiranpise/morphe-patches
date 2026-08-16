package app.template.patches.casttotv.premium

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CAST_TO_TV_COMPATIBILITY
import app.template.patches.shared.findMutableMethodOf
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Unlocks Cast to TV - XCast premium by forcing all subscription and
 * pro-purchase gate methods to return true.
 *
 * ## Architecture
 *
 * Premium state is stored in SharedPreferences via `H6/f1.b(String, Z)Z`
 * (getter) and `H6/f1.j(String, Z)V` (setter), keyed by obfuscated product
 * tag strings that map to SKUs:
 *
 *   "VCLJLJL"       → com.camerasideas.xcast.removead   (remove-ads IAP)
 *   "VCLioJLJL"     → discount remove-ads IAP
 *   "s7vkQunh"      → com.inshot.xcast.pro              (pro IAP)
 *   "JLWRIOJKLWFD"  → xcast.sub.monthly                 (monthly sub)
 *   "JLWOOFKLWFD"   → xcast.sub.yearly                  (yearly sub)
 *
 * Gate hierarchy (all in obfuscated class H6/a):
 *
 *   e()  = VCLJLJL || VCLioJLJL || g()[yearly] || c()[monthly]  ← master sub gate
 *   h()  = s7vkQunh                                              ← isPro (private)
 *   a()  = h()                                                   ← isPro (public wrapper)
 *   j()  = e() || d()
 *   k()  = e() || d()
 *   l()  = e() || (...)
 *   i()  = !(e() || a())   ← "not licensed" / show-upgrade-prompt check
 *
 * ## Patch
 *
 * `IsSubscribedFingerprint` locates e() via the stable SharedPrefs key strings
 * "VCLJLJL" and "VCLioJLJL" plus the f1.b() getter call pattern.
 *
 * At runtime we also locate a() within the same class: it is the only
 * public static Z() method in the gate class whose sole non-NOP opcodes are
 * INVOKE_STATIC + MOVE_RESULT + RETURN (exactly 3 real instructions),
 * i.e. a pure single-delegate forwarder.
 *
 * Patching both e() and a() causes:
 *   - All subscription checks (j/k/l) → true
 *   - All isPro checks → true
 *   - i() ("not licensed") → false  → all upgrade prompts suppressed
 */
@Suppress("unused")
val castToTVPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Cast to TV - XCast premium by forcing subscription and pro gates to return true.",
) {
    compatibleWith(CAST_TO_TV_COMPATIBILITY)

    dependsOn(
        spoofInstallSourcePatch,
        spoofSignatureVerificationPatch,
    )

    execute {
        // ── 1. Master subscription gate ────────────────────────────────────────
        // e() covers remove-ads, yearly/monthly subscriptions and all j/k/l gates.
        IsSubscribedFingerprint.method.returnEarly(true)

        // ── 2. isPro gate (a() = public wrapper of private h()) ────────────────
        // Locate a() within the same class as e() at runtime.
        // Signature: public static Z, no parameters, exactly one real INVOKE_STATIC
        // (the call into private h()), one MOVE_RESULT, one RETURN.
        val gateClass = IsSubscribedFingerprint.classDef
        val isProImmutable = gateClass.methods.firstOrNull { method ->
            AccessFlags.PUBLIC.isSet(method.accessFlags) &&
            AccessFlags.STATIC.isSet(method.accessFlags) &&
            !AccessFlags.PRIVATE.isSet(method.accessFlags) &&
            method.returnType == "Z" &&
            method.parameters.isEmpty() &&
            method.implementation?.instructions?.count { insn ->
                insn.opcode != Opcode.NOP
            } in 3..5
        } ?: throw PatchException("isPro gate method (a()) not found in ${gateClass.type}")

        mutableClassDefBy(gateClass.type)
            .findMutableMethodOf(isProImmutable)
            .returnEarly(true)
    }
}
