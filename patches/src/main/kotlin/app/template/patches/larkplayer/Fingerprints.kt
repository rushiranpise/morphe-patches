package app.template.patches.larkplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

private const val BILLING_INFO_PROVIDER = "Lo/dw2;"

private fun implementsBillingInfoProvider(classDef: com.android.tools.smali.dexlib2.iface.ClassDef) =
    BILLING_INFO_PROVIDER in classDef.interfaces

internal val HasPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IF_EQZ),
    ),
    custom = { method, classDef ->
        implementsBillingInfoProvider(classDef) && method.name == "d"
    }
)

internal val HasHistoryPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.CHECK_CAST),
        opcode(Opcode.IGET_OBJECT),
        opcode(Opcode.IF_EQZ),
    ),
    custom = { method, classDef ->
        implementsBillingInfoProvider(classDef) && method.name == "J"
    }
)

internal val IsPermanentFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.SGET_OBJECT),
        opcode(Opcode.NEW_INSTANCE),
        opcode(Opcode.CONST_4),
        opcode(Opcode.INVOKE_DIRECT),
    ),
    custom = { method, classDef ->
        implementsBillingInfoProvider(classDef) && method.name == "f"
    }
)
