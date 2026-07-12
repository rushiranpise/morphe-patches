package app.template.patches.shared

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodImplementation

// ── Constants ─────────────────────────────────────────────────────────────────
//
// Log tag written to logcat by the injected <clinit> stubs.
// Filter: adb logcat -s PAIRIP_STRINGS
// Each line format: PAIRIP_STRINGS  Lsome/pkg/Class;->fieldName = <value>
//
// The value is the decrypted string filled by libpairipcore.so at runtime.
// We inject the logger AFTER gutPairIpVm() kills VMRunner.<clinit> — meaning
// we must use a secondary patched APK (pass-1) where we:
//   a) DO NOT kill VMRunner (the native lib must run to fill the strings), OR
//   b) Hook into a post-fill point (Application.onCreate after super)
//
// PASS-1 APK strategy:
//   - Kill only SignatureCheck + LicenseClient (so app doesn't crash/exit)
//   - Keep VMRunner alive so native lib loads and fills string holders
//   - Inject logger <clinit>s that fire AFTER the holder fields are populated
//   - Run pass-1 APK, capture: adb logcat -s PAIRIP_STRINGS > strings.log
//   - Run parsePairipLogcat("strings.log") → generates depairip_strings.tsv
//   - Commit depairip_strings.tsv to resources, enable pass-2 patch
//     which calls restorePairIpHolders() + killPairIpFull()

private const val LOG_TAG = "PAIRIP_STRINGS"

fun BytecodePatchContext.findPairipStringHolders(): Map<String, List<String>> {
    val result = linkedMapOf<String, List<String>>()
    classDefForEach { classDef ->
        val fields = classDef.fields
            .filter { field ->
                field.type == "Ljava/lang/String;" &&
                    AccessFlags.STATIC.isSet(field.accessFlags) &&
                    field.initialValue == null
            }
            .map { it.name }
            .toList()

        if (fields.isEmpty()) return@classDefForEach

        val type = classDef.type
        val looksLikeHolder =
            type.startsWith("Lcom/pairip/") ||
                classDef.methods.toList().isEmpty() ||
                fields.size >= 3

        if (looksLikeHolder) result[type] = fields
    }
    return result
}

// ── Pass-1: Inject string loggers into holder classes ─────────────────────────────────────────────

/**
 * PASS-1 PATCH — Injects `android.util.Log.d()` calls into each Pairip string-holder class.
 *
 * For each auto-detected holder (see [findPairipStringHolders]), injects a synthetic
 * `<clinit>()V` that:
 *   1. Waits for libpairipcore.so to populate the static String fields (the fields are
 *      written by JNI_OnLoad during class load, before any Java <clinit> would run — BUT
 *      since these holder classes have NO <clinit>, the native VM populates them via
 *      reflection/field-set after the class is loaded. Our injected <clinit> fires at
 *      first class reference, which may be BEFORE the native VM runs.)
 *
 * To solve the timing issue, we hook into the Application's `onCreate()` instead of
 * <clinit> — calling a static `dumpStrings()` method on each holder after
 * `super.onCreate()` completes (by which time the native VM has finished all IAP programs).
 *
 * Strategy (executed by this function):
 *   - For each holder class: add `public static dumpStrings()V` that logs all fields
 *   - Hook `com.pairip.application.Application.attachBaseContext` AFTER the `super` call
 *     to invoke all `dumpStrings()` methods sequentially
 *
 * After running the pass-1 APK:
 *   ```
 *   adb logcat -s PAIRIP_STRINGS -d > pairip_strings.log
 *   ```
 * Then call [parsePairipLogcat] to generate `depairip_strings.tsv`.
 *
 * NOTE: Pass-1 APK must NOT kill VMRunner — only kill SignatureCheck + LicenseClient
 *       so the app launches without crashing while the native VM still runs.
 */
fun BytecodePatchContext.injectPairipStringLoggers() {
    val holders = findPairipStringHolders()
    if (holders.isEmpty()) {
        println("[PairIpStringLogger] No string holder classes found.")
        return
    }

    val dumpMethodFlag = AccessFlags.PUBLIC.value or AccessFlags.STATIC.value

    // Step 1: inject dumpStrings() into each holder
    val holderList = holders.entries.toList()
    for ((classType, fields) in holderList) {
        val mutableClass = mutableClassDefByOrNull(classType) ?: continue
        if (mutableClass.methods.any { it.name == "dumpStrings" }) continue

        // Build: for each field → Log.d(LOG_TAG, "classType->fieldName = " + fieldValue)
        val sb = StringBuilder()
        for (field in fields) {
            sb.append("""
                sget-object v0, $classType->$field:Ljava/lang/String;
                if-nez v0, :has_$field
                const-string v0, "<null>"
                :has_$field
                const-string v1, "$LOG_TAG"
                new-instance v2, Ljava/lang/StringBuilder;
                invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V
                const-string v3, "$classType->$field = "
                invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                move-result-object v2
                invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
                move-result-object v2
                invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
                move-result-object v2
                invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
                move-result v1
            """.trimIndent() + "\n")
        }
        sb.append("return-void\n")

        val dumpMethod = ImmutableMethod(
            classType, "dumpStrings", emptyList(), "V", dumpMethodFlag,
            null, null, ImmutableMethodImplementation(4, emptyList(), null, null),
        ).toMutable()
        dumpMethod.addInstructions(0, sb.toString())
        mutableClass.methods.add(dumpMethod)
    }

    // Step 2: hook Application.attachBaseContext to call all dumpStrings() AFTER super
    // The native VM populates string holders during VMRunner.invoke() which is called
    // from StartupLauncher.launch() which is called from attachBaseContext → super chain.
    // By hooking AFTER invoke-super we guarantee all strings are populated.
    val appClass = mutableClassDefByOrNull("Lcom/pairip/application/Application;") ?: run {
        println("[PairIpStringLogger] Application class not found — dumpStrings() injected but not called.")
        return
    }
    val attachBase = appClass.methods.firstOrNull { it.name == "attachBaseContext" } ?: return

    // Build the call chain: invoke each holder's dumpStrings() after the existing body
    val callChain = StringBuilder()
    for ((classType, _) in holderList) {
        callChain.append("invoke-static {}, $classType->dumpStrings()V\n")
    }

    // Find the index of return-void and insert before it
    val impl = attachBase.implementation ?: return
    val returnIdx = impl.instructions.indexOfFirst {
        it.opcode.name == "RETURN_VOID"
    }.takeIf { it >= 0 } ?: return

    attachBase.addInstructions(returnIdx, callChain.toString())

    println("[PairIpStringLogger] Injected dumpStrings() into ${holderList.size} holders.")
    println("[PairIpStringLogger] Run pass-1 APK, then:")
    println("[PairIpStringLogger]   adb logcat -s $LOG_TAG -d > pairip_strings.log")
    println("[PairIpStringLogger] Then call parsePairipLogcat(\"pairip_strings.log\")")
}

// ── Logcat parser: logcat → depairip_strings.tsv ────────────────────────────────────────────────

/**
 * Parses the logcat output captured from a pass-1 patched APK and generates the
 * `depairip_strings.tsv` file that [restorePairIpHolders] expects.
 *
 * Input format (each line from `adb logcat -s PAIRIP_STRINGS -d`):
 * ```
 * D PAIRIP_STRINGS: Lsome/pkg/Class;->fieldName = actual decrypted string value
 * ```
 * (The `= ` separator is the literal string " = " injected by [injectPairipStringLoggers])
 *
 * Output TSV format (matching [restorePairIpHolders]'s parser):
 * ```
 * @Lsome/pkg/Class;
 * fieldName\tactual decrypted string value
 * fieldName2\tanother value
 * @Lanother/Class;
 * ...
 * ```
 *
 * @param logcatFile  path to the file containing `adb logcat -s PAIRIP_STRINGS -d` output
 * @param outputFile  path to write `depairip_strings.tsv` (default: same dir as logcatFile)
 * @return  map of classType → (fieldName → value) for all parsed entries
 */
fun parsePairipLogcat(
    logcatFile: String,
    outputFile: String = logcatFile.substringBeforeLast('.') + "_depairip_strings.tsv",
): Map<String, Map<String, String>> {
    val logcatLines = java.io.File(logcatFile).readLines()

    // Parse: "D PAIRIP_STRINGS: Lsome/Class;->field = value"
    // Also handle: "05-01 12:34:56.789  1234  5678 D PAIRIP_STRINGS: Lsome/Class;->field = value"
    val entryRegex = Regex("""PAIRIP_STRINGS[:\s]+(\S+)->(\S+)\s+=\s+(.*)""")

    // Group by class, preserving field order
    val result = linkedMapOf<String, LinkedHashMap<String, String>>()
    for (line in logcatLines) {
        val m = entryRegex.find(line) ?: continue
        val classType = m.groupValues[1]  // e.g. Lsome/pkg/Class;
        val fieldName = m.groupValues[2]  // e.g. fieldName
        val value     = m.groupValues[3].trimEnd()  // actual decrypted string
            .replace("\\n", "\n")
            .replace("\\t", "\t")
            .replace("\\\\", "\\")
        result.getOrPut(classType) { linkedMapOf() }[fieldName] = value
    }

    if (result.isEmpty()) {
        System.err.println("[PairIpStringLogger] WARNING: No PAIRIP_STRINGS entries found in $logcatFile")
        return emptyMap()
    }

    // Write TSV
    val out = java.io.File(outputFile)
    out.bufferedWriter().use { w ->
        for ((classType, fields) in result) {
            w.write("@$classType\n")
            for ((field, value) in fields) {
                w.write("$field\t$value\n")
            }
        }
    }

    println("[PairIpStringLogger] Wrote ${result.values.sumOf { it.size }} strings " +
        "across ${result.size} classes → $outputFile")
    println("[PairIpStringLogger] Copy to resources as 'depairip_strings.tsv' then enable pass-2 patch.")

    return result
}

// ── Convenience: minimal pass-1 pairip kill (keeps VMRunner alive) ──────────────────────────────

/**
 * Minimal Pairip kill suitable for the **pass-1 logging APK**.
 *
 * Unlike [killPairIpFull], this keeps VMRunner ALIVE so that `libpairipcore.so` loads
 * and populates string-holder fields. Only kills the signature check + license gating
 * so the app doesn't crash or exit.
 *
 * Use this in combination with [injectPairipStringLoggers] to build the pass-1 APK.
 * After capturing logcat and generating `depairip_strings.tsv`, switch to [killPairIpFull]
 * + [restorePairIpHolders] for the final production patch.
 *
 * ⚠ The pass-1 APK is RE-SIGNED, so JNI_OnLoad will crash it unless the native lib
 *   is also patched. Apply [patchPairipNative] (from PairIpExtensions.kt) even in pass-1.
 */
fun BytecodePatchContext.killPairIpForLogging() {
    val sig   = "Lcom/pairip/SignatureCheck;"
    val lic   = "Lcom/pairip/licensecheck/LicenseClient;"
    val state = "Lcom/pairip/licensecheck/LicenseClient\$LicenseCheckState;"

    // Kill signature check only
    mutableClassDefByOrNull(sig)
        ?.methods?.firstOrNull { it.name == "verifyIntegrity" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    mutableClassDefByOrNull(sig)
        ?.methods?.firstOrNull { it.name == "verifySignatureMatches" }
        ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

    // Kill Play LVL so app doesn't show paywall
    mutableClassDefByOrNull(lic)
        ?.methods?.firstOrNull { it.name == "initializeLicenseCheck" }
        ?.apply {
            clearBody()
            addInstructions(0, """
                sget-object v0, $state->LOCAL_CHECK_OK:$state
                sput-object v0, $lic->licenseCheckState:$state
                return-void
            """.trimIndent())
        }

    mutableClassDefByOrNull(lic)
        ?.methods?.firstOrNull { it.name == "startPaywallActivity" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    mutableClassDefByOrNull(lic)
        ?.methods?.firstOrNull { it.name == "processResponse" }
        ?.apply { clearBody(); addInstructions(0, "return-void") }

    // VMRunner stays ALIVE — native lib must run to populate string holders
    println("[PairIpStringLogger] Pass-1 kill applied (VMRunner kept alive for string capture).")
}
