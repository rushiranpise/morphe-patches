package app.template.patches.casetracker

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.CASETRACKER_COMPATIBILITY

/**
 * Removes ads from Case Tracker — Immigration (com.saldous.casetracker).
 *
 * ## How ad gating works
 *
 * The app serves three ad formats:
 *   - AppOpen ads   (com.saldous.casetracker.ads.appOpen)
 *   - Interstitial  (com.saldous.casetracker.ads.InterstitialDisplayKt)
 *   - Native / banner (various presentation classes)
 *
 * All ad-show paths consult:
 *   c.z0  ← SharedPreferences["HAS_PURCHASED_REMOVE_ADS"]
 *
 * When z0 == true the ad load/show is skipped entirely.
 *
 * Additionally, ads/w.a() is the central handler that sets z0 upon a
 * successful "remove ads" in-app purchase.  We also patch it so the
 * in-memory statics are immediately written true before any billing
 * callback logic, as a belt-and-suspenders measure.
 *
 * ## Strategy
 *
 * Primary: replace c.a(String, boolean)Z to always return true so that
 *   c.z0 is set to true on DataManager init (along with A0 and B0).
 *
 * Secondary: prepend ads/w.a() to force all three purchase statics true
 *   and write all three SharedPrefs keys, so even if billing fires
 *   with an Unsubscribed event it cannot reset the flags.
 *
 * Ad SDKs present: AppLovin MAX, AdMob, IronSource.
 */
@Suppress("unused")
val caseTrackerRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes interstitial, app-open and native ads from Case Tracker — Immigration.",
    default = false
) {
    compatibleWith(CASETRACKER_COMPATIBILITY)

    execute {
        // Primary: force c.a() → true so z0 is true on init
        SharedPrefGetBooleanFingerprint
            .match(classDefBy(SharedPrefGetBooleanFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    return v0
                    """.trimIndent()
                )
            }

        // Secondary: prepend ads/w.a() so that any billing Unsubscribed
        // callback cannot overwrite the flags back to false.
        // We sput true into all three statics and return early.
        AdsWriterFingerprint
            .match(classDefBy(AdsWriterFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(
                    0,
                    """
                    const/4 v0, 0x1
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->z0:Z
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->A0:Z
                    sput-boolean v0, Lcom/saldous/casetracker/data/c;->B0:Z
                    return-void
                    """.trimIndent()
                )
            }
    }
}