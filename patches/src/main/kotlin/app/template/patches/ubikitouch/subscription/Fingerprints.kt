package app.template.patches.ubikitouch.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// UbikiTouch (eu.toneiv.ubktouch) v1.16.13
//
// Premium architecture (Play Billing, product: ubktouch_unlock_pro_version):
//   gp1.y()Z → reads IS_PURCHASED_PREF from Paper (NoSQL prefs) via ym0.T(m(), false)
//   gp1.m()Z → decodes XOR-encoded pref key "IS_PURCHASED_PREF"
//   jg.nvkl() → queries purchases, checks productIds contains "ubktouch_unlock_pro_version"
//              → calls vh1.w(true) which calls ym0.z0(null, gp1.m(), true, false) to save pref
//   eu.toneiv.ubktouch.util.xwzp → calls gp1.y() and propagates result via b71.mpow(boolean)
//
// Patch strategy:
//   gp1.y() → always return true (primary premium state getter used everywhere)

/**
 * Matches gp1.y()Z — static method reading IS_PURCHASED_PREF via ym0.T().
 * Forcing true bypasses all premium gates throughout the app.
 */
internal val IsPurchasedFingerprint = Fingerprint(
    definingClass = "Lgp1;",
    name = "y",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_STATIC), // gp1.m()
        opcode(Opcode.INVOKE_STATIC), // ym0.T(key, false)
    ),
)

/**
 * Matches eu.toneiv.ubktouch.util.xwzp premium propagation method.
 * Calls b71.mpow(boolean) on the binding with the current premium state.
 * Patching to force mpow(true) ensures the accessible service and UI
 * reflect premium even if gp1.y() is only checked lazily.
 */
internal val PremiumPropagatorFingerprint = Fingerprint(
    definingClass = "Leu/toneiv/ubktouch/util/xwzp;",
    returnType = "V",
    filters = listOf(
        opcode(Opcode.INVOKE_INTERFACE), // b71.mpow(false)
    ),
)
