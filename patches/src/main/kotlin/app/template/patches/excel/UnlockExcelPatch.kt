package app.template.patches.excel

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.excel.ads.excelDisableAdsDependency
import app.template.patches.excel.integrity.excelBypassCodeTransparencyDependency
import app.template.patches.excel.login.excelDisableLoginRequirementDependency
import app.template.patches.excel.manifest.excelRemoveSharedUserIdDependency
import app.template.patches.excel.premium.excelUnlock365FamilyDependency
import app.template.patches.excel.signature.excelSpoofSignatureDependency
import app.template.patches.shared.Constants.EXCEL_COMPATIBILITY

@Suppress("unused")
val unlockExcelPatch = bytecodePatch(
    name = "Unlock Excel",
    description = "Removes login requirement, unlocks premium, blocks ads, bypasses signature and code transparency checks.",
    default = true,
) {
    compatibleWith(EXCEL_COMPATIBILITY)

    dependsOn(
        excelRemoveSharedUserIdDependency(),
        excelBypassCodeTransparencyDependency(),
        excelDisableLoginRequirementDependency(),
        excelUnlock365FamilyDependency(),
        excelDisableAdsDependency(),
        excelSpoofSignatureDependency(),
    )
}
