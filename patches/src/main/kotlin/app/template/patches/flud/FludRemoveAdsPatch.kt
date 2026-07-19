package app.template.patches.flud

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FLUD_COMPATIBILITY

private fun dismissActivity() =
    "invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V\n" +
    "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\n" +
    "return-void"

/**
 * Flud (com.delphicoder.flud) is ad-supported. The Pro version is a separate paid APK
 * (com.delphicoder.flud.pro) — there is no in-app billing in the free version.
 *
 * Ads served: AdMob interstitial (ca-app-pub-8308447967239879/5050482671) mediated via
 * AppLovin MAX, plus an AdMob banner. Both load through s13 (AdViewHelper):
 *   s13.a() — loads interstitial + banner
 *   s13.b() — decides when to show interstitial and calls InterstitialAd.show()
 */
@Suppress("unused")
val fludRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes all ads in app",
    default = true,
) {
    compatibleWith(FLUD_COMPATIBILITY)

    execute {
        // Layer 1: Prevent all ad loading — stops interstitial and banner from ever loading.
        runCatching {
            AdLoaderFingerprint.method.addInstructions(0, "return-void")
        }

        // Layer 2: Prevent interstitial from being shown — catches any already-loaded ad.
        runCatching {
            InterstitialTriggerFingerprint.method.addInstructions(
                0,
                """
                    const/4 v0, 0x0
                    return-object v0
                """,
            )
        }

        // Layer 3: PairIP LicenseClient.checkLicense(Context) -> no-op.
        runCatching {
            LicenseClientFingerprint.method.addInstructions(0, "return-void")
        }

        // Layer 4: LicenseActivity.onCreate -> finish immediately.
        runCatching {
            LicenseActivityOnCreateFingerprint.method.addInstructions(0, dismissActivity())
        }
    }
}
