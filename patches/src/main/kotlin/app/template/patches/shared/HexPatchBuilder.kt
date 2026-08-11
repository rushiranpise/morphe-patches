@file:Suppress("unused")

package app.template.patches.shared

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.byteArrayOf
import java.io.RandomAccessFile
import kotlin.math.max

@Suppress("DEPRECATION")
fun hexPatch(ignoreMissingTargetFiles: Boolean = false, block: HexPatchBuilder.() -> Unit) =
    hexPatch(ignoreMissingTargetFiles, fun(): Set<Replacement> = HexPatchBuilder().apply(block))

@Suppress("JavaDefaultMethodsNotOverriddenByDelegation")
class HexPatchBuilder internal constructor(
    private val replacements: MutableSet<Replacement> = mutableSetOf(),
) : Set<Replacement> by replacements {
    infix fun String.asPatternTo(replacementPattern: String) = byteArrayOf(this) to byteArrayOf(replacementPattern)

    infix fun <T> Pair<T, T>.inFile(filePath: String) {
        when (first) {
            is String if second is String -> {
                val first = first as String
                val second = second as String

                replacements += Replacement(
                    first.toByteArray(), second.toByteArray(),
                    filePath
                )
            }

            is ByteArray if second is ByteArray -> {
                val first = first as ByteArray
                val second = second as ByteArray

                replacements += Replacement(first, second, filePath)
            }

            else -> {
                throw PatchException("Unsupported types for pattern and replacement: $first, $second")
            }
        }
    }
}

// The replacements being passed using a function is intended.
// Previously the replacements were a property of the patch. Getter were being delegated to that property.
// This late evaluation was being leveraged in app.morphe.patches.all.misc.hex.HexPatch.
// Without the function, the replacements would be evaluated at the time of patch creation.
// This isn't possible because the delegated property is not accessible at that time.
@Deprecated("Use the hexPatch function with the builder parameter instead.")
fun hexPatch(ignoreMissingTargetFiles: Boolean = false, replacementsSupplier: () -> Set<Replacement>) =
    rawResourcePatch {
        execute {
            replacementsSupplier().groupBy { it.targetFilePath }.forEach { (targetFilePath, replacements) ->
                val targetFile = get(targetFilePath, true)
                if (ignoreMissingTargetFiles && !targetFile.exists()) return@forEach

                RandomAccessFile(targetFile, "rw").use { raf ->
                    replacements.forEach { it.replacePattern(raf) }
                }
            }
        }
    }

/**
 * Represents a pattern to search for and its replacement pattern in a file.
 *
 * @property bytes The bytes to search for.
 * @property replacementBytes The bytes to replace the [bytes] with.
 * @property targetFilePath The path to the file to make the changes in relative to the APK root.
 */
class Replacement(
    private val bytes: ByteArray,
    private val replacementBytes: ByteArray,
    internal val targetFilePath: String,
) {
    val replacementBytesPadded = replacementBytes + ByteArray(bytes.size - replacementBytes.size)

    @Deprecated("Use the constructor with ByteArray parameters instead.")
    constructor(
        pattern: String,
        replacementPattern: String,
        targetFilePath: String,
    ) : this(
        byteArrayOf(pattern),
        byteArrayOf(replacementPattern),
        targetFilePath
    )

    /**
     * Replaces the [bytes] with the [replacementBytes] in the target file.
     *
     * @param targetFile The RandomAccessFile to make the changes in.
     */
    fun replacePattern(targetFile: RandomAccessFile) {
        val startIndex = indexOfPatternIn(targetFile)

        if (startIndex == -1L) {
            throw PatchException(
                "Pattern not found in target file: " +
                        bytes.joinToString(" ") { "%02x".format(it) }
            )
        }

        targetFile.seek(startIndex)
        targetFile.write(replacementBytesPadded)
    }

    /**
     * Returns the absolute offset of the first occurrence of [bytes] in the file
     * using a buffered Boyer-Moore algorithm.
     *
     * @param file The RandomAccessFile to search in.
     *
     * @return The offset of the first occurrence of the [bytes] in the file or -1L
     * if the [bytes] is not found.
     */
    private fun indexOfPatternIn(file: RandomAccessFile): Long {
        val needle = bytes
        val right = IntArray(256) { -1 }

        for ((i, element) in needle.withIndex()) right[element.toInt().and(0xFF)] = i

        val bufferSize = 65536
        val buffer = ByteArray(bufferSize + needle.size)

        var fileOffset = 0L
        val fileLength = file.length()

        while (fileOffset < fileLength) {
            file.seek(fileOffset)
            val bytesRead = file.read(buffer)

            if (bytesRead < needle.size) break

            var skip: Int
            var i = 0
            while (i <= bytesRead - needle.size) {
                skip = 0

                for (j in needle.size - 1 downTo 0) {
                    if (needle[j] != buffer[i + j]) {
                        skip = max(1, j - right[buffer[i + j].toInt().and(0xFF)])
                        break
                    }
                }

                if (skip == 0) return fileOffset + i
                i += skip
            }

            fileOffset += (bytesRead - needle.size + 1)
        }
        return -1L
    }
}
