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
// v3.6.320: class changed from Lv9/t; → LH9/u0; (still onClick, same truck pref strings).
object TruckPurchasedToastGateFingerprint : Fingerprint(
    definingClass = "LH9/u0;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.TRUCK_SUBSCRIPTION_PURCHASED", "com.tomtom.mobile.TRUCK_TOAST_CONSUMED"),
)

// "Are You A Truck Driver?" create-profile dialog; returning null suppresses it.
// e9/x0 now hosts an incompatible-voice-locale dialog — class replaced here.
// New target: e9/U0.Z which is a dialog that creates a truck profile.
// Note: e9/U0 is also the NavBanner subscribe button handler (onClick). Both share the class.
// We target the Z method specifically for the dialog suppression.
// v3.6.320: was Le9/x0;->Z — now use Le9/x0; STILL (verified: e9/x0 has Z returning LFf/d;,
// but the content changed to MobileIncompatibleVoiceLocaleDialog — this fingerprint DROPS).
// Drop TruckCreateProfileDialog patch: e9/x0 is no longer the truck dialog; no stable
// replacement found without obfuscated class dependency. Suppressed truck paths 2,3,5 already
// prevent the dialog from being reachable.
// (fingerprint intentionally removed — see UnlockPremiumPatch.kt)

// Showstopper gate that triggers the Purchasely paywall; returning false disables it.
object TruckShowstopperGateFingerprint : Fingerprint(
    definingClass = "Lv9/d;",
    name = "a",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_LARGE_VEHICLES_DISCOUNT_TOAST_FREE_TRUCK_SUBSCRIPTION_EXPIRATION_DATE"),
)

// NavBanner subscribe button click handler (case a==1 triggers truck paywall).
// v3.6.320: class changed from Le9/P0; → Le9/U0; (Le9/P0; is now MapMigrationConfirmationDialog).
// Le9/U0; has onClick + field a:I + "Trial timeline" string. Case 1 in packed-switch → paywall.
object TruckNavBannerSubscribeFingerprint : Fingerprint(
    definingClass = "Le9/U0;",
    name = "onClick",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Landroid/view/View;"),
    strings = listOf("Trial timeline"),
)

// Opens the subscription screen scrolled to the truck tab.
// Returning void at offset 0 prevents the truck tab flag from ever being written.
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
object ShowLargeVehiclesBannerFingerprint : Fingerprint(
    definingClass = "Le9/J2\$d;",
    name = "invoke",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    strings = listOf("com.tomtom.mobile.MOBILE_REMOTE_SHOW_LARGE_VEHICLES_BANNER_IN_VEHICLE_PROFILE"),
)

// NavBanner message click handler; case a==4 triggers the truck subscription screen.
// v3.6.320: class changed from LPc/v; → Le9/T0;
// ("EvConstantSpeedConsumptionsScreen" string moved from Pc/v to e9/T0).
object TruckBannerMessageClickFingerprint : Fingerprint(
    definingClass = "Le9/T0;",
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
object BillingPurchaseStarterFingerprint : Fingerprint(
    definingClass = "Lpb/a;",
    name = "l3",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "LCj/u;",
    parameters = listOf("Landroid/app/Activity;", "Ltb/b;"),
)

// Returns the current active subscription (tb/a) from the subscription store (X9/r).
object CurrentSubscriptionFingerprint : Fingerprint(
    definingClass = "Le9/u2;",
    name = "J1",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ltb/a;",
    parameters = emptyList(),
)
