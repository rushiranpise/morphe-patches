package app.template.patches.lawfully

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.rawResourcePatch
import app.template.patches.shared.Constants.LAWFULLY_COMPATIBILITY

private val libappPatch = rawResourcePatch {
    execute {
        val lib = get("lib/arm64-v8a/libapp.so")
            ?: throw PatchException("lib/arm64-v8a/libapp.so not found — merge base + arm64-v8a split.")
        val raw = lib.readBytes().toMutableList()
        val NOP   = listOf(0x1F, 0x20, 0x03, 0xD5).map { it.toByte() }
        val bPrem = listOf(0x06, 0x00, 0x00, 0x14).map { it.toByte() }
        val TRUE4 = listOf(0xC0, 0x82, 0x00, 0x91).map { it.toByte() }
        val FALSE4 = listOf(0xC0, 0xC2, 0x00, 0x91).map { it.toByte() }

        fun find(sig: List<Int>): Int {
            val pat = sig.map { it.toByte() }
            var r: Int? = null
            outer@ for (i in 0..raw.size - pat.size) {
                for (j in pat.indices) if (raw[i + j] != pat[j]) continue@outer
                if (r != null) throw PatchException("Sig >1 match")
                r = i
            }
            return r ?: throw PatchException("Sig not found: ${sig.take(4)}")
        }

        fun write(base: Int, off: Int, data: List<Byte>) =
            data.forEachIndexed { i, b -> raw[base + off + i] = b }

        // A: display gate
        find(listOf(0x3F,0x00,0x16,0x6B,0x60,0x0B,0x00,0x54,0x22,0xF0,0x45,0xB8,0x42,0x80,0x1C,0x8B,0x70,0xB7,0x4C,0xF9,0x5F,0x00,0x10,0x6B,0x61,0x03,0x00,0x54)).also {
            write(it, 0x04, bPrem); write(it, 0x18, NOP); write(it, 0x2C, NOP)
        }

        // B: sub-state gate TRUE→FALSE
        find(listOf(0x41,0x37,0x40,0xF9,0x21,0x6C,0x49,0xF9,0x3F,0x00,0x16,0x6B,0x60,0x02,0x00,0x54,0x22,0x30,0x46,0xB8,0x42,0x80,0x1C,0x8B,0xE2,0x00,0x20,0x37)).also { b ->
            listOf(0x2C, 0x48).forEach { off ->
                if (raw.subList(b + off, b + off + 4) == TRUE4) write(b, off, FALSE4)
            }
        }

        // C: NOP premium reset sturs
        find(listOf(0xFF,0x01,0x10,0xEB,0xC9,0x05,0x00,0x54,0x40,0xB0,0x02,0xB8,0x40,0xF0,0x02,0xB8,0x40,0x30,0x03,0xB8)).also {
            write(it, 0x08, NOP); write(it, 0x0C, NOP); write(it, 0x10, NOP)
        }

        lib.writeBytes(raw.toByteArray())
    }
}

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium Features in app.",
    default = true,
) {
    dependsOn(libappPatch)
    compatibleWith(LAWFULLY_COMPATIBILITY)
    execute { IsActiveFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }
}
