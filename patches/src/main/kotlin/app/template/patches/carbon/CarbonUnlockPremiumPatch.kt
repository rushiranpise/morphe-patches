package app.template.patches.carbon

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.CARBON_COMPATIBILITY
import java.security.MessageDigest

@Suppress("unused")
val carbonUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks Carbon premium subscription.",
    default = true
) {
    compatibleWith(CARBON_COMPATIBILITY)

    execute {
        val bundlePath = "assets/index.android.bundle"
        val bundle = get(bundlePath)
        if (!bundle.exists()) throw PatchException("$bundlePath not found.")

        val bytes = bundle.readBytes()
        bytes.requireHermes(bundlePath)
        if (!bytes.containsAscii("setIsSubscribed") || !bytes.containsAscii("hasActiveSubscription")) {
            throw PatchException("Carbon subscription signatures not found.")
        }

        val patched = bytes.forceBooleanGetByIdTrue(
            mapOf(
                47940 to "isSubscribed"
            )
        ) + bytes.forceBooleanAtOffsetsTrue(
            listOf(
                0x72A6FD to "settings status isActive 1",
                0x72A736 to "settings status isActive 2",
                0x72A8A3 to "settings status isActive 3"
            ),
            28130
        )
        if (patched == 0) {
            throw PatchException("No Carbon subscription gates were patched.")
        }

        bundle.writeBytes(bytes.withUpdatedSha1())
    }
}

private val hermesMagic = byteArrayOf(0xC6.toByte(), 0x1F, 0xBC.toByte(), 0x03)

private fun ByteArray.requireHermes(path: String) {
    if (size < 12 || !copyOfRange(0, hermesMagic.size).contentEquals(hermesMagic)) {
        throw PatchException("Invalid Hermes bytecode bundle: $path")
    }
    val version = readLittleEndianInt(8)
    if (version != 96) {
        throw PatchException("Unsupported Carbon Hermes bytecode version: $version")
    }
}

private fun ByteArray.readLittleEndianInt(offset: Int): Int {
    var value = 0
    for (index in 0 until 4) {
        value = value or ((this[offset + index].toInt() and 0xFF) shl (index * 8))
    }
    return value
}

private fun ByteArray.containsAscii(value: String): Boolean {
    val pattern = value.toByteArray(Charsets.UTF_8)
    val last = size - pattern.size
    outer@ for (start in 0..last) {
        for (index in pattern.indices) {
            if (this[start + index] != pattern[index]) continue@outer
        }
        return true
    }
    return false
}

private fun ByteArray.forceBooleanGetByIdTrue(stringIds: Map<Int, String>): Int {
    var patched = 0
    for (index in 0 until size - 5) {
        if (this[index].toInt() != 0x37) continue

        val stringId = (this[index + 4].toInt() and 0xFF) or
            ((this[index + 5].toInt() and 0xFF) shl 8)
        if (stringId !in stringIds) continue

        val destinationRegister = this[index + 1]
        this[index] = 0x78
        this[index + 1] = destinationRegister
        this[index + 2] = 0x78
        this[index + 3] = destinationRegister
        this[index + 4] = 0x78
        this[index + 5] = destinationRegister
        patched++
    }
    return patched
}

private fun ByteArray.forceBooleanAtOffsetsTrue(offsets: List<Pair<Int, String>>, stringId: Int): Int {
    var patched = 0
    for ((index, name) in offsets) {
        if (index < 0 || index + 5 >= size || this[index].toInt() != 0x37) {
            throw PatchException("Carbon $name signature not found.")
        }
        val actualStringId = (this[index + 4].toInt() and 0xFF) or
            ((this[index + 5].toInt() and 0xFF) shl 8)
        if (actualStringId != stringId) {
            throw PatchException("Carbon $name string id mismatch.")
        }

        val destinationRegister = this[index + 1]
        this[index] = 0x78
        this[index + 1] = destinationRegister
        this[index + 2] = 0x78
        this[index + 3] = destinationRegister
        this[index + 4] = 0x78
        this[index + 5] = destinationRegister
        patched++
    }
    return patched
}

private fun ByteArray.withUpdatedSha1(): ByteArray {
    val updated = copyOf()
    val digest = MessageDigest.getInstance("SHA-1")

    val content = updated.copyOf(size - 20)
    val trailingHash = digest.digest(content)
    trailingHash.copyInto(updated, size - 20)

    val sourceHashInput = updated.copyOfRange(0, 12) + updated.copyOfRange(32, updated.size)
    val sourceHash = MessageDigest.getInstance("SHA-1").digest(sourceHashInput)
    sourceHash.copyInto(updated, 12)

    return updated
}
