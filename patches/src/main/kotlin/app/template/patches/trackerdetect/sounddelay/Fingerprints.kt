package app.template.patches.trackerdetect.sounddelay

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Z1/Z.<clinit>()V — static initializer that computes the play-sound delay.
 *
 * Computes: 10 (const/16 0xa) → int-to-long → × 60 (const-wide/16 0x3c) → sput-wide Z1/Z.b
 * Result stored in Z1/Z.b = 600L (seconds). This long is read by the item detail UI composable
 * and compared against the elapsed time since the tracker was first seen. When elapsed < 600s
 * the "Play Sound" button is hidden and a "X minutes remaining" label is shown instead.
 *
 * Uniqueness: only one <clinit> in the entire DEX uses literal(0xa) followed by MUL_LONG_2ADDR
 * and literal(0x3c) — verified against all 5045 smali classes.
 */
val PlaySoundDelayClinitFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
    filters = listOf(
        literal(0xa),                         // const/16 v0, 0xa  (the 10-minute value)
        opcode(Opcode.INT_TO_LONG),           // int-to-long v0, v0
        literal(0x3c),                        // const-wide/16 v2, 0x3c  (60 seconds/minute)
        opcode(Opcode.MUL_LONG_2ADDR),        // mul-long/2addr v0, v2
    )
)
