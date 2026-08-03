package app.template.patches.parallelspace

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// Parallel Space Pro v4.0.9159
//
// PRO STATUS ARCHITECTURE
// Pro state is stored in SharedPreferences via j11 (the app's SP singleton).
// Key: SPConstant.BILLING_PURCHASE_CURRENT_STATE = "bpcs"
// Value: int — 2 = Pro/purchased, 1 = pending, 0 = free
//
// j11.c(String) = SharedPreferences.getInt(key, 0)
// j11.i(int, String) = SharedPreferences.putInt(key, value)
//
// CHANGED FROM v4.0.9123:
// Old target: com/lbe/parallel/ad.k()Z — class ad no longer contains this method.
// New target: com/lbe/parallel/ee.l()Z — primary isPro getter (re-reads SP).
//             com/lbe/parallel/ee.k()Z — cached isPro getter (reads field ee.b).
//             com/lbe/parallel/le.g()V — Pro status notifier (calls r50.e() if Pro).
//
// ── Fingerprint 1: ee.l()Z — primary isPro getter ────────────────────────────
// Re-reads bpcs from SharedPreferences via j11.c(), updates field ee.b,
// then compares to 2 and returns boolean.
//
// SMALI VERIFIED (classes.dex):
//   .method public final l()Z  .registers 3
//   [000] invoke-static {}, j11.b()j11          ← SP singleton
//   [002] const-string v1, "bpcs"
//   [003] invoke-virtual { v0, v1 }, j11.c(String)I
//   [004] move-result v0
//   [005] iput v0, p0, ee.b:I                   ← update cache
//   [006] const/4 v1, 2
//   [007] if-ne v0, v1, :L0                     ← not pro
//   [008] const/4 v0, 1
//          goto :L1
//   :L0  const/4 v0, 0
//   :L1  return v0
//
// Fingerprinted by: string("bpcs") + methodCall(j11, "c") in exact order.
// definingClass + name is already unique, but filters add confidence.
internal object IsProRefreshFingerprint : Fingerprint(
    definingClass = "Lcom/lbe/parallel/ee;",
    name = "l",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("bpcs"),
        methodCall(
            definingClass = "Lcom/lbe/parallel/j11;",
            name = "c",
        ),
    ),
)

// ── Fingerprint 2: ee.k()Z — cached isPro getter ─────────────────────────────
// Reads field ee.b (int, cached bpcs value), compares to 2, returns boolean.
// Called frequently in UI path; uses the cached value to avoid SP reads.
//
// SMALI VERIFIED (classes.dex):
//   .method public final k()Z  .registers 3
//   [000] iget v0, p0, ee.b:I
//   [001] const/4 v1, 2
//   [002] if-ne v0, v1, :L0
//   [003] const/4 v0, 1   goto :L1
//   :L0  const/4 v0, 0
//   :L1  return v0
//
// No filters needed — definingClass + name + signature is 100% unique.
internal object IsProCachedFingerprint : Fingerprint(
    definingClass = "Lcom/lbe/parallel/ee;",
    name = "k",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ── Fingerprint 3: le.g()V — Pro status notifier ─────────────────────────────
// Called on billing state change. Reads bpcs from SP; if == 2 calls r50.e()
// which notifies all registered Pro-status listeners in the UI.
// We nop the if-ne guard at [036] so it ALWAYS notifies as Pro.
//
// SMALI VERIFIED (classes.dex):
//   .method public final g()V  .registers 3
//   [030-034] j11.b().c("bpcs") → v0 (int)
//   [035] const/4 v1, 2
//   [036] if-ne v0, v1, :L2     ← PATCH: nop this → always notify as Pro
//   [037] iget v0, p0, le.b:r50
//   [038] invoke-interface { v0 }, r50.e()V    ← notifies listeners
//   :L2  return-void
//
// Anchored by string("bpcs") + methodCall(j11, "c") after the setup block.
// instructionMatches[1] = methodCall(j11, "c") at index 33.
// if-ne is at index 36 = methodCallIndex + 3.
internal object IsProNotifierFingerprint : Fingerprint(
    definingClass = "Lcom/lbe/parallel/le;",
    name = "g",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("bpcs"),
        methodCall(
            definingClass = "Lcom/lbe/parallel/j11;",
            name = "c",
        ),
    ),
)
