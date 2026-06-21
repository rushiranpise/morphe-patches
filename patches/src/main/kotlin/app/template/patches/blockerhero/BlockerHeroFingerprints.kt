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

/**
 * j4/v.p(Object)Object — coroutine that sends the "set accountability partner" API request.
 * State 1 (index 30) is just before the actual HTTP call is dispatched.
 * v1 = partner type string (B field), v3 = j4/w ViewModel, v3.h = Y3/b prefs manager.
 * Injecting Y3/b.s("KEY_ACCOUNTABILITY_PARTNER", v1) at index 30 persists the selection
 * locally before the API call, so it survives 401 failures.
 */
val SetAccountabilityPartnerFingerprint = Fingerprint(
    definingClass = "Lj4/v;",
    name = "p",
    returnType = "Ljava/lang/Object;",
    parameters = listOf("Ljava/lang/Object;"),
    strings = listOf("KEY_ACCOUNTABILITY_PARTNER")
)

/**
 * k4/j.a(j4/w, O7/a, h0/s, I)V — launches the remove accountability partner flow.
 * p0 = j4/w ViewModel, p0.h = Y3/b prefs manager.
 * Inject at index 0 to clear KEY_ACCOUNTABILITY_PARTNER locally before the API call,
 * so remove works even when the API returns 401.
 */
val RemoveAccountabilityPartnerFingerprint = Fingerprint(
    definingClass = "Lk4/j;",
    name = "a",
    returnType = "V",
    parameters = listOf("Lj4/w;", "LO7/a;", "Lh0/s;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

/**
 * j4/w.l(String, O7/a, k4/b, h0/s, I)V — remove accountability partner ViewModel method.
 * Called from A3/f.a() when user taps "Remove Accountability Partner".
 * At instr 10: iget-object v1, v6, Lj4/w;->h:LY3/b; — v1 = Y3/b prefs manager.
 * Inject at index 11 (right after iget) to clear KEY_ACCOUNTABILITY_PARTNER locally
 * before the API remove call, so remove works even on 401.
 */
val RemovePartnerViewModelFingerprint = Fingerprint(
    definingClass = "Lj4/w;",
    name = "l",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "LO7/a;", "Lk4/b;", "Lh0/s;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("accountabilityPartnerViewModel")
)

/**
 * l4/d.b(String, j4/w, k4/b, O7/a, h0/s, I)V
 * The confirm handler called when user taps Confirm on the remove partner dialog.
 * p1 = j4/w ViewModel (v2 after instr 1 prologue copy).
 * Inject at index 2 to call j4/w.m("", "KEY_ACCOUNTABILITY_PARTNER") —
 * clears the pref locally AND emits to StateFlow to refresh UI.
 */
val RemovePartnerConfirmFingerprint = Fingerprint(
    definingClass = "Ll4/d;",
    name = "b",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Lj4/w;", "Lk4/b;", "LO7/a;", "Lh0/s;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    strings = listOf("blockingViewModel", "accountabilityPartnerViewModel")
)

/**
 * w4/b.b(String, O7/a, Y3/b, t0/p, h0/s, I)V
 * The final remove partner API call handler — called from H/r.i() state 0xc.
 * p2 = Y3/b prefs manager (passed directly as argument).
 * Inject at index 1: Y3/b.s("KEY_ACCOUNTABILITY_PARTNER", "") clears pref
 * immediately when the final cancel/remove API is dispatched.
 * v3, v4 are free at index 1 (first use: const v3 at instr 2).
 */
val FinalRemovePartnerFingerprint = Fingerprint(
    definingClass = "Lw4/b;",
    name = "b",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "LO7/a;", "LY3/b;", "Lt0/p;", "Lh0/s;", "I"),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL)
)

/**
 * j4/e.a()Object — the reset timer coroutine.
 * State 0 (instr 2-8): v1=j4/w, v0=Y3/b, calls Y3/b.n() then returns SUSPENDED.
 * Inject at instr 7 (after Y3/b.n() at instr 6, before return at instr 7):
 * clear KEY_ACCOUNTABILITY_PARTNER so UI shows "no partner" after reset.
 * v0=Y3/b still valid at instr 7. v2, v3 free (not used in state 0).
 */
val ResetTimerCoroutineFingerprint = Fingerprint(
    definingClass = "Lj4/e;",
    name = "a",
    returnType = "Ljava/lang/Object;",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    strings = listOf("this\$0")
)

/**
 * T3/a.c(String,I,String,FocusTime,UserBlockedItem,Y3/b,r4/q,t4/r,O7/a,O7/a,h0/s,I,I)V
 * Shows the "Are you sure you want to cancel this request?" dialog for TIME_DELAY partner.
 * At instr 386: invoke Y3/b.o(String) — removes _PARTNER_REQ_TIME_MILLIS pref.
 * v8=Y3/b at instr 385 (move-object/from16 v8, v46). v4, v5 free as scratch.
 * Inject at instr 387 to also clear KEY_ACCOUNTABILITY_PARTNER.
 */
val CancelTimeDelayFingerprint = Fingerprint(
    definingClass = "LT3/a;",
    name = "c",
    returnType = "V",
    strings = listOf("KEY_TIME_DELAY_PARTNER_TIME_IN_HOURS")
)

/**
 * k4/j.<clinit>()V — static initializer that builds the partner type options list.
 * k4/j.a = List from k4/a.z (all 3 enum values: FRIEND, MYSELF, TIME_DELAY).
 * Replace with a list containing only MYSELF (k4/a.w) and TIME_DELAY (k4/a.x)
 * to disable the FRIEND option (email won't send on patched APK).
 */
val PartnerTypeListFingerprint = Fingerprint(
    definingClass = "Lk4/j;",
    name = "<clinit>",
    returnType = "V",
    parameters = emptyList()
)
