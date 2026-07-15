package app.template.patches.larkplayer

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.Opcode

/**
 * Fingerprint for o/b15.d()Z — IBillingInfoProvide.hasPurchase().
 *
 * Class: Lo/b15;
 * Returns true if PurchaseBean (active subscription) is non-null.
 * Matched via class+method name; opcodes: INVOKE_VIRTUAL, MOVE_RESULT_OBJECT, IF_EQZ.
 */
internal val HasPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IF_EQZ),
    ),
    custom = { method, classDef ->
        classDef.type == "Lo/a15;" && method.name == "d"
    }
)

/**
 * Fingerprint for o/b15.J()Z — IBillingInfoProvide.hasHistoryPurchase().
 *
 * Class: Lo/b15;
 * Returns true if PurchaseHistoryRecord (any past purchase) is non-null.
 * Matched via class+method name; opcodes: IGET_OBJECT, CHECK_CAST, IGET_OBJECT, IF_EQZ.
 */
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
        classDef.type == "Lo/a15;" && method.name == "J"
    }
)

/**
 * Fingerprint for o/b15.f()Z — IBillingInfoProvide.isPermanent().
 *
 * Class: Lo/b15;
 * Called by processMinePremiumData to compute premiumStatus:
 *   f()Z == true  → premiumStatus = 1 (permanent premium, blocks PayPremiumFragment open)
 *   f()Z == false + d()Z == false → premiumStatus = 0 (no premium)
 *
 * Without this patch: d()Z=true but e() returns null → autoRenewing check fails →
 *   premiumStatus = 3 (expired) → PayPremiumFragment opens → sees d()Z=true →
 *   calls activity.finish() immediately (user sees page close).
 *
 * With this patch: f()Z=true → premiumStatus = 1 → SettingsFragment gate skips
 *   PayPremiumFragment entirely. Settings shows "Premium" label correctly.
 */
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
        classDef.type == "Lo/a15;" && method.name == "f"
    }
)
