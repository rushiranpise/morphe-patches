package app.template.patches.callrecorder

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

/**
 * Private instance method — the core time-range check.
 * Returns true when currentTimeMillis() is within [premiumStartTimestamp, premiumEndTimestamp].
 * Called only by the public static wrapper below.
 */
val RewardIsActiveInstanceFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/database/PremiumRewardState;",
    name = "rewardIsActive",
    parameters = listOf(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

/**
 * Public static wrapper — the call site used everywhere in the app.
 * Reads PremiumRewardState from encrypted SharedPreferences then delegates
 * to the private instance method above.
 */
val RewardIsActiveStaticFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/database/PremiumRewardState;",
    name = "rewardIsActive",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z"
)

/**
 * Checks whether the user has hit the daily rewarded-video view cap.
 * Returning false lifts the ad-watch limit entirely.
 */
val IsMaxViewsLimitReachedFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/database/RewardedVideoViews;",
    name = "isMaxViewsLimitReached",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z"
)

/**
 * Static gate that decides whether a finished recording should be discarded
 * (e.g. because the free recording limit was exceeded).
 * Returning false keeps every recording.
 */
val ShouldDiscardStaticFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/service/recordings/CallRecording;",
    name = "shouldDiscard",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/catalinagroup/callrecorder/database/Preferences;",
        "J"
    ),
    returnType = "Z"
)

/**
 * Returns a Pair describing the promo offer to show, or null if none.
 * Returning null suppresses the premium promo banner entirely.
 */
val ShouldShowPromoFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/database/PremiumPromo;",
    name = "shouldShowPromo",
    parameters = listOf(
        "Landroid/content/Context;",
        "Lcom/catalinagroup/callrecorder/database/Preferences;"
    ),
    returnType = "Landroid/util/Pair;"
)

/**
 * Controls whether the premium tutorial/offer screen is shown to the user.
 * Returning false skips the tutorial entirely.
 */
val ShouldShowTutorialFingerprint = Fingerprint(
    definingClass = "Lcom/catalinagroup/callrecorder/database/TutorialPremiumOffer;",
    name = "shouldShow",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Z"
)

// ---------------------------------------------------------------------------
// IAB Billing manager — obfuscated class (e4/n at runtime)
//
// g()Z body (hasPurchaseTokenOrProductId):
//   invoke-virtual {p0}, Le4/n;->m()Lr1/a;
//   iget-object v1, v0, Lr1/a;->d:Ljava/lang/Object;   <- Long token
//   check-cast v1, Ljava/lang/Long;
//   if-nez v1, :cond_1
//   iget-object v0, v0, Lr1/a;->e:Ljava/lang/Object;   <- String productId
//   check-cast v0, Ljava/lang/String;
//   ...
//
// h()Z body (hasPurchaseToken):
//   invoke-virtual {p0}, Le4/n;->m()Lr1/a;
//   iget-object v0, v0, Lr1/a;->d:Ljava/lang/Object;   <- Long token only
//   check-cast v0, Ljava/lang/Long;
//   ...
//
// Discriminator: g() reads BOTH ->d and ->e; h() reads ONLY ->d.
// We match each via fieldAccess() filters on the r1/a field reads.
// ---------------------------------------------------------------------------

/**
 * IAB purchase check — hasPurchaseTokenOrProductId (obfuscated: g()Z).
 *
 * Fingerprinted by the two sequential iget-object reads:
 *   1. Lr1/a;->d:Ljava/lang/Object;  (Long purchaseToken)
 *   2. Lr1/a;->e:Ljava/lang/Object;  (String productId)
 * Only g() reads both fields; h() reads only d.
 */
object IabHasPurchaseFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        fieldAccess(
            definingClass = "Lr1/a;",
            name = "d",
            type = "Ljava/lang/Object;",
            opcode = Opcode.IGET_OBJECT
        ),
        fieldAccess(
            definingClass = "Lr1/a;",
            name = "e",
            type = "Ljava/lang/Object;",
            opcode = Opcode.IGET_OBJECT
        )
    )
)

/**
 * IAB purchase token check — hasPurchaseToken (obfuscated: h()Z).
 *
 * Fingerprinted by the single iget-object read of r1/a->d.
 * Absence of r1/a->e distinguishes it from IabHasPurchaseFingerprint;
 * Morphe picks the best match, and h() contains only the d filter.
 */
object IabHasPurchaseTokenFingerprint : Fingerprint(
    returnType = "Z",
    parameters = listOf(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        fieldAccess(
            definingClass = "Lr1/a;",
            name = "d",
            type = "Ljava/lang/Object;",
            opcode = Opcode.IGET_OBJECT
        )
    )
)