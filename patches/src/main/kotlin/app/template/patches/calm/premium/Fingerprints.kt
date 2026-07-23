package app.template.patches.calm.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

// Calm 6.99.1 — subscription gates
//
// Architecture:
//   Subscription.valid:Z (server-parsed field) is the root subscription flag.
//   UserRepository.isSubscribed() wraps it: getCurrentSubscription().getValid()
//   Called 20+ times across all UI ViewModels.
//
//   Content items (PackItem, ActionData) have server-set isUnlocked/isLocked
//   fields — Calm's backend sends these pre-configured per subscription state.
//   Patching getValid() handles the UI gates; patching isUnlocked/isLocked
//   handles the per-content access gates from the server response.
//
// SubscriptionRefreshResponse.getValid() covers the periodic server-side
// subscription validity refresh endpoint (/subscription/refresh).

// Subscription.getValid() — root boolean read by UserRepository.isSubscribed().
// Simple iget-boolean field getter. Non-obfuscated class + method.
//
// Smali (classes5):
//   .method public final getValid()Z
//       iget-boolean v0, p0, Lcom/calm/android/data/subscription/Subscription;->valid:Z
//       return v0
object SubscriptionGetValidFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "getValid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// UserRepository.isSubscribed() — convenience wrapper, called 20+ times in UI.
// Fingerprinted with stable call chain: getCurrentSubscription() → getValid().
//
// Smali (classes5):
//   .method public final isSubscribed()Z
//       invoke-virtual {p0}, UserRepository;->getCurrentSubscription()...
//       move-result-object v0
//       invoke-virtual {v0}, Subscription;->getValid()Z
//       move-result v0
//       return v0
object UserRepositoryIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/core/data/repositories/UserRepository;",
    name = "isSubscribed",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/calm/android/core/data/repositories/UserRepository;",
            name = "getCurrentSubscription",
        ),
        methodCall(
            definingClass = "Lcom/calm/android/data/subscription/Subscription;",
            name = "getValid",
        ),
    ),
)

// SubscriptionRefreshResponse.getValid() — server refresh endpoint response.
// Simple iget-boolean field getter on the API response object.
//
// Smali (classes5):
//   .method public final getValid()Z
//       iget-boolean v0, p0, ...SubscriptionRefreshResponse;->valid:Z
//       return v0
object SubscriptionRefreshGetValidFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/api/SubscriptionRefreshResponse;",
    name = "getValid",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// PackItem.isUnlocked() — per-content unlocked flag (server-set from API response).
// Controls whether a meditation/story/music item is accessible.
//
// Smali (classes5):
//   .method public final isUnlocked()Z
//       iget-boolean v0, p0, Lcom/calm/android/data/packs/PackItem;->isUnlocked:Z
//       return v0
object PackItemIsUnlockedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/packs/PackItem;",
    name = "isUnlocked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// ActionData.isLocked() — per-content locked flag (server-set).
// Used to gate individual action items in the content feed.
//
// Smali (classes5):
//   .method public final isLocked()Z
//       iget-boolean v0, p0, Lcom/calm/android/data/packs/ActionData;->isLocked:Z
//       return v0
object ActionDataIsLockedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/packs/ActionData;",
    name = "isLocked",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// Subscription.getSubscriptionPlan() — returns the subscription plan ID string
// (e.g. "calm_premium_annual", "calm_premium_monthly"). Shown in the manage
// subscription screen and used by ProductSubscriptionRepository to look up
// the display price. Returning "lifetime" spoofs the plan to Lifetime.
//
// Smali (classes5):
//   .method public final getSubscriptionPlan()Ljava/lang/String;
//       iget-object v0, p0, Subscription;->subscriptionPlan:Ljava/lang/String;
//       return-object v0
object SubscriptionGetPlanFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "getSubscriptionPlan",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// Subscription.getPlanDuration() — returns the plan duration string
// ("annual", "monthly", "6_months", "lifetime", etc.). Used by
// ManageSubscriptionViewModel.getSubscriptionDuration() / getSubscriptionPeriod()
// to look up the display string resource for the manage subscription screen.
// "lifetime" maps to manage_subscription_plan_details_duration_lifetime
// and manage_subscription_plan_period_lifetime.
//
// Smali (classes5):
//   .method public final getPlanDuration()Ljava/lang/String;
//       iget-object v0, p0, Subscription;->planDuration:Ljava/lang/String;
//       return-object v0
object SubscriptionGetPlanDurationFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "getPlanDuration",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// Subscription.isLifetime() — boolean flag for lifetime subscription.
// Not used for any access gate, but read by analytics and potentially
// displayed in the profile/subscription UI.
//
// Smali (classes5):
//   .method public final isLifetime()Z
//       iget-boolean v0, p0, Subscription;->isLifetime:Z
//       return v0
object SubscriptionIsLifetimeFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "isLifetime",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
)

// Subscription.getSubscriptionType() — maps the raw type string to Subscription.Type enum.
// SubscriptionDetails.type is populated by calling this, and IndividualSubscriptionKt
// checks type == Subscription.Type.Android to decide whether to show the family plan
// upgrade banner. Returning Gift (not Android) hides the upsell entirely.
//
// Smali (classes5):
//   .method public final getSubscriptionType()Lcom/calm/android/data/subscription/Subscription$Type;
//       iget-object v0, p0, Subscription;->type:String;
//       if-eqz v0, :cond_7   ← null check
//       invoke-virtual {v0}, String;->hashCode()I
//       sparse-switch ...     ← maps "android"/"stripe"/"gift"/etc to enum values
//       :cond_7
//       sget-object v0, Subscription$Type;->None   ← null → None
//       return-object v0
object SubscriptionGetTypeFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "getSubscriptionType",
    returnType = "Lcom/calm/android/data/subscription/Subscription\$Type;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/String;",
            name = "hashCode",
        ),
    ),
)
