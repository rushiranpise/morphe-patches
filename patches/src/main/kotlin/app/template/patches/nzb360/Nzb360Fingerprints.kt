package app.template.patches.nzb360

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val IsAASubscriptionActiveFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper;",
    name = "isAASubscriptionActive",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val IsUnlockedFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper;",
    name = "isUnlocked",
    parameters = listOf(
        "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper\$Service;",
        "Z"
    ),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val IsLockedTwoArgFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper;",
    name = "isLocked",
    parameters = listOf(
        "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper\$Service;",
        "Z"
    ),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

val IsLockedOneArgFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper;",
    name = "isLocked",
    parameters = listOf("Lcom/kevinforeman/nzb360/helpers/NZB360LicenseHelper\$Service;"),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

// UpgradeCenterKt.SubscriptionSection — mutableStateOf("Monthly") initial plan seed.
// Matching on "Monthly" string; patch changes it to "Yearly" so the toggle
// opens on the yearly plan by default.
val SubscriptionSectionDefaultPlanFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/upgradecenter/UpgradeCenterKt;",
    name = "SubscriptionSection",
    strings = listOf("Monthly")
)

// Contract$UIState.isSubscribed()Z
// Read by UpgradeCenter composable to decide whether to show the paywall or "already subscribed" UI.
val IsSubscribedFingerprint = Fingerprint(
    definingClass = "Lcom/kevinforeman/nzb360/upgradecenter/Contract\$UIState;",
    name = "isSubscribed",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)