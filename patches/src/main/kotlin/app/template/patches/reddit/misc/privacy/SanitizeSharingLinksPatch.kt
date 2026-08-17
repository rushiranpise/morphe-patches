package app.template.patches.reddit.misc.privacy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.methodCall
import app.morphe.patcher.newInstance
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.REDDIT_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Sanitize Sharing Links
//
// Reddit's ShareLinkFactory (obfuscated as class 'a' in
// com.reddit.sharing.custom.url) builds share URLs.
// The factory class is obfuscated but its coroutine continuation inner classes
// retain stable non-obfuscated names (ShareLinkFactory$generatePostShareLink$1,
// ShareLinkFactory$getShortUrl$1) which are used in instance-of checks inside
// the public factory methods.
//
// Strategy: fingerprint the factory method that wraps generatePostShareLink
// via the stable new-instance of its continuation class, then return the
// raw input URL (p1 = String permalink) before tracking params are added.
//
// generatePostShareLink = method 'a':
//   a(String, String, szc0, SharingNavigator$ShareTrigger, ContinuationImpl)
//   → builds share URL with UTM params appended
//   → p1 = post permalink (raw URL) — return this directly
//
// getShortUrl = method 'c':
//   c(String, String, ContinuationImpl)
//   → fetches shortened URL with correlation tracking
//   → p1 = input URL — return this directly
// ─────────────────────────────────────────────────────────────────────────────

// Fingerprint for the generatePostShareLink method (method 'a') in the
// obfuscated ShareLinkFactory class, anchored on new-instance of its stable
// inner continuation class.
private object GeneratePostShareLinkFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "L",
        "Lcom/reddit/sharing/SharingNavigator\$ShareTrigger;",
        "L",
    ),
    filters = listOf(
        newInstance("Lcom/reddit/sharing/custom/url/ShareLinkFactory\$generatePostShareLink\$1;"),
    ),
)

// Fingerprint for the getShortUrl method (method 'c') in ShareLinkFactory,
// anchored on new-instance of its stable inner continuation class.
private object GetShortUrlFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "L"),
    filters = listOf(
        newInstance("Lcom/reddit/sharing/custom/url/ShareLinkFactory\$getShortUrl\$1;"),
    ),
)

@Suppress("unused")
val redditSanitizeSharingLinksPatch = bytecodePatch(
    name = "Sanitize Sharing Links",
    description = "Returns the raw permalink before UTM tracking parameters are appended to shared Reddit URLs.",
    default = true,
) {
    compatibleWith(REDDIT_COMPATIBILITY)

    execute {
        // generatePostShareLink: p1 = permalink String → return it directly
        runCatching {
            // Return p1 (first String param) as a wrapped coroutine result
            // wrapped in kotlin.Unit won't work; we need to return p1 directly.
            // But this is a suspend fun returning Object — returning p1 (String)
            // is valid: the caller casts result to String.
            GeneratePostShareLinkFingerprint.method.addInstructions(0, "return-object p1")
        }

        // getShortUrl: p1 = input URL String → return it directly (skip network call)
        runCatching {
            GetShortUrlFingerprint.method.addInstructions(0, "return-object p1")
        }
    }
}
