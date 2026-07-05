package app.template.patches.tracked

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.TRACKED_COMPATIBILITY
import java.security.MessageDigest

private val trackedUnlockPremiumHermesPatch = rawResourcePatch(
    name = "Unlock Premium Hermes gates",
    description = "Unlocks Tracked's local React Native premium gate.",
    default = false,
) {
    compatibleWith(TRACKED_COMPATIBILITY)

    execute {
        val bundlePath = "assets/index.android.bundle"
        val bundle = get(bundlePath)
        if (!bundle.exists()) throw PatchException("$bundlePath not found.")

        val bytes = bundle.readBytes()
        bytes.requireTrackedHermes(bundlePath)
        if (!bytes.containsAscii("useIsActive") || !bytes.containsAscii("Tracked Pro")) {
            throw PatchException("Tracked Hermes premium signatures not found.")
        }

        var patched = 0
        patched += bytes.returnTrueAtFunctionOffset(0x00CC3094, "useIsActive")
        patched += bytes.forceProTierComparison()
        patched += bytes.replaceAsciiUnique("requiresPro", "disabledPro")

        if (patched == 0) throw PatchException("No Tracked Hermes gates were patched.")
        bundle.writeBytes(bytes.withUpdatedSha1())
    }
}

@Suppress("unused")
val trackedUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features.",
    default = true,
) {
    compatibleWith(TRACKED_COMPATIBILITY)
    dependsOn(trackedUnlockPremiumHermesPatch)

    execute {
        EntitlementIsActiveFingerprint
            .match(classDefBy(EntitlementIsActiveFingerprint.definingClass!!))
            .method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")
    }
}

private val trackedHermesMagic = byteArrayOf(0xC6.toByte(), 0x1F, 0xBC.toByte(), 0x03)

private fun ByteArray.requireTrackedHermes(path: String) {
    if (size < 12 || !copyOfRange(0, trackedHermesMagic.size).contentEquals(trackedHermesMagic)) {
        throw PatchException("Invalid Hermes bytecode bundle: $path")
    }
    val version = readLittleEndianInt(8)
    if (version != 98) {
        throw PatchException("Unsupported Tracked Hermes bytecode version: $version")
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

private fun ByteArray.returnTrueAtFunctionOffset(offset: Int, name: String): Int {
    if (offset < 0 || offset + 3 >= size || this[offset].toInt() != 0x34) {
        throw PatchException("Tracked $name signature not found.")
    }
    this[offset] = 0x95.toByte()
    this[offset + 1] = 0x00
    this[offset + 2] = 0x76
    this[offset + 3] = 0x00
    return 1
}

private fun ByteArray.forceProTierComparison(): Int {
    val signature = byteArrayOf(
        0x3B, 0x08, 0x05, 0x00,
        0x90.toByte(), 0x07, 0x1E, 0x7A,
        0x17, 0x07, 0x08, 0x07,
        0xB0.toByte(), 0x11, 0x07,
        0x3B, 0x08, 0x05, 0x00,
        0x91.toByte(), 0x05, 0x8E.toByte(), 0x33, 0x01, 0x00,
        0x17, 0x07, 0x08, 0x05
    )
    val offset = indexOfBytes(signature)
    if (offset < 0) throw PatchException("Tracked Pro tier comparison signature not found.")
    if (indexOfBytes(signature, offset + 1) >= 0) {
        throw PatchException("Tracked Pro tier comparison signature is ambiguous.")
    }
    byteArrayOf(0x95.toByte(), 0x07, 0xAE.toByte(), 0x02).copyInto(this, offset + 8)
    return 1
}

private fun ByteArray.replaceAsciiUnique(from: String, to: String): Int {
    if (from.length != to.length) throw PatchException("Tracked string replacement length mismatch.")
    val source = from.toByteArray(Charsets.UTF_8)
    val target = to.toByteArray(Charsets.UTF_8)
    val offset = indexOfBytes(source)
    if (offset < 0) throw PatchException("Tracked $from signature not found.")
    if (indexOfBytes(source, offset + 1) >= 0) {
        throw PatchException("Tracked $from signature is ambiguous.")
    }
    target.copyInto(this, offset)
    return 1
}

private fun ByteArray.indexOfBytes(pattern: ByteArray, startIndex: Int = 0): Int {
    val last = size - pattern.size
    outer@ for (start in startIndex..last) {
        for (index in pattern.indices) {
            if (this[start + index] != pattern[index]) continue@outer
        }
        return start
    }
    return -1
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
