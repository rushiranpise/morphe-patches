package app.template.patches.reddit.media

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY

// ─────────────────────────────────────────────────────────────────────────────
// Fix RedGifs Audio
//
// RedGifs videos embedded in Reddit posts have no audio and no mute/unmute
// control.  The root cause (confirmed via HTTP capture by issue reporter):
//
//   1. Reddit's API returns an oembed response for RedGifs posts with:
//        provider_name = "RedGifs"
//        embedHtml     = <iframe src="https://www.redgifs.com/ifr/{slug}">
//        url           = https://media.redgifs.com/{Slug}-silent.mp4  ← silent!
//
//   2. aps.a("RedGifs") → ProviderName.UNKNOWN (no RedGifs case in enum)
//
//   3. UNKNOWN routes to ExoPlayer with VideoMedia.url (the -silent.mp4)
//      → zero audio tracks → no mute icon → silent playback
//
//   4. YOUTUBE/TIKTOK would route to WebView with VideoMedia.embedHtml
//      (the iframe to redgifs.com/ifr/{slug}) → full audio via RedGifs player
//
// Fix: inject a "RedGifs" check at the top of aps.a() before the enum loop.
//   - aps.a() has .registers 4: p0=input, v0..v2 free
//   - If p0 contains "redgifs" (case-insensitive), return YOUTUBE immediately
//   - YOUTUBE triggers the existing WebView iframe path in EmbedVideoKt
//   - VideoMedia.embedHtml (already correctly populated) is then rendered
//     via redgifs.com/ifr/{slug} with full audio and mute control
//
// This reuses Reddit's own existing WebView embed infrastructure — no new
// components required.
//
// Source: GitHub issue oenderg/Reddidnt → MorpheApp/morphe-patches#353
//         HTTP capture by @robertogogoni (64/69 RedGifs videos confirmed audio)
// ─────────────────────────────────────────────────────────────────────────────

@Suppress("unused")
val redditFixRedGifsAudioPatch = bytecodePatch(
    name = "Fix RedGifs Audio",
    description = "Routes RedGifs embeds through the WebView iframe path so videos play with audio instead of using the silent direct-stream URL.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // Inject at index 0 of aps.a(String)ProviderName.
        // .registers 4 — p0=inputString, v0/v1/v2 free.
        //
        // Logic:
        //   v0 = "redgifs"
        //   v1 = p0.toLowerCase()
        //   if v1.contains(v0) → return YOUTUBE
        //   else fall through to original enum loop
        //
        // Uses addInstructionsWithLabels because we need a forward branch (:not_redgifs).
        // This is safe here: the method has NO existing labels at index 0 —
        // the first label (:L0) is the loop start which begins after our injection.
        // Our labels (:not_redgifs) are self-contained within the injected block.
        ProviderNameFromStringFingerprint.method.addInstructionsWithLabels(
            0,
            """
                # v0 = "redgifs" (lowercase needle)
                const-string v0, "redgifs"
                # v1 = p0.toLowerCase()
                invoke-virtual { p0 }, Ljava/lang/String;->toLowerCase()Ljava/lang/String;
                move-result-object v1
                # if v1 does not contain "redgifs", skip to original logic
                invoke-virtual { v1, v0 }, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z
                move-result v2
                if-eqz v2, :not_redgifs
                # return YOUTUBE — triggers WebView iframe path with embedHtml
                sget-object v0, Lcom/reddit/mediacomponent/api/props/MediaData${'$'}EmbedVideo${'$'}ProviderName;->YOUTUBE:Lcom/reddit/mediacomponent/api/props/MediaData${'$'}EmbedVideo${'$'}ProviderName;
                return-object v0
                :not_redgifs
                nop
            """.trimIndent(),
        )
    }
}
