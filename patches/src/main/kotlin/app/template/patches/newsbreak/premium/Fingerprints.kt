package app.template.patches.newsbreak.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ── IsPremiumFingerprint ───────────────────────────────────────────────────────
//
// Targets: PremiumHelper.isPremium()Z
//   Lcom/particlemedia/feature/premium/ui/PremiumHelper;->isPremium()Z
//
// The central premium gate for all ad-free and premium-article features.
// Called by every paywall trigger, article content lock, and feature guard.
//
// Implementation:
//   return D.b("nb_premium_user")
//     D = SharedPreferences helper singleton
//     b(String) = getBoolean(key, false)
//
// Class: PremiumHelper (Kotlin object/singleton), non-obfuscated — stable.
// Method: public static final Z, no params.
// Fingerprint: definingClass + name (both stable, non-obfuscated) +
//   string("nb_premium_user") as the SharedPrefs key read inside the body.
//
// DEX: classes8.dex
//
object IsPremiumFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/premium/ui/PremiumHelper;",
    name = "isPremium",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        string("nb_premium_user"),
    ),
)

// ── ProcessPremiumContentFingerprint ──────────────────────────────────────────
//
// Targets: UserChannelListApi.processPremiumContent(JSONObject)V
//   Lcom/particlemedia/api/channel/UserChannelListApi;->processPremiumContent(Lorg/json/JSONObject;)V
//
// Called once per app session when the channel list API response is processed.
// This method reads "show_nb_premium" from the server JSON and writes it to
// SharedPrefs via Jf.D.k(). If the server returns false (non-subscriber),
// the method skips ALL sub-processors:
//   - processSubscriptionStatus() — writes nb_premium_user
//   - processPremiumAdConfig() — writes trigger enable flags
//   - processReadingModeConfig() — writes read_mode_enabled
//
// This means isPremium() returning true is useless when show_nb_premium=false:
// all the ad/trigger/feature configs are never written so features stay hidden.
//
// Patch: inject at index 0 to force-write show_nb_premium=true and
// nb_premium_user=true before any server values are processed. The original
// method then runs and may write false values — so we also patch
// processSubscriptionStatus to return early, preventing the false overwrite.
//
// Fingerprint: private void (JSONObject) on non-obfuscated UserChannelListApi.
// Anchored by stable string "show_nb_premium" written inside the method body.
// DEX: classes7.dex
//
object ProcessPremiumContentFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/api/channel/UserChannelListApi;",
    name = "processPremiumContent",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Lorg/json/JSONObject;"),
    filters = listOf(
        string("show_nb_premium"),
    ),
)

// ── ProcessSubscriptionStatusFingerprint ──────────────────────────────────────
//
// Targets: UserChannelListApi.processSubscriptionStatus(JSONObject)V
//   Lcom/particlemedia/api/channel/UserChannelListApi;->processSubscriptionStatus(Lorg/json/JSONObject;)V
//
// Called from processPremiumContent when show_nb_premium=true. Reads the server's
// subscription_status.nb_premium JSON object and writes nb_premium_user based on
// subscriptionStatus == "paid". On a non-subscriber account this writes false.
//
// Patch: returnEarly() — we already wrote nb_premium_user=true in
// processPremiumContent's injected prologue. Preventing this method from running
// stops the server from overwriting our true with false.
//
// Fingerprint: private void (JSONObject) anchored by "subscription_status" string.
// DEX: classes7.dex
//
object ProcessSubscriptionStatusFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/api/channel/UserChannelListApi;",
    name = "processSubscriptionStatus",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf("Lorg/json/JSONObject;"),
    filters = listOf(
        string("subscription_status"),
    ),
)
//
// Targets: PremiumHelper.isPremiumOrInTrial()Z
//   Lcom/particlemedia/feature/premium/ui/PremiumHelper;->isPremiumOrInTrial()Z
//
// Higher-level gate used by article reading mode locks and trial-related UX.
// Calls isPremium() first, then inTrial() if not premium.
// Patching isPremium() is sufficient since isPremiumOrInTrial() delegates to it,
// but patching this separately ensures article trial gates also report true
// even if isPremium() is short-circuited before calling this.
//
// DEX: classes8.dex
// Access: public static final Z
//
object IsPremiumOrInTrialFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/premium/ui/PremiumHelper;",
    name = "isPremiumOrInTrial",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ── PremiumStatusGetSubscriptionStatusFingerprint ─────────────────────────────
//
// Targets: PremiumStatus.getSubscriptionStatus()String
//   Lcom/particlemedia/feature/premium/data/PremiumStatus;->getSubscriptionStatus()Ljava/lang/String;
//
// PremiumPaywallBottomSheetDialogFragment checks getSubscriptionStatus()=="paid"
// to auto-dismiss. Profile header uses it to show management vs paywall button.
// This is fetched live from the server via PremiumViewModel.loadPremiumStatus()
// — separate from SharedPrefs nb_premium_user read by isPremium().
// Patch: always return "paid".
// DEX: classes8.dex — public final, non-static, non-obfuscated class+method.
//
object PremiumStatusGetSubscriptionStatusFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/premium/data/PremiumStatus;",
    name = "getSubscriptionStatus",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ── PremiumStatusGetPaidStatusFingerprint ──────────────────────────────────────
//
// Targets: PremiumStatus.getPaidStatus()String
//   Lcom/particlemedia/feature/premium/data/PremiumStatus;->getPaidStatus()Ljava/lang/String;
//
// Also checked in billing-result handlers and UserChannelListApi. "active" = valid.
// DEX: classes8.dex — public final, non-static.
//
object PremiumStatusGetPaidStatusFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/premium/data/PremiumStatus;",
    name = "getPaidStatus",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
)

// ── SocialProfileIsPremiumUserFingerprint ──────────────────────────────────────
//
// Targets: SocialProfile.isPremiumUser()Z
//   Lcom/particlemedia/feature/content/social/bean/SocialProfile;->isPremiumUser()Z
//
// The profile header (UnifiedProfileHeaderFragment) reads socialProfile.isPremiumUser()
// to decide whether to show the "Try Premium for FREE — no ads" promo banner and
// premium avatar ring. The server populates this from JSON field "is_premium_user".
// For non-subscribers the server returns false → promo card shows.
//
// Also read by:
//   - Feed renderer (line 2517) for premium avatar badge
//   - Profile setup flow (line 838) for avatar ring display
//
// Patch: always return true — profile header hides the promo card and shows
// the premium management UI instead.
// DEX: classes8.dex — public, non-static, non-final (Java bean getter).
//
object SocialProfileIsPremiumUserFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/content/social/bean/SocialProfile;",
    name = "isPremiumUser",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)

// ── LoadPremiumStatusFingerprint ───────────────────────────────────────────────
//
// Targets: PremiumViewModel.loadPremiumStatus(String, boolean)V
//   Lcom/particlemedia/feature/premium/ui/PremiumViewModel;->loadPremiumStatus(Ljava/lang/String;Z)V
//
// Called every time the profile tab is visited and on every paywall dialog open.
// Makes a coroutine-based server API call to fetch the current PremiumStatus.
// The response handler writes the server's PremiumStatus (subscriptionStatus != "paid"
// for non-subscribers) back into PremiumViewModel._premiumStatusFlow — overwriting
// any prior patched state and causing the promo banner to reappear on tab switch.
//
// Patch: returnEarly() — skip the server fetch entirely. The premium state set
// by our getSubscriptionStatus() / getPaidStatus() patches stays in effect
// permanently since nothing overwrites it.
//
// DEX: classes8.dex
// Access: public final, non-static
// Params: [Ljava/lang/String; (scene), Z (resetPriorValue)
// Anchored by stable string "premium_paid" written in the coroutine lambda body.
//
object LoadPremiumStatusFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/premium/ui/PremiumViewModel;",
    name = "loadPremiumStatus",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;", "Z"),
)

// ── IsGuestAccountFingerprint ──────────────────────────────────────────────────
//
// Targets: ParticleAccount.isGuestAccount()Z
//   Lcom/particlemedia/feature/guide/login/account/ParticleAccount;->isGuestAccount()Z
//
// Returns true when accountType == 0 || username is empty || username starts with "HG_".
// On a re-signed APK, Google OAuth fails silently and accountType stays 0 (default),
// so the app treats the logged-in user as a guest.
//
// E1.F.n() delegates to GlobalDataCache.getInstance().getActiveAccount().isGuestAccount().
// When n() returns true, UnifiedProfileHeaderFragment.onViewCreated shows the
// "Try Premium for FREE — no ads" guestPremiumBanner at line 886-892, regardless
// of subscription status. This banner persists on every tab switch.
//
// Patch: returnEarly(false) — user is never treated as guest. The banner is hidden,
// and all getSelf() && !E1.F.n() checks return true, enabling the correct
// logged-in premium management UI.
//
// DEX: classes8.dex
// Access: public, non-static, non-final
//
object IsGuestAccountFingerprint : Fingerprint(
    definingClass = "Lcom/particlemedia/feature/guide/login/account/ParticleAccount;",
    name = "isGuestAccount",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("HG_"),
    ),
)
