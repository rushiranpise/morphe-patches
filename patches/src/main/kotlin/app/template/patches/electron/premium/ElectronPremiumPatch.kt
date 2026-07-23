package app.template.patches.electron.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ELECTRON_COMPATIBILITY

// Electron v3.0.3 — RevenueCat entitlement bypass (two-tier premium).
//
// cl5 is the premium ViewModel with 3 separate StateFlow pairs:
//
//   cl5.a / cl5.b  (same wx4 object) = isPremium
//     Written by cl5.c(CustomerInfo) based on entitlement IDs.
//     Read by: ad loader (r5), all premium badge UI, settings screen.
//
//   cl5.c / cl5.d  (same wx4 object) = isLifetimePurchase
//     Written by cl5.c(CustomerInfo) based on product "electron_plus_1.99".
//     Read by: tv0 / dw (feature button tap handlers).
//     When FALSE → adds qs3 (paywall destination) to nav backstack → shows paywall.
//     When TRUE  → calls cl5.b() which sets cl5.e = TRUE.
//
//   cl5.e / cl5.f  (same wx4 object) = showPaywallOverlay
//     Written by cl5.b() (set TRUE) and z13 dismiss handlers (set FALSE).
//     Read by: jz (Compose layer that renders the paywall modal).
//
// Our first patch only wrote TRUE to cl5.a — ads gone, premium badge shown, but
// features gated by the cl5.d (isLifetime) check still showed the paywall on tap.
//
// Fix: inject at index 0 of cl5.c(CustomerInfo) to write TRUE into BOTH:
//   • cl5.a (isPremium)      → subscription features + no ads
//   • cl5.c (isLifetime)     → lifetime-gated feature buttons work without paywall
// Then return-void to skip all entitlement null-checks.
//
// Register plan (.registers 10 → p0=this:cl5, p1=CustomerInfo, v0-v7 locals):
//   v0 = cl5.a StateFlow   v1 = null   v2 = Boolean.TRUE   v3 = cl5.c StateFlow

@Suppress("unused")
val electronPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Electron premium features",
) {
    compatibleWith(ELECTRON_COMPATIBILITY)

    execute {
        EntitlementObserverFingerprint.method.addInstructions(
            0,
            """
                const/4 v1, 0x0
                sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;

                iget-object v0, p0, Lcl5;->a:Lwx4;
                invoke-virtual { v0 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                invoke-virtual { v0, v1, v2 }, Lwx4;->l(Ljava/lang/Object;Ljava/lang/Object;)Z

                iget-object v3, p0, Lcl5;->c:Lwx4;
                invoke-virtual { v3 }, Ljava/lang/Object;->getClass()Ljava/lang/Class;
                invoke-virtual { v3, v1, v2 }, Lwx4;->l(Ljava/lang/Object;Ljava/lang/Object;)Z

                return-void
            """,
        )
    }
}
