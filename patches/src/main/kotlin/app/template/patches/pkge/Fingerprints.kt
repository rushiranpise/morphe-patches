package app.template.patches.pkge

import app.morphe.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Targets PremiumPurchasedStatusBuffer.send(Z)V — called by InitializeDetectingPremiumStatusUseCase
// with the result of PremiumPurchasedStorage.isPremiumPurchased(); patching this forces premium.
internal val premiumStatusSendFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { methodDef, classDef ->
        classDef.type == "Lnet/posylka/core/premium/status/storages/PremiumPurchasedStatusBuffer;" &&
                methodDef.name == "send"
    }
}
