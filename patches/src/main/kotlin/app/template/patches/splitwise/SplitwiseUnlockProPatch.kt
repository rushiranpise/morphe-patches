package app.template.patches.splitwise

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SPLITWISE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags


private val ItemizationFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ItemizationFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ItemizationFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ReceiptScanningFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ReceiptScanningFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ReceiptScanningFeatureStatusGetScanFabVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getScanFabVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)


private val ReceiptScanningFeatureStatusGetHasScanTechniquesFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getHasScanTechniques",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)


private val ReceiptScanningFeatureStatusIsNativeScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isNativeScan",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)


private val ReceiptScanningFeatureStatusIsServerScanFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "isServerScan",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)


private val ProDuoCarouselFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ProDuoCarouselFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoCarouselFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ProAccountCardFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ProAccountCardFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)


private val ProFeatureUtilsLoadChartsFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/utils/ProFeatureUtils;",
    name = "loadCharts",
    parameters = listOf(
        "Landroid/app/Activity;",
        "Lcom/Splitwise/SplitwiseMobile/utils/ProFeatureUtils\$ChartType;",
        "Ljava/lang/Long;"
    ),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)


/**
 * Unlock Splitwise Pro (v26.5.4+)
 *
 * Splitwise stores the current user's Pro status in [Person.isPro]:Z, which is
 * populated server-side and checked throughout the app.  This patch operates on
 * two independent layers so that Pro status survives every code path:
 *
 * ## Layer 1 — Person.isPro()
 *
 * `Person.isPro()Z` is the single public getter called by every Pro-gated
 * feature: group image uploads (GroupSettingsViewModel / SplitwiseCustomizeGroupFragment /
 * SplitwiseSetUpGroupFragment), receipt scanning (ImageCaptureScreen /
 * TransactionSourceAdjustAutoSplitModalFragment), and bank-import onboarding
 * (ImportedTransactionSourceOnboardingScreen).  Replacing its body with
 * `const/4 true; return` ensures every Pro check returns true regardless of
 * what the server tells us.
 *
 * ## Layer 2 — Upsell banner suppression
 *
 * Three "upgrade" surfaces are suppressed by forcing their `getEnabled()Z`
 * methods to return false:
 *  - `ProAccountCardFeatureStatus` — the "Upgrade to Pro" card in the Account tab.
 *  - `ProDuoSettingsUpsellFeatureStatus` — the Duo/Pro upsell banner in Settings.
 *  - `ReceiptScanningFeatureStatus` — force-enables Pro-only receipt scanning.
 *
 * @package com.Splitwise.SplitwiseMobile
 */
@Suppress("unused")
val splitwiseUnlockProPatch = bytecodePatch(
    name = "Unlock Pro",
    description = "Unlocks Splitwise Pro features, removes ad banners, and suppresses all upgrade upsell prompts.",
    default = true
) {
    compatibleWith(SPLITWISE_COMPATIBILITY)

    execute {

        fun forceBoolean(value: Boolean, vararg fps: Fingerprint) {
            val body = if (value) "const/4 v0, 0x1\nreturn v0" else "const/4 v0, 0x0\nreturn v0"
            fps.forEach { fp ->
                runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, body)
                }
            }
        }

        fun stubVoid(vararg fps: Fingerprint) {
            fps.forEach { fp ->
                runCatching { fp.match(classDefBy(fp.definingClass!!)).method }.getOrNull()?.apply {
                    if (implementation == null) return@apply
                    removeInstructions(0, instructions.count())
                    addInstructions(0, "return-void")
                }
            }
        }

        // Core Pro entitlement and upsell suppression.
        forceBoolean(true, PersonIsProFingerprint, ReceiptScanningEnabledFingerprint)
        forceBoolean(false, ProAccountCardEnabledFingerprint, ProDuoSettingsUpsellEnabledFingerprint)

        // Folded in from Unlock More Features.
        forceBoolean(
            true,
            AdFeatureStatusGetEnabledFingerprint,
            AdFeatureStatusGetVisibleFingerprint,
            ItemizationFeatureStatusGetEnabledFingerprint,
            ItemizationFeatureStatusGetVisibleFingerprint,
            ReceiptScanningFeatureStatusGetEnabledFingerprint,
            ReceiptScanningFeatureStatusGetVisibleFingerprint,
            ReceiptScanningFeatureStatusGetScanFabVisibleFingerprint,
            ReceiptScanningFeatureStatusGetHasScanTechniquesFingerprint,
            ReceiptScanningFeatureStatusIsNativeScanFingerprint,
            ReceiptScanningFeatureStatusIsServerScanFingerprint,
            ProDuoCarouselFeatureStatusGetEnabledFingerprint,
            ProDuoCarouselFeatureStatusGetVisibleFingerprint,
            ProAccountCardFeatureStatusGetEnabledFingerprint,
            ProAccountCardFeatureStatusGetVisibleFingerprint
        )

        // Folded in from Suppress Chart Paywall Route.
        stubVoid(ProFeatureUtilsLoadChartsFingerprint)
    }
}