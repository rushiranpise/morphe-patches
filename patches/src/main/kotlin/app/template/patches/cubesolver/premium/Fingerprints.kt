package app.template.patches.cubesolver.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── App virtualized methods ──────────────────────────────────────────────────

/**
 * App.onCreate() — virtualized; reflective Method.invoke on a field populated
 * by the VM → NPE at startup without the VM. No-op it.
 *
 * Smali: classes/com/jeffprod/cubesolver/App.smali
 *   .method public final onCreate()V
 *   sget-object v0, Lcom/unity3d/ads/datastore/Vq/aFGUz;->toBwWTADpaU:Ljava/lang/reflect/Method;
 *   invoke-virtual {v0, v3, v1}, Ljava/lang/reflect/Method;->invoke(...)
 */
internal object AppOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/App;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Ljava/lang/reflect/Method;",
            name = "invoke",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;", "[Ljava/lang/Object;"),
        ),
    )
)

/**
 * MainActivity.onCreate(Bundle) — virtualized via Vq/aFGUz.pcKC reflective
 * Method.invoke → NPE. Reconstructed with real WebView setup in the patch.
 *
 * Smali: classes/com/jeffprod/cubesolver/MainActivity.smali
 *   .method public final onCreate(Landroid/os/Bundle;)V
 */
internal object MainActivityOnCreateFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Ljava/lang/reflect/Method;",
            name = "invoke",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;", "[Ljava/lang/Object;"),
        ),
    )
)

/**
 * MainActivity.onResume() — virtualized; same Method.invoke pattern → NPE.
 */
internal object MainActivityOnResumeFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
    name = "onResume",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Ljava/lang/reflect/Method;",
            name = "invoke",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;", "[Ljava/lang/Object;"),
        ),
    )
)

/**
 * MainActivity.onPause() — virtualized; same Method.invoke pattern → NPE.
 */
internal object MainActivityOnPauseFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
    name = "onPause",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Ljava/lang/reflect/Method;",
            name = "invoke",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;", "[Ljava/lang/Object;"),
        ),
    )
)

/**
 * MainActivity.onDestroy() — virtualized; same Method.invoke pattern → NPE.
 */
internal object MainActivityOnDestroyFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
    name = "onDestroy",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Ljava/lang/reflect/Method;",
            name = "invoke",
            returnType = "Ljava/lang/Object;",
            parameters = listOf("Ljava/lang/Object;", "[Ljava/lang/Object;"),
        ),
    )
)

// ─── Premium / puzzle unlock ──────────────────────────────────────────────────

/**
 * MainActivity.k() — master puzzle-unlock gate.
 * Reads gp.k:Z; if true → writes localStorage["ulcsall"] = "ok" (all unlocked),
 * else "false". We force it to always write "ok" via j(key, value).
 *
 * Smali: classes/com/jeffprod/cubesolver/MainActivity.smali
 *   .method public final k()V
 *   .registers 3
 *   sget-boolean v0, Lgp;->k:Z
 *   if-eqz v0, :L0
 *   const-string v0, "ok"
 *   goto :L1
 *   :L0 const-string v0, "false"
 *   :L1 const-string v1, "ulcsall"
 *   invoke-virtual {p0, v1, v0}, Lcom/jeffprod/cubesolver/MainActivity;->j(Ljava/lang/String;Ljava/lang/String;)V
 */
internal object PuzzleUnlockFingerprint : Fingerprint(
    definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
    name = "k",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Lcom/jeffprod/cubesolver/MainActivity;",
            name = "j",
            returnType = "V",
            parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;"),
        ),
    )
)

// ─── Ads ─────────────────────────────────────────────────────────────────────

/**
 * k93.loadRewardedAd() — @JavascriptInterface called by WebView when the user
 * taps "watch ad to unlock". We skip the ad and reward immediately via k().
 *
 * Smali: classes/k93.smali
 *   .method public final loadRewardedAd()V
 *   iget-object p0, p0, Lk93;->a:Ljava/lang/ref/WeakReference;
 *   invoke-virtual {p0}, Ljava/lang/ref/Reference;->get()Ljava/lang/Object;
 *   check-cast p0, Lcom/jeffprod/cubesolver/MainActivity;
 *   if-eqz p0, :L0
 *   new-instance v0, Landroid/os/Handler;
 *   ...
 *   invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z
 */
internal object RewardedAdBridgeFingerprint : Fingerprint(
    definingClass = "Lk93;",
    name = "loadRewardedAd",
    returnType = "V",
    parameters = listOf(),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            definingClass = "Landroid/os/Handler;",
            name = "post",
            returnType = "Z",
            parameters = listOf("Ljava/lang/Runnable;"),
        ),
    )
)

/**
 * bf.a(Context, String, Laf) — AppLovin SDK init wrapper.
 * No-op → AppLovin never initializes, no ads loaded.
 *
 * Smali: classes/bf.smali
 *   .method public final a(Landroid/content/Context;Ljava/lang/String;Laf;)V
 *   invoke-static {p1}, Lcom/applovin/sdk/AppLovinSdk;->getInstance(Landroid/content/Context;)...
 */
internal object AppLovinInitFingerprint : Fingerprint(
    definingClass = "Lbf;",
    name = "a",
    returnType = "V",
    parameters = listOf("Landroid/content/Context;", "Ljava/lang/String;", "Laf;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            definingClass = "Lcom/applovin/sdk/AppLovinSdk;",
            name = "getInstance",
            returnType = "Lcom/applovin/sdk/AppLovinSdk;",
            parameters = listOf("Landroid/content/Context;"),
        ),
    )
)

// ─── Telemetry registrars ─────────────────────────────────────────────────────
// NOTE: These registrars build real component lists (Arrays.asList / Lee4.r) —
// they do NOT call Collections.emptyList() anywhere in their bodies. We therefore
// identify them only by definingClass + name, then inject emptyList at index 0.

/**
 * CrashlyticsRegistrar.getComponents() — public final; builds and returns
 * a List of Firebase Crashlytics components.
 *
 * Smali: classes/com/google/firebase/crashlytics/CrashlyticsRegistrar.smali
 *   .method public final getComponents()Ljava/util/List;
 *   ...
 *   invoke-static {p0}, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;
 */
internal object CrashlyticsRegistrarFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/crashlytics/CrashlyticsRegistrar;",
    name = "getComponents",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

/**
 * AnalyticsConnectorRegistrar.getComponents() — public (no final); builds
 * and returns a List of Firebase Analytics Connector components.
 *
 * Smali: classes/com/google/firebase/analytics/connector/internal/AnalyticsConnectorRegistrar.smali
 *   .method public getComponents()Ljava/util/List;
 *   ...
 *   invoke-static {p0}, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;
 */
internal object AnalyticsRegistrarFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/analytics/connector/internal/AnalyticsConnectorRegistrar;",
    name = "getComponents",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

/**
 * FirebasePerfRegistrar.getComponents() — public (no final); builds and
 * returns a List of Firebase Performance components.
 *
 * Smali: classes/com/google/firebase/perf/FirebasePerfRegistrar.smali
 *   .method public getComponents()Ljava/util/List;
 *   ...
 *   invoke-static {p0}, Ljava/util/Arrays;->asList([Ljava/lang/Object;)Ljava/util/List;
 */
internal object PerfRegistrarFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/perf/FirebasePerfRegistrar;",
    name = "getComponents",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)

/**
 * FirebaseSessionsRegistrar.getComponents() — public final; builds and
 * returns a List of Firebase Sessions components via Lee4.r().
 *
 * Smali: classes/com/google/firebase/sessions/FirebaseSessionsRegistrar.smali
 *   .method public getComponents()Ljava/util/List;
 *   ...
 *   invoke-static {p0}, Lee4;->r([Ljava/lang/Object;)Ljava/util/List;
 */
internal object SessionsRegistrarFingerprint : Fingerprint(
    definingClass = "Lcom/google/firebase/sessions/FirebaseSessionsRegistrar;",
    name = "getComponents",
    accessFlags = listOf(AccessFlags.PUBLIC),
    returnType = "Ljava/util/List;",
    parameters = listOf(),
)
