package app.template.patches.splitwise

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Person.isPro() ───────────────────────────────────────────────────────────
// The canonical pro-status getter on the current user model.
// Called from: GroupSettingsViewModel, SplitwiseCustomizeGroupFragment,
// SplitwiseSetUpGroupFragment, ImageCaptureScreen, TransactionSourceAdjust*,
// ImportedTransactionSourceOnboardingScreen.
val PersonIsProFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/Person;",
    name = "isPro",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── AdFeatureStatus.getEnabled() ─────────────────────────────────────────────
// Gate that controls whether any ad campaign is active.
// Returns true → ad is enabled. Patching to return false disables all ads.
val AdFeatureStatusGetEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── AdFeatureStatus.getVisible() ─────────────────────────────────────────────
// Controls ad visibility. Patching to return false hides all ad UI.
val AdFeatureStatusGetVisibleFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/AdFeatureStatus;",
    name = "getVisible",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── ProAccountCardFeatureStatus.getEnabled() ─────────────────────────────────
// Drives the "Upgrade to Pro" account card/banner in the account section.
// Patching to return false hides the upsell card so Pro users don't see it.
val ProAccountCardEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProAccountCardFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── ProDuoSettingsUpsellFeatureStatus.getEnabled() ────────────────────────────
// Controls the Pro/Duo upsell banner in Settings. Suppress after unlock.
val ProDuoSettingsUpsellEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ProDuoSettingsUpsellFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)

// ── ReceiptScanningFeatureStatus.getEnabled() ─────────────────────────────────
// Pro-only receipt scanning feature gate. Force-enable for all users.
val ReceiptScanningEnabledFingerprint = Fingerprint(
    definingClass = "Lcom/Splitwise/SplitwiseMobile/data/ReceiptScanningFeatureStatus;",
    name = "getEnabled",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC)
)