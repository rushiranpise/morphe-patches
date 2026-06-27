package app.template.patches.tivimate

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Lᵛˉʾ;->ᵘᵝ()Z — BillingClient isPurchased.
 * Pinned by "BillingClient"+"9.1.0" unique to this class (upgraded from 9.0.0 in 5.3.3).
 */
val IsPurchasedFingerprint = Fingerprint(
    classFingerprint = Fingerprint(strings = listOf("BillingClient", "9.1.0")),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * ProtectedTvPlayerApplication.b()V — SHA-256 cert check (renamed from HyGokf in 5.3.3).
 */
val SignatureCheckFingerprint = Fingerprint(
    definingClass = "Lar/tvplayer/tv/ProtectedTvPlayerApplication;",
    name = "b",
    returnType = "V",
    parameters = emptyList(),
)
