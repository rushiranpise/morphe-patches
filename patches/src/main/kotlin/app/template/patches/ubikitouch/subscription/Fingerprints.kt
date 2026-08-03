package app.template.patches.ubikitouch.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// UbikiTouch (eu.toneiv.ubktouch) v1.17.7
//
// Premium architecture (Play Billing, product: ubktouch_unlock_pro_version):
//   sw1.z()Z  → reads XOR-encoded pref key via sw1.h() → MMKV.decodeBool(key, false)
//   sw1.h()Z  → Base64+XOR decodes IS_PURCHASED_PREF key ("EwkFCg8IGRIbCR8eBQoIHxw=", key 0x5a)
//   uh        → purchase callback lambda; calls lo1.w(true) on confirmed purchase
//   lo1.w(Z)V → writes boolean via Ls31;->L() (SharedPrefs) + broadcasts ACTION_INAPP_UPDATE
//   z5        → BillingClient query; checks list contains "ubktouch_unlock_pro_version"
//
// Change from v1.16.13:
//   OLD: gp1.y()Z — reads IS_PURCHASED_PREF from Paper (NoSQL) via ym0.T(key, false)
//   NEW: sw1.z()Z — same semantics; backing store migrated Paper → MMKV (com.tencent.mmkv)
//   gp1 class no longer contains y() — it is now a coroutine/thread-local utility class.
//   PremiumPropagatorFingerprint dropped: eu.toneiv.ubktouch.util.xwzp became a
//   system-settings utility; mpow propagation moved into lo1.w() itself.
//   Patching sw1.z() alone covers all 10+ call sites in the app.
//
// Patch strategy: sw1.z() → always return true

/**
 * Matches sw1.z()Z — the sole static premium-state getter in v1.17.7.
 *
 * sw1.z() instruction sequence (complete):
 *   invoke-static {}, Lsw1;->h()Ljava/lang/String;           ← INVOKE_STATIC (XOR-decode key)
 *   move-result-object v0
 *   const/4 v1, 0x0                                          ← default false
 *   invoke-static {v0, v1}, Lsr0;->rqym(Ljava/lang/String;Z)Z  ← INVOKE_STATIC (MMKV read)
 *   move-result v0
 *   return v0
 *
 * Two-filter ordered pair [INVOKE_STATIC, INVOKE_STATIC] narrows to two candidates:
 *   · sw1.z()Z  — no CONST_STRING anywhere in the method body
 *   · df.N()Z   — contains const-string "LOG_ENABLE" before the second INVOKE_STATIC
 *
 * The custom predicate excludes df.N() by requiring the absence of any CONST_STRING
 * opcode in the method body. No obfuscated names appear anywhere in this fingerprint.
 */
internal val IsPurchasedFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.INVOKE_STATIC),  // sw1.h() — XOR-decodes the MMKV pref key
        opcode(Opcode.INVOKE_STATIC),  // sr0.rqym(String, Z) — reads boolean from MMKV
    ),
    custom = { method, _ ->
        // sw1.z() contains no CONST_STRING (key is XOR-decoded at runtime via sw1.h()).
        // df.N()Z — the only other static ()Z matching the opcode pair — does contain
        // const-string "LOG_ENABLE", so this predicate uniquely selects sw1.z().
        method.implementation?.instructions?.none { it.opcode == Opcode.CONST_STRING } == true
    },
)
