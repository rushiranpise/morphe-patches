package app.template.patches.tiktok_lite.misc

/*
 * Ported from hxreborn/hxreborn-tiktok-patches (GPL-3.0)
 * https://github.com/hxreborn/hxreborn-tiktok-patches
 */

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// ── Stop Video Looping ────────────────────────────────────────────────────────
// TTVideoEngine.LILLLLLL(Z)V in Lite (obfuscated name -- NOT "setLooping").
// hxreborn uses definingClass+name="setLooping" which only matches full TikTok.
// Lite smali confirmed: .method public final LILLLLLL(Z)V at line 158566,
// containing const-string "setLooping:" at line 158575.
// Fingerprint: strings=["setLooping:"] + PUBLIC FINAL (Z)V + definingClass.
// name field OMITTED -- the method name is obfuscated in Lite.
internal object VideoEngineSetLoopingFingerprint : Fingerprint(
    definingClass = "Lcom/ss/ttvideoengine/TTVideoEngine;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Z"),
    strings = listOf("setLooping:"),
)

// ── Fix Google Login ──────────────────────────────────────────────────────────
// hxreborn targets Lcom/bytedance/lobby/google/GoogleAuth; (full TikTok path).
// Lite path: Lcom/ss/android/ugc/aweme/mini_lobby/google/GoogleAuth;
// .method public final isAvailable()Z confirmed at line 18033 in Lite smali.
// GoogleOneTapAuth does NOT override isAvailable() in Lite -- only patch GoogleAuth.
internal object GoogleAuthAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/ss/android/ugc/aweme/mini_lobby/google/GoogleAuth;",
    name = "isAvailable",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = emptyList(),
)

// ── Sanitize Share URLs ───────────────────────────────────────────────────────
// ShareUrlTrackerFingerprint: returnType=String, STATIC, >=2 String params,
// strings=["utm_campaign","share_link_id"].
// Confirmed in Lite: X/7it.L(String,String)String at line 4017 contains both strings.
// No adaptation needed -- string-based fingerprint matches by content.
internal object ShareUrlTrackerFingerprint : Fingerprint(
    returnType = "Ljava/lang/String;",
    strings = listOf("utm_campaign", "share_link_id"),
    custom = { method, _ ->
        AccessFlags.STATIC.isSet(method.accessFlags) &&
            method.parameterTypes.count { it == "Ljava/lang/String;" } >= 2
    },
)
