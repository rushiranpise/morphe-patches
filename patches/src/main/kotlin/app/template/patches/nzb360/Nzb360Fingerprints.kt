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
