package app.template.patches.somyac.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string

/**
 * Targets IAPViewManager.iapSendLicenceToWatch() — the method that builds a
 * PurchaseItem and transmits it (AES-encrypted) from the companion phone app
 * to the Wear OS watch app.
 *
 * Architecture:
 *   The actual premium UI logic lives in Dart (libapp.so). When the watch
 *   connects, the Flutter Dart code calls the Android MethodChannel
 *   "sendLicenceToWatch" with a `purchaseRemote` argument (0 = not purchased,
 *   1 = purchased) derived from its own billing state check (_isPurchaseValid).
 *   The Android companion receives this, builds a PurchaseItem with:
 *     • purchaseLocal  = hardcoded 0 (set from v4 in iapSendLicenceToWatch)
 *     • purchaseRemote = value passed from Dart (p6)
 *   and sends it to the watch. The watch checks:
 *     PurchaseItem.isPurchaseOn() → purchaseLocal == 1 || purchaseRemote == 1
 *   to gate premium features.
 *
 * Patch strategy:
 *   Override both purchaseLocal and purchaseRemote to 1 in
 *   iapSendLicenceToWatch before the PurchaseItem is built and sent.
 *   This makes every transmission claim the device is licensed, regardless
 *   of actual Google Play billing state.
 *
 * Fingerprint (verified in both v1.0.4 browser and v1.5.0 translator — identical bytecode):
 *
 *   Filter 1: string "iapSendLicenceToWatch, platform: "  (unique across all smali)
 *   Filter 2: methodCall setPurchaseLocal   → instructionMatches[1].index
 *   Filter 3: methodCall setPurchaseRemote  → instructionMatches[2].index
 *
 * Smali (classes2/com/somyac/companion/IAPViewManager.smali):
 *   .method public iapSendLicenceToWatch(ILjava/lang/String;ILjava/lang/String;Ljava/lang/String;IZJLjava/lang/String;)V
 *   ...
 *   706:  const-string v2, "iapSendLicenceToWatch, platform: "   ← filter 0
 *   ...
 *   796:  invoke-virtual {p2, v4}, PurchaseItem->setPurchaseLocal(I)V    ← filter 1 (v4=0 hardcoded)
 *   801:  invoke-virtual {p2, p6}, PurchaseItem->setPurchaseRemote(I)V   ← filter 2 (p6=from Dart)
 */
internal val IapSendLicenceToWatchFingerprint = Fingerprint(
    accessFlags = listOf(com.android.tools.smali.dexlib2.AccessFlags.PUBLIC),
    returnType = "V",
    parameters = listOf(
        "I",
        "Ljava/lang/String;",
        "I",
        "Ljava/lang/String;",
        "Ljava/lang/String;",
        "I",
        "Z",
        "J",
        "Ljava/lang/String;"
    ),
    filters = listOf(
        string("iapSendLicenceToWatch, platform: "),
        methodCall(
            definingClass = "Lcom/somyac/watch/libbase/licence/PurchaseItem;",
            name = "setPurchaseLocal"
        ),
        methodCall(
            definingClass = "Lcom/somyac/watch/libbase/licence/PurchaseItem;",
            name = "setPurchaseRemote"
        )
    )
)

/**
 * Targets MainActivity$2.OnConnectedDeviceList(Result, List<BaseDevice>) —
 * the observer callback that serialises connected Wear OS devices into
 * strings and sends them to Flutter via MethodChannel$Result.success(list).
 *
 * Flutter checks list.isNotEmpty to set isConnectedToWatch. When no watch is
 * physically paired, this list is empty and Flutter shows "Connect watch to
 * use premium" — blocking the premium UI entirely, even for a licensed user.
 *
 * Patch: inject a fake "1.Galaxy Watch6" (Platform.ANDROID=1) entry into
 * the output ArrayList immediately before the success() call so Flutter
 * always sees at least one connected device.
 *
 * Smali (classes2/com/somyac/companion/MainActivity$2.smali):
 *   81:  const-string "OnConnectedDeviceList: "    ← filter 0
 *   129: invoke-interface List;->add(Object)Z      ← filter 1 (last add in loop)
 *   136: invoke-interface Result;->success(Object) ← filter 2 (patch before this)
 *
 * Inject at instructionMatches[2].index - 3 (before the 3-instruction iget→
 * invoke-static→move-result-object chain that precedes success()):
 *   const-string v0, "1.Galaxy Watch6"
 *   invoke-interface {p1, v0}, Ljava/util/List;->add(Ljava/lang/Object;)Z
 */
internal val ConnectedDeviceListFingerprint = Fingerprint(
    definingClass = "Lcom/somyac/companion/MainActivity\$2;",
    name = "OnConnectedDeviceList",
    filters = listOf(
        string("OnConnectedDeviceList: "),
        methodCall(definingClass = "Ljava/util/List;", name = "add"),
        methodCall(
            definingClass = "Lio/flutter/plugin/common/MethodChannel\$Result;",
            name = "success"
        )
    )
)
