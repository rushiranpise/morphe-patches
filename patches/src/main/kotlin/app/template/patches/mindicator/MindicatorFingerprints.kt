package app.template.patches.mindicator

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * com.mobond.mindicator.ui.c.f0(Activity)Z
 *
 * Shows the interstitial ad. Checks static fields `d` and `c` (the loaded
 * InterstitialAd), calls InterstitialAd.show(Activity), and returns true.
 * Fingerprinted by the InterstitialAd.show() call inside it (unique to this method).
 * Returning false (0x0) suppresses all interstitial ad displays.
 */
val ShowInterstitialFingerprint = Fingerprint(
    definingClass = "Lcom/mobond/mindicator/ui/c;",
    name = "f0",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * com.mobond.mindicator.ui.c.A(Context, I)V
 *
 * Loads an interstitial ad. Checks `d` flag first and returns early if set.
 * Fingerprinted by class/name/accessFlags — the sole public static V method
 * taking (Context, int) in this class.
 * Suppressing load prevents any interstitial from ever being ready to show.
 */
val LoadInterstitialFingerprint = Fingerprint(
    definingClass = "Lcom/mobond/mindicator/ui/c;",
    name = "A",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * com.mobond.mindicator.ui.c.W(Activity, I, b, k)View
 *
 * Loads the native exit banner ad using DFP ad unit "/79488325/dfpnativeadunit_exit".
 * Fingerprinted by that unique ad-unit string.
 * Returning null skips the exit-screen native ad entirely.
 */
val LoadExitNativeAdFingerprint = Fingerprint(
    definingClass = "Lcom/mobond/mindicator/ui/c;",
    name = "W",
    returnType = "Landroid/view/View;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        string("/79488325/dfpnativeadunit_exit")
    )
)
