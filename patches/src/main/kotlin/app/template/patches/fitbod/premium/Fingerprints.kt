package app.template.patches.fitbod.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// IsUserSubscribedProvider.isUserSubscribed():Z — central subscription gate.
// Checks IS_FORCE_SUBSCRIPTION SharedPref, then SUBSCRIPTION_PRODUCT_ID, then remote flag FORCE_SUBSCRIPTION.
// Called by FeatureAccessStateProvider.isUserUnlocked() and SettingsFragmentViewModel.
object IsUserSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/fitbod/fitbod/billing/IsUserSubscribedProvider;",
    name = "isUserSubscribed",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
)
