package app.template.patches.homeworkout.premium

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.LEAP_FITNESS_ALL
import app.template.patches.shared.installer.spoofInstallSourcePatch
import app.template.patches.shared.returnEarly
import app.template.patches.shared.signature.spoofSignatureVerificationPatch

// Leap Fitness Group — premium unlock (47 apps)
//
// Approach (per Entree3k reference pattern for homeworkout/loseweightmen):
//
//   Step 1: IapSpFingerprint locates IapSp class (Z6/a) via its SharedPreferences
//           accessor — returnType=SharedPreferences, strings=["iap_sp"].
//           "iap_sp" is the SharedPrefs file name — plaintext, stable across updates.
//
//   Step 2: classDefForEach scopes to that exact class, then patches ALL methods
//           with returnType=Z and parameters=(String) → returnEarly(false).
//           "false" = not free = purchased.
//           This covers Z6/a.a(String)Z (the isFree check) and any future variants
//           in the same class without needing individual fingerprints.
//
// All 47 Leap Fitness apps share the same IapSp.kt / IapManager.kt / ABTestHelper.kt
// internal SDK — confirmed via smali .source annotations on homeworkout v1.7.7.
// No PairIP in any Leap Fitness app (confirmed via manifest + full DEX string scan).

private const val STRING = "Ljava/lang/String;"

@Suppress("unused")
val homeWorkoutPremiumPatch = bytecodePatch(
    name = "Unlock premium",
    description = "Unlocks all premium features across all Leap Fitness Group apps by overriding the IapSp isFree checks.",
) {
    compatibleWith(*LEAP_FITNESS_ALL)
    dependsOn(spoofInstallSourcePatch, spoofSignatureVerificationPatch)

    execute {
        val iapSpType = IapSpFingerprint.method.definingClass

        classDefForEach { classDef ->
            if (classDef.type != iapSpType) return@classDefForEach

            mutableClassDefBy(classDef).methods
                .filter { method ->
                    method.returnType == "Z" &&
                        method.parameterTypes.map { it.toString() } == listOf(STRING)
                }
                .forEach { it.returnEarly(false) }
        }
    }
}
