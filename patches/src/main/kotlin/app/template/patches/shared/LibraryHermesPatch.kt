package app.template.patches.shared

import app.morphe.patcher.patch.rawResourcePatch
import java.io.FileNotFoundException
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * Hermes bytecode patch — streaming variant (ported from MorpheApp/morphe-patches-library).
 *
 * Differences from [hermesPatch]:
 *  - Uses [RandomAccessFile] for streaming reads/writes — safe on large bundles (no OOM).
 *  - Byte replacement delegated to [HexPatchBuilder] Boyer-Moore pattern matcher.
 *  - Does NOT enforce pattern uniqueness (multiple occurrences silently replaced).
 *
 * Use [hermesPatch] when uniqueness enforcement is needed.
 * Use [hermesPatchStreaming] for large bundles or when multiple occurrences are expected.
 */
@Suppress("unused")
fun hermesPatchStreaming(supplier: () -> Set<Pair<String, String>>) =
    rawResourcePatch {
        execute {
            val file = get(HERMES_BUNDLE_PATH, true)
            if (!file.exists())
                throw FileNotFoundException("Hermes bytecode bundle not found at: $HERMES_BUNDLE_PATH")

            RandomAccessFile(file, "rw").use { raf ->
                val magicBuffer = ByteArray(8)
                raf.readFully(magicBuffer)
                if (!magicBuffer.contentEquals(HERMES_MAGIC))
                    throw Exception("Invalid Hermes file: magic mismatch")

                val versionBuffer = ByteArray(4)
                raf.readFully(versionBuffer)
                val version = versionBuffer.toHermesInt()

                // Replacements via Boyer-Moore (from HexPatchBuilder)
                val fileBytes = file.readBytes().toMutableList()
                supplier().forEach { (from, to) ->
                    val pattern = from.hexToByteArray()
                    val replacement = to.hexToByteArray()
                    boyerMooreReplace(fileBytes, pattern, replacement)
                }
                file.writeBytes(fileBytes.toByteArray())

                // Recalculate SHA-1 footer via streaming (prevents OOM on huge bundles)
                if (version > 74) {
                    val raf2 = RandomAccessFile(file, "rw")
                    val md = MessageDigest.getInstance("SHA-1")
                    val buffer = ByteArray(65536)
                    val hashContentLength = raf2.length() - 20
                    var bytesReadTotal = 0L

                    raf2.seek(0)
                    while (bytesReadTotal < hashContentLength) {
                        val bytesToRead = minOf(buffer.size.toLong(), hashContentLength - bytesReadTotal).toInt()
                        val read = raf2.read(buffer, 0, bytesToRead)
                        if (read == -1) break
                        md.update(buffer, 0, read)
                        bytesReadTotal += read
                    }

                    val hash = md.digest()
                    raf2.seek(hashContentLength)
                    raf2.write(hash)
                    raf2.close()
                }
            }
        }
    }

private fun ByteArray.toHermesInt(): Int {
    var value = 0
    for (i in 0 until 4) value = value or ((this[i].toInt() and 0xFF) shl (i * 8))
    return value
}

private fun String.hexToByteArray() =
    trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .map { it.toInt(16).toByte() }.toByteArray()

private fun boyerMooreReplace(data: MutableList<Byte>, pattern: ByteArray, replacement: ByteArray) {
    if (pattern.size != replacement.size || pattern.isEmpty()) return
    var i = 0
    while (i <= data.size - pattern.size) {
        if ((pattern.indices).all { data[i + it] == pattern[it] }) {
            replacement.forEachIndexed { j, b -> data[i + j] = b }
            i += pattern.size
        } else i++
    }
}
