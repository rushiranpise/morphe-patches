package app.template.patches.blockerhero

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * UserSubscriptionKt.isLifetime(UserSubscription)Z
 * Checks if productId == "blockerhero_lifetime".
 */
val IsLifetimeFingerprint = Fingerprint(
    definingClass = "Lcom/blockerhero/data/db/entities/UserSubscriptionKt;",
    name = "isLifetime",
    returnType = "Z",
    parameters = listOf("Lcom/blockerhero/data/db/entities/UserSubscription;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

/**
 * UserSubscriptionKt.isTakenFromGooglePlay(UserSubscription)Z
 * Checks if productId is in the Play subscription list.
 */
val IsTakenFromGooglePlayFingerprint = Fingerprint(
    definingClass = "Lcom/blockerhero/data/db/entities/UserSubscriptionKt;",
    name = "isTakenFromGooglePlay",
    returnType = "Z",
    parameters = listOf("Lcom/blockerhero/data/db/entities/UserSubscription;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

/**
 * n5/a.h(List, J) UserSubscription
 * Resolves the active subscription from DB rows by checking type and expiry.
 * Returns null when no active subscription exists — causing the paywall.
 */
val GetActiveSubscriptionFingerprint = Fingerprint(
    returnType = "Lcom/blockerhero/data/db/entities/UserSubscription;",
    parameters = listOf("Ljava/util/List;", "J"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("inapp", "subs", "manual_subs")
)

/**
 * Y3/b.l()Z — returns userId > 0 (logged-in check).
 * Used as the primary gate before showing paywall/features.
 */
val IsLoggedInFingerprint = Fingerprint(
    definingClass = "LY3/b;",
    name = "l",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * Y3/b.j()I — returns MyApplication.v.intValue() (the user ID).
 * Used by k()Z to check if user is in the premium user-ID set.
 */
val GetUserIdFingerprint = Fingerprint(
    definingClass = "LY3/b;",
    name = "j",
    returnType = "I",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL)
)

/**
 * E4/a.p(Object)Object — shows GenericResponse.getMessage() as toast on API error (e.g. 401).
 * Coroutine resume handler for HTTP error responses.
 */
val ApiErrorToastFingerprint = Fingerprint(
    definingClass = "LE4/a;",
    name = "p",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)

/**
 * E4/b.p(Object)Object — shows Throwable.getMessage() as toast on network exception.
 * Coroutine resume handler for network errors.
 */
val NetworkErrorToastFingerprint = Fingerprint(
    definingClass = "LE4/b;",
    name = "p",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
)

/**
 * p5/f.Q(Context, String)V — static toast helper used for all app error/notification toasts.
 * Nopping this silences unauthenticated API error toasts (from T0/Y0, E4/a, E4/b etc.)
 * shown as a result of background API calls failing due to null Bearer token.
 */
val ToastHelperFingerprint = Fingerprint(
    definingClass = "Lp5/f;",
    name = "Q",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC)
)
