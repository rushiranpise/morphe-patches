package app.template.patches.historicalcalendar.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.HISTORICALCALENDAR_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlock Premium features in app.",
    default = true,
) {
    compatibleWith(HISTORICALCALENDAR_COMPATIBILITY)

    execute {
        // 1. x00.a()Z — isPremium getter: always return true
        mutableClassDefByOrNull("Lx00;")?.methods
            ?.firstOrNull { it.name == "a" && it.returnType == "Z" && it.parameters.isEmpty() }
            ?.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // 2. x00.b(Z)V — isPremium setter from billing: force p1=1 (always premium)
        mutableClassDefByOrNull("Lx00;")?.methods
            ?.firstOrNull { it.name == "b" && it.returnType == "V" && it.parameters.size == 1 }
            ?.addInstructions(0, "const/4 p1, 0x1")

        // 3. fi5.s(Z)V — SettingsViewModel isPremium setter: force p1=1
        mutableClassDefByOrNull("Lfi5;")?.methods
            ?.firstOrNull { it.name == "s" && it.returnType == "V" && it.parameters.size == 1 }
            ?.addInstructions(0, "const/4 p1, 0x1")

        // 4. yh4.C()V — billing flow launcher: block it
        mutableClassDefByOrNull("Lyh4;")?.methods
            ?.firstOrNull { it.name == "C" && it.returnType == "V" && it.parameters.isEmpty() }
            ?.apply {
                clearBody()
                addInstructions(0, "return-void")
            }

        // 5. zs.t() — main list ad injector: force v2=1 (isPremium) after iget-boolean at index 2
        mutableClassDefByOrNull("Lzs;")?.methods
            ?.firstOrNull { it.name == "t" }
            ?.addInstructions(3, "const/4 v2, 0x1")

        // 6. hg.<init>(s62;Z) — Settings lambda isPremium: force p2=1
        mutableClassDefByOrNull("Lhg;")?.methods
            ?.firstOrNull { method ->
                method.name == "<init>" &&
                    method.parameters.size == 2 &&
                    method.parameterTypes.map { it.toString() } == listOf("Ls62;", "Z")
            }
            ?.addInstructions(0, "const/4 p2, 0x1")

        // 7. hg.<init>(Z;s62) — alternate Settings lambda: force p1=1
        mutableClassDefByOrNull("Lhg;")?.methods
            ?.firstOrNull { method ->
                method.name == "<init>" &&
                    method.parameters.size == 2 &&
                    method.parameterTypes.map { it.toString() } == listOf("Z", "Ls62;")
            }
            ?.addInstructions(0, "const/4 p1, 0x1")
    }
}
