package app.template.patches.messenger.metaai

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Meta AI FAB (AiFabComponent render) — classes4/X/7Ey ───────────────────
// Matches the render() method of the floating Meta AI compose button (AiFabComponent).
// render() returns LX/1Kh; (a UI component); returning null at index 0 makes the
// framework render nothing — the FAB disappears.
//
// Discriminators:
//   • returns LX/1Kh;  (the component base class)
//   • parameters = [LX/2C0;]
//   • strings = ["AiFabComponent"]  (logged in error path within same method)
//
// Verified: classes4/X/7Ey.smali → render(LX/2C0;)LX/1Kh;
//   main return-object at line 858, const-string "AiFabComponent" at line 861.
//
// Verified against com.facebook.orca 573.0.0.44.88.
internal val MetaAiFabRenderFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("LX/2C0;"),
    strings = listOf("AiFabComponent"),
)

// ─── Meta AI Creation drawer item gate — classes7/X/F6D.A00()Z ───────────────
// F6D.A00()Z is the lazy-loader / gate for the AiCreationFolderItem in the
// navigation drawer. It checks a kill-switch then constructs the item.
// Returning false at index 0 skips construction entirely — item is never added
// to the drawer list in AvB().
//
// Discriminators:
//   • returns Z, no parameters, PRIVATE
//   • strings = ["com.facebook.messaging.navigation.plugins.aicreationfolder.NavigationAicreationfolderKillSwitch"]
//
// Verified: classes7/X/F6D.smali → method private A00()Z, line 92.
//
// Verified against com.facebook.orca 573.0.0.44.88.
internal val MetaAiCreationFolderItemFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf(),
    strings = listOf("com.facebook.messaging.navigation.plugins.aicreationfolder.NavigationAicreationfolderKillSwitch"),
)

// ─── Meta AI Home drawer item gate — classes7/X/F6D.A01()Z ──────────────────
// F6D.A01()Z is the lazy-loader / gate for the AiHomeFolderItem.
// Same structure as A00; discriminated by its unique kill-switch string.
// Returning false hides the "AI Home" entry from the navigation drawer.
//
// Discriminators:
//   • returns Z, no parameters, PRIVATE
//   • strings = ["com.facebook.messaging.navigation.plugins.aihomefolder.NavigationAihomefolderKillSwitch"]
//
// Verified: classes7/X/F6D.smali → method private A01()Z, line 261.
//
// Verified against com.facebook.orca 573.0.0.44.88.
internal val MetaAiHomeFolderItemFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE),
    parameters = listOf(),
    strings = listOf("com.facebook.messaging.navigation.plugins.aihomefolder.NavigationAihomefolderKillSwitch"),
)

// ─── Meta AI in search results gate — classes3/X/5sR.A00() ──────────────────
// 5sR.A00(1bi, AtomicInteger, I)Z reads the MobileConfig kill-switch for the
// AI search agent implementations and caches the result.
// Returning false disables the AI suggestions row in search.
//
// Discriminators:
//   • returns Z
//   • parameters = [LX/1bi;, Ljava/util/concurrent/atomic/AtomicInteger;, I]
//   • strings = ["messaging.search.aiagent.implementations.SearchAiagentImplementationsKillSwitch"]
//   • methodCall on MobileConfigUnsafeContext.Afy
//
// Verified: classes3/X/5sR.smali → method public static A00(LX/1bi;...I)Z.
//
// Verified against com.facebook.orca 573.0.0.44.88.
internal val MetaAiSearchFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf("LX/1bi;", "Ljava/util/concurrent/atomic/AtomicInteger;", "I"),
    strings = listOf("messaging.search.aiagent.implementations.SearchAiagentImplementationsKillSwitch"),
)
