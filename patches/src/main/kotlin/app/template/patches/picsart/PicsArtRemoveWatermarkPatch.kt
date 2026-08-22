package app.template.patches.picsart

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.PICSART_COMPATIBILITY
import app.template.patches.picsart.picsartDisableSignatureCheckPatch

/**
 * Removes PicsArt watermarks from exported photos and videos.
 *
 * Photo watermark: WatermarkUseCaseImpl.b() (getImageWithWatermark) loads
 * `assets/effects/effects_watermark.png` and draws it scaled onto the export
 * bitmap (WatermarkUseCase.kt l.72+). Replacing the asset with a 1x1 fully
 * transparent PNG makes the draw a no-op — no bytecode patching needed, survives
 * method obfuscation between versions.
 *
 * Video watermark: WatermarkRepositoryImpl.getWatermarkSettings() reads
 * `assets/video/video_watermark_config.json`; the top-level `enabled` flag gates
 * whether any watermark is composited. Setting it to false disables the video
 * watermark (the SVG presets are then never applied).
 *
 * Both paths are resource-level, so this patch is robust across updates as long
 * as the asset paths stay stable (they are referenced by literal strings).
 */
private val transparentPng = byteArrayOf(
    137.toByte(), 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1,
    0, 0, 0, 1, 8, 6, 0, 0, 0, 31, 21, 196.toByte(), 137.toByte(), 0, 0, 0, 13, 73, 68, 65,
    84, 120, 156.toByte(), 99, 96, 96, 96, 96, 0, 0, 0, 5, 0, 1, 165.toByte(), 246.toByte(), 69, 64, 0, 0,
    0, 0, 73, 69, 78, 68, 174.toByte(), 66, 96, 130.toByte(),
)

@Suppress("unused")
val picsartRemoveWatermarkPatch = rawResourcePatch(
    name = "Remove Watermark",
    description = "Removes the PicsArt watermark from exported photos and videos.",
) {
    compatibleWith(PICSART_COMPATIBILITY)
    dependsOn(picsartDisableSignatureCheckPatch)

    execute {
        // Photo watermark → transparent 1x1 PNG (no-op draw)
        val photoWm = get("assets/effects/effects_watermark.png")
            ?: throw PatchException("assets/effects/effects_watermark.png not found — app version changed?")
        photoWm.writeBytes(transparentPng)

        // Video watermark → enabled:false in config JSON
        val videoCfg = get("assets/video/video_watermark_config.json")
            ?: throw PatchException("assets/video/video_watermark_config.json not found — app version changed?")
        val cfg = videoCfg.readText()
        // Keep the file structurally valid; only flip the top-level enabled flag.
        val patched = cfg.replace("\"enabled\": true", "\"enabled\": false")
        if (patched == cfg) {
            throw PatchException(
                "video_watermark_config.json has no '\"enabled\": true' to flip — format changed?",
            )
        }
        videoCfg.writeText(patched)
    }
}
