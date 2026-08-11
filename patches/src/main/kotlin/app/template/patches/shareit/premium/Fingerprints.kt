package app.template.patches.shareit.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * Targets shareit/premium/anr;->isVip()Z — the sole VIP gate for all subscription checks.
 *
 * anr implements the yg interface (shareit/premium/yg), which is the service registered
 * at "/subscription/service/subs" via the WMRouter service locator (amf.a()).
 *
 * All three limit methods (isFileSubscribeLimit, isTransferSubscribeLimit,
 * isHomeSubscribeLimit) gate on isVip() first. Patching this single method
 * cascades to the entire feature-gating surface.
 *
 * Call chain:
 *   com.ushareit.component.subscription.a.a()
 *     → yg.isVip()  [interface dispatch]
 *       → anr.isVip()  [impl, classes3]
 *         → com.ushareit.subscription.hepler.b.a().d()
 *
 * Stable identifiers (instruction order in isVip()):
 *   1. methodCall(hepler.b, "a") — singleton getter, returns hepler.b instance
 *   2. methodCall(hepler.b, "d") — actual VIP state query (reads MutableLiveData)
 *
 * Both class/method names are in an unobfuscated subscription package;
 * they survive R8/ProGuard across updates.
 *
 * Smali (classes3/shareit/premium/anr.smali):
 *   .method public isVip()Z   ← PUBLIC, non-static, no params, returns Z
 *     invoke-static {}, Lcom/ushareit/subscription/hepler/b;->a()Lcom/ushareit/subscription/hepler/b;
 *     move-result-object v0
 *     invoke-virtual {v0}, Lcom/ushareit/subscription/hepler/b;->d()Z
 *     move-result v0
 *     return v0
 *   .end method
 */
object IsVipFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/ushareit/subscription/hepler/b;",
            name = "a"
        ),
        methodCall(
            definingClass = "Lcom/ushareit/subscription/hepler/b;",
            name = "d"
        ),
    )
)

/**
 * Targets com/base/core/x/d;->c(Landroid/content/Context;)V — the APK signature tamper check.
 *
 * Architecture:
 *   jp.smali calls c.a(Context, int) → triggers two arms:
 *     (1) XNative.check() → libXXX.so nativeCheck (MD5-based hash verify)
 *     (2) d.a(Context) → spawns thread with 5s delay → calls d.c(Context)
 *
 *   d.c() reads expected cert fingerprints from an asset (obfuscated filename),
 *   computes the installed APK's signing cert MD5 via PackageManager via reflection,
 *   calls ArrayList.contains(fingerprint). If false (signature mismatch) → kills app:
 *     reflective startActivity(Intent(context, Process::class)) → sleep(20000ms) → crash
 *
 *   The HWUI FORTIFY crash (pthread_mutex_lock on destroyed mutex in CommonPool)
 *   occurs because background threads are mid-work when the process is killed.
 *
 * Patch approach: returnEarly() (return-void) at entry — skips cert read, comparison,
 * and kill path entirely. The 5s delay thread from d.a() still fires but returns immediately.
 *
 * Fingerprint stable identifiers (classes/com/base/core/x/d.smali, method c):
 *   - definingClass + name safe: com.base.core.x is unobfuscated (Keep annotation on XNative)
 *   - literal(0x4E20L) = 20000ms sleep — unique, only appears in the kill path
 *   - methodCall(SystemClock, "sleep") — in the kill branch after the cert check
 *
 * Smali evidence (line 498-555 in d.smali):
 *   if-nez v1, :cond_8c         ← branch: nonzero=ok, zero=kill
 *   ...                          ← reflective startActivity
 *   const-wide/16 v1, 0x4e20    ← 20000ms
 *   invoke-static {v1,v2}, Landroid/os/SystemClock;->sleep(J)V
 *   goto :goto_96
 *   :cond_8c                    ← ok path
 */
object TamperCheckFingerprint : Fingerprint(
    definingClass = "Lcom/base/core/x/d;",
    name = "c",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "V",
)

/**
 * Targets shareit/premium/anp;->h()I — reads the persisted subscription state integer from SharedPrefs.
 *
 * anp.h() is called by anp.i()Z which:
 *   1. Seeds MutableLiveData initial value in hepler.b.a(Context) at initIAP time
 *   2. Drives the UI premium badge display (separate from isVip() gate)
 *
 * Return value semantics: 0=none, 1=active subscription, 2=expired.
 * Returning 1 makes anp.i() return true → LiveData seeded as VIP on init → badge shows.
 *
 * Smali (classes3/shareit/premium/anp.smali):
 *   .method public static h()I    ← PUBLIC STATIC, no params, returns I
 *     invoke-static {}, Lshareit/premium/anp;->a()Lshareit/premium/anp;
 *     sget-object v1, Lshareit/premium/anp;->d:Ljava/lang/String;
 *     invoke-virtual {v0, v1, v2}, Lshareit/premium/anp;->a(Ljava/lang/String;I)I
 *
 * Fingerprint: definingClass + name + signature fully identifies this.
 */
object SubStateFingerprint : Fingerprint(
    definingClass = "Lshareit/premium/anp;",
    name = "h",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    returnType = "I",
)

/**
 * Targets shareit/premium/anp;->o()Ljava/lang/Boolean; — reads "sub_vip" SharedPrefs boolean.
 *
 * Used in anr.isOpenIAPForMe() as the "ever_vip" flag:
 *   if (ever_vip && config.a.l()) return true (show lifetime IAP option)
 *
 * Returning Boolean.TRUE ensures the lifetime/pro IAP option is always visible.
 *
 * Smali (classes3/shareit/premium/anp.smali):
 *   .method public static o()Ljava/lang/Boolean;    ← PUBLIC STATIC, no params
 *     const-string v1, "sub_vip"
 *     invoke-virtual {v0, v1, v2}, Lshareit/premium/anp;->c(Ljava/lang/String;Z)Z
 */
object EverVipFingerprint : Fingerprint(
    definingClass = "Lshareit/premium/anp;",
    name = "o",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(),
    returnType = "Ljava/lang/Boolean;",
    filters = listOf(
        string("sub_vip"),
    )
)

/**
 * Targets com/base/core/x/XNative;->check(Landroid/content/Context;I)V
 *
 * XNative.check() is called by c.a() (the tamper check orchestrator), which is
 * triggered on EVERY activity lifecycle event via jp's ActivityLifecycleCallbacks.
 *
 * Each call to check() → nativeCheck() (in libXXX.so) → on mismatch:
 *   1. Calls back XNative.onFail() via JNI → writes salva_config/enable=false
 *   2. Spawns a native thread: sleep(5) ... sleep(20) → exit()
 *
 * Because lifecycle callbacks fire on every screen navigation, multiple kill
 * threads accumulate. The first one (sleep 20) is handled by the native hex patch,
 * but subsequent navigations spawn fresh threads that also call exit().
 *
 * Patch approach: returnEarly() (return-void) at entry — nativeCheck() is never
 * called, no kill threads are spawned, no onFail() fires for any screen.
 *
 * Note: d.c() (Java cert check) is a separate one-time check patched separately.
 * This patch covers the repeating native arm triggered per activity lifecycle.
 *
 * Smali (classes/com/base/core/x/XNative.smali):
 *   .method static check(Landroid/content/Context;I)V   ← static, package-private
 *     sget-boolean p0, XNative->sLoaded:Z
 *     if-eqz p0, :cond_17
 *     invoke-static {p0,p1}, XNative->nativeCheck(Context;I)I  ← kill trigger
 */
object XNativeCheckFingerprint : Fingerprint(
    definingClass = "Lcom/base/core/x/XNative;",
    name = "check",
    parameters = listOf("Landroid/content/Context;", "I"),
    returnType = "V",
)
