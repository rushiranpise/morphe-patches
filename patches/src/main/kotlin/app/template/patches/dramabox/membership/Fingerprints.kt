package app.template.patches.dramabox.membership

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Target: SplashLockActivity.onCreate(Bundle)V ──────────────────────────
//
// com.storymatrix.drama.lock.SplashLockActivity
//
// This is the LAUNCHER activity (declared in AndroidManifest as
// com.storymatrix.drama.lock.SplashLockActivity). It shows the
// "Ingrese su codigo de activacion" screen on every cold launch.
//
// Gate logic in onCreate:
//   SharedPreferences("drama_lock_prefs").getBoolean("is_verified", false)
//   → true  → call launchMain() → finish() (proceeds normally)
//   → false → inflate the activation UI (TextView, EditText, Button)
//
// On ACTIVAR press → NetworkThread → GET http://spotube.panelmx.online/api/check_code.php?code=<input>
//   → response contains "\"ok\":true" → onSuccess() → writes is_verified=true + activation_code → launchMain()
//   → otherwise → shows error message (not found / expired / already_used / blocked)
//
// Patch: insert invoke-direct launchMain() + return-void at index 1 (immediately
// after super.onCreate). The super call at index 0 is preserved — Activity
// lifecycle requires it. launchMain() itself calls startActivity(MainActivity)
// + finish(), so the lock screen is never rendered and no network call is made.
//
// Fingerprint strategy:
//   - Class name is non-obfuscated → definingClass is safe
//   - String "Ingrese su codigo de activacion" is unique across all 71 297 smali
//     files (1 hit) — anchors the class unambiguously in case definingClass
//     somehow resolves to a renamed variant in a future update
//   - method: PROTECTED, return V, params (Bundle)
//   - filter: string("drama_lock_prefs") — unique to this class (2 hits in the
//     file, both in this class, and filtered further by the PROTECTED flag
//     which is only on onCreate)
//
// DEX: classes2 — smali verified against versionCode 581.
object SplashLockOnCreateFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PROTECTED),
    parameters = listOf("Landroid/os/Bundle;"),
    definingClass = "Lcom/storymatrix/drama/lock/SplashLockActivity;",
    filters = listOf(
        string("drama_lock_prefs"),
        string("is_verified"),
    ),
)

// ═══════════════════════════════════════════════════════════════════════════
// DramaBox 5.8.1 — VIP / Membership Fingerprints
// Package: com.storymatrix.drama  |  DEX: classes8.dex (switch), classes5.dex (Z6)
//
// Architecture summary:
//   Server response → BasicUserInfo.isVip() [already returns 1; dead iget]
//   → x9/switch.lO(BasicUserInfo, Z)  [user info sync entry point]
//     → x9/switch.RT(BasicUserInfo)   [propagates VIP fields to DataStore]
//       → Z6/dramabox.E7(Z)           [DataStore VIP write]
//   Runtime gate:
//     Z6/dramabox.s2()Z               [DataStore VIP read → UI + content guards]
//   Billing success:
//     x9/switch.l1(DramaPurchase)     [called after Google Play Billing success]
//       → Z6/dramabox.E7(Z)           [same DataStore write path]
//
//   Note: BasicUserInfo.isVip() and DramaPurchase.isVip() both return
//   hardcoded Integer(1) — dead iget-object then const/4 v0, 0x1.
//   This is an R8 constant-propagation artifact from their server always
//   returning isVip=1 for tested accounts. The DataStore write (E7) and
//   read (s2) are still exercised by the billing path and need patching
//   for fresh installs / logged-out states.
// ═══════════════════════════════════════════════════════════════════════════

// ─── CLASS ANCHORS ──────────────────────────────────────────────────────────

// Anchors the obfuscated x9/switch class (classes8.dex).
// IO(BootStrpModel)V is the bootstrap handler called once at startup with the
// server's full user state. String "bootStrpModel" is globally unique (1 hit).
// All three x9/switch patch targets (lO, RT, l1) are resolved within this class.
object SwitchClassAnchorFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/lib/data/BootStrpModel;"),
    filters = listOf(
        string("bootStrpModel"),
        methodCall(definingClass = "Lcom/lib/data/BootStrpModel;", name = "getUser"),
    ),
)

// Anchors the obfuscated Z6/dramabox class (classes5.dex) — the global
// DataStore singleton holding all runtime user state (VIP flag, coins, etc.).
// String "FirstOpenToday" is a DataStore key present only in this class (1 hit).
object GlobalStateClassAnchorFingerprint : Fingerprint(
    filters = listOf(
        string("FirstOpenToday"),
    ),
)

// ─── x9/switch PATCH TARGETS ────────────────────────────────────────────────

// x9/switch.lO(BasicUserInfo, Z)V — user-info sync entry point.
// Called from login flow (NumberVerifyActivity, a9/IO$O), logout (MainVM),
// and settings (SettingActivity). Internally calls RT(BasicUserInfo).
// Return-void early so VIP propagation is never triggered from stale/logged-out
// state. String "info" appears in the checkNotNullParameter call; combined with
// the BasicUserInfo parameter type the classFingerprint narrows to 1 method.
object UserInfoSyncFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/lib/data/BasicUserInfo;", "Z"),
    filters = listOf(
        string("info"),
        methodCall(
            definingClass = "Lkotlin/jvm/internal/Intrinsics;",
            name = "checkNotNullParameter",
        ),
    ),
    classFingerprint = SwitchClassAnchorFingerprint,
)

// x9/switch.RT(BasicUserInfo)V — the core VIP propagation method.
// Reads isVip/memberType/coins from BasicUserInfo and writes them to the
// Z6/dramabox DataStore via E7(Z). Also fires RxBus VIP-change events.
// String "basicUserInfo" is globally unique (1 hit). Patching this to
// return-void is sufficient to freeze the DataStore VIP state as-set.
object VipPropagationFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/lib/data/BasicUserInfo;"),
    filters = listOf(
        string("basicUserInfo"),
        methodCall(
            definingClass = "Lkotlin/jvm/internal/Intrinsics;",
            name = "checkNotNullParameter",
        ),
    ),
    classFingerprint = SwitchClassAnchorFingerprint,
)

// x9/switch.l1(DramaPurchase)V — billing success handler.
// Called after Google Play Billing acknowledges a purchase (subscription or
// one-time). Reads DramaPurchase.isVip() [also hardcoded 1] and writes VIP
// state to DataStore via Z6/dramabox.E7(). We intercept here to guarantee
// the VIP state write happens positively on every billing callback, and to
// block any non-VIP write path (e.g. coin-only purchases that set isVip=0).
// String "purchase" (checkNotNullParameter arg) + DramaPurchase param type
// within classFingerprint narrows to exactly 1 method.
object BillingSuccessFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/lib/recharge/bean/DramaPurchase;"),
    filters = listOf(
        string("purchase"),
        methodCall(
            definingClass = "Lkotlin/jvm/internal/Intrinsics;",
            name = "checkNotNullParameter",
        ),
    ),
    classFingerprint = SwitchClassAnchorFingerprint,
)

// ─── Z6/dramabox PATCH TARGETS ──────────────────────────────────────────────

// Z6/dramabox.s2()Z — runtime VIP state getter (DataStore read).
// Read by x9/switch.l1 (billing), membership UI (MembershipActivityV2/V3),
// and the content paywall decision logic. Returning true unconditionally
// makes every runtime VIP check succeed regardless of DataStore state.
// Unique by return type + access flags within class; resolved via
// GlobalStateClassAnchorFingerprint.
object VipStateGetterFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        // DataStore read pattern: sget-object dramaboxapp → aget-object KProperty[] →
        // invoke dramaboxapp → check-cast Boolean → booleanValue
        methodCall(
            definingClass = "Lcom/lib/datastore/dramaboxapp;",
            name = "dramabox",
        ),
    ),
    classFingerprint = GlobalStateClassAnchorFingerprint,
)

// Z6/dramabox.E7(Z)V — runtime VIP state setter (DataStore write).
// Called from x9/switch.RT and x9/switch.l1. We inject true at entry so
// even if the upstream propagation methods are left intact, the DataStore
// always stores VIP=true.
// Resolved within GlobalStateClassAnchorFingerprint class by param + return.
object VipStateSetterFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/lib/datastore/dramaboxapp;",
            name = "dramaboxapp",
        ),
    ),
    classFingerprint = GlobalStateClassAnchorFingerprint,
)
