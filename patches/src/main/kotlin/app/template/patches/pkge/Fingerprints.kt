package app.template.patches.pkge

import app.morphe.patcher.fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

internal val premiumStatusSendFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.FINAL)
    returns("V")
    parameters("Z")
    custom { methodDef, classDef ->
        classDef.type == "Lnet/posylka/core/premium/status/storages/PremiumPurchasedStatusBuffer;" &&
                methodDef.name == "send"
    }
}

// isPremiumPurchased(AdaptyProfile)Z — the direct Adapty access-level check.
// Called by SubscriptionsHelperImpl for auto-update and other premium gates.
internal val isPremiumPurchasedAdaptyFingerprint = fingerprint {
    accessFlags(AccessFlags.PRIVATE, AccessFlags.FINAL)
    returns("Z")
    parameters("Lcom/adapty/models/AdaptyProfile;")
    strings("premium")
    custom { methodDef, classDef ->
        classDef.type == "Lnet/posylka/posylka/subscription/SubscriptionsHelperImpl;" &&
                methodDef.name == "isPremiumPurchased"
    }
}
