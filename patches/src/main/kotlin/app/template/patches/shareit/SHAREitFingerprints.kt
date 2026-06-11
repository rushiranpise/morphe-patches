package app.template.patches.shareit

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// hepler/b.d()Z — main isPurchased() gate
// unique string: "PurchaseManager" + "mRemoveAdsLiveData == null"
object PurchaseManagerIsPurchasedFingerprint : Fingerprint(
    definingClass = "Lcom/ushareit/subscription/hepler/b;",
    name = "d",
    returnType = "Z",
    parameters = emptyList(),
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("PurchaseManager", "mRemoveAdsLiveData == null")
)

// ank.a(String)Z — per-SKU purchase check
// unique string: none but unique descriptor + definingClass
object AnkSkuPurchasedFingerprint : Fingerprint(
    definingClass = "Lshareit/premium/ank;",
    name = "a",
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    accessFlags = listOf(AccessFlags.PUBLIC)
)
