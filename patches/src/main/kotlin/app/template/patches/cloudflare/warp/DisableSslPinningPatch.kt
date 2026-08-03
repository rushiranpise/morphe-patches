package app.template.patches.cloudflare.warp

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import app.template.patches.shared.Constants.WARP_COMPATIBILITY

/**
 * Targets OkHttpClient CertificatePinner.check(String, List)V
 * — the public overload that validates all TLS certificates against pinned hashes.
 *
 * WHY THIS EXISTS:
 * 1.1.1.1 / WARP pins ~70 CA public key hashes for *.cloudflareclient.com in
 * StandardRootCASetForAPI (com.cloudflare.common.certificateauthority).
 * These are passed to OkHttp's CertificatePinner at startup via WarpModule.
 * Any TLS connection to the Cloudflare API (registration, account fetching,
 * config updates) will be terminated if the server certificate chain doesn't
 * contain one of the pinned hashes — making traffic inspection impossible.
 *
 * WHY THIS TARGET:
 * OkHttp exposes three check() overloads:
 *   - check(String, List<X509Certificate>) — public, called by OkHttp's RealConnection
 *   - check(String, Certificate...) — varargs, delegates to the List overload
 *   - check$okhttp(String, Function0<List<X509Certificate>>) — internal, calls the List overload
 *
 * Returning void from check(String, List) short-circuits all three paths:
 * the varargs and Function0 overloads both delegate to this one. No exception
 * is thrown → OkHttp proceeds with the connection as if it passed pin validation.
 *
 * WHY THIS CLASS IS STABLE:
 * OkHttpClient is bundled (not from the system) and its class name is stable because:
 *   1. It's referenced by name in Dagger/Hilt injection bindings (cannot be renamed by R8)
 *   2. The CertificatePinner class is part of OkHttp's public API surface, kept by
 *      the library's own consumer ProGuard rules.
 *
 * NOTE: This patch is NOT enabled by default. It is only useful for network traffic
 * inspection or debugging. It has no effect on WARP tunnel functionality.
 * It does NOT bypass Cloudflare's server-side account validation.
 *
 * VERIFIED v6.38.8 (versionCode 5431):
 *   classes2/okhttp3/CertificatePinner.smali line 256:
 *     .method public final check(Ljava/lang/String;Ljava/util/List;)V
 *     .registers 4
 *     [no try-catch blocks]
 *
 *   classes2/okhttp3/CertificatePinner.smali line 326:
 *     .method public final check$okhttp(Ljava/lang/String;Lkotlin/jvm/functions/Function0;)V
 *     [delegates to check(String, List) after resolving the lambda]
 */
private object CertificatePinnerCheckFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/List;"),
    custom = { _, classDef ->
        classDef.type == "Lokhttp3/CertificatePinner;"
    },
)

@Suppress("unused")
val disableSslPinningPatch = bytecodePatch(
    name = "Disable SSL Pinning",
    description = "Bypasses OkHttp certificate pinning on Cloudflare API calls to allow TLS traffic inspection.",
    default = false,
) {
    compatibleWith(WARP_COMPATIBILITY)

    execute {
        CertificatePinnerCheckFingerprint.method.addInstructions(0, "return-void")
    }
}
