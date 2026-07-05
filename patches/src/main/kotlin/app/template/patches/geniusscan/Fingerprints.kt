package app.template.patches.geniusscan

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

val SubscriptionFeatureCheckFingerprint = Fingerprint(
    definingClass = "Ltk/p;",
    name = "f",
    returnType = "Z",
    parameters = listOf("Ltk/a;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)

// tk/p.d(tk/q)Ltk/q — resolves plan from "pro" pref / enterprise expiry / RevenueCat
val SubscriptionPlanResolverFingerprint = Fingerprint(
    definingClass = "Ltk/p;",
    name = "d",
    returnType = "Ltk/q;",
    parameters = listOf("Ltk/q;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("pro"),
)

val UpgradeCompareActivityFingerprint = Fingerprint(
    definingClass = "Lcom/thegrizzlylabs/geniusscan/ui/upgrade/UpgradeCompareActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
