package app.template.extension.extension;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies Block Blast trait overrides by evaluating a JS engine inside the Cocos runtime.
 * Patches call {@link #add} with their override config, then {@link #start} runs the engine.
 */
@SuppressWarnings("unused")
public final class BlockBlastTraits {
    private static final String TAG = "BBTraits";
    private static final List<String> CONFIGS = new ArrayList<>();
    private static final int POLL_MS = 200;
    private static final int POLL_COUNT = 600;
    private static final long DUMP_DELAY_MS = 60_000;

    // Serializes the live trait config, skipping functions, cc objects and cycles.
    private static final String DUMP_JS =
        "(function(){var r={};function safe(f){try{return f();}catch(e){return 'ERR '+e;}}"
        + "function ser(v){var seen=[];return JSON.stringify(v,function(k,x){"
        + "if(typeof x==='function')return undefined;"
        + "if(x&&typeof x==='object'){if(x instanceof cc.Node||x instanceof cc.Component)return '[cc]';"
        + "if(seen.indexOf(x)>=0)return '[cycle]';seen.push(x);}return x;});}"
        + "var tci=window.traitConfigInfo;"
        + "r.overrides=JSON.parse(JSON.stringify(window.__bbTraits||null));"
        + "r.time=Date.now();"
        + "r.storage=safe(function(){var s=window.__require('Storage').storage,o={};"
        + "['classScore','classHighScore','classRoundNum','classGameNum','classLastScore'].forEach(function(k){o[k]=s.getItem(k);});return o;});"
        + "r.activeTraits=safe(function(){return tci.traitsClassNameMap;});"
        + "r.states=safe(function(){var o={};for(var n in tci.traitsClassNameMap){"
        + "var t=TRAIT(n);o[n]=t?{active:safe(function(){return t.active;}),state:t.state}:null;}return o;});"
        + "jsb.fileUtils.writeStringToFile(ser(r),'%s/traits.json');})();";

    private static volatile boolean installed;
    private static boolean dump;

    private BlockBlastTraits() {
    }

    public static void add(String config) {
        CONFIGS.add(config);
    }

    public static void dump() {
        dump = true;
    }

    // Called from JS through jsb.reflection once the engine is installed.
    public static void installed() {
        installed = true;
    }

    public static void start(Context context, String engine) {
        if (CONFIGS.isEmpty() && !dump) return;
        ClassLoader loader = context.getClassLoader();
        String install = engine + "([" + TextUtils.join(",", CONFIGS) + "]);";
        String dumpJs = dump ? String.format(DUMP_JS, context.getExternalFilesDir(null).getAbsolutePath()) : null;
        new Thread(() -> {
            try {
                // The JS runtime has no hook into plain-dex Java, so poll until the engine reports in.
                for (int i = 0; i < POLL_COUNT && !installed; i++) {
                    Thread.sleep(POLL_MS);
                    eval(loader, install);
                }
                // Repeat so in-game state (reset on restart) is captured while playing.
                while (dumpJs != null) {
                    Thread.sleep(DUMP_DELAY_MS);
                    eval(loader, dumpJs);
                }
            } catch (InterruptedException ignored) {
            }
        }, TAG).start();
    }

    private static void eval(ClassLoader loader, String js) {
        try {
            Runnable run = () -> {
                try {
                    Class.forName("org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge", true, loader)
                        .getMethod("evalString", String.class).invoke(null, js);
                } catch (Throwable t) {
                    Log.e(TAG, "eval", t);
                }
            };
            Class.forName("org.cocos2dx.lib.Cocos2dxHelper", true, loader)
                .getMethod("runOnGLThread", Runnable.class).invoke(null, run);
        } catch (Throwable ignored) {
            // GL thread not up yet; the next poll retries.
        }
    }
}
