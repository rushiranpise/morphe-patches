package app.template.patches.somyac.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.SOMYAC_WATCH_BROWSER_COMPATIBILITY
import app.template.patches.shared.Constants.SOMYAC_WATCH_TRANSLATOR_COMPATIBILITY

// Shared internal patch — not exposed directly; composed into per-app top-level patches.
private val somyacPremiumBytecodePatch = bytecodePatch {
    execute {
        // ── Fix 1: always send purchaseLocal=1, purchaseRemote=1 to the watch ──────
        //
        // iapSendLicenceToWatch() builds a PurchaseItem and AES-encrypts it for
        // transmission to the watch. The watch calls isPurchaseOn() which returns
        // true when purchaseLocal==1 OR purchaseRemote==1.
        //
        // Original: purchaseLocal=v4 (hardcoded 0), purchaseRemote=p6 (from Dart billing).
        // Patch: override both to 1 before setPurchaseLocal/Remote are called.
        // Reverse order so the lower-index injection does not shift the higher index.
        IapSendLicenceToWatchFingerprint.apply {
            val setPurchaseRemoteIdx = instructionMatches[2].index
            val setPurchaseLocalIdx  = instructionMatches[1].index

            method.addInstruction(setPurchaseRemoteIdx, "const/4 p6, 0x1")
            method.addInstruction(setPurchaseLocalIdx,  "const/4 v4, 0x1")
        }

        // ── Fix 2: always report a connected watch to Flutter ─────────────────────
        //
        // The Flutter UI checks isConnectedToWatch = connectedDeviceList.isNotEmpty.
        // Without a physical watch paired, OnConnectedDeviceList sends an empty list
        // to Flutter which shows "Connect watch to use premium", blocking the premium
        // UI entirely even for licensed users.
        //
        // Patch: inject "1.Galaxy Watch6" (Platform.ANDROID.toInt()=1) into the output
        // ArrayList immediately before Result.success(list) is called.
        // This makes Flutter always see at least one connected Wear OS device.
        //
        // Instruction layout before success() (3 instructions):
        //   iget-object p2, p0, this$0
        //   invoke-static {p2}, MainActivity::access$100(MainActivity)Result
        //   move-result-object p2
        //   invoke-interface {p2, p1}, Result::success(p1)   ← instructionMatches[2].index
        // Inject 2 instructions at (successIdx - 3), before the iget-object block.
        ConnectedDeviceListFingerprint.apply {
            val successIdx = instructionMatches[2].index
            method.addInstructions(
                successIdx - 3,
                "const-string v0, \"1.Galaxy Watch6\"\n" +
                "invoke-interface { p1, v0 }, Ljava/util/List;->add(Ljava/lang/Object;)Z"
            )
        }
    }
}

/**
 * Mini Web Browser for Wear OS — Premium unlock.
 *
 * Two fixes:
 *  1. iapSendLicenceToWatch: override purchaseLocal=1, purchaseRemote=1 so the
 *     watch always receives a valid licence token (fixes feature lock on watch).
 *  2. OnConnectedDeviceList: inject a fake Wear OS device entry so Flutter always
 *     reports isConnectedToWatch=true (fixes "Connect watch to use premium" gate).
 */
@Suppress("unused")
val watchBrowserPremiumPatch = bytecodePatch(
    name = "Mini Web Browser Premium",
    description = "Unlocks premium by sending a valid licence to the watch and bypassing " +
            "the watch-connection gate in the companion app.",
    default = true
) {
    compatibleWith(SOMYAC_WATCH_BROWSER_COMPATIBILITY)
    dependsOn(somyacPremiumBytecodePatch)
}

/**
 * Translator for Wear OS — Premium unlock.
 *
 * Identical codebase to Mini Web Browser — same two patches apply.
 */
@Suppress("unused")
val watchTranslatorPremiumPatch = bytecodePatch(
    name = "Translator Premium",
    description = "Unlocks premium by sending a valid licence to the watch and bypassing " +
            "the watch-connection gate in the companion app.",
    default = true
) {
    compatibleWith(SOMYAC_WATCH_TRANSLATOR_COMPATIBILITY)
    dependsOn(somyacPremiumBytecodePatch)
}
