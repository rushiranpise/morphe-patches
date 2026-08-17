package app.template.patches.calm.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall

// ── Subscription validity ─────────────────────────────────────────────────────

object SubscriptionGetValidFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/subscription/Subscription;",
    name = "getValid",
    returnType = "Z",
    parameters = emptyList(),
)

object SubscriptionRefreshResponseGetValidFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/api/SubscriptionRefreshResponse;",
    name = "getValid",
    returnType = "Z",
    parameters = emptyList(),
)

// ── User subscription checks ──────────────────────────────────────────────────

object UserRepositoryIsSubscribedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/core/data/repositories/UserRepository;",
    name = "isSubscribed",
    returnType = "Z",
    parameters = emptyList(),
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

object UserRepositoryGetHasValidSubscriptionFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/core/data/repositories/UserRepository;",
    name = "getHasValidSubscription",
    returnType = "Z",
    parameters = emptyList(),
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

// ── Free access gates ─────────────────────────────────────────────────────────

// Fixes "Unlock everything" in-player banner via SessionPlayerViewModel.shouldShowBanner().
object IsGuideInFreeAccessLimitFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/core/data/repositories/ProgramRepository;",
    name = "isGuideInFreeAccessLimit",
    returnType = "Z",
    parameters = listOf("Lcom/calm/android/data/Guide;"),
)

// Fixes videos not playing. Returns Single<Boolean> — must emit Single.just(false).
// Cannot use returnEarly: when isGuideInFreeAccessLimit→false, lambda emits !isFree()
// which is true for premium content, still routing taps to upsell.
object IsFreeAccessSessionLockedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/core/data/repositories/ProgramRepository;",
    name = "isFreeAccessSessionLocked",
    returnType = "Lio/reactivex/Single;",
    parameters = listOf("Ljava/lang/String;", "Z"),
)

// ── Home feed upsell banner ───────────────────────────────────────────────────

// PackViewHolderFactory.getViewHolder() — intercept BannerPromo ViewHolder creation.
//
// The "Unlock everything with Calm Premium" card is PackItem.Type.Upsell, served
// unconditionally by the server. No local filter removes it.
//
// Previous approach: patch BannerPromoViewHolder.onBindView() — caused VerifyError.
// The existing method reuses p1 (parameter PackCell) as PackCellBannerPromoBinding
// via iget-object. addInstructionsToEnd shifts instruction offsets, breaking the
// verifier's type tracking for p1 at offset 0x1D.
//
// Fix: patch getViewHolder() instead. This method has .registers 15 and direct
// access to userRepository field. We use methodCall(BannerPromoViewHolder.<init>)
// as the filter anchor, then addInstructionsWithLabels BEFORE that index to inject:
//   isSubscribed() → if true → return null
// When getViewHolder returns null, the adapter skips the card entirely.
//
// Note: getViewHolder return type is PackCellViewHolder (non-null in Kotlin) but
// the Java bytecode accepts null returns — the adapter's createViewHolder handles null.
object PackViewHolderFactoryGetViewHolderFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/packs/PackViewHolderFactory;",
    name = "getViewHolder",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/calm/android/packs/viewholders/BannerPromoViewHolder;",
            name = "<init>",
        ),
    ),
)

// ── Content lock flags ────────────────────────────────────────────────────────

// Simple field getter. PackItem.isLocked() does NOT exist in v6.101.1.
object PackItemIsUnlockedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/packs/PackItem;",
    name = "isUnlocked",
    returnType = "Z",
    parameters = emptyList(),
)

// Simple field getter set via ActionData.Builder.setIsLocked().
object ActionDataIsLockedFingerprint : Fingerprint(
    definingClass = "Lcom/calm/android/data/packs/ActionData;",
    name = "isLocked",
    returnType = "Z",
    parameters = emptyList(),
)
