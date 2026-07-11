package app.template.patches.uptodown.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Le6/w2;->d()Z — isTurbo() check.
 * No string literals in the method; anchored via invoke-interface CharSequence.length().
 * custom: method name "d", fields w:String and x:Z on the class.
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
 * TrackingWorker.doWork() — anti-tamper cert hash check.
 * Anchored via hardcoded SHA256 cert hash + "SHA256" strings.
 */
object AntiTamperFingerprint : Fingerprint(
    strings = listOf("822b9ca12b534ebcf426632221d951bfc60eb08f9f0cf2839c321b0685c2e8a4", "SHA256"),
)
