package app.template.patches.uptodown.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Le6/w2;->d()Z — composite isTurbo() gate.
 *
 * Logic: returns true when turboToken (field w:String) is non-empty OR
 * the isTurbo flag (field x:Z) is set on the user object.
 *
 * Anchored via the CharSequence.length() interface call (the turboToken
 * null/empty check), plus a custom predicate requiring the exact method
 * name "d" and the two stable field descriptors on the class.
 *
 * Safe: field names w/x and method name d are obfuscated, but the
 * CharSequence.length() filter narrows candidates to a tiny set and the
 * custom predicate eliminates all false positives. The class itself
 * (Le6/w2) and its field *types* are stable across minor versions.
 */
object IsTurboFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_INTERFACE,
            definingClass = "Ljava/lang/CharSequence;",
            name = "length",
        ),
    ),
    custom = { method, classDef ->
        method.name == "d" &&
            classDef.fields.any { it.name == "x" && it.type == "Z" } &&
            classDef.fields.any { it.name == "w" && it.type == "Ljava/lang/String;" }
    },
)

/**
 * TrackingWorker.doWork() — anti-tamper integrity check.
 *
 * Anchored via two stable strings that are hard-coded in the method body:
 *   - The SHA-256 cert-hash literal (unchanged across versions)
 *   - The "SHA256" algorithm name passed to Lu6/b;->d()
 *
 * Using [strings] (unordered) rather than [filters] because the two
 * const-string instructions appear far apart in the method body and the
 * relative order may shift between minor obfuscation passes.
 */
object AntiTamperFingerprint : Fingerprint(
    strings = listOf("822b9ca12b534ebcf426632221d951bfc60eb08f9f0cf2839c321b0685c2e8a4", "SHA256"),
)
