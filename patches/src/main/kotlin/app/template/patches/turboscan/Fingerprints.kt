package app.template.patches.turboscan

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val DocLimitCheckFingerprint = Fingerprint(
    definingClass = "Lo/GetTargetFragmentUsageViolation;",
    name = "IconCompatParcelizer",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PRIVATE),
)

val PageLimitCheckFingerprint = Fingerprint(
    definingClass = "Lo/GetTargetFragmentUsageViolation;",
    name = "RemoteActionCompatParcelizer",
    returnType = "Z",
    parameters = listOf("I"),
    accessFlags = listOf(AccessFlags.PRIVATE),
)

val UpgradeGateFingerprint = Fingerprint(
    definingClass = "Lo/GetTargetFragmentUsageViolation;",
    name = "asInterface",
    returnType = "Z",
    parameters = listOf("Landroid/app/Activity;", "Z"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// ApplicationController.onCreate — contains DEX integrity check that div-by-zeros on tamper
val OnCreateIntegrityCheckFingerprint = Fingerprint(
    definingClass = "Lcom/piksoft/turboscan/ApplicationController;",
    name = "onCreate",
    returnType = "V",
    parameters = emptyList(),
    strings = listOf("sHasPermanentMenuKey"),
)
