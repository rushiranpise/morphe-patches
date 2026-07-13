package app.template.patches.minimalwidgets

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// PremiumManager.isUnlocked(Context) — reads SharedPrefs "premium_prefs"/"premium_unlocked".
// Every widget lock check funnels through this via PremiumWidgetUtils.isLocked().
val PremiumManagerIsUnlockedFingerprint = Fingerprint(
    definingClass = "Lcom/jndapp/minimal/widgets/billing/PremiumManager;",
    name = "isUnlocked",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

// BillingManager.setUnlocked — called by Play Billing after purchase acknowledged.
// Patching it to always write true ensures any re-sync path also unlocks.
val BillingManagerSetUnlockedFingerprint = Fingerprint(
    definingClass = "Lcom/jndapp/minimal/widgets/billing/PremiumManager;",
    name = "setUnlocked",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Z"),
)
