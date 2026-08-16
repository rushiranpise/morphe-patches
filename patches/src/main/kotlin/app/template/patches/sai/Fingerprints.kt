package app.template.patches.sai

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── HasActiveSubscriptionFingerprint ─────────────────────────────────────────
// dj9.b(Context):Z — synchronous SharedPreferences read of "has_active_subscription".
// Gate used in non-reactive code paths throughout the app.
//
// Smali: classes4/dj9.smali method b
//   .method public static b(Landroid/content/Context;)Z
//     const-string v0, "has_active_subscription"
//     invoke-interface SharedPreferences->getBoolean(String;Z)Z
//
// Stable anchors: string literal (developer key, never obfuscated) +
//   SharedPreferences.getBoolean SDK call + PUBLIC STATIC (Context)Z.
internal val HasActiveSubscriptionFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("has_active_subscription"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences;",
            name = "getBoolean",
        ),
    ),
)

// ── SetActiveSubscriptionFingerprint ─────────────────────────────────────────
// dj9.c(Context, Z, Lcu1;):Object — coroutine that writes "has_active_subscription"
// to SharedPreferences and emits into the DataStore preferences Flow.
//
// Smali: classes4/dj9.smali method c
//   p0=Ldj9; p1=Context p2=Z(boolean) p3=Lcu1;(continuation)
//   const-string v4, "has_active_subscription"
//   invoke-interface Editor->putBoolean(String;Z)V
//
// Prepend "const/4 p2, 0x1" to force the boolean to true before any write.
// Stable anchors: same string + Editor.putBoolean SDK call.
internal val SetActiveSubscriptionFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("has_active_subscription"),
        methodCall(
            definingClass = "Landroid/content/SharedPreferences\$Editor;",
            name = "putBoolean",
        ),
    ),
)

// ── OnboardingPaywallPageFingerprint ──────────────────────────────────────────
// fv6.<init>(Application):V — the OnboardingViewModel constructor that builds the
// page list. It unconditionally adds the paywall slide (mu6.d) as the last page
// regardless of subscription state. We remove that add() call so the paywall page
// is never shown.
//
// Smali: classes4/fv6.smali, method <init>(Application)V
//   ...
//   sget-object v8, Lmu6;->d:Lmu6;          ← paywall page enum  (was vu6 in v2.3.2)
//   [lu6 construction with resource IDs]
//   invoke-direct/range {v7..v12}, Llu6;-><init>(Lmu6;IIILjava/util/List;)V
//   invoke-virtual {p1, v7}, Lqc5;->add(Object)Z  ← THIS is what we remove  (was xc5)
//
// Patch: find the instructionMatches index of the mu6.d sget, then find the
// next qc5.add() call after it and replace it with "const/4 v0, 0x0".
//
// v2.3.2 → v2.3.3: ov6 → fv6, vu6 → mu6, xc5 → qc5, uu6 → lu6 (all R8 renames).
// Fingerprint updated to track new obfuscated names.
//
// Stable anchors:
//   • fieldAccess(SGET_OBJECT, Lmu6;->d) — paywall page enum constant, developer-defined
//   • methodCall(Lqc5;->add) — the page-list add() immediately after
//   • CONSTRUCTOR, PUBLIC, (Application)V
internal val OnboardingPaywallPageFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("Landroid/app/Application;"),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            definingClass = "Lmu6;",
            name = "d",
        ),
        methodCall(
            definingClass = "Lqc5;",
            name = "add",
        ),
    ),
)

// ── PaywallEmitFingerprint ────────────────────────────────────────────────────
// sn5.emit(Object, Leu1;):Object — the FlowCollector on sr0.d that drives the
// paywall overlay inside MainActivity (shown when returning from onboarding or
// when subscription lapses mid-session).
//
// Smali: classes4/sn5.smali, method emit (.registers 5)
//   p0=Lsn5; p1=Object(Boolean) p2=Leu1;
//   Default case (key=3, not in packed-switch 0/1/2): subscription boolean check.
//     TRUE  → MainActivity.I.setValue(null) + MainActivity.H(null)  [dismiss]
//     FALSE → read "subscription_prompt_shown" → show paywall overlay
//
// Patch: clearBody() + unconditionally execute the TRUE branch.
//   v0=COROUTINE_SUSPENDED(Lfca;->a), v1=null, p0→MainActivity, p1→MainActivity.I:Li87;
//
// v2.3.2 → v2.3.3: yn5→sn5, Lpca;→Lfca;, Lr87;→Li87;, Lb49;→Ls39;
// String anchor and custom predicate are stable — fingerprint declaration unchanged.
//
// Stable anchors:
//   • string("subscription_prompt_shown") — DataStore key read only in this emit()
//   • custom { name == "emit" }
internal val PaywallEmitFingerprint = Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = listOf(
        string("subscription_prompt_shown"),
    ),
    custom = { method, _ -> method.name == "emit" },
)
