package app.template.patches.accpro.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── PROTECTION NOTE ────────────────────────────────────────────────────────────
// App Cache Cleaner Pro uses DexGuard obfuscation: all string constants are
// encrypted at class-load time. string() filters are UNSAFE here — do NOT add
// any string() filters in this file.
// Fingerprints MUST use only: definingClass, name, returnType, accessFlags,
// parameters, methodCall(), fieldAccess(), opcode().
// ───────────────────────────────────────────────────────────────────────────────

// ── PAIRIP ─────────────────────────────────────────────────────────────────────
// Targets com.pairip.licensecheck.LicenseClient.checkLicense(Context)V
//
// Called in com.pairip.application.Application.attachBaseContext() before super,
// which makes it the very first thing that runs in the process.
// Triggers Play Store license verification; on failure shows a blocking paywall
// activity (LicenseActivity) and schedules app exit.
//
// Fingerprint: stable non-obfuscated class + method name (Pairip library,
// never obfuscated). The "Skipping license check in isolated process." log
// string is present in plaintext (it's a Pairip-internal log, not app string).
// However, DexGuard may encrypt it — anchor on the non-string filters only:
// definingClass + name + parameters is already fully unique.
//
// DEX: classes — smali verified against versionCode 240005229.
internal val PairipCheckLicenseFingerprint = Fingerprint(
    definingClass = "Lcom/pairip/licensecheck/LicenseClient;",
    name = "checkLicense",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
)

// ── IS PRO GETTER ───────────────────────────────────────────────────────────────
// Targets dpx.鷋()Z — the central "isPro" boolean getter.
//
// dpx is the Application base class (com.a0soft.gphone.acc.free.MainApp extends
// dpx; com.pairip.application.Application extends MainApp extends dpx).
// All premium feature gates call this method on the Application instance.
//
// Logic (smali, classes.dex, versionCode 240005229, .registers 2):
//   sget-object p0, Lipi;->鷋:Lipi;         // load singleton
//   sget-object p0, Lipi;->鷋:Lipi;         // load singleton (duplicated by R8)
//   iget-object v0, p0, Lipi;->鱹:AtomicBoolean  // load "settled" flag
//   invoke-virtual {v0}, AtomicBoolean;->get()Z   // is billing settled?
//   if-eqz v0, :cond_13                           // if NOT settled → return true (fail-open)
//   iget-object p0, p0, Lipi;->س:AtomicBoolean   // load "isPurchased" flag
//   invoke-virtual {p0}, AtomicBoolean;->get()Z   // return isPurchased
//   :cond_13 → const/4 p0, 0x1; return p0         // fail-open path
//
// returnEarly(true) short-circuits before any ipi field is read.
//
// Fingerprint: definingClass + name makes this unique (only one 鷋()Z on Ldpx;).
// Confirmed by full DEX scan — no other method has this exact combination.
// The fieldAccess filter on Lipi;->鷋 (the sget-object) adds an extra anchor
// confirming this is the billing method, not any other 鷋()Z in the codebase.
//
// Access flags: PUBLIC FINAL (no STATIC).
// DEX: classes — smali verified against versionCode 240005229.
internal val IsProFingerprint = Fingerprint(
    definingClass = "Ldpx;",
    name = "鷋",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        // sget-object Lipi;->鷋:Lipi — the singleton load. Stable obfuscated
        // field name on stable obfuscated class; both are app-owned and won't
        // change independently (name is reused by DexGuard across classes but
        // definingClass pins us to ipi specifically).
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lipi;",
            name = "鷋",
        ),
        // AtomicBoolean.get() — first call checks the "settled" flag.
        // Not a DexGuard-encrypted string; java.util SDK class, always stable.
        methodCall(
            definingClass = "Ljava/util/concurrent/atomic/AtomicBoolean;",
            name = "get",
        ),
    ),
)

// ── AD-FREE / REWARD CHECK ─────────────────────────────────────────────────────
// Targets yo.衊(Z)Z — the "isAdFreeOrRewarded" gate.
//
// Called across the app to decide whether to show interstitial ads and reward
// prompts. Returns true when: (a) billing settled && purchased, OR (b) billing
// settled && ad-free reward active, OR (c) billing NOT settled (fail-open),
// OR (d) launch count < 10.
//
// Logic summary (smali, classes.dex, versionCode 240005229):
//   reads ipi.鱹 (settled), ipi.డ (purchased2), ipi.罏 (adFreeReward)
//   if settled && !purchased2 && !reward → check launch count threshold
//   returnEarly(true) bypasses all branches.
//
// Fingerprint: definingClass + name + parameters is unique.
// Filter: sget-object on Lipi;->鷋 confirms this is the billing method
// (same singleton access pattern as IsProFingerprint).
//
// Access flags: PUBLIC STATIC (no FINAL, no instance receiver).
// DEX: classes — smali verified against versionCode 240005229.
internal val IsAdFreeFingerprint = Fingerprint(
    definingClass = "Lyo;",
    name = "衊",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Z"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lipi;",
            name = "鷋",
        ),
        methodCall(
            definingClass = "Ljava/util/concurrent/atomic/AtomicBoolean;",
            name = "get",
        ),
    ),
)

// ── APP-OWN KILL METHOD ─────────────────────────────────────────────────────────
// Targets l20.糷()V — the process self-destruct method.
//
// Called from 5 sites: gdo.handleMessage(what=0x65), s6, p6 (×2), dpx, l20 itself.
// Body: Process.killProcess(myPid()) + System.exit(10)
// Wrapped in try-catchall that swallows exceptions and returns-void — nop'ing
// the body is safe and prevents all call sites from killing the process.
//
// Triggered by yo.onDestroy() via Handler.sendEmptyMessageDelayed(0x65, 1852ms)
// when the APK signature hash does not match -0x41eb0f28 (the original cert).
//
// Fingerprint: definingClass + name + parameters is fully unique.
// Filter: Process.killProcess() confirms this is the kill method, not any
// other 糷()V overload in the codebase.
//
// Access flags: PUBLIC STATIC.
// DEX: classes — smali verified against versionCode 240005229.
internal val SelfDestructFingerprint = Fingerprint(
    definingClass = "Ll20;",
    name = "糷",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/os/Process;",
            name = "killProcess",
        ),
    ),
)

// ── SIGNATURE CHECK + IPI STATE SETTER ─────────────────────────────────────────
// Targets cyq.罏(Context, List)V — the app's own signature verification and
// purchase state management method.
//
// This is the central billing update handler called from 5 sites (dbt, dul, bwk,
// cqd ×2, izf ×2). It:
//   1. Checks APK signature hash against hardcoded -0x41eb0f28 (original cert)
//   2. If mismatch → sets ipi.س=false (not purchased), ipi.డ=false
//   3. If match + valid purchase token → sets ipi.س=true, ipi.డ=true
//   4. Sets ipi.鱹=true (billing settled) unconditionally
//
// returnEarly() prevents the signature check and blocks the ipi state being
// written to false, keeping the fail-open state (ipi.鱹==false → dpx.鷋()=true).
//
// NOTE: since our IsProFingerprint already patches dpx.鷋()Z to always return
// true, this patch is belt-and-suspenders, but it prevents ipi.鱹 from being
// set to true which would shift dpx.鷋()Z into the "check ipi.س" branch.
//
// Fingerprint: definingClass + name + parameters is fully unique.
// Filter: AtomicBoolean.set() confirms this is the ipi state setter.
// DexGuard note: no string() filters — all strings in cyq are encrypted.
//
// Access flags: PUBLIC STATIC.
// DEX: classes — smali verified against versionCode 240005229.
internal val SignatureCheckFingerprint = Fingerprint(
    definingClass = "Lcyq;",
    name = "罏",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(
        "Landroid/content/Context;",
        "Ljava/util/List;",
    ),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/util/concurrent/atomic/AtomicBoolean;",
            name = "set",
        ),
    ),
)

// ── UPGRADE DIALOG LAUNCHER ─────────────────────────────────────────────────────
// Targets LicWnd.衊(Context)V — the "Upgrade to full version" dialog launcher.
//
// Called from 13 sites across the app (wo, yo, gdg, hus, fiz, ffk, fpz, bfd ×2,
// cbe, cqd, and others). Each call site checks its own local condition and calls
// this directly WITHOUT checking dpx.鷋()Z — so the isPro gate does not block it.
//
// Body: creates Intent(Context, LicWnd.class) + startActivity → shows paywall.
// returnEarly() blocks all 13 upgrade dialog triggers with a single nop.
//
// Fingerprint: definingClass + name + params is fully unique — no filters needed.
// LicWnd is a non-obfuscated named Activity class, stable under DexGuard.
//
// Access flags: PUBLIC STATIC.
// DEX: classes — smali verified against versionCode 240005229.
internal val LicWndShowFingerprint = Fingerprint(
    definingClass = "Lcom/a0soft/gphone/acc/wnd/LicWnd;",
    name = "衊",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
)

// ── UPGRADE ALERTDIALOG BUILDER ─────────────────────────────────────────────────
// Targets dvr.鷋(Context)V — the "Upgrade to full version" AlertDialog builder.
//
// THIS is the actual dialog shown at cold-start (not LicWnd.衊 which starts an Activity).
// Called from 8 sites (dq, hnf, gdg, ffk, cbe, cqd, dwa and others).
// The most critical caller: dq.invokeSuspend() — a Kotlin coroutine that runs on startup,
// reads ipi.鱹 (billing settled) and ipi.డ (isPurchased2), and if settled && !purchased
// calls dvr.鷋(Context) to show the AlertDialog with Yes/No buttons.
//
// dvr.鷋 builds an AlertDialog via pu0 (obfuscated AlertDialog.Builder) with:
//   - title/message resource IDs (0x7f120153, 0x7f120152)
//   - negative button (0x7f1200b0) → null listener (dismiss)
//   - positive button (0x7f1200eb) → bfd onClick (calls LicWnd.衊 if ipi.鱹==true)
// Then calls pu0.籓() → show().
//
// returnEarly() blocks the builder entirely — no AlertDialog is ever created.
// This is the root cause of the "Upgrade to full version" dialog on cold start.
//
// Fingerprint: definingClass + name + params is fully unique for methods
// (the dvr.鷋 static field [I is a field, ignored by method fingerprinting).
// DexGuard note: no string() filters — all strings in dvr are encrypted.
//
// Access flags: PUBLIC STATIC.
// DEX: classes — smali verified against versionCode 240005229.
internal val UpgradeDialogFingerprint = Fingerprint(
    definingClass = "Ldvr;",
    name = "鷋",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
)

// ── UPGRADE DIALOGFRAGMENT LAUNCHER ────────────────────────────────────────────
// Targets yo.攦()Z — shows the "Upgrade to full version" DialogFragment (ox).
//
// THIS is the actual cold-start trigger. Call chain:
//   dul.轠(Z)V
//     → Le9.罏(Context)Z / Le9.鱹(Context)Z  [condition checks]
//     → yo.攦()Z   ← HERE
//     → ox DialogFragment (getSupportFragmentManager + show)
//     → ox.ض(Bundle)Dialog builds AlertDialog with resource IDs 0x7f120153/0x7f120152
//
// Two call sites: dul.smali:5173, deb.smali:430
// This is completely separate from dvr.鷋(Context)V which builds an AlertDialog
// directly. Both must be nop'd.
//
// returnEarly() returns false (Z) before getSupportFragmentManager is called.
//
// Fingerprint: definingClass + name + params is unique (only one 攦()Z in yo).
// Filter: Activity.isFinishing() is the very first call in the method — stable,
// non-obfuscated SDK method, confirms this is the fragment-show method.
// DexGuard note: const-string "ox" is encrypted — no string() filter used.
//
// Access flags: PUBLIC FINAL (not static).
// DEX: classes — smali verified against versionCode 240005229.
internal val UpgradeDialogFragmentFingerprint = Fingerprint(
    definingClass = "Lyo;",
    name = "攦",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        methodCall(
            definingClass = "Landroid/app/Activity;",
            name = "isFinishing",
        ),
    ),
)
