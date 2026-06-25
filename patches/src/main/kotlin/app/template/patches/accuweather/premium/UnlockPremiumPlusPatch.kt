package app.template.patches.accuweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val unlockPremiumPlusPatch = bytecodePatch(
    name = "Unlock Premium+",
    description = "Unlocks AccuWeather's Premium+ subscription tier without a Play Store purchase. " +
        "Enables the full 15-day and hourly forecast detail, MinuteCast extended precision, " +
        "air quality and health indexes, real-feel temperature, severe weather notifications, " +
        "and widget customisation.",
) {
    compatibleWith(Constants.ACCUWEATHER_COMPATIBILITY)

    execute {
        // The subscription Flow emits u9/a sealed subclasses:
        //   u9/a$b "Success(active=" — not logged in / no sub → wraps emptySet
        //   u9/a$a "Mismatch(active=" — logged in but inactive/mismatched → wraps some set
        // Both are unwrapped by u9/b$a$a.emit() via u9/a.a()Set, then emitted as
        // Set<u9/c> to all ViewModels which gate via Set.contains(u9/c(PREMIUM_PLUS)).
        //
        // Fix: replace a()Set on both subclasses to always return {PREMIUM_PLUS}.
        // We clear the original body and rewrite with ensureRegisters(5) so the
        // compiler template uses .registers 5 instead of the original 2
        // (avoiding the "register index out of range" VerifyError).
        //
        // u9/c direct ctor: <init>(Lu9/p; Ljava/lang/String;)V — needs v0..v2 (3 regs + p0=this).
        // Total needed: 5 registers (v0,v1,v2 locals + p0 param; compiler maps p0 to v3).

        val newBody = """
            new-instance v0, Lu9/c;
            sget-object v1, Lu9/p;->e:Lu9/p;
            const-string v2, ""
            invoke-direct {v0, v1, v2}, Lu9/c;-><init>(Lu9/p;Ljava/lang/String;)V
            invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
            move-result-object v0
            return-object v0
        """

        fun patchGetter(pin: String) {
            val classDef = classDefByStrings(pin).firstOrNull()
                ?: throw PatchException(
                    "AccuWeather: subscription wrapper class pinned by \"$pin\" not found.",
                )
            val method = mutableClassDefBy(classDef).methods
                .firstOrNull { m ->
                    !AccessFlags.STATIC.isSet(m.accessFlags) &&
                        m.returnType == "Ljava/util/Set;" &&
                        m.parameterTypes.isEmpty()
                }
                ?: throw PatchException(
                    "AccuWeather: a()Set getter not found in ${classDef.type}.",
                )
            method.clearBody()
            method.ensureRegisters(5)
            method.addInstructions(0, newBody)
        }

        patchGetter("Success(active=")   // u9/a$b — not-logged-in / no-sub path
        patchGetter("Mismatch(active=")  // u9/a$a — logged-in inactive-sub path
    }
}
