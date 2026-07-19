package app.template.patches.pcremote

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com/monect/core/data/model/LoggedInUser.isPremium()Z
 *
 * Root premium gate. Returns true only when:
 *   - premiumExpirationDate > now(), OR
 *   - rewardPremiumExpiration > now()
 *
 * Consumed via com/monect/core/Config.isPremium()Z (thin wrapper)
 * which is the gate checked by all 26 feature gate sites:
 * ads, screen projector, file explorer, remote desktop, task manager,
 * data cable, settings, and HTTP client.
 *
 * Stable unobfuscated class — safe fingerprint across versions.
 * Patch: always return true.
 */
internal val LoggedInUserIsPremiumFingerprint = Fingerprint(
    definingClass = "Lcom/monect/core/data/model/LoggedInUser;",
    name = "isPremium",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
