package app.template.patches.crimeradar.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Matches SubscriptionState.isActive()Z.
 *
 * Smali (classes/com/particlemedia/feature/subscription/SubscriptionState.smali):
 *   .method public final isActive()Z
 *     .registers 2
 *     iget-boolean v0, p0, Lcom/particlemedia/feature/subscription/SubscriptionState;->isActive:Z
 *     return v0
 *   .end method
 *
 * SubscriptionStateStore.buildState() reads "subscription_crimeradar_is_active"
 * from SharedPreferences and stores it into SubscriptionState.isActive.
 * All premium feature gates call subscriptionState.isActive() directly:
 *   - Map layers: ParkSafe, PowerOutage, GasStation premium overlays
 *   - Replay playback paywall after login sync
 *   - Profile / inbox / settings premium UI sections
 *
 * Returning true unconditionally means every SubscriptionState instance —
 * whether fresh from SharedPrefs or server-synced — reports active subscription.
 *
 * Stable signals: non-obfuscated class + method name. No filters needed —
 * the combination of definingClass + name + PUBLIC FINAL Z no-params is unique.
 */
internal object IsActiveFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/subscription/SubscriptionState;",
    name = "isActive",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

/**
 * Matches RadarMapSubscriptionGateway.freeLimit()I.
 *
 * Smali (classes/com/particlemedia/feature/subscription/RadarMapSubscriptionGateway.smali):
 *   .method public freeLimit()I
 *     .registers 2
 *     const/4 v0, 0x1       ← free tier: 1 followed location
 *     return v0
 *   .end method
 *
 * MapFollowedLocationActivity calls gateway.freeLimit() and passes the result
 * as the limit parameter to MapFollowedLocationAdapter.updateList3(), which caps
 * how many saved locations the adapter renders/allows adding.
 * premiumLimit() returns 0xa (10). Returning 10 here removes the free-tier cap.
 *
 * Stable signals: non-obfuscated class + method name. PUBLIC I no-params is unique.
 */
internal object FreeLimitFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/subscription/RadarMapSubscriptionGateway;",
    name = "freeLimit",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "I",
    parameters = emptyList(),
)

/**
 * Matches AdsPremiumProvider$Companion$from$1.isAdFreeEnabled()Z.
 *
 * Smali (classes/com/particlemedia/ads/AdsPremiumProvider$Companion$from$1.smali):
 *   .method public isAdFreeEnabled()Z
 *     iget-object v0, p0, ...->a:Lkh/a;          ← obfuscated Function0
 *     invoke-interface {v0}, Lkh/a;->invoke()Ljava/lang/Object;
 *     move-result-object v0
 *     check-cast v0, Ljava/lang/Boolean;
 *     invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z
 *     return v0
 *
 * Same pattern as Zests but Function0 interface type is obfuscated (kh/a vs
 * kotlin/jvm/functions/Function0). Non-obfuscated class name and Boolean.booleanValue
 * call make this fingerprint stable.
 */
internal object IsAdFreeEnabledFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/ads/AdsPremiumProvider\$Companion\$from\$1;",
    name = "isAdFreeEnabled",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Z",
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
)

/**
 * Matches GLocationList.getLimit()I.
 *
 * Smali (classes9/com/particlemedia/feature/map/data/GLocationList.smali):
 *   .method public final getLimit()I
 *     iget v0, p0, Lcom/particlemedia/feature/map/data/GLocationList;->limit:I
 *     return v0
 *
 * This is the SERVER-SET limit. updateSavedListFull(GLocationList) calls getLimit()
 * and posts the result into GlobalLocationRepository.savedLimit LiveData, which the
 * Activity reads FIRST (before falling back to freeLimit()). The server sends limit=1
 * for free users, bypassing any freeLimit() patch. Returning Integer.MAX_VALUE here
 * ensures the server value can never restrict the adapter slot count.
 */
internal object GLocationListGetLimitFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/map/data/GLocationList;",
    name = "getLimit",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "I",
    parameters = emptyList(),
)

/**
 * Matches GlobalLocationRepository.onAccountChanged()V.
 *
 * Smali (classes/com/particlemedia/feature/map/GlobalLocationRepository.smali):
 *   .method public final onAccountChanged()V
 *     sget-object v0, ...->savedLimit:Landroidx/lifecycle/c0;
 *     const/4 v1, 0x1
 *     invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
 *     invoke-virtual {v0, v1}, .../W;->setValue(Ljava/lang/Object;)V
 *     ...
 *
 * Resets savedLimit LiveData to 1 on every account change (login/logout).
 * Skipping this reset prevents the limit from being re-applied after sign-in.
 * string("mapSaveLocationList") is a unique stable filter inside this method.
 */
internal object OnAccountChangedFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/map/GlobalLocationRepository;",
    name = "onAccountChanged",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = emptyList(),
    filters = listOf(
        string("mapSaveLocationList"),
    ),
)
