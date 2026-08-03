package app.template.patches.tomtomgo

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// ── Car subscription ─────────────────────────────────────────────────────────

// CombineLatest combiner that aggregates per-provider hasActiveSubscriptions booleans.
// Returning Boolean.TRUE makes the car subscription appear active regardless of server state.
object HasActiveSubscriptionsCombinerFingerprint : Fingerprint(
    definingClass = "Lqb/a\$b;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("activeSubscriptionsExistenceList"),
)

// Per-provider mapper that checks Collection.isEmpty() on the subscription list.
// Returning Boolean.TRUE bypasses the isEmpty() check for each provider.
object HasActiveSubscriptionsMapperFingerprint : Fingerprint(
    definingClass = "Lsb/d\$f;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(definingClass = "Ljava/util/Collection;", name = "isEmpty"),
    ),
)

// ── Truck subscription ───────────────────────────────────────────────────────

// Default branch of the Db/d state machine (state >= 4).
// Reads TRUCK_SUBSCRIPTION_PURCHASED preference; returning TRUE bypasses the check.
object TruckGateDefaultBranchFingerprint : Fingerprint(
    definingClass = "LDb/d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
)

// Controls post-profile upsell toast display; returning false suppresses the toast.
object TruckPurchasedToastGateFingerprint : Fingerprint(
    definingClass = "Lv9/t;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.TRUCK_SUBSCRIPTION_PURCHASED", "com.tomtom.mobile.TRUCK_TOAST_CONSUMED"),
)

// "Are You A Truck Driver?" create-profile dialog; returning null suppresses it.
object TruckCreateProfileDialogFingerprint : Fingerprint(
    definingClass = "Le9/x0;",
    name = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "LFf/d;",
    parameters = listOf("Landroid/content/Context;", "Landroid/os/Bundle;"),
)

// Showstopper gate that triggers the Purchasely paywall; returning false disables it.
object TruckShowstopperGateFingerprint : Fingerprint(
    definingClass = "Lv9/d;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_LARGE_VEHICLES_DISCOUNT_TOAST_FREE_TRUCK_SUBSCRIPTION_EXPIRATION_DATE"),
)

// NavBanner subscribe button click handler; case a==1 triggers the truck paywall.
object TruckNavBannerSubscribeFingerprint : Fingerprint(
    definingClass = "Le9/P0;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("Trial timeline"),
)

// Opens the subscription screen scrolled to the truck tab.
// Returning void at offset 0 prevents the truck tab from ever being set as default.
// v3.6.320: moved from Le9/l1; to Le9/p1; (same method name Y, same param Bundle).
object SubscriptionScreenTruckTabFingerprint : Fingerprint(
    definingClass = "Le9/p1;",
    name = "Y",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    strings = listOf("open_at_truck_subscriptions_page"),
)

// Remote flag that controls truck NavBanner visibility in vehicle profile.
// Defaults to false server-side; returning TRUE forces the banner visible.
// v3.6.320: class renamed from Le9/C2$d; to Le9/J2$d;
object ShowLargeVehiclesBannerFingerprint : Fingerprint(
    definingClass = "Le9/J2\$d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_REMOTE_SHOW_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE"),
)

// NavBanner message click handler; case a==4 triggers the truck subscription screen.
object TruckBannerMessageClickFingerprint : Fingerprint(
    definingClass = "LPc/v;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("EvConstantSpeedConsumptionsScreen"),
)

// Urban Airship in-app message launcher; suppressing it prevents the server-triggered
// truck subscription modal from appearing on startup.
object AirshipIAMLauncherFingerprint : Fingerprint(
    definingClass = "Lai/i;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;", "Ljava/lang/Object;"),
)

// ── Subscription type helpers ─────────────────────────────────────────────────

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

// Starts a Google Play billing flow for a subscription.
// Returning Result.success(true) short-circuits the IAP flow without launching Play.
// v3.6.320: method renamed from k3 to l3 (same params Activity + tb/b, same return CJu).
object BillingPurchaseStarterFingerprint : Fingerprint(
    definingClass = "Lpb/a;",
    name = "l3",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "LCj/u;",
    parameters = listOf("Landroid/app/Activity;", "Ltb/b;"),
)

// Returns the current active subscription (tb/a) from the subscription store (X9/r).
// v3.6.320: moved from Le9/o2;->J1 to Le9/u2;->J1, store field G1:LX9/p → H1:LX9/r.
object CurrentSubscriptionFingerprint : Fingerprint(
    definingClass = "Le9/u2;",
    name = "J1",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ltb/a;",
    parameters = emptyList(),
)
