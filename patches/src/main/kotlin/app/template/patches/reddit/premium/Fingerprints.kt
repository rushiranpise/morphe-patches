package app.template.patches.reddit.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ─────────────────────────────────────────────────────────────────────────────
// Reddit Premium fingerprints — verified against 2026.32.0 smali
//
// All methods are `public` only — NOT `public final`.
// Using definingClass + name is sufficient; accessFlags omitted for safety.
//
// Smali-verified:
//   MyAccount.getHasPremium()Z          → .method public getHasPremium()Z
//   MyAccount.isPremiumSubscriber()Z    → .method public isPremiumSubscriber()Z
//   MyAccount.getHasPremiumAvatarTreatment()Z → .method public getHasPremiumAvatarTreatment()Z
//   MyAccount.getHideAds()Z             → .method public final getHideAds()Z  (FINAL only on this one)
//   Account.getHasPremium()Z            → .method public getHasPremium()Z
//   Account.isPremiumSubscriber()Z      → .method public isPremiumSubscriber()Z
//   Account.getHideAds()Z               → .method public final getHideAds()Z
//   User.getHasPremium()Z               → .method public getHasPremium()Z
// ─────────────────────────────────────────────────────────────────────────────

// MyAccount.getHasPremium()Z — iget-boolean hasPremium:Z
internal object MyAccountHasPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getHasPremium",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// MyAccount.isPremiumSubscriber()Z — iget-boolean isPremiumSubscriber:Z
internal object MyAccountIsPremiumSubscriberFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "isPremiumSubscriber",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// MyAccount.getHasPremiumAvatarTreatment()Z
internal object MyAccountHasPremiumAvatarTreatmentFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getHasPremiumAvatarTreatment",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// MyAccount.getHideAds()Z — .method public final getHideAds()Z
internal object MyAccountHideAdsFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getHideAds",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// Account.getHasPremium()Z — .method public getHasPremium()Z
internal object AccountHasPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "getHasPremium",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// Account.isPremiumSubscriber()Z — .method public isPremiumSubscriber()Z
internal object AccountIsPremiumSubscriberFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "isPremiumSubscriber",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// Account.getHideAds()Z — .method public final getHideAds()Z
internal object AccountHideAdsFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/Account;",
    name = "getHideAds",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// User (protobuf class) — .method public getHasPremium()Z
// reads hasPremium_ boolean field
internal object UserHasPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/data/common/client/user/User;",
    name = "getHasPremium",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
)

// ─────────────────────────────────────────────────────────────────────────────
// PremiumPreferences — new comment highlighting + avatar treatment
//
// AccountPreferences$PremiumPreferences (classes9)
// Three boolean getters, all .method public final, .registers 1:
//   isNewCommentsHighlightingEnabled()Z → iget-boolean isNewCommentsHighlightingEnabled:Z
//   isAvatarTreatmentEnabled()Z         → iget-boolean isAvatarTreatmentEnabled:Z
//   isLinkPreviewsEnabled()Z            → iget-boolean isLinkPreviewsEnabled:Z
//
// These are stored prefs read from server response. Forcing true makes the
// UI behave as if the server granted the premium preference.
// ─────────────────────────────────────────────────────────────────────────────

private const val PREMIUM_PREFS =
    "Lcom/reddit/domain/model/AccountPreferences\$PremiumPreferences;"

// isNewCommentsHighlightingEnabled()Z — .method public final
internal object PremiumPrefsNewCommentsHighlightFingerprint : Fingerprint(
    definingClass = PREMIUM_PREFS,
    name = "isNewCommentsHighlightingEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// isAvatarTreatmentEnabled()Z — controls avatar accessories pref toggle
internal object PremiumPrefsAvatarTreatmentFingerprint : Fingerprint(
    definingClass = PREMIUM_PREFS,
    name = "isAvatarTreatmentEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// isLinkPreviewsEnabled()Z — premium link previews
internal object PremiumPrefsLinkPreviewsFingerprint : Fingerprint(
    definingClass = PREMIUM_PREFS,
    name = "isLinkPreviewsEnabled",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// ─────────────────────────────────────────────────────────────────────────────
// LinkInsightsOnboardingStatus — Performance Analytics gate
//
// MyAccount.getLinkInsightsOnboardingStatus() returns enum:
//   NOT_ONBOARDED, PENDING, ONBOARDING_COMPLETE, DOMAIN_VERIFICATION_FAILED,
//   ACCESS_REVOKED
//
// Client checks for ONBOARDING_COMPLETE to enable the analytics UI tab.
// We intercept the getter and return ONBOARDING_COMPLETE via sget-object.
// The actual analytics data still comes from the server — this only unlocks
// the UI entry point.
// ─────────────────────────────────────────────────────────────────────────────

internal object MyAccountLinkInsightsStatusFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/domain/model/MyAccount;",
    name = "getLinkInsightsOnboardingStatus",
    returnType = "Lcom/reddit/domain/model/LinkInsightsOnboardingStatus;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)
