package app.template.patches.onetapcleaner.gma

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import app.template.patches.shared.clearBody

// Internal: applied automatically as a dependency of Unlock Pro.
@Suppress("unused")
val disableGmaPcamPatch = bytecodePatch(
    description = "Prevents the Google Mobile Ads SDK from executing the cached pcam.jar integrity " +
        "agent (module 1785301552478). On a re-signed APK, pcam.jar performs an APK signature " +
        "attestation check and calls System.exit(0) on failure — killing the app ~3-4s after " +
        "launch from a GMA(BG) background thread, independently of PairIP's LicenseClient. " +
        "Pinned by '/1785301552478.jar' which is unique to the obfuscated pcam loader class.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // The pcam loader downloads, caches, and executes the pcam.jar integrity agent.
        // Pinned by the unique jar path string. No-op c()V and d()V (the execute/run methods
        // called after setup), and b(File)V (the DexClassLoader setup method).
        // clearBody() required — all three have try-catch blocks.
        // Module ID changed: 1781541265554 (v5.21) → 1785301552478 (v5.22).
        val pcamLoaderClass = classDefByStrings("/1785301552478.jar")
            .firstOrNull()
            ?: throw PatchException(
                "1Tap Cleaner: GMA pcam loader not found — GMA SDK version changed.",
            )
        val mutablePcamLoader = mutableClassDefBy(pcamLoaderClass)

        // c()V — the main pcam execution method (creates DexClassLoader, runs integrity check)
        mutablePcamLoader.methods.firstOrNull {
            it.name == "c" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("1Tap Cleaner: pcam loader c()V not found.")

        // d()V — secondary execution method; may not exist in all GMA versions
        mutablePcamLoader.methods.firstOrNull {
            it.name == "d" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }

        // b(File)V — the DexClassLoader initialization method (loads pcam.jar into ClassLoader)
        mutablePcamLoader.methods.firstOrNull {
            it.name == "b" && it.returnType == "V" &&
                it.parameterTypes == listOf("Ljava/io/File;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("1Tap Cleaner: pcam loader b(File)V not found.")
    }
}
