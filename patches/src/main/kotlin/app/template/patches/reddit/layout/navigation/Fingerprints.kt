package app.template.patches.reddit.layout.navigation

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Navigation fingerprints — verified against 2026.32.0 smali
//
// BottomNavTab enum (2026.32.0):
//   Home, Communities, Answers, Post, Chat, Inbox,
//   Games, UnifiedInbox, Profile, MyCommunities
//
// The "Create post" button is the `Post` tab (NOT "CREATE" as previously assumed).
//
// BottomNavScreen.m5(List, zkb, I)V — the composable method that renders
// the nav bar from a List<BottomNavTab>. This is the stable injection point.
//
// Stable anchor strings in BottomNavTab.clinit:
//   "Home", "Post", "Chat" — these are stored as const-string in the enum
//   initializer and stable across obfuscation.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * BottomNavTab.Post field accessor — stable because BottomNavTab is a
 * non-obfuscated enum class and field names match Kotlin source.
 */
internal object BottomNavTabPostFieldFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/launch/bottomnav/BottomNavTab;",
    name = "<clinit>",
    returnType = "V",
    filters = listOf(
        string("Post"),
        fieldAccess(
            opcode = Opcode.SPUT_OBJECT,
            definingClass = "Lcom/reddit/launch/bottomnav/BottomNavTab;",
            name = "Post",
        ),
    ),
)

/**
 * BottomNavScreen.m5(Ljava/util/List;Lzkb;I)V — receives the assembled
 * tab list and renders the nav bar composable.
 * Fingerprinted by its stable parameter signature: (List, zkb, I).
 * We inject a filter at the start to remove the Post tab from the list.
 */
internal object BottomNavScreenM5Fingerprint : Fingerprint(
    definingClass = "Lcom/reddit/launch/bottomnav/BottomNavScreen;",
    name = "m5",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/util/List;", "L", "I"),
)
