package app.template.patches.speedtest

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── GooglePurchaseManager.b()Z ──────────────────────────────────────────────
// Main ad-free runtime check inside the PurchaseManager implementation.
// Called from the purchase/ads pipeline to decide whether ads run.
// Forcing true → app treats user as ad-free subscriber.
val PurchaseManagerIsAdFreeFingerprint = Fingerprint(
    definingClass = "Lcom/ookla/speedtest/purchase/google/D;",
    name = "b",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── PurchaseDataCompat.isUserAdFree(k0)Z ────────────────────────────────────
// Static util: checks legacy isAdFree flag AND purchase token map.
// Called from D.b() and UI entry points. Forcing true covers both paths.
val PurchaseDataCompatIsUserAdFreeFingerprint = Fingerprint(
    definingClass = "Lcom/ookla/speedtest/purchase/google/j0;",
    name = "b",
    parameters = listOf("Lcom/ookla/speedtest/purchase/google/k0;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// ── PurchaseDataCompat.hasInAppTokens(k0)Z ──────────────────────────────────
// Checks in-app purchase token count ≥ 1 (active in-app purchase gate).
// Forcing true prevents the "no active purchase" fallback flow.
val PurchaseDataCompatHasInAppTokensFingerprint = Fingerprint(
    definingClass = "Lcom/ookla/speedtest/purchase/google/j0;",
    name = "c",
    parameters = listOf("Lcom/ookla/speedtest/purchase/google/k0;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

// ── SharedPrefsAdFreeCache.isAdFree()Z ──────────────────────────────────────
// Legacy SharedPreferences-backed ad-free flag (pre-4.4.11 compat path).
// Forcing true ensures cached state is always ad-free on every cold start.
val SharedPrefsIsAdFreeFingerprint = Fingerprint(
    definingClass = "Lcom/ookla/speedtest/purchase/google/l0;",
    name = "b",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)
