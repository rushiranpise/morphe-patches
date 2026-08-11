package app.template.patches.cubesolver.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.CUBESOLVER_COMPATIBILITY
import app.template.patches.shared.killPairIpFull
import app.template.patches.shared.pairIPManifestPatch

// ─── Manifest: base providers ─────────────────────────────────────────────────

/**
 * Disables providers that crash when the PairIP VM is absent:
 *  - androidx.startup.InitializationProvider  (WorkManager NPE)
 *  - io.sfbx.appconsent…InjektorInitializerContentProvider  (consent SDK crash)
 *  - com.google.firebase.provider.FirebaseInitProvider  (Firebase init crash)
 */
@Suppress("unused")
val cubesolverBaseProvidersPatch = resourcePatch(
    default = true
) {
    compatibleWith(CUBESOLVER_COMPATIBILITY)

    execute {
        document("AndroidManifest.xml").use { doc ->
            val providers = doc.getElementsByTagName("provider")
            val toDisable = listOf(
                "androidx.startup.InitializationProvider",
                "io.sfbx.appconsent.core.diinjektor.InjektorInitializerContentProvider",
                "com.google.firebase.provider.FirebaseInitProvider",
            )
            for (i in 0 until providers.length) {
                val node = providers.item(i)
                val name = node.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                if (toDisable.any { name == it || name.contains(it) }) {
                    node.attributes.getNamedItem("android:enabled")
                        ?.let { it.nodeValue = "false" }
                        ?: run {
                            val attr = doc.createAttribute("android:enabled")
                            attr.value = "false"
                            node.attributes.setNamedItem(attr)
                        }
                }
            }
        }
    }
}

// ─── Manifest: ad providers ───────────────────────────────────────────────────

/**
 * Disables ad SDK content providers so they cannot auto-initialize:
 *  - com.google.android.gms.ads.MobileAdsInitProvider
 *  - com.applovin.sdk.AppLovinInitProvider
 */
@Suppress("unused")
val cubesolverRemoveAdsPatch = resourcePatch(
    default = true
) {
    compatibleWith(CUBESOLVER_COMPATIBILITY)

    execute {
        document("AndroidManifest.xml").use { doc ->
            val providers = doc.getElementsByTagName("provider")
            val toDisable = listOf(
                "com.google.android.gms.ads.MobileAdsInitProvider",
                "com.applovin.sdk.AppLovinInitProvider",
            )
            for (i in 0 until providers.length) {
                val node = providers.item(i)
                val name = node.attributes.getNamedItem("android:name")?.nodeValue ?: ""
                if (toDisable.any { name == it || name.contains(it) }) {
                    node.attributes.getNamedItem("android:enabled")
                        ?.let { it.nodeValue = "false" }
                        ?: run {
                            val attr = doc.createAttribute("android:enabled")
                            attr.value = "false"
                            node.attributes.setNamedItem(attr)
                        }
                }
            }
        }
    }
}

// ─── JS: force paid gates in bundle.js ───────────────────────────────────────

/**
 * Byte-patches assets/www/build/bundle.js (5.0.3) to make all users appear paid:
 *
 *  1. isPaidUser()  → always return true
 *  2. _0x59b6b0()  → always return true  (kilominx gate)
 *  3. `==="ok"?(` → `!=="x"?(`  (customize-design gate; getItem never returns "x")
 *
 * Pattern is padded with spaces to preserve original file length.
 */
@Suppress("unused")
val cubesolverForceUnlockJsPatch = rawResourcePatch(
    default = true
) {
    compatibleWith(CUBESOLVER_COMPATIBILITY)

    execute {
        val bundle = get("assets/www/build/bundle.js") ?: return@execute
        val bytes = bundle.readBytes()

        val findReplace = listOf(
            "function isPaidUser(){return window[\"localStorage\"][\"getItem\"](IAP_PAID_KEY)===\"ok\"}"
                .toByteArray() to
            "function isPaidUser(){return!![]}".toByteArray(),

            "function _0x59b6b0(){const _0xaf3abd=_0x41eb;return window[_0xaf3abd(202)+\"ge\"][\"getItem\"](IAP_PAID_KEY)===\"ok\"||window[\"localStorage\"][\"getItem\"](KILOMINX_UNLOCK_KEY)===\"ok\"}"
                .toByteArray() to
            "function _0x59b6b0(){return!![]}".toByteArray(),

            "===\"ok\"?(".toByteArray() to "!==\"x\"?(".toByteArray(),
        )

        var patched = 0
        var result = bytes
        for ((old, new) in findReplace) {
            val idx = indexOf(result, old)
            if (idx >= 0) {
                result = result.copyOf()
                val padded = ByteArray(old.size) { ' '.code.toByte() }
                new.copyInto(padded, 0)
                padded.copyInto(result, idx)
                patched++
            }
        }

        if (patched == findReplace.size) {
            bundle.writeBytes(result)
            println("CubeSolver JS unlock: patched $patched/${findReplace.size} gate(s) in bundle.js")
        } else {
            println("CubeSolver JS unlock: only $patched/${findReplace.size} matched — bundle.js may differ from 5.0.3")
        }
    }
}

private fun indexOf(haystack: ByteArray, needle: ByteArray): Int {
    if (needle.isEmpty() || needle.size > haystack.size) return -1
    outer@ for (i in 0..haystack.size - needle.size) {
        for (j in needle.indices) {
            if (haystack[i + j] != needle[j]) continue@outer
        }
        return i
    }
    return -1
}

// ─── Bytecode: PairIP kill + reconstruct virtualized activity ─────────────────

/**
 * Core patch — kills PairIP and restores the app's ability to start:
 *
 *  PairIP layer (via shared killPairIpFull):
 *   • VMRunner.<clinit>                   → return-void  (stops native lib load)
 *   • StartupLauncher.launch()            → return-void  (skips VM startup)
 *   • SignatureCheck.verifyIntegrity()    → return-void  (skips Java cert check)
 *   • LicenseClient + all call sites      → neutralized
 *
 *  App reconstruction (all methods are virtualized → Method.invoke → NPE):
 *   • App.onCreate()          → return-void
 *   • MainActivity.onCreate() → real WebView setup (resource IDs verified from R.java 5.0.3)
 *   • MainActivity.onResume/onPause/onDestroy() → restore super calls
 *
 *  Manifest (via shared pairIPManifestPatch):
 *   • android:name → com.jeffprod.cubesolver.App (skip PairIP Application)
 *   • LicenseActivity removed
 *   • CHECK_LICENSE permission removed
 *
 * Resource IDs (5.0.3):
 *   0x7f0b001c = R.layout.activity_main
 *   0x7f08022b = R.id.webview
 *
 * Requires cubesolverBaseProvidersPatch (manifest providers must be disabled first).
 */
@Suppress("unused")
val cubesolverBasePatch = bytecodePatch {
    compatibleWith(CUBESOLVER_COMPATIBILITY)
    dependsOn(
        cubesolverBaseProvidersPatch,
        pairIPManifestPatch("com.jeffprod.cubesolver.App"),
    )

    execute {
        // PairIP — full kill via shared utility
        killPairIpFull()

        // App.onCreate() is virtualized → NPE; just no-op it
        AppOnCreateFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            m.method.addInstructions(0, "return-void")
        }

        // MainActivity.onCreate() — replace virtualized body with real WebView setup
        MainActivityOnCreateFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

                const v0, 0x7f0b001c
                invoke-virtual {p0, v0}, Landroid/app/Activity;->setContentView(I)V

                const v0, 0x7f08022b
                invoke-virtual {p0, v0}, Landroid/app/Activity;->findViewById(I)Landroid/view/View;
                move-result-object v0
                check-cast v0, Landroid/webkit/WebView;
                iput-object v0, p0, Lcom/jeffprod/cubesolver/MainActivity;->b:Landroid/webkit/WebView;

                invoke-virtual {v0}, Landroid/webkit/WebView;->getSettings()Landroid/webkit/WebSettings;
                move-result-object v1
                const/4 v2, 0x1
                invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setJavaScriptEnabled(Z)V
                invoke-virtual {v1, v2}, Landroid/webkit/WebSettings;->setDomStorageEnabled(Z)V

                new-instance v2, Lk93;
                invoke-direct {v2, p0}, Lk93;-><init>(Lcom/jeffprod/cubesolver/MainActivity;)V
                const-string v3, "Android"
                invoke-virtual {v0, v2, v3}, Landroid/webkit/WebView;->addJavascriptInterface(Ljava/lang/Object;Ljava/lang/String;)V

                const-string v3, "file:///android_asset/www/index.html"
                invoke-virtual {v0, v3}, Landroid/webkit/WebView;->loadUrl(Ljava/lang/String;)V

                invoke-virtual {p0}, Lcom/jeffprod/cubesolver/MainActivity;->k()V

                const/4 v3, 0x0
                invoke-virtual {p0, v3}, Lcom/jeffprod/cubesolver/MainActivity;->i(Z)V

                return-void
            """.trimIndent())
        }

        // Virtualized lifecycle methods — restore super calls
        MainActivityOnResumeFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                invoke-super {p0}, Landroid/app/Activity;->onResume()V
                return-void
            """.trimIndent())
        }
        MainActivityOnPauseFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                invoke-super {p0}, Landroid/app/Activity;->onPause()V
                return-void
            """.trimIndent())
        }
        MainActivityOnDestroyFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                invoke-super {p0}, Landroid/app/Activity;->onDestroy()V
                return-void
            """.trimIndent())
        }
    }
}

// ─── Bytecode: remove ads ─────────────────────────────────────────────────────

/**
 * Removes ads:
 *  • bf.a()              → return-void  (AppLovin SDK init — never runs)
 *  • k93.loadRewardedAd() → grant reward instantly via MainActivity.k(), no ad shown
 *  • Telemetry registrars → return emptyList() (Crashlytics, Analytics, Perf, Sessions)
 *
 * NOTE: The four Firebase registrars do NOT call emptyList() in their original
 * bodies — they build full component lists via Arrays.asList / Lee4.r(). We
 * match them by definingClass + name alone and inject emptyList at index 0.
 */
@Suppress("unused")
val cubesolverRemoveAdsBytecodePatch = bytecodePatch(
    name = "Remove Ads",
    description = "Removes advertisements and disables crash/analytics telemetry.",
    default = true
) {
    compatibleWith(CUBESOLVER_COMPATIBILITY)
    dependsOn(cubesolverBasePatch, cubesolverRemoveAdsPatch)

    execute {
        // AppLovin SDK init → no-op
        AppLovinInitFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            m.method.addInstructions(0, "return-void")
        }

        // Rewarded-ad bridge: reward immediately without showing any ad.
        // Reads WeakReference<MainActivity> from k93.a, calls k() directly.
        RewardedAdBridgeFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                iget-object v0, p0, Lk93;->a:Ljava/lang/ref/WeakReference;
                invoke-virtual {v0}, Ljava/lang/ref/Reference;->get()Ljava/lang/Object;
                move-result-object v0
                if-eqz v0, :cond_skip
                check-cast v0, Lcom/jeffprod/cubesolver/MainActivity;
                invoke-virtual {v0}, Lcom/jeffprod/cubesolver/MainActivity;->k()V
                :cond_skip
                return-void
            """.trimIndent())
        }

        // Telemetry registrars — inject emptyList() before the existing body.
        // All four registrars have complex bodies with real components; we
        // short-circuit them by returning early with an empty list.
        val emptyListSmali = """
            invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
            move-result-object v0
            return-object v0
        """.trimIndent()

        CrashlyticsRegistrarFingerprint.let { fp ->
            fp.matchOrNull()?.method?.addInstructions(0, emptyListSmali)
        }
        AnalyticsRegistrarFingerprint.let { fp ->
            fp.matchOrNull()?.method?.addInstructions(0, emptyListSmali)
        }
        PerfRegistrarFingerprint.let { fp ->
            fp.matchOrNull()?.method?.addInstructions(0, emptyListSmali)
        }
        SessionsRegistrarFingerprint.let { fp ->
            fp.matchOrNull()?.method?.addInstructions(0, emptyListSmali)
        }
    }
}

// ─── Bytecode: puzzles unlock ─────────────────────────────────────────────────

/**
 * Unlocks all puzzle designs by:
 *  1. Forcing MainActivity.k() to always write localStorage["ulcsall"] = "ok"
 *     (the WebView JS reads this key to decide which puzzles are locked).
 *  2. Patching bundle.js JS gates via cubesolverForceUnlockJsPatch (dependsOn).
 *
 * j(key, value) calls WebView.evaluateJavascript to write into localStorage.
 */
@Suppress("unused")
val cubesolverPuzzlesUnlockPatch = bytecodePatch(
    name = "Puzzles Unlock",
    description = "Unlocks all puzzle designs including kilominx and ad-gated variants.",
    default = true
) {
    compatibleWith(CUBESOLVER_COMPATIBILITY)
    dependsOn(cubesolverBasePatch, cubesolverForceUnlockJsPatch)

    execute {
        PuzzleUnlockFingerprint.let { fp ->
            val m = fp.matchOrNull() ?: return@let
            val impl = m.method.implementation ?: return@let
            m.method.removeInstructions(0, impl.instructions.size)
            m.method.addInstructions(0, """
                const-string v0, "ok"
                const-string v1, "ulcsall"
                invoke-virtual {p0, v1, v0}, Lcom/jeffprod/cubesolver/MainActivity;->j(Ljava/lang/String;Ljava/lang/String;)V
                return-void
            """.trimIndent())
        }
    }
}
