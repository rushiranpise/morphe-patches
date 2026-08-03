package app.template.patches.blockingsites.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.template.patches.shared.Constants.BLOCKING_SITES_COMPATIBILITY

@Suppress("unused")
val blockingSitesUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in Blocking Sites.",
    default = true,
) {
    compatibleWith(BLOCKING_SITES_COMPATIBILITY)

    execute {
        // com.pairip.application.Application.attachBaseContext calls this
        // before the app's own MyApplication starts. Returning immediately
        // preserves normal application initialization.
        PairipCheckLicenseFingerprint.method.addInstructions(0, "return-void")

        // Force both the umbrella check AND the underlying preference read
        // to always report true. Several call sites (MainActivity's nav
        // header, DashFragment, AdvancedSettingsActivity,
        // AccountabilityPartnerActivity) OR in SubscriptionState.isSubscribed()
        // directly and bypass SubscriptionHelper.isSubscribed() entirely, so
        // patching only that method left the real gate
        // (AppPreferenceUseCase.getIsUserSubscribed()) unaffected — verified
        // by an unpurchased-account runtime test still showing the paywall.
        SubscriptionHelperIsSubscribedFingerprint.method.addInstructions(
            0, "const/4 v0, 0x1\nreturn v0"
        )
        AppPreferenceGetIsUserSubscribedFingerprint.method.addInstructions(
            0, "const/4 v0, 0x1\nreturn v0"
        )

        // Also force the stored product ID to the lifetime SKU so plan-label
        // screens (e.g. ProfileActivity's "Current plan" text) match a known
        // product ID and render a real plan name instead of falling through
        // to "no active plan" text.
        AppPreferenceGetCurrentSubscriptionIdFingerprint.method.addInstructions(
            0, "const-string v0, \"lifetime_access\"\nreturn-object v0"
        )

        // Deepest choke point: the "Premium" bottom-nav screen
        // (BlockerPremiumActivity.updateSubscriptionUi /
        // BlockPPremiumFragment) reads SubscriptionUiState directly instead
        // of AppPreferenceUseCase/SubscriptionHelper. Force it to report as
        // subscribed with the lifetime product ID so that screen renders
        // showLifTimeSubscriptionUI() instead of the Buy Now purchase flow.
        SubscriptionUiStateIsSubscribedFingerprint.method.addInstructions(
            0, "const/4 v0, 0x1\nreturn v0"
        )
        SubscriptionUiStateGetProductIdFingerprint.method.addInstructions(
            0, "const-string v0, \"lifetime_access\"\nreturn-object v0"
        )
    }
}
