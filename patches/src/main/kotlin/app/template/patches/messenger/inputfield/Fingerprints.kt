package app.template.patches.messenger.inputfield

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

// ─── Typing indicator Runnable (classes5/X/Ay7) ──────────────────────────────
// Matches ConversationTypingContext$sendActiveStateRunnable$1 run()V via the
// stable __redex_internal_original_name field.
//
// run() sends the "user is typing" indicator to the server.
// replaceInstruction(0, "return-void") silences it entirely.
//
// Verified: classes5/X/Ay7.smali — implements Runnable,
//   __redex_internal_original_name = "ConversationTypingContext$sendActiveStateRunnable$1"
//   run()V → monitor-enter → sends typing state.
//
// Verified against com.facebook.orca 573.0.0.44.88.
internal val SendTypingIndicatorFingerprint = Fingerprint(
    returnType = "V",
    parameters = listOf(),
    custom = { method, classDef ->
        method.name == "run" &&
            classDef.fields.any {
                it.name == "__redex_internal_original_name" &&
                    (it.initialValue as? StringEncodedValue)?.value ==
                    "ConversationTypingContext\$sendActiveStateRunnable\$1"
            }
    },
)
