package app.template.patches.lawfully

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// l2/d.isActive()Z — Google Play Billing subscription state check.
// Checks if subscription state enum != expired; returns true when active.
// Patched to always return true so all subscription checks pass.
object IsActiveFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.SGET_OBJECT),
        opcode(Opcode.IF_EQ),
    ),
    custom = { method, classDef ->
        method.name == "isActive" &&
            classDef.type == "Ll2/d;"
    },
)
