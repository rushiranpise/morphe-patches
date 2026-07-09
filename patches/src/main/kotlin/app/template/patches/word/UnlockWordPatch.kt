package app.template.patches.word

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.WORD_COMPATIBILITY
import app.template.patches.word.ads.wordDisableAdsDependency
import app.template.patches.word.integrity.wordBypassCodeTransparencyDependency
import app.template.patches.word.login.wordDisableLoginRequirementDependency
import app.template.patches.word.manifest.wordRemoveSharedUserIdDependency
import app.template.patches.word.premium.wordUnlock365FamilyDependency
import app.template.patches.word.signature.wordSpoofSignatureDependency

@Suppress("unused")
val unlockWordPatch = bytecodePatch(
    name = "Unlock Word",
    description = "Removes login requirement, unlocks premium, blocks ads, bypasses signature and code transparency checks.",
    default = true,
) {
    compatibleWith(WORD_COMPATIBILITY)

    dependsOn(
        wordRemoveSharedUserIdDependency(),
        wordBypassCodeTransparencyDependency(),
        wordDisableLoginRequirementDependency(),
        wordUnlock365FamilyDependency(),
        wordDisableAdsDependency(),
        wordSpoofSignatureDependency(),
    )
}
