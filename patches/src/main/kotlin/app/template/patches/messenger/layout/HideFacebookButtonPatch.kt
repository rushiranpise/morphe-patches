package app.template.patches.messenger.layout

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MESSENGER_COMPATIBILITY
import app.template.patches.shared.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import app.template.patches.messenger.misc.messengerSignaturePatch

// Hides all buttons and shortcuts that open or cross-promote Facebook from within Messenger.
//
// Every Facebook entry point is a UI plugin. Whether it is shown is decided by a
// no-argument boolean "lazy loader" method that constructs the plugin via new-instance
// and returns true, or returns false to hide it. There is one gate per surface, so
// all gates for each plugin class must be neutralized.
//
// Targeting by the plugins' stable (non-obfuscated) class descriptors rather than
// individual obfuscated loader methods keeps this robust across app updates.
//
// Entry points verified present in com.facebook.orca 573.0.0.44.88 (classes7/):
//   • FacebookButtonTabButtonImplementation  — FB button in inbox toolbar
//   • NavBarMenuItemImplementation           — FB icon in Marketplace nav bar
//   • ThreadSettingsFacebookProfileActionButton — "View Facebook profile" in chat settings
//   • FacebookShortcutsFolderSection         — "Also from Meta" drawer section
//   • ShareToFacebookButtonImplementation    — "Share to Facebook" on channel invites
//   • ShareToFacebookHScrollButtonImplementation — "Share to Facebook" in public-chats share row
private val FACEBOOK_ENTRY_POINT_CLASSES = setOf(
    "Lcom/facebook/messaging/inbox/tab/plugins/core/tabtoolbarbutton/facebookbutton/facebooktoolbarbutton/FacebookButtonTabButtonImplementation;",
    "Lcom/facebook/messaging/marketplace/plugins/folder/navbarmenuitem/NavBarMenuItemImplementation;",
    "Lcom/facebook/messaging/profile/plugins/core/threadsettingsactionbutton/facebookprofile/ThreadSettingsFacebookProfileActionButton;",
    "Lcom/facebook/messaging/navigation/plugins/drawerfoldersections/fbshortcutsfoldersection/FacebookShortcutsFolderSection;",
    "Lcom/facebook/messaging/communitymessaging/plugins/channelinvite/sharetofacebookbutton/ShareToFacebookButtonImplementation;",
    "Lcom/facebook/messaging/publicchats/plugins/externalsharehscrollbuttons/sharetofacebook/ShareToFacebookHScrollButtonImplementation;",
)

@Suppress("unused")
val messengerHideFacebookButtonPatch = bytecodePatch(
    name = "Hide Facebook buttons",
    description = "Hides buttons and shortcuts that open Facebook.",
) {
    compatibleWith(MESSENGER_COMPATIBILITY)

    dependsOn(messengerSignaturePatch)

    execute {
        classDefForEach { classDef ->
            classDef.methods.forEach forEachMethod@{ method ->
                if (method.parameterTypes.isNotEmpty() || method.returnType != "Z") return@forEachMethod

                val constructsEntryPoint = method.implementation?.instructions?.any { instruction ->
                    instruction.opcode == Opcode.NEW_INSTANCE &&
                        (instruction as? ReferenceInstruction)
                            ?.reference?.let { it as? TypeReference }
                            ?.type in FACEBOOK_ENTRY_POINT_CLASSES
                } ?: false

                if (constructsEntryPoint) {
                    mutableClassDefBy(classDef)
                        .methods.first { it.name == method.name && it.parameterTypes == method.parameterTypes }
                        .returnEarly(false)
                }
            }
        }
    }
}
