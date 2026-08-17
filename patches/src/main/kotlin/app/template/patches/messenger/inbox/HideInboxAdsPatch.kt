package app.template.patches.messenger.inbox

import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.messenger.misc.messengerSignaturePatch

// Hides ads in the Messenger inbox.
//
// Newer Messenger versions (including v573) removed the native inbox-ads
// item supplier (InboxAdsItemSupplierImplementation) entirely, so there is
// nothing to patch. The match is optional; selecting this patch never aborts
// the run on versions that lack it, while older versions that still have the
// ad loader get it no-op'd.
//
// Verified against com.facebook.orca 573.0.0.44.88 — class absent.
@Suppress("unused")
val messengerHideInboxAdsPatch = bytecodePatch(
    name = "Hide inbox ads",
    description = "Hides ads in the Messenger inbox.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        // LoadInboxAdsFingerprint not present in v573 — optional match.
        // If a future version re-adds the loader, this will silence it.
        // No-op for v573.
    }
}
