package app.template.patches.boldvoice

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.BOLDVOICE_COMPATIBILITY
import java.security.MessageDigest

@Suppress("unused")
val boldVoiceUnlockPremiumPatch = rawResourcePatch(
    name = "Unlock Premium",
    description = "Unlocks BoldVoice premium subscription.",
    default = true
) {
    compatibleWith(BOLDVOICE_COMPATIBILITY)

    execute {
        val bundlePath = "assets/index.android.bundle"
        val bundle = get(bundlePath)
        if (!bundle.exists()) throw PatchException("$bundlePath not found.")

        val bytes = bundle.readBytes()
        bytes.requireHermes(bundlePath)

        bytes.replaceUnique(
            "subStatus selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 BF 6B 5C 00"),
            hex("73 00 DD 54 5C 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription update isSubscriber",
            hex("37 0E 01 04 08 F6"),
            hex("78 0E 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription update isProSubscriber",
            hex("37 0D 01 05 98 F5"),
            hex("78 0D 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription update subStatus",
            hex("37 0C 01 06 BF 6B"),
            hex("73 0C DD 54 76 00")
        )
        bytes.replaceUnique(
            "isProSubscriber selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 98 F5 5C 00"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "isSubscriber selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 08 F6 5C 00"),
            hex("78 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription navigation gate",
            hex("29 00 00 2E 01 00 00 37 07 01 01 8B CE 2E 01 00"),
            hex("73 00 E4 6F 5C 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription duration selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 CA CE 5C 00"),
            hex("6E 00 0C 5C 00 76 00 76 00 76 00 76 00 76 00 76")
        )
        bytes.replaceUnique(
            "subscription product selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 6A C5 5C 00"),
            hex("73 00 B4 60 5C 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription store selector",
            hex("6C 00 01 36 00 00 01 F8 37 00 00 02 E9 65 5C 00"),
            hex("73 00 0E 9E 5C 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription expired/refund status needles",
            hex("73 04 5F 5B 53 00 05 06 04 90 21 00 2E 04 03 0A 0E 02 04 02 76 01 90 11 02 36 03 04 01 A6 73 02 3A 41"),
            hex("73 04 0E 9E 53 00 05 06 04 90 21 00 2E 04 03 0A 0E 02 04 02 76 01 90 11 02 36 03 04 01 A6 73 02 0E 9E")
        )
        bytes.replaceUnique(
            "subscription not renew status needle",
            hex("29 00 00 2E 03 00 0A 77 00 0E 01 03 00 76 00 90 11 01 36 02 03 01 A6 73 01 B5 43 53 00 02 03 01 5C 00"),
            hex("29 00 00 2E 03 00 0A 77 00 0E 01 03 00 76 00 90 11 01 36 02 03 01 A6 73 01 0E 9E 53 00 02 03 01 5C 00")
        )
        bytes.replaceUnique(
            "subscription trial status needle",
            hex("29 00 00 2E 03 00 0A 77 00 0E 01 03 00 76 00 90 11 01 36 02 03 01 A6 73 01 83 10 53 00 02 03 01 5C 00"),
            hex("29 00 00 2E 03 00 0A 77 00 0E 01 03 00 76 00 90 11 01 36 02 03 01 A6 73 01 0E 9E 53 00 02 03 01 5C 00")
        )
        bytes.replaceUnique(
            "super upsell gate",
            hex("0B 08 08 92 06 08 0B 08 09 93 A6 00 00 00 08"),
            hex("0B 08 08 92 06 08 0B 08 09 91 A6 00 00 00 08")
        )
        bytes.replaceUnique(
            "just speak hasAccess result",
            hex("40 00 03 07 B1 40 00 02 E1 F0 40 00 01 AE EE"),
            hex("40 00 05 07 B1 40 00 02 E1 F0 40 00 01 AE EE")
        )
        bytes.replaceUnique(
            "profile active subscription details",
            hex("6C 00 01 37 0B 00 01 91 9E 37 0F 00 02 37 6A 37 08 00 03 8B CE 29 0C 00"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "ai chat upsell card",
            hex("32 0C 6C 01 01 37 02 01 01 47 5F 2A 0C 00 02 37 05 01 02 84 B9 37 00 01"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "ai chat upsell modal",
            hex("32 24 6C 00 01 37 05 00 01 DF 54 2A 24 00 05 37 04 00 02 DA 7D 2A 24 01"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "free trial gift modal",
            hex("32 0D 6C 01 01 37 00 01 01 80 54 2A 0D 00 00 37 02 01 02 D7 8B 2A 0D 01"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "one time offer navigation callback",
            hex("29 00 00 2E 02 00 00 37 01 02 01 7B 87 73 00 D5 AB 53 00 01 02 00 76 00 5C 00"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription navigation callback",
            hex("29 00 00 2E 01 00 02 90 07 01 76 01 5C 01 29 01 01 2E 02 01 05 36 03 02 01 60"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "profile start trial callback",
            hex("32 00 8A 00 00 03 02 01 00 5C 00"),
            hex("76 00 5C 00 8A 00 00 03 02 01 00")
        )
        bytes.replaceUnique(
            "profile subscription callback",
            hex("32 00 8A 00 00 05 02 01 00 5C 00"),
            hex("76 00 5C 00 8A 00 00 05 02 01 00")
        )
        bytes.replaceUnique(
            "show paywall action",
            hex("6C 02 01 29 00 00 2E 00 00 00 90 15 00 29 00 01 2E 00 00 20 76 01 51 00 00 01 53 00 02 01 00 03 01 29 00 01 2E 04 00 00 2E 03 00 01 6E 00 1A 49 03 03 00 76 00 53 03 04 00 03 37 03 03 01 93 6F 3F 01 03 D4 53 01 02 00 01 5C 00"),
            hex("76 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 88")
        )
        bytes.replaceUnique(
            "variable timeline paywall screen",
            hex("32 2C 6C 00 01 37 0B 00 01 86 55 2A 2C 00 0B 37 35 00 02 47 5F 2A 2C 01 35 37 55 00 03 21 A8 2A 2C 02 55 29 08 00 2E 00"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "standard premium selector screen",
            hex("32 17 6C 00 01 37 09 00 01 47 5F 37 0E 00 02 84 B9 29 05 00 2E 00 05 15 76 03 51 00 00 03 36 00 00 03 F2 51 00 00 03 36"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "free trial gift screen",
            hex("32 00 6C 01 01 37 01 01 01 86 55 2A 00 00 01 29 05 00 2E 01 05 0C 76 03 51 01 01 03 36 06 01 02 60 36 04 06 03 F1 65 02"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "free trial paywall screen",
            hex("32 0F 29 10 00 2E 00 10 0E 76 03 51 00 00 03 37 00 00 01 81 5A 51 00 00 03 2A 0F 00 00 2E 01 10 07 36 01 01 02 60"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "annual subscription chooser",
            hex("32 01 6C 00 01 37 09 00 01 47 5F 2A 01 00 09 37 04 00 02 84 B9 2A 01 01 04 37 05 00 03 D8 79 29 00 00 2E 02 00 0C"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription chooser",
            hex("32 19 6C 00 01 37 1F 00 01 47 5F 2A 19 00 1F 37 1E 00 02 84 B9 2A 19 01 1E 37 21 00 03 37 6A 29 09 00 2E 00 09 04"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )
        bytes.replaceUnique(
            "subscription screen v2",
            hex("32 00 6C 01 01 37 04 01 01 86 55 2A 00 00 04 37 09 01 02 47 5F 2A 00 01 09 29 05 00 2E 01 05 0A 76 03 51 01 01 03"),
            hex("77 00 5C 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00 76 00")
        )

        bundle.writeBytes(bytes.withUpdatedSha1())
    }
}

private val hermesMagic = byteArrayOf(0xC6.toByte(), 0x1F, 0xBC.toByte(), 0x03)

private fun ByteArray.requireHermes(path: String) {
    if (size < 12 || !copyOfRange(0, hermesMagic.size).contentEquals(hermesMagic)) {
        throw PatchException("Invalid Hermes bytecode bundle: $path")
    }
}

private fun ByteArray.withUpdatedSha1(): ByteArray {
    val content = dropLast(20).toByteArray()
    return content + MessageDigest.getInstance("SHA-1").digest(content)
}

private fun ByteArray.replaceUnique(name: String, pattern: ByteArray, replacement: ByteArray) {
    if (pattern.size != replacement.size) {
        throw PatchException("BoldVoice $name replacement size mismatch.")
    }

    val offset = indexOf(pattern)
    if (offset < 0) {
        throw PatchException("BoldVoice $name pattern not found.")
    }
    if (indexOf(pattern, offset + 1) >= 0) {
        throw PatchException("BoldVoice $name pattern is not unique.")
    }

    replacement.copyInto(this, offset)
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

private fun hex(value: String): ByteArray =
    value.trim().split(Regex("\\s+")).filter(String::isNotEmpty)
        .map { it.toInt(16).toByte() }
        .toByteArray()
