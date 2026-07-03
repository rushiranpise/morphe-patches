package app.template.patches.shared.cert

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.logging.Logger
import java.util.zip.ZipInputStream

internal var autoSha256: String? = null
    private set
internal var autoSha1: String? = null
    private set
internal var autoBase64Der: String? = null
    private set

private val log = Logger.getLogger("CertExtractor")
private fun ByteArray.toHex() = joinToString("") { "%02X".format(it) }

private fun populateFromEncoded(encoded: ByteArray) {
    autoSha256    = MessageDigest.getInstance("SHA-256").digest(encoded).toHex()
    autoSha1      = MessageDigest.getInstance("SHA-1").digest(encoded).toHex()
    autoBase64Der = Base64.getEncoder().encodeToString(encoded)
    log.info("Cert: SHA-1=$autoSha1")
}

internal fun seedCert(base64Der: String) {
    populateFromEncoded(Base64.getDecoder().decode(base64Der))
}

private fun parseCertFromStream(stream: InputStream): ByteArray? =
    CertificateFactory.getInstance("X.509").generateCertificates(stream)
        .filterIsInstance<X509Certificate>().firstOrNull()?.encoded

private fun isCertEntry(name: String) =
    name.startsWith("META-INF/") && name.substringAfterLast('.') in setOf("RSA", "DSA", "EC")

private fun certFromApkBytes(apkBytes: ByteArray): ByteArray? {
    ZipInputStream(ByteArrayInputStream(apkBytes)).use { zis ->
        while (true) {
            val entry = zis.nextEntry ?: break
            if (!entry.isDirectory && isCertEntry(entry.name)) {
                val encoded = parseCertFromStream(zis)
                if (encoded != null) return encoded
            }
        }
    }
    return null
}

private fun extractFromSigningBlock(apkBytes: ByteArray): ByteArray? {
    val buf = java.nio.ByteBuffer.wrap(apkBytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
    var eocdOffset = -1
    for (i in apkBytes.size - 22 downTo maxOf(0, apkBytes.size - 65557)) {
        if (buf.getInt(i) == 0x06054b50.toInt()) { eocdOffset = i; break }
    }
    if (eocdOffset < 0) return null
    val cdOffset = buf.getInt(eocdOffset + 16)
    val blockEnd = cdOffset
    if (blockEnd < 32) return null
    val magic = byteArrayOf(0x41,0x50,0x4b,0x20,0x53,0x69,0x67,0x20,0x42,0x6c,0x6f,0x63,0x6b,0x20,0x34,0x32)
    if (!apkBytes.copyOfRange(blockEnd - 16, blockEnd).contentEquals(magic)) return null
    val blockSize = buf.getLong(blockEnd - 24)
    val blockStart = blockEnd - blockSize.toInt() - 8
    if (blockStart < 0) return null
    var pos = blockStart + 8
    val pairsEnd = blockEnd - 24
    while (pos < pairsEnd - 12) {
        val pairLen  = buf.getLong(pos).toInt()
        val pairId   = buf.getInt(pos + 8)
        val valueStart = pos + 12
        val valueEnd   = pos + 8 + pairLen
        if (pairId == 0x7109871a.toInt() || pairId == 0xf05368c0.toInt()) {
            if (valueEnd > valueStart + 28) {
                try {
                    val v = java.nio.ByteBuffer.wrap(apkBytes, valueStart, valueEnd - valueStart)
                        .order(java.nio.ByteOrder.LITTLE_ENDIAN)
                    v.int; v.int; v.int
                    val digestsLen = v.int; v.position(v.position() + digestsLen)
                    v.int
                    val certLen = v.int
                    if (certLen > 0 && certLen < apkBytes.size) {
                        val certBytes = ByteArray(certLen); v.get(certBytes)
                        val cert = CertificateFactory.getInstance("X.509")
                            .generateCertificate(ByteArrayInputStream(certBytes)) as? X509Certificate
                        if (cert != null) return cert.encoded
                    }
                } catch (_: Exception) {}
            }
        }
        pos = valueStart + pairLen - 4
        if (pairLen <= 4) break
    }
    return null
}

internal fun extractFromFile(file: File): Boolean {
    val fileBytes = file.readBytes()
    var apkBytes: ByteArray? = null
    ZipInputStream(ByteArrayInputStream(fileBytes)).use { outer ->
        while (true) {
            val entry = outer.nextEntry ?: break
            if (!entry.isDirectory && (entry.name == "base.apk" || entry.name.endsWith("/base.apk"))) {
                log.info("Bundle: reading base.apk from ${file.name}")
                apkBytes = outer.readBytes(); break
            }
        }
    }
    val targetBytes = apkBytes ?: fileBytes
    extractFromSigningBlock(targetBytes)?.let { populateFromEncoded(it); return true }
    certFromApkBytes(targetBytes)?.let { populateFromEncoded(it); return true }
    return false
}

/**
 * Reads the signing certificate from the original app.
 *
 * Other patches (GmsCore, Firebase, Signature spoof) all depend on this.
 * Apply this patch together with those — no options needed if the original
 * app is still installed on your device.
 *
 * If the original app is already uninstalled or replaced, provide its path below.
 * You can also paste cert values directly if you already have them
 * (e.g. from APKMirror or apksigner).
 */
val extractApkCertificatePatch = rawResourcePatch(
    name = "Provide Original app certificate",
    description = "By Default it Reads the signing certificate from the original app installed. " +
        "Required for GmsCore, Firebase, and Signature spoof patches. " +
        "Only fill options below if the original is uninstalled.",
    default = false,
) {
    val originalApkPath by stringOption(
        key = "originalApkPath",
        default = null,
        title = "Path to original APK (if uninstalled)",
        description = "Full path to the original APK or .apks bundle. " +
            "Example: /sdcard/Download/Waze.apks",
        required = false,
    ) { path -> path == null || File(path).let { it.exists() && it.isFile } }

    val manualCertSha1 by stringOption(
        key = "manualCertSha1",
        default = null,
        title = "Certificate SHA-1 (manual)",
        description = "40-char hex. Found on APKMirror under app signature info.",
        required = false,
    ) { it == null || it.matches(Regex("^[0-9A-Fa-f]{40}$")) }

    val manualCertSha256 by stringOption(
        key = "manualCertSha256",
        default = null,
        title = "Certificate SHA-256 (manual)",
        description = "64-char hex. Optional companion to SHA-1.",
        required = false,
    ) { it == null || it.matches(Regex("^[0-9A-Fa-f]{64}$")) }

    val manualCertBase64 by stringOption(
        key = "manualCertBase64",
        default = null,
        title = "Certificate Base64 DER (manual)",
        description = "Needed only for Signature spoof patch. " +
            "Get it with: apksigner verify --print-certs original.apk",
        required = false,
    ) { it == null || it.isNotBlank() }

    execute {
        // Manual values take priority
        val sha1Manual = manualCertSha1?.takeIf { it.length == 40 }
        if (sha1Manual != null) {
            autoSha1      = sha1Manual
            autoSha256    = manualCertSha256?.takeIf { it.length == 64 }
            autoBase64Der = manualCertBase64?.takeIf { it.isNotBlank() }
            log.info("Using manual cert values")
            return@execute
        }

        // Strategy 1: installed app sourceDir
        try {
            val manifestText = get("AndroidManifest.xml").readText()
            val pkgName = Regex("""package="([^"]+)"""").find(manifestText)?.groupValues?.get(1)
                ?: throw Exception("pkg not found")
            val ctx = Class.forName("android.app.ActivityThread")
                .getDeclaredMethod("currentApplication").apply { isAccessible = true }
                .invoke(null) ?: throw Exception("null ctx")
            val pm = ctx.javaClass.getMethod("getPackageManager").invoke(ctx)
                ?: throw Exception("null pm")
            val pkgInfo = pm.javaClass
                .getMethod("getPackageInfo", String::class.java, Int::class.java)
                .invoke(pm, pkgName, 0) ?: throw Exception("null pkgInfo")
            val appInfo = try {
                pkgInfo.javaClass.getMethod("getApplicationInfo").invoke(pkgInfo)
            } catch (_: Exception) { pkgInfo.javaClass.getField("applicationInfo").get(pkgInfo) }
                ?: throw Exception("null appInfo")
            val sourceDir = try {
                appInfo.javaClass.getMethod("getSourceDir").invoke(appInfo)
            } catch (_: Exception) { appInfo.javaClass.getField("sourceDir").get(appInfo) }
                as? String ?: throw Exception("null sourceDir")
            log.info("Reading cert from installed app: $sourceDir")
            if (extractFromFile(File(sourceDir))) return@execute
        } catch (e: Exception) {
            log.info("Installed app strategy failed: ${e.message}")
        }

        // Strategy 2: user-supplied path
        val path = originalApkPath
        if (path.isNullOrBlank()) {
            log.warning("No cert found. Provide original APK path or manual cert values.")
            return@execute
        }
        val file = File(path)
        if (!file.exists()) { log.warning("File not found: $path"); return@execute }
        log.info("Reading cert from: ${file.name}")
        if (!extractFromFile(file))
            log.warning("No cert in ${file.name} — ensure this is the original Google-signed build.")
    }
}
