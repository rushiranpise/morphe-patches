package app.template.patches.tomtomgo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

object HasActiveSubscriptionsCombinerFingerprint : Fingerprint(
    definingClass = "Lqb/a\$b;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("activeSubscriptionsExistenceList"),
)

object HasActiveSubscriptionsMapperFingerprint : Fingerprint(
    definingClass = "Lsb/d\$f;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/Collection;",
            name = "isEmpty",
        ),
    ),
)

// Db/d default branch (a>=4) reads TRUCK_SUBSCRIPTION_PURCHASED.
// Gates LargeVehiclesDiscountDialogScreen condition chain in e9/r0.
object TruckGateDefaultBranchFingerprint : Fingerprint(
    definingClass = "LDb/d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
)

// v9/d.a()Z — showstopper gate, triggers Purchasely truck paywall via C0/x.f().
object TruckShowstopperGateFingerprint : Fingerprint(
    definingClass = "Lv9/d;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_LARGE_VEHICLES_DISCOUNT_TOAST_FREE_TRUCK_SUBSCRIPTION_EXPIRATION_DATE"),
)

// e9/C2$c.invoke() — reads MOBILE_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE_DISMISSED.
// Used as: Db/d(a=1, Function0=C2$c) → pswitch_2 = NOT(C2$c()).
// Banner shows when: SHOW_LARGE_VEHICLES=true AND NOT(C2$c())=true (NOT dismissed).
// Returning TRUE → NOT(TRUE)=FALSE → banner hidden → truck icon unlocked in vehicle profile.
object TruckBannerDismissedFingerprint : Fingerprint(
    definingClass = "Le9/C2\$c;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE_DISMISSED"),
)

// e9/P0.onClick() — multi-case click handler for NavBanner buttons.
// Case a=1 → pswitch_2: opens Purchasely truck subscription paywall via U3/a->k().
// This is set as NavBanner$a->C callback on the truck upgrade banner in e9/C2.
// Prepend: if a==1 return-void so truck subscribe button does nothing.
object TruckNavBannerSubscribeFingerprint : Fingerprint(
    definingClass = "Le9/P0;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("Trial timeline"),
)
