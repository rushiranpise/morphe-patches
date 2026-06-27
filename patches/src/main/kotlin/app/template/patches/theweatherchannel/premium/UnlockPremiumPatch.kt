package app.template.patches.theweatherchannel.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.blockerhero.clearBody
import app.template.patches.blockerhero.ensureRegisters
import app.template.patches.shared.Constants

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks The Weather Channel's Premium and Premium Pro subscription tiers. " +
        "Enables the ad-free experience, extended 15-day hourly forecast, real-feel temperature, " +
        "air quality index, minute-by-minute precipitation, severe weather notifications, and " +
        "radar overlays gated behind the subscription paywall.",
) {
    compatibleWith(Constants.THEWEATHERCHANNEL_COMPATIBILITY)

    execute {
        // Architecture:
        //
        // All subscription gates in TWC read from a single source of truth:
        //   AppState.getPremiumSubscriptions()ImmutableList<String>
        //
        // This list is populated at runtime by SubscriptionStateStore from the RevenueCat/Play
        // Billing flow: if subscription.isActive() → listOf(product.getName()) else listOf("null_state").
        //
        // Product name strings that gate features:
        //   "ad-free"     → adFree(AppState)Z      → suppresses ad SDK init
        //   "premium"     → premium(AppState)Z      → gates forecast, real-feel, AQI, etc.
        //   "premium-pro" → premiumPro(AppState)Z   → gates premium-pro-only features
        //
        // Fix: patch AppState.getPremiumSubscriptions() to always return a persistent list
        // containing all three tier strings. AppState is a non-obfuscated Kotlin data class.

        val appStateClass = mutableClassDefByOrNull("Lcom/weather/corgikit/context/AppState;")
            ?: throw PatchException(
                "TWC: Lcom/weather/corgikit/context/AppState; not found. Package layout changed.",
            )

        val getPremiumSubscriptions = appStateClass.methods.firstOrNull { method ->
            method.name == "getPremiumSubscriptions" &&
                method.returnType == "Lkotlinx/collections/immutable/ImmutableList;" &&
                method.parameterTypes.isEmpty()
        } ?: throw PatchException(
            "TWC: AppState.getPremiumSubscriptions()ImmutableList not found. Getter renamed.",
        )

        getPremiumSubscriptions.clearBody()
        getPremiumSubscriptions.ensureRegisters(5)
        getPremiumSubscriptions.addInstructions(
            0,
            """
                const/4 v0, 0x3
                new-array v0, v0, [Ljava/lang/Object;
                const-string v1, "ad-free"
                const/4 v2, 0x0
                aput-object v1, v0, v2
                const-string v1, "premium"
                const/4 v2, 0x1
                aput-object v1, v0, v2
                const-string v1, "premium-pro"
                const/4 v2, 0x2
                aput-object v1, v0, v2
                invoke-static {v0}, Lkotlinx/collections/immutable/ExtensionsKt;->persistentListOf([Ljava/lang/Object;)Lkotlinx/collections/immutable/PersistentList;
                move-result-object v0
                return-object v0
            """,
        )
    }
}
