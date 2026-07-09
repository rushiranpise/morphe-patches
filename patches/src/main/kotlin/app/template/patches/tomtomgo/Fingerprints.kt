package app.template.patches.tomtomgo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Car: qb/a$b CombineLatest combiner → always true
object HasActiveSubscriptionsCombinerFingerprint : Fingerprint(
    definingClass = "Lqb/a\$b;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("activeSubscriptionsExistenceList"),
)

// Car: sb/d$f per-provider mapper → always true
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

// Truck: Db/d default branch (a>=4) reads TRUCK_SUBSCRIPTION_PURCHASED
object TruckGateDefaultBranchFingerprint : Fingerprint(
    definingClass = "LDb/d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
)

// Truck purchased toast/upsell controller. Returning false prevents post-profile upsell toast.
object TruckPurchasedToastGateFingerprint : Fingerprint(
    definingClass = "Lv9/t;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.TRUCK_SUBSCRIPTION_PURCHASED", "com.tomtom.mobile.TRUCK_TOAST_CONSUMED"),
)

// "Are You A Truck Driver?" create-profile dialog.
object TruckCreateProfileDialogFingerprint : Fingerprint(
    definingClass = "Le9/x0;",
    name = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "LFf/d;",
    parameters = listOf("Landroid/content/Context;", "Landroid/os/Bundle;"),
)

// Truck: v9/d.a()Z showstopper gate → Purchasely paywall via C0/x.f()
object TruckShowstopperGateFingerprint : Fingerprint(
    definingClass = "Lv9/d;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_LARGE_VEHICLES_DISCOUNT_TOAST_FREE_TRUCK_SUBSCRIPTION_EXPIRATION_DATE"),
)

// Truck: e9/P0.onClick(a=1) → truck paywall via U3/a->k()
object TruckNavBannerSubscribeFingerprint : Fingerprint(
    definingClass = "Le9/P0;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("Trial timeline"),
)

// Truck: e9/l1.Y(Bundle) sets X1=true → subscription screen opens on truck tab
object SubscriptionScreenTruckTabFingerprint : Fingerprint(
    definingClass = "Le9/l1;",
    name = "Y",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf("open_at_truck_subscriptions_page"),
)

// Truck visibility: e9/C2$d reads MOBILE_REMOTE_SHOW_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE.
// Defaults to false server-side → truck NavBanner never appears → no truck switch option.
// Force true so banner always shows. Combined with C2$c (DISMISSED flag, default false),
// NOT(C2$c)=NOT(false)=true → AND(true, true) → banner visible → user can switch to truck.
object ShowLargeVehiclesBannerFingerprint : Fingerprint(
    definingClass = "Le9/C2\$d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_REMOTE_SHOW_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE"),
)

// Pc/v.onClick(View) — multi-case click handler for NavBanner message clicks.
// case a=4 → pswitch_2: triggers truck subscription screen via RxJava chain:
//   Ld/b(9, e9/D2->a) → De/b(5, e9/C2) → U3/a->k(q6/p->b, false) → e9/l1
// Pinned by unique "EvConstantSpeedConsumptionsScreen" string (pswitch_0, adjacent case).
object TruckBannerMessageClickFingerprint : Fingerprint(
    definingClass = "LPc/v;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("EvConstantSpeedConsumptionsScreen"),
)

// ai/i.invoke(Context, bi/a) — Urban Airship in-app message launcher singleton.
// Called server-side when Airship delivers the truck subscription IAM on app startup.
// Creates com.urbanairship.android.layout.ui.a and calls a() which starts ModalActivity.
// Pinned by unique "com.urbanairship.android.layout.ui.EXTRA_DISPLAY_ARGS_LOADER" in ai/h
// sibling; here we pin via definingClass + both parameter types + name.
object AirshipIAMLauncherFingerprint : Fingerprint(
    definingClass = "Lai/i;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
)

object SubscriptionTypeCarFingerprint : Fingerprint(
    definingClass = "Ltb/d;",
    name = "d",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ltb/a;"),
)

object SubscriptionTypeTruckFingerprint : Fingerprint(
    definingClass = "Ltb/d;",
    name = "f",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ltb/a;"),
)

object SubscriptionDetailsIsTruckFingerprint : Fingerprint(
    definingClass = "Ltb/d;",
    name = "b",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ltb/b;"),
)

object BillingPurchaseStarterFingerprint : Fingerprint(
    definingClass = "Lpb/a;",
    name = "k3",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "LCj/u;",
    parameters = listOf("Landroid/app/Activity;", "Ltb/b;"),
)

object CurrentSubscriptionFingerprint : Fingerprint(
    definingClass = "Le9/o2;",
    name = "J1",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ltb/a;",
    parameters = emptyList(),
)
