package app.template.patches.messenger.inbox

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

// ─── Inbox subtabs Runnable (classes2/X/2Je) ─────────────────────────────────
// Matches InboxSubtabsItemSupplierImplementation$onSubscribe$1 run()V via the
// stable __redex_internal_original_name field.
//
// run() body (verified v573):
//   [0] iget-object  v0, p0, A00            (INVOKE_VIRTUAL on AtomicBoolean at index 2)
//   [1] iget-object  v1, v0, A05:AtomicBoolean
//   [2] const/4      v0, 0x1
//   [3] invoke-virtual {v1, v0}, AtomicBoolean;->set(Z)V
//   [4] return-void
//
// replaceInstruction(2, "const/4 v0, 0x0") flips the AtomicBoolean to false,
// preventing the subtab supplier from signalling "ready" → subtabs never appear.
internal val CreateInboxSubTabsFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.IGET_OBJECT,
        Opcode.IGET_OBJECT,
        Opcode.CONST_4,
        Opcode.INVOKE_VIRTUAL,
        Opcode.RETURN_VOID,
    ),
    custom = { method, classDef ->
        method.name == "run" &&
            classDef.fields.any { field ->
                if (field.name != "__redex_internal_original_name") return@any false
                (field.initialValue as? StringEncodedValue)?.value ==
                    "InboxSubtabsItemSupplierImplementation\$onSubscribe\$1"
            }
    },
)

// ─── Friends inbox / notes tray kill-switch (classes2/X/1mq.A00) ─────────────
// Matches a method that reads the MobileConfig kill-switch for the stories/notes
// horizontal tray (FriendsInboxUnitKillSwitch) and returns a boolean deciding
// whether to show it.
//
// Discriminator: class contains the full kill-switch string constant
// "com.facebook.messaging.friendsinboxunit.plugins.inboxunit.FriendsInboxUnitKillSwitch"
// AND method returns Z with no parameters.
//
// Verified: classes2/X/1mq.smali → method private A00()Z.
// returnEarly(false) hides the tray unconditionally.
internal val FriendsInboxTrayFingerprint = Fingerprint(
    returnType = "Z",
    strings = listOf("com.facebook.messaging.friendsinboxunit.plugins.inboxunit.FriendsInboxUnitKillSwitch"),
)
