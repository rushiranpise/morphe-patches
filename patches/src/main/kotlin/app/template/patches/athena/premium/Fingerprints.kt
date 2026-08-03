package app.template.patches.athena.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Targets Settings.getPremiumUnlocked() in com.kin.athena.domain.model.Settings.
// The class is fully unobfuscated — stable across releases.
// Smali:
//   .method public final getPremiumUnlocked()Z
//     iget-boolean v0, p0, Lcom/kin/athena/domain/model/Settings;->premiumUnlocked:Z
//     return v0
object GetPremiumUnlockedFingerprint : Fingerprint(
    definingClass = "Lcom/kin/athena/domain/model/Settings;",
    name = "getPremiumUnlocked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)
