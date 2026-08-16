package app.template.patches.messenger.metaai

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Meta AI FAB render() — classes4/X/7Ey ───────────────────────────────────
// The render() method of the floating AI compose button. Returning null makes
// the framework render nothing — the FAB is removed from the layout.
//
// Stable anchors (zero obfuscated references):
//   • string "fab_expanded"  — UI state key, only in this one class across the APK
//   • string "AiFabComponent" — component name logged in the error path
//   • PUBLIC (instance method, not static)
//
// "fab_expanded" is unique to this class in classes4 — confirmed 1 match v573.
// No parameter types used — the two strings alone uniquely identify render().
//
// Verified: classes4/X/7Ey.smali → render(LX/2C0;)LX/1Kh; — v573.
internal val MetaAiFabRenderFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    strings = listOf("fab_expanded", "AiFabComponent"),
)

// ─── Meta AI Creation drawer item gate — classes7/X/F6D.A00()Z ───────────────
// Private no-arg boolean gate for AiCreationFolderItem in the nav drawer.
//
// Stable anchors (zero obfuscated references):
//   • methodCall on non-obfuscated AiCreationFolderItem.A00(FbUserSession)Z
//     (the static visibility gate on the item class itself)
//   • string "com.facebook.messaging.navigation.plugins.aicreationfolder.folderitem.AiCreationFolderItem"
//     (the class name logged for diagnostics inside this gate method)
//
// AiCreationFolderItem is a non-obfuscated class — its descriptor survives R8.
//
// Verified: classes7/X/F6D.smali → method private A00()Z — v573.
internal val MetaAiCreationFolderItemFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/facebook/messaging/navigation/plugins/aicreationfolder/folderitem/AiCreationFolderItem;",
            name = "A00",
        ),
    ),
    strings = listOf("com.facebook.messaging.navigation.plugins.aicreationfolder.folderitem.AiCreationFolderItem"),
)

// ─── Meta AI Home drawer item gate — classes7/X/F6D.A01()Z ──────────────────
// Private no-arg boolean gate for AiHomeFolderItem in the nav drawer.
//
// Stable anchors (zero obfuscated references):
//   • methodCall on non-obfuscated AiHomeFolderItem.A00(FbUserSession)Z
//   • string "com.facebook.messaging.navigation.plugins.aihomefolder.folderitem.AiHomeFolderItem"
//
// Verified: classes7/X/F6D.smali → method private A01()Z — v573.
internal val MetaAiHomeFolderItemFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf(),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/facebook/messaging/navigation/plugins/aihomefolder/folderitem/AiHomeFolderItem;",
            name = "A00",
        ),
    ),
    strings = listOf("com.facebook.messaging.navigation.plugins.aihomefolder.folderitem.AiHomeFolderItem"),
)

// ─── Meta AI search suggestions gate — classes3/X/5sR ────────────────────────
// Reads the MobileConfig kill-switch for AI search agent implementations.
// Returning false disables the AI suggestions row in search.
//
// Stable anchors (zero obfuscated references):
//   • methodCall on MobileConfigUnsafeContext;->Afy(J)Z
//     (stable interface method — MobileConfigUnsafeContext is non-obfuscated SDK)
//   • string "messaging.search.aiagent.implementations.SearchAiagentImplementationsKillSwitch"
//     (kill-switch string constant, non-obfuscated)
//   • PUBLIC STATIC, returns Z
//
// MobileConfigUnsafeContext is a Facebook SDK interface — never renamed by R8.
//
// Verified: classes3/X/5sR.smali → public static A00(...)Z — v573.
internal val MetaAiSearchFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/facebook/mobileconfig/factory/MobileConfigUnsafeContext;",
            name = "Afy",
        ),
    ),
    strings = listOf("messaging.search.aiagent.implementations.SearchAiagentImplementationsKillSwitch"),
)
