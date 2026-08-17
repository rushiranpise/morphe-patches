package app.template.patches.parallelspace

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.literal
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Parallel Space Pro v4.0.9162
//
// PRO STATUS ARCHITECTURE
// The app stores Pro state in SharedPreferences under the key "bpcs"
// (SPConstant.BILLING_PURCHASE_CURRENT_STATE — stable, non-obfuscated constant).
// Value: int — 2 = Pro, 1 = pending/expired, 0 = free.
//
// Purchase model: SUBSCRIPTION (monthly/yearly via Google Play Billing).
// There is no server-side license validation — all checks are client-side.
//
// There are TWO distinct isPro code paths, both must be patched:
//
//   PATH A — BillingClient gate (td.J()Z):
//     Synchronized method; reads td.b:I (runtime billing state updated by Play Billing).
//     Called by be.d()Z (the billing manager's public gateway) and billing internals.
//
//   PATH B — Direct SP read (de.l()Z):
//     Reads "bpcs" directly from SharedPreferences. Called from SplashActivity,
//     CloneAndIncognitoInstallActivity, HomeView (n40.smali × 2), and other UI
//     entry points that do NOT go through be.d(). Patching PATH A alone leaves
//     all these callers locked.
//
// ── Fingerprint 1: td.J()Z — synchronized BillingClient isPro gate ──────────
//
// SMALI VERIFIED (classes.dex, v4.0.9162):
//   .class public final Lcom/lbe/parallel/td;
//   .method public final J()Z  .registers 5
//   [0]  iget-object v0, p0, td->a:Object
//   [1]  monitor-enter v0                           ← instructionMatches[0] (MONITOR_ENTER)
//   [2]  iget v1, p0, td->b:I                      (runtime bpcs int)
//   [3]  const/4 v2, 0x2                            ← instructionMatches[1] (literal 2)
//   [4]  const/4 v3, 0x0
//   [5]  if-ne v1, v2, :cond_15                    ← PATCH: nop  [monitorIdx + 4]
//   [6]  iget-object v1, p0, td->i:zzar
//   [7]  if-eqz v1, :cond_15                       ← PATCH: nop  [monitorIdx + 6]
//   [8]  iget-object v1, p0, td->j:dm1
//   [9]  if-eqz v1, :cond_15                       ← PATCH: nop  [monitorIdx + 8]
//   [10] const/4 v3, 0x1
//   [11] goto :goto_15
//   [12] move-exception v1
//   [13] goto :goto_17
//   [14] :cond_15/:goto_15  monitor-exit v0
//   [15] return v3
//   [16] monitor-exit v0
//   [17] throw v1
//
// PATCH: nop [9], [7], [5] in reverse order → always reaches [10] const v3=1 → return true.
// monitor-enter/exit is preserved. returnEarly(true) is NOT safe here (skips monitor-enter).
//
// FINGERPRINT ANCHORS — fully update-proof, no type names:
//   opcode(MONITOR_ENTER)  — structural marker of the synchronized isPro gate.
//   literal(2L)            — the "bpcs == 2 means Pro" comparison value.
//
// UPDATE HISTORY:
//   v4.0.9162: was anchored on fieldAccess(zzar) — WRONG. zzar is a Google Play
//   Billing SDK internal class name that changes on every billing library update.
//   Replaced with opcode(MONITOR_ENTER) + literal(2) which are purely structural
//   and survive Play Billing SDK upgrades. Verified unique in classes.dex.
internal object IsProGateFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.MONITOR_ENTER),
        literal(2L),
    ),
)

// ── Fingerprint 2: de.l()Z — direct SharedPreferences isPro read ─────────────
//
// SMALI VERIFIED (classes.dex, v4.0.9162):
//   .class public final Lcom/lbe/parallel/de;   (R8-obfuscated class name)
//   .method public final l()Z  .registers 3     (R8-obfuscated method name)
//   [0] invoke-static {}, i11->b()i11           (SP singleton getter)
//   [1] move-result-object v0
//   [2] const-string v1, "bpcs"                 ← instructionMatches[0]
//   [3] invoke-virtual {v0, v1}, i11->c(String)I
//   [4] move-result v0
//   [5] iput v0, p0, de->b:I                    (update cache field)
//   [6] const/4 v1, 0x2
//   [7] if-ne v0, v1, :cond_11
//   [8] const/4 v0, 0x1
//      goto :L
//   :cond_11  const/4 v0, 0x0
//   :L  return v0
//
// PATCH: returnEarly(true) — safe, no monitor, no try/catch.
//
// FINGERPRINT ANCHOR — fully update-proof:
//   string("bpcs") — SPConstant.BILLING_PURCHASE_CURRENT_STATE, a named constant
//   in the app's own non-obfuscated SPConstant class. Will not change unless the
//   developers rename their own SP key. Verified unique: the ONLY ()Z method in
//   classes.dex containing this string.
internal object IsProDirectFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("bpcs"),
    ),
)
