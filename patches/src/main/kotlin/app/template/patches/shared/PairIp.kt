package app.template.patches.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import java.util.Base64

private const val SIGNATURE_CHECK = "Lcom/pairip/SignatureCheck;"
private const val VM_RUNNER = "Lcom/pairip/VMRunner;"
private const val STARTUP_LAUNCHER = "Lcom/pairip/StartupLauncher;"

fun BytecodePatchContext.gutPairIpVm(vararg extraNoOps: Pair<String, String>) {
    mutableClassDefBy(SIGNATURE_CHECK).methods.first { it.name == "verifyIntegrity" }.returnEarly()
    val vmRunner = mutableClassDefBy(VM_RUNNER)
    vmRunner.methods.first { it.name == "<clinit>" }.returnEarly()
    vmRunner.methods.first { it.name == "invoke" }.returnEarly()
    mutableClassDefBy(STARTUP_LAUNCHER).methods.first { it.name == "launch" }.returnEarly()
    for ((cls, method) in extraNoOps)
        mutableClassDefBy(cls).methods.first { it.name == method }.returnEarly()
}

fun BytecodePatchContext.restorePairIpHolders(resourceDir: String) {
    fun resource(name: String) =
        object {}.javaClass.getResourceAsStream("/$resourceDir/$name")?.bufferedReader()?.readText()
            ?: error("Missing: /$resourceDir/$name")

    fun bakeClinit(type: String, registerCount: Int, body: String) {
        val holder = mutableClassDefByOrNull(type) ?: error("Holder $type not found")
        if (holder.methods.any { it.name == "<clinit>" }) error("$type already has <clinit>")
        val clinit = ImmutableMethod(
            type, "<clinit>", emptyList(), "V",
            AccessFlags.STATIC.value or AccessFlags.CONSTRUCTOR.value,
            null, null, ImmutableMethodImplementation(registerCount, emptyList(), null, null),
        ).toMutable()
        holder.methods.add(clinit)
        clinit.addInstructions(0, body)
    }

    var type: String? = null
    val body = StringBuilder()
    fun flushStrings() {
        val t = type ?: return
        body.append("return-void")
        bakeClinit(t, 1, body.toString())
        body.setLength(0)
    }
    resource("depairip_strings.tsv").lineSequence().filter { it.isNotBlank() }.forEach { line ->
        if (line.startsWith("@")) { flushStrings(); type = line.substring(1) }
        else {
            val tab = line.indexOf('\t')
            body.append("const-string v0, \"${line.substring(tab + 1)}\"\nsput-object v0, $type->${line.substring(0, tab)}:Ljava/lang/String;\n")
        }
    }
    flushStrings()

    resource("depairip_methods.tsv").lineSequence().filter { it.isNotBlank() }.forEach { line ->
        val (t, rc, b64) = line.split('\t', limit = 3)
        bakeClinit(t, rc.toInt(), String(Base64.getDecoder().decode(b64)))
    }
}

// ── killPairIpFull ────────────────────────────────────────────────────────────

/**
 * Full DEX-layer Pairip kill. No-ops:
 *   VMRunner.<clinit>()                    — stops System.loadLibrary("pairipcore")
 *   VMRunner.invoke()                      — belt-and-suspenders
 *   SignatureCheck.verifyIntegrity()       — DEX cert check
 *   SignatureCheck.verifySignatureMatches() — returns true
 *   StartupLauncher.launch()               — IAP VM invoker
 *   LicenseClient.initializeLicenseCheck() — Play LVL, forced LOCAL_CHECK_OK
 *   LicenseClient.connectToLicensingService()
 *   LicenseClient.processResponse()        — NOT_LICENSED path
 *   LicenseClient.startPaywallActivity()   — paywall + System.exit failsafe
 *   All VMRunner.invoke() call sites outside com.pairip.*
 *
 * Also calls [initPairipStringHolders] to inject "" <clinit>s into string
 * holder classes to prevent NPEs when the native lib is not running.
 */
fun BytecodePatchContext.killPairIpFull(
    vararg extraNoOps: Pair<String, String>,
) {
    val sig   = "Lcom/pairip/SignatureCheck;"
    val vm    = "Lcom/pairip/VMRunner;"
    val sl    = "Lcom/pairip/StartupLauncher;"
    val lic   = "Lcom/pairip/licensecheck/LicenseClient;"
    val state = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

    mutableClassDefByOrNull(vm)?.methods?.firstOrNull { it.name == "<clinit>" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }
    mutableClassDefByOrNull(vm)?.methods?.firstOrNull { it.name == "invoke" }
        ?.returnEarly()

    mutableClassDefByOrNull(sig)?.methods?.firstOrNull { it.name == "verifyIntegrity" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }
    mutableClassDefByOrNull(sig)?.methods?.firstOrNull { it.name == "verifySignatureMatches" }
        ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

    mutableClassDefByOrNull(sl)?.methods?.firstOrNull { it.name == "launch" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == "initializeLicenseCheck" }
        ?.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, $state->LOCAL_CHECK_OK:$state
                sput-object v0, $lic->licenseCheckState:$state
                return-void
            """.trimIndent())
        }
    listOf("connectToLicensingService", "lambda\$retryOrThrow\$0", "processResponse", "startPaywallActivity")
        .forEach { name ->
            mutableClassDefByOrNull(lic)?.methods?.firstOrNull { it.name == name }
                ?.apply { clearBody(); addInstructions(0, "return-void") }
        }

    // Clear all VMRunner.invoke() call sites outside com.pairip.*
    classDefForEach { classDef ->
        if (classDef.type.startsWith("Lcom/pairip/")) return@classDefForEach
        val callers = classDef.methods.filter { method ->
            method.implementation?.instructions?.any { insn ->
                insn.opcode.name.startsWith("INVOKE") &&
                    (insn as? ReferenceInstruction)?.reference.let { ref ->
                        ref is MethodReference &&
                            ref.definingClass == vm &&
                            ref.name == "invoke"
                    }
            } == true
        }
        if (callers.isEmpty()) return@classDefForEach
        val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
        for (method in callers) {
            mutableClass.methods.firstOrNull {
                it.name == method.name && it.returnType == method.returnType
            }?.returnEarly()
        }
    }

    for ((cls, method) in extraNoOps)
        mutableClassDefByOrNull(cls)?.methods?.firstOrNull { it.name == method }
            ?.apply { clearBody(); addInstructions(0, "return-void") }

    initPairipStringHolders()
}

// ── initPairipStringHolders ───────────────────────────────────────────────────

/**
 * Injects a synthetic `<clinit>()V` into every auto-detected Pairip string-holder
 * class, initialising all static String fields to "" (empty, non-null).
 *
 * Prevents NPEs caused by null fields when libpairipcore.so is not running.
 * Used as a fallback inside [killPairIpFull]; replaced by [restorePairIpHolders]
 * when a real depairip_strings.tsv is available.
 *
 * String holders are detected via [findPairipStringHolders] from PairIpStringLogger.kt:
 * classes with super=Object, no <clinit>, no virtual methods, ≥1 public static String field.
 */
fun BytecodePatchContext.initPairipStringHolders() {
    val holders = findPairipStringHolders()
    for ((classType, fields) in holders) {
        val mutableClass = mutableClassDefByOrNull(classType) ?: continue
        if (mutableClass.methods.any { it.name == "<clinit>" }) continue

        val sb = StringBuilder()
        sb.append("const-string v0, \"\"\n")
        for (field in fields) {
            sb.append("sput-object v0, $classType->$field:Ljava/lang/String;\n")
        }
        sb.append("return-void\n")

        val clinit = com.android.tools.smali.dexlib2.immutable.ImmutableMethod(
            classType, "<clinit>", emptyList(), "V",
            AccessFlags.STATIC.value or AccessFlags.CONSTRUCTOR.value,
            null, null, ImmutableMethodImplementation(1, emptyList(), null, null),
        ).toMutable()
        mutableClass.methods.add(clinit)
        clinit.addInstructions(0, sb.toString())
    }
}

// ── patchPairipNative ────────────────────────────────────────────────────────

/**
 * Patches `libpairipcore.so` JNI_OnLoad to return JNI_VERSION_1_6 immediately,
 * bypassing the APK signature hash loop that raises SIGSEGV on re-signed builds.
 *
 * Locates JNI_OnLoad dynamically by parsing the ELF64 .dynsym symbol table —
 * no hardcoded signature or offset required. Works regardless of build variant,
 * SO version, or whether the file comes from a split or merged APK.
 *
 * Steps:
 *   1. Parse ELF64 header → find .dynsym and .dynstr section offsets
 *   2. Walk .dynsym entries → find symbol named "JNI_OnLoad" → get st_value (vaddr)
 *   3. Walk PT_LOAD program headers → map vaddr → file offset
 *   4. Overwrite 12 bytes at file offset:
 *        movz w0, #1, lsl#16  →  w0 = 0x00010000
 *        movk w0, #6          →  w0 = 0x00010006  (JNI_VERSION_1_6)
 *        ret
 *
 * @param libPath  path to libpairipcore.so inside the APK context
 *                 (e.g. "lib/arm64-v8a/libpairipcore.so")
 */
fun app.morphe.patcher.patch.ResourcePatchContext.patchPairipNative(
    libPath: String = "lib/arm64-v8a/libpairipcore.so",
) {
    val lib = get(libPath)
    if (!lib.exists()) throw app.morphe.patcher.patch.PatchException(
        "$libPath not found — ensure the ABI split is included in the input APKS.",
    )

    val bytes = lib.readBytes()

    fun i32(off: Int) = java.nio.ByteBuffer.wrap(bytes, off, 4)
        .order(java.nio.ByteOrder.LITTLE_ENDIAN).int
    fun i64(off: Int) = java.nio.ByteBuffer.wrap(bytes, off, 8)
        .order(java.nio.ByteOrder.LITTLE_ENDIAN).long
    fun u32(off: Int) = i32(off).toLong() and 0xFFFFFFFFL
    fun u64(off: Int) = i64(off)

    // ── 1. Validate ELF magic ────────────────────────────────────────────────
    if (bytes[0] != 0x7f.toByte() || bytes[1] != 'E'.code.toByte() ||
        bytes[2] != 'L'.code.toByte() || bytes[3] != 'F'.code.toByte()
    ) throw app.morphe.patcher.patch.PatchException("$libPath is not a valid ELF file.")

    val is64 = bytes[4] == 2.toByte()
    if (!is64) throw app.morphe.patcher.patch.PatchException(
        "$libPath is 32-bit — only arm64-v8a is supported by this patch.",
    )

    // ── 2. ELF64 header fields ───────────────────────────────────────────────
    val eShoff    = u64(0x28).toInt()   // section header table offset
    val eShentsize = (bytes[0x3a].toInt() and 0xff) or ((bytes[0x3b].toInt() and 0xff) shl 8)
    val eShnum     = (bytes[0x3c].toInt() and 0xff) or ((bytes[0x3d].toInt() and 0xff) shl 8)
    val eShstrndx  = (bytes[0x3e].toInt() and 0xff) or ((bytes[0x3f].toInt() and 0xff) shl 8)
    val ePhoff    = u64(0x20).toInt()
    val ePhentsize = (bytes[0x36].toInt() and 0xff) or ((bytes[0x37].toInt() and 0xff) shl 8)
    val ePhnum     = (bytes[0x38].toInt() and 0xff) or ((bytes[0x39].toInt() and 0xff) shl 8)

    // ── 3. Find .dynsym and .dynstr via section headers ──────────────────────
    // First get shstrtab offset to read section names
    val shstrOff = eShoff + eShstrndx * eShentsize
    val shstrData = u64(shstrOff + 0x18).toInt()  // sh_offset

    var dynsymOff = -1; var dynsymSize = 0; var dynsymEnt = 0
    var dynstrOff = -1

    for (i in 0 until eShnum) {
        val shOff  = eShoff + i * eShentsize
        val nameIdx = i32(shOff)
        val shType  = i32(shOff + 4)    // sh_type: 11=DYNSYM, 3=STRTAB
        val shOffset = u64(shOff + 0x18).toInt()
        val shSize   = u64(shOff + 0x20).toInt()
        val shEntsize = u64(shOff + 0x38).toInt()

        // Read section name from shstrtab
        var nameEnd = shstrData + nameIdx
        val nameBytes = mutableListOf<Byte>()
        while (nameEnd < bytes.size && bytes[nameEnd] != 0.toByte()) {
            nameBytes.add(bytes[nameEnd++])
        }
        val name = String(nameBytes.toByteArray())

        when (name) {
            ".dynsym" -> { dynsymOff = shOffset; dynsymSize = shSize; dynsymEnt = shEntsize }
            ".dynstr" -> { dynstrOff = shOffset }
        }
    }

    if (dynsymOff < 0) throw app.morphe.patcher.patch.PatchException(
        "Could not find .dynsym section in $libPath.",
    )
    if (dynstrOff < 0) throw app.morphe.patcher.patch.PatchException(
        "Could not find .dynstr section in $libPath.",
    )

    // ── 4. Walk .dynsym to find JNI_OnLoad virtual address ──────────────────
    // ELF64 Sym64: st_name(4) st_info(1) st_other(1) st_shndx(2) st_value(8) st_size(8)
    val entSize = if (dynsymEnt > 0) dynsymEnt else 24
    val numSyms = dynsymSize / entSize
    var jniVaddr = -1L

    for (i in 0 until numSyms) {
        val symOff  = dynsymOff + i * entSize
        val nameIdx = i32(symOff)
        val stValue = u64(symOff + 8)

        // Read symbol name from .dynstr
        var nOff = dynstrOff + nameIdx
        val symNameBytes = mutableListOf<Byte>()
        while (nOff < bytes.size && bytes[nOff] != 0.toByte()) {
            symNameBytes.add(bytes[nOff++])
        }
        val symName = String(symNameBytes.toByteArray())

        if (symName == "JNI_OnLoad") {
            jniVaddr = stValue
            break
        }
    }

    if (jniVaddr < 0) throw app.morphe.patcher.patch.PatchException(
        "JNI_OnLoad symbol not found in .dynsym of $libPath — " +
            "the symbol table may be stripped.",
    )

    // ── 5. Map JNI_OnLoad vaddr → file offset via PT_LOAD segments ──────────
    // ELF64 Phdr: p_type(4) p_flags(4) p_offset(8) p_vaddr(8) p_paddr(8) p_filesz(8) ...
    var jniFileOffset = -1

    for (i in 0 until ePhnum) {
        val phOff   = ePhoff + i * ePhentsize
        val pType   = i32(phOff)         // 1 = PT_LOAD
        if (pType != 1) continue
        val pOffset = u64(phOff + 8)
        val pVaddr  = u64(phOff + 16)
        val pFilesz = u64(phOff + 32)

        if (jniVaddr >= pVaddr && jniVaddr < pVaddr + pFilesz) {
            jniFileOffset = (pOffset + (jniVaddr - pVaddr)).toInt()
            break
        }
    }

    if (jniFileOffset < 0) throw app.morphe.patcher.patch.PatchException(
        "Could not map JNI_OnLoad vaddr ${java.lang.Long.toHexString(jniVaddr)} " +
            "to a file offset in $libPath.",
    )

    // ── 6. Overwrite first 12 bytes with movz+movk+ret ───────────────────────
    // ARM64 LE:
    //   movz w0, #1, lsl#16  →  52 a0 00 20  →  bytes: 20 00 a0 52
    //   movk w0, #6          →  72 80 00 c0  →  bytes: c0 00 80 72
    //   ret                  →  d6 5f 03 c0  →  bytes: c0 03 5f d6
    val patch = byteArrayOf(
        0x20, 0x00, 0xa0.toByte(), 0x52,
        0xc0.toByte(), 0x00, 0x80.toByte(), 0x72,
        0xc0.toByte(), 0x03, 0x5f, 0xd6.toByte(),
    )
    patch.copyInto(bytes, jniFileOffset)
    lib.writeBytes(bytes)
}
