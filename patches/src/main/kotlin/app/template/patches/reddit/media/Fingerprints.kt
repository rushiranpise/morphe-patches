package app.template.patches.reddit.media

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// RedGifs audio fix fingerprints — verified against 2026.32.0 smali
//
// Problem:
//   Reddit's EmbedVideo pipeline maps the oembed provider_name string to a
//   ProviderName enum (YOUTUBE | TIKTOK | UNKNOWN).  Only YOUTUBE and TIKTOK
//   get the WebView iframe path which plays video with audio.  RedGifs maps
//   to UNKNOWN and falls through to ExoPlayer using VideoMedia.url — which
//   points to the -silent.mp4 variant (no audio track, no mute icon).
//
// VideoAttribution.providerName for RedGifs posts = "RedGifs" (from oembed
//   provider_name field in Reddit API response, confirmed by HTTP capture).
//   VideoMedia.embedHtml already contains <iframe src="redgifs.com/ifr/{id}">
//   but is never used because the provider maps to UNKNOWN.
//
// Fix:
//   Intercept aps.a(String)ProviderName — the Companion fromString mapper —
//   at index 0.  If input contains "redgifs" (case-insensitive), return YOUTUBE
//   before the loop runs.  YOUTUBE triggers the existing WebView iframe path
//   in EmbedVideoKt which renders embedHtml (the RedGifs player with audio).
//
// ─────────────────────────────────────────────────────────────────────────────

// aps.a(String)ProviderName — the ProviderName.fromString() mapper
// Smali (classes11/aps.smali):
//   .method public static a(Ljava/lang/String;)Lcom/reddit/mediacomponent/api/props/MediaData$EmbedVideo$ProviderName;
//   .registers 4  (p0=inputString, v0..v2 free)
//   Iterates enum entries, returns UNKNOWN on no match.
// Anchored on:
//   - getEntries() call (stable, always first instruction)
//   - areEqual() call (Kotlin Intrinsics, always stable)
//   - sget UNKNOWN (stable field ref, fallback return)
internal object ProviderNameFromStringFingerprint : Fingerprint(
    returnType = "Lcom/reddit/mediacomponent/api/props/MediaData\$EmbedVideo\$ProviderName;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("Ljava/lang/String;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            smali = "Lcom/reddit/mediacomponent/api/props/MediaData\$EmbedVideo\$ProviderName;->getEntries()Lkwg;",
        ),
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            smali = "Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z",
        ),
    ),
)
