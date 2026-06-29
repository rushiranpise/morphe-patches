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
        "agent (module 1781199372735). On a re-signed APK, pcam.jar performs an APK signature " +
        "attestation check and calls System.exit(0) on failure — killing the app ~3-4s after " +
        "launch from a GMA(BG) background thread, independently of PairIP's LicenseClient. " +
        "Pinned by '/1781199372735.jar' which is unique to the obfuscated sm1 loader class.",
) {
    compatibleWith(Constants.ONETAPCLEANER_COMPATIBILITY)

    execute {
        // sm1 is the pcam loader: downloads, caches, and executes the pcam.jar integrity agent.
        // Pinned by the unique jar path string. No-op c()V and d()V (the execute/run methods
        // called by jl0 after setup), and b(File)V (the DexClassLoader setup method).
        // clearBody() required — all three have try-catch blocks.
        val sm1Class = classDefByStrings("/1781199372735.jar")
            .firstOrNull()
            ?: throw PatchException(
                "1Tap Cleaner: GMA pcam loader (1781199372735.jar) not found — GMA SDK version changed.",
            )
        val mutableSm1 = mutableClassDefBy(sm1Class)

        // c()V — the main pcam execution method (loads classes from DexClassLoader, runs integrity)
        mutableSm1.methods.firstOrNull {
            it.name == "c" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("1Tap Cleaner: sm1.c()V not found.")

        // d()V — secondary execution method called from jl0
        mutableSm1.methods.firstOrNull {
            it.name == "d" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
        // d()V may not exist in all versions — not fatal if missing

        // b(File)V — the DexClassLoader initialization method (loads pcam.jar into ClassLoader)
        mutableSm1.methods.firstOrNull {
            it.name == "b" && it.returnType == "V" &&
                it.parameterTypes == listOf("Ljava/io/File;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("1Tap Cleaner: sm1.b(File)V not found.")
    }
}
