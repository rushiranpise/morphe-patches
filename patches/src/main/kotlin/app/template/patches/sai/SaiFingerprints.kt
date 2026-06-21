package app.template.patches.sai

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * LicenseClient.processResponse(int, Bundle)V
 *
 * Handles Play Store license check response.
 * Code 0=ok, 2=paywall, error=dialog. Prepending return-void
 * exits before any paywall/error logic runs.
 */
val ProcessResponseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "processResponse",
    parameters = listOf("I", "Landroid/os/Bundle;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

/**
 * LicenseClient.performLocalInstallerCheck()Z
 *
 * Returns true only for Play Store installs. Forcing true
 * satisfies the origin gate regardless of install source.
 */
val PerformLocalInstallerCheckFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "performLocalInstallerCheck",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE)
)

/**
 * io3.emit(Object, nu0) → Object
 *
 * Flow operator that observes the MainActivity subscription StateFlow.
 * DEFAULT case (Boolean): always shows SubscriptionFragment on first
 * emission regardless of subscription state.
 * PSWITCH_0 (AdsConsentState): initialises Google Ads SDK when
 * canRequestAds=true.
 *
 * Returning COROUTINE_SUSPENDED (fq6.a) at entry blocks BOTH paths —
 * no paywall auto-show and no ads SDK init.
 */
val SubscriptionObserverEmitFingerprint = Fingerprint(
    definingClass = "Lio3;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnu0;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("SubscriptionFragment")
)

/**
 * a7.a()Z — canRequestAds gate
 *
 * Reads the AdsConsentState MutableStateFlow and returns b7.a
 * (canRequestAds). Used in InstallerFragment.h0() and c0() to gate
 * banner ad loading. Forcing false prevents all ad requests.
 */
val CanRequestAdsFingerprint = Fingerprint(
    definingClass = "La7;",
    name = "a",
    parameters = emptyList(),
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)

/**
 * hw2.onClick(View)V — install dialog button handler
 *
 * hw2 is a dual-purpose OnClickListener with two constructors:
 *   constructor(InstallerFragment, ma): sets a=0 → DEFAULT → opens SubscriptionFragment (paywall upsell)
 *   constructor(ma, InstallerFragment): sets a=1 → pswitch_0 → opens install permission settings
 *
 * The DEFAULT case (a=0) must be suppressed (just dismiss the dialog, no paywall).
 * The pswitch_0 case (a=1) must be preserved (opens MANAGE_UNKNOWN_APP_SOURCES settings).
 *
 * InstallerFragment.m0() is NOT patched — it correctly shows the "allow unknown sources"
 * permission dialog which is needed for the install flow to work.
 */
val InstallerUpsellClickFingerprint = Fingerprint(
    definingClass = "Lhw2;",
    name = "onClick",
    parameters = listOf("Landroid/view/View;"),
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("SubscriptionFragment")
)

/**
 * pw2.emit(Object, nu0) → Object
 *
 * Flow operator wired to InstallerFragment's subscription StateFlow.
 * DEFAULT case (Boolean subscription state):
 *   - Sets InstallerFragment.g0 (isPro flag) = Boolean value
 *   - If true (subscribed): calls g0()/f0() to clear ads, destroys AdView,
 *     calls n0(false) to show main content ConstraintLayout (VISIBLE)
 *   - If false (not subscribed): calls c0() to load banner ads
 *
 * Fix: force the Boolean to true before iput-boolean g0, so the
 * subscribed path always runs. La7.a() (already patched to false)
 * ensures c0() won't load ads even if called by other switch cases.
 *
 * Instruction index 8 (iput-boolean p1, InstallerFragment.g0) is
 * preceded by: iget(0) const(1) sget(2) iget(3) packed-switch(4)
 * check-cast(5) invoke(6) move-result(7) → insert const/4 p1,1 at 8.
 */
val InstallerSubscriptionObserverFingerprint = Fingerprint(
    definingClass = "Lpw2;",
    name = "emit",
    parameters = listOf("Ljava/lang/Object;", "Lnu0;"),
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)
