package app.template.patches.shared

import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.PatchException
import java.io.FileNotFoundException
import java.security.MessageDigest

internal const val HERMES_BUNDLE_PATH = "assets/index.android.bundle"
internal val HERMES_MAGIC = bytesOf("C6 1F BC 03 C1 03 19 1F")

fun hermesPatch(supplier: () -> Set<Pair<String, String>>) =
    rawResourcePatch {
        execute {
            val file = get(HERMES_BUNDLE_PATH, true)
            if (!file.exists()) {
                throw FileNotFoundException("Hermes bytecode bundle not found at: $HERMES_BUNDLE_PATH")
            }

            var fileBytes = file.readBytes()
            if (!fileBytes.copyOfRange(0, HERMES_MAGIC.size).contentEquals(HERMES_MAGIC)) {
                throw PatchException("Invalid Hermes bytecode bundle: $HERMES_BUNDLE_PATH")
            }

            val version = fileBytes.readLittleEndianInt(8)
            supplier().forEach { (from, to) ->
                fileBytes.replaceUnique(bytesOf(from), bytesOf(to))
            }

            if (version > 74) {
                val content = fileBytes.dropLast(20).toByteArray()
                fileBytes = content + MessageDigest.getInstance("SHA-1").digest(content)
            }

            file.writeBytes(fileBytes)
        }
    }

private fun bytesOf(hex: String): ByteArray =
    hex.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .map { it.toInt(16).toByte() }
        .toByteArray()

private fun ByteArray.readLittleEndianInt(offset: Int): Int {
    var value = 0
    for (index in 0 until 4) {
        value = value or ((this[offset + index].toInt() and 0xFF) shl (index * 8))
    }
    return value
}

private fun ByteArray.replaceUnique(pattern: ByteArray, replacement: ByteArray) {
    if (pattern.size != replacement.size) {
        throw PatchException("Hermes replacement size mismatch.")
    }
    val index = indexOf(pattern)
    if (index < 0) {
        throw PatchException("Hermes pattern not found: ${pattern.toHex()}")
    }
    if (indexOf(pattern, index + 1) >= 0) {
        throw PatchException("Hermes pattern is not unique: ${pattern.toHex()}")
    }
    replacement.copyInto(this, index)
}

private fun ByteArray.indexOf(pattern: ByteArray, start: Int = 0): Int {
    if (pattern.isEmpty() || pattern.size > size) return -1
    val last = size - pattern.size
    outer@ for (offset in start..last) {
        for (index in pattern.indices) {
            if (this[offset + index] != pattern[index]) continue@outer
        }
        return offset
    }
    return -1
}

private fun ByteArray.toHex(): String =
    joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
