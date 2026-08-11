package app.template.patches.aaad.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets SubscriptionManager$SubscriptionStatus.isActive()Z
 *
 * Data class getter for the `isActive` boolean field that drives all premium gates.
 * Cascading patch: fixing this getter affects every call site in the app (hasActiveSubscription,
 * canRecoverLicense, UI state, etc.) because they all ultimately call isActive() on the returned
 * SubscriptionStatus object.
 *
 * The class name is stable (non-obfuscated Kotlin data class).
 *
 * Smali (classes3/com/legs/appsforaa/utils/SubscriptionManager$SubscriptionStatus.smali):
 *   .method public final isActive()Z
 *     .registers 2
 *     iget-boolean v0, p0, Lcom/.../SubscriptionManager$SubscriptionStatus;->isActive:Z
 *     return v0
 *   .end method
 *
 * Access flags: PUBLIC FINAL
 * Return type:  Z
 * Parameters:   none (instance method)
 */
object SubscriptionStatusIsActiveFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(),
    definingClass = "Lcom/legs/appsforaa/utils/SubscriptionManager\$SubscriptionStatus;",
    name = "isActive",
)

/**
 * Targets SubscriptionManager.hasActiveSubscription(Context, Continuation)Object
 *
 * The primary premium gate coroutine. Called throughout the app to determine whether
 * the user has an active subscription. Returns a boxed Boolean (suspend function pattern).
 *
 * Internally: calls getSubscriptionStatus() (Firebase Functions) → checks isActive().
 *
 * By patching this we skip the server round-trip entirely and return true immediately.
 * This works regardless of cache TTL or network state.
 *
 * Smali (classes3/com/legs/appsforaa/utils/SubscriptionManager.smali):
 *   .method public final hasActiveSubscription(Landroid/content/Context;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;
 *     ... coroutine machinery ...
 *     invoke-virtual {p2}, Lcom/.../SubscriptionManager$SubscriptionStatus;->isActive()Z
 *     move-result p1
 *     invoke-static {p1}, Lkotlin/coroutines/jvm/internal/Boxing;->boxBoolean(Z)Ljava/lang/Boolean;
 *     move-result-object p1
 *     return-object p1
 *   .end method
 *
 * Access flags: PUBLIC FINAL
 * Return type:  Ljava/lang/Object; (suspend fun)
 * Parameters:   Landroid/content/Context;, Lkotlin/coroutines/Continuation;
 * Filters: calls isActive() on SubscriptionStatus, then Boxing.boxBoolean()
 */
object HasActiveSubscriptionFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/Context;", "Lkotlin/coroutines/Continuation;"),
    definingClass = "Lcom/legs/appsforaa/utils/SubscriptionManager;",
    name = "hasActiveSubscription",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/legs/appsforaa/utils/SubscriptionManager\$SubscriptionStatus;",
            name = "isActive",
        ),
        methodCall(
            definingClass = "Lkotlin/coroutines/jvm/internal/Boxing;",
            name = "boxBoolean",
        ),
    ),
)

/**
 * Targets ProStatusHelper.isProUser(DataSnapshot)Z — Firebase RTDB legacy path.
 *
 * Used in the legacy license check flow (pre-Stripe, old Firebase direct payment).
 * Checks snapshot.exists(), getValue(), and map.containsKey("purchase_date").
 *
 * Smali (classes3/com/legs/appsforaa/utils/ProStatusHelper.smali):
 *   .method public static final isProUser(Lcom/google/firebase/database/DataSnapshot;)Z
 *     invoke-virtual {p0}, DataSnapshot.exists()Z
 *     ...
 *     invoke-virtual {p0}, DataSnapshot.getValue()Object
 *     ...
 *     const-string v0, "purchase_date"
 *     invoke-interface {...}, Map.containsKey(Object)Z
 *     return v0
 *
 * Access flags: PUBLIC STATIC FINAL
 * Return type: Z
 */
object IsProUserFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/google/firebase/database/DataSnapshot;"),
    definingClass = "Lcom/legs/appsforaa/utils/ProStatusHelper;",
    name = "isProUser",
    filters = listOf(
        methodCall(
            definingClass = "Lcom/google/firebase/database/DataSnapshot;",
            name = "exists",
        ),
        methodCall(
            definingClass = "Lcom/google/firebase/database/DataSnapshot;",
            name = "getValue",
        ),
    ),
)

/**
 * Targets ProStatusHelper.isProValue(Object)Z — raw Firebase value check.
 *
 * Checks if a raw value from Firebase equals Boolean.TRUE or contains "purchase_date".
 * Called from listeners that observe the RTDB node directly.
 *
 * Smali (classes3/com/legs/appsforaa/utils/ProStatusHelper.smali):
 *   .method public static final isProValue(Ljava/lang/Object;)Z
 *     sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
 *     invoke-static {p0, v0}, Intrinsics.areEqual(...)Z
 *     ...
 *     instance-of v0, p0, Ljava/lang/Boolean;
 *     ...
 *
 * Access flags: PUBLIC STATIC FINAL
 * Return type: Z
 */
object IsProValueFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    definingClass = "Lcom/legs/appsforaa/utils/ProStatusHelper;",
    name = "isProValue",
    filters = listOf(
        methodCall(
            definingClass = "Lkotlin/jvm/internal/Intrinsics;",
            name = "areEqual",
        ),
    ),
)

/**
 * MainActivityNew.access$setProUser$p(MainActivityNew, Z)V — synthetic field setter
 *
 * Kotlin-generated synthetic accessor for the private isProUser field on MainActivityNew.
 * Called from coroutine inner classes (checkProStatus$1, checkLegacyProStatus$1) when
 * Firebase RTDB or Stripe/Firebase Functions return the pro status.
 *
 * isProUser drives installApp(), installAPK(), requestAuthorizedDownload(),
 * trackDownload(), checkEligibility(), and updateProStatusUI().
 *
 * Forcing p1=true ensures all Firebase callbacks — regardless of what the server
 * returns — always set isProUser=true on the Activity.
 *
 * Smali: .method public static final synthetic access$setProUser$p(Lcom/.../MainActivityNew;Z)V
 *   iput-boolean p1, p0, Lcom/.../MainActivityNew;->isProUser:Z
 *   return-void
 */
object SetProUserFingerprint : Fingerprint(
    definingClass = "Lcom/legs/appsforaa/MainActivityNew;",
    name = "access\$setProUser\$p",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL, AccessFlags.SYNTHETIC),
    parameters = listOf("Lcom/legs/appsforaa/MainActivityNew;", "Z")
)

/**
 * MainActivityNew.access$setProStatusLoaded$p(MainActivityNew, Z)V
 *
 * Synthetic setter for isProStatusLoaded. When false, installApp() shows
 * "Loading your account status..." toast and returns without installing.
 * Forcing p1=true ensures the app never shows the loading toast.
 */
object SetProStatusLoadedFingerprint : Fingerprint(
    definingClass = "Lcom/legs/appsforaa/MainActivityNew;",
    name = "access\$setProStatusLoaded\$p",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL, AccessFlags.SYNTHETIC),
    parameters = listOf("Lcom/legs/appsforaa/MainActivityNew;", "Z")
)

/**
 * MainActivityNew.showNotEligibleDialog()V — install-blocked popup
 *
 * Shows the "Pro is required" / "not eligible" MaterialAlertDialog when
 * isProUser=false AND isEligible=false. Belt-and-suspenders: if any
 * path reaches this despite our field patches, the dialog is suppressed.
 */
object ShowNotEligibleDialogFingerprint : Fingerprint(
    definingClass = "Lcom/legs/appsforaa/MainActivityNew;",
    name = "showNotEligibleDialog",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList()
)
