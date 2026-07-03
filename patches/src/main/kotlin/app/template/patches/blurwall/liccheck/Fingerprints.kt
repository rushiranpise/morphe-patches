package app.template.patches.blurwall.liccheck

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// LͿ/Ϳ;->މ(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;
// SharedPreferences getter. Uses "flip_clock" as the prefs file name.
// Called with key="start_blur" to gate premium features; returns Boolean.FALSE
// by default (non-paying user). Patched to short-circuit for "start_blur" → TRUE.
object SharedPrefsGetterFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/Object;"),
    filters = listOf(
        string("flip_clock"),
        opcode(Opcode.INVOKE_INTERFACE),   // SharedPreferences.getBoolean
    ),
)
