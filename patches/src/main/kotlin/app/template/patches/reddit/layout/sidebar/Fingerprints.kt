package app.template.patches.reddit.layout.sidebar

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Sidebar fingerprints — verified against 2026.32.0 smali (classes3)
//
// Three section-load coroutine lambdas, all with stable non-obfuscated
// definingClass paths. Each invokeSuspend has .registers 3 (About/Resources)
// or .registers 8 (RedditPro). Blanked via return-object p1.
// ─────────────────────────────────────────────────────────────────────────────

// About section — .registers 3, calls c.Z()V then returns kotlin.Unit
// Smali verified: .method public final invokeSuspend(Ljava/lang/Object;)Ljava/lang/Object;
internal object LoadAboutDrawerItemsFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/screens/drawer/community/CommunityDrawerPresenter\$loadAboutDrawerItems\$2;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "Lcom/reddit/screens/drawer/community/c;->Z()V",
        ),
    ),
)

// Resources section — same structure as About
internal object LoadResourcesDrawerItemsFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/screens/drawer/community/CommunityDrawerPresenter\$loadResourcesDrawerItems\$2;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_VIRTUAL,
            smali = "Lcom/reddit/screens/drawer/community/c;->Z()V",
        ),
    ),
)

// Reddit Pro section — .registers 8
internal object LoadRedditProFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/screens/drawer/community/CommunityDrawerPresenter\$loadRedditPro\$1;",
    name = "invokeSuspend",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
)
