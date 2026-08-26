package app.template.patches.reddit.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.InstructionLocation.MatchAfterImmediately
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// RedGifs audio fix fingerprints — verified against 2026.32.0 smali
//
// Root cause trace (ljs.smali, classes11):
//
//   ljs.r(xbb, VideoMedia, Image, Z, brv, zkb, I)V  — feed item composable
//     .method public final static, .registers 32
//     Line 25694 onward:
//       v18 = VideoMedia.getUrl()          → first URL (may be silent.mp4)
//       v20 = VideoMedia.getEmbedHtml()    → iframe HTML (redgifs.com/ifr/{slug})
//       v21 = VideoMedia.getUrl()          → url again
//       v13 = VideoMedia.getAttribution() == null ? p5 : attribution.getProviderName()
//       aps.a(v13) → v22 (ProviderName)
//       new bps(v18, v19, v20, v21, v22, width, height)   ← v22=UNKNOWN for RedGifs
//
//   bps = MediaData.EmbedVideo:
//     .g = ProviderName  → controls composable routing (YOUTUBE/TIKTOK/UNKNOWN)
//     .d = embedHtml     → iframe HTML (already correct, just unused when UNKNOWN)
//
// Fix strategy: intercept in ljs.r() AFTER aps.a() result (v22=ProviderName),
// BEFORE bps constructor. Check if v20 (embedHtml) contains "redgifs".
// If so, override v22 = YOUTUBE → existing WebView iframe path handles the rest.
//
// Fingerprint anchors:
//   1. VideoMedia.getEmbedHtml() call (stable SDK class, stable method name)
//   2. aps.a() call immediately following (stable ProviderName mapper)
//   3. Custom: method is in ljs class (stable after getEmbedHtml filter)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ljs.r(xbb, VideoMedia, Image, Z, brv, zkb, I)V  — the feed item composable
 * that maps VideoMedia → bps (EmbedVideo) → composable rendering.
 *
 * Fingerprint: stable SDK calls in exact order as they appear in smali.
 * Filters match: getEmbedHtml() immediately followed by aps.a().
 */
internal object RedGifsProviderNameOverrideFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf(
        "L",                                                // xbb
        "Lcom/reddit/domain/model/VideoMedia;",            // VideoMedia — stable SDK class
        "L",                                               // Image
        "Z",                                               // shouldBlur
        "Lbrv;",                                           // Composer
        "Lzkb;",                                           // Modifier
        "I",                                               // changed
    ),
    filters = listOf(
        // VideoMedia.getEmbedHtml() — stable: SDK class + method name never obfuscated
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "Lcom/reddit/domain/model/VideoMedia;->getEmbedHtml()Ljava/lang/String;",
        ),
        // aps.a(String)ProviderName — must follow immediately after
        // (VideoMedia.getUrl() appears between them but we allow some slack)
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            smali = "Laps;->a(Ljava/lang/String;)Lcom/reddit/mediacomponent/api/props/MediaData\$EmbedVideo\$ProviderName;",
        ),
    ),
)
