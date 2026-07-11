package app.template.patches.buzzcast

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.BUZZCAST_COMPATIBILITY
import org.w3c.dom.Element

private val buzzCastStripAdManifestPatch = resourcePatch(
    name = "Strip ad manifest entries",
    description = "Removes ad permissions and manifest entries.",
    default = true,
) {
    compatibleWith(BUZZCAST_COMPATIBILITY)

    execute {
        document("AndroidManifest.xml").use { doc ->
            val adPermissions = setOf(
                "android.permission.ACCESS_ADSERVICES_AD_ID",
                "android.permission.ACCESS_ADSERVICES_ATTRIBUTION",
            )

            for (tag in listOf("uses-permission", "uses-library", "activity", "property")) {
                val nodes = doc.getElementsByTagName(tag)
                for (i in nodes.length - 1 downTo 0) {
                    val node = nodes.item(i) as? Element ?: continue
                    val name = node.getAttribute("android:name")
                    val remove = when (tag) {
                        "uses-permission" -> name in adPermissions
                        "uses-library" -> name == "android.ext.adservices"
                        "activity" -> name.startsWith("com.unity3d.services.ads.") ||
                            name == "com.unity3d.ads.adplayer.FullScreenWebViewDisplay"
                        "property" -> name == "android.adservices.AD_SERVICES_CONFIG"
                        else -> false
                    }
                    if (remove) node.parentNode?.removeChild(node)
                }
            }
        }
    }
}

@Suppress("unused")
val buzzCastUnlockVipPatch = bytecodePatch(
    name = "Unlock SVIP",
    description = "Unlocks SViP features in app.",
    default = true,
) {
    compatibleWith(BUZZCAST_COMPATIBILITY)
    dependsOn(buzzCastStripAdManifestPatch)

    execute {
        fun replaceBody(className: String, methodName: String, returnType: String, smali: String) {
            mutableClassDefBy(className).methods
                .first { it.name == methodName && it.returnType == returnType }
                .apply {
                    removeInstructions(0, implementation!!.instructions.count())
                    addInstructions(0, smali)
                }
        }

        fun replaceBodyIfPresent(className: String, methodName: String, returnType: String, smali: String) {
            val classDef = runCatching { mutableClassDefBy(className) }.getOrNull() ?: return
            classDef.methods
                .firstOrNull { it.name == methodName && it.returnType == returnType }
                ?.apply {
                    removeInstructions(0, implementation!!.instructions.count())
                    addInstructions(0, smali)
                }
        }

        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getIsVip",
            "I",
            "const/4 v0, 0x2\nreturn v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getVipLevel",
            "I",
            "const/4 v0, 0x2\nreturn v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/bean/UserBean;",
            "getVipLevel",
            "I",
            "const/4 v0, 0x2\nreturn v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getVip",
            "Ljava/lang/Integer;",
            "const/4 v0, 0x2\ninvoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\nmove-result-object v0\nreturn-object v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getVipExpireTime",
            "J",
            "const-wide v0, 0x7fffffffffffffffL\nreturn-wide v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getThirdEndTime",
            "J",
            "const-wide v0, 0x7fffffffffffffffL\nreturn-wide v0",
        )
        replaceBody(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "getVipSign",
            "Ljava/lang/String;",
            "const-string v0, \"vvip1_1\"\nreturn-object v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "isVip",
            "Z",
            "const/4 v0, 0x1\nreturn v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/faceshow/aaspring/beans/UserVipData;",
            "isOfficial",
            "Z",
            "const/4 v0, 0x1\nreturn v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/faceshow/aaspring/utils/VipUserInfoUtil\$1;",
            "getVipLevel",
            "I",
            "const/4 v0, 0x2\nreturn v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/faceshow/aaspring/utils/VipUserInfoUtil\$1;",
            "isVip",
            "Z",
            "const/4 v0, 0x1\nreturn v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/lib_service_center/live/game/UserVipData1;",
            "getOfficialAccount",
            "Ljava/lang/Integer;",
            "const/4 v0, 0x1\ninvoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\nmove-result-object v0\nreturn-object v0",
        )
        replaceBodyIfPresent(
            "Lcom/guochao/faceshow/session/model/UserSessionModel;",
            "getOfficialAccount",
            "Ljava/lang/Integer;",
            "const/4 v0, 0x1\ninvoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\nmove-result-object v0\nreturn-object v0",
        )

        mutableClassDefBy("Lcom/guochao/faceshow/aaspring/modulars/vip/viewmodel/BuyVipViewModel;")
            .methods
            .filter { it.name in setOf("h", "y") && it.returnType == "V" }
            .forEach { method ->
                method.removeInstructions(0, method.implementation!!.instructions.count())
                method.addInstructions(0, "return-void")
            }

        mutableClassDefBy("Lcom/guochao/faceshow/aaspring/modulars/googlepay/InAppBillingViewModel;")
            .methods
            .filter { method ->
                method.returnType == "I" && method.parameters.any {
                    it.type == "Landroidx/fragment/app/FragmentActivity;"
                }
            }
            .forEach { method ->
                method.removeInstructions(0, method.implementation!!.instructions.count())
                method.addInstructions(0, "const/4 v0, -0x1\nreturn v0")
            }
        mutableClassDefBy("Lcom/guochao/faceshow/aaspring/modulars/googlepay/InAppBillingViewModel;")
            .methods
            .filter { method ->
                method.returnType == "I" && method.parameters.any {
                    it.type == "Lcom/android/billingclient/api/ProductDetails;" ||
                        it.type == "Lcom/android/billingclient/api/SkuDetails;"
                }
            }
            .forEach { method ->
                method.removeInstructions(0, method.implementation!!.instructions.count())
                method.addInstructions(0, "const/4 v0, -0x1\nreturn v0")
            }

        replaceBody(
            "Lcom/unity3d/ads/UnityAds;",
            "isSupported",
            "Z",
            "const/4 v0, 0x0\nreturn v0",
        )
        mutableClassDefBy("Lcom/unity3d/ads/UnityAds;").methods
            .filter { it.name in setOf("initialize", "load", "show") && it.returnType == "V" }
            .forEach { method ->
                method.removeInstructions(0, method.implementation!!.instructions.count())
                method.addInstructions(0, "return-void")
            }
    }
}
