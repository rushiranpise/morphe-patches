package app.template.patches.reddit.ad

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─────────────────────────────────────────────────────────────────────────────
// Ad fingerprints — verified against 2026.32.0 smali (classes5)
//
// Reddidnt approach: target composable RENDER methods by stable UI tag string.
// Never inject into constructors (VerifyError) or low-register methods.
// ─────────────────────────────────────────────────────────────────────────────

// com.reddit.ads.impl.feeds.composables.c
// .method public final d(Lbrv;Lkotlin/jvm/functions/Function0;Lye10;Lmc20;
//                        Luph;Lekw;Lbrv;Lzkb;I)V  .registers 32
// const-string "promoted_post_unit"
internal object PromotedPostComposableFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf(
        "L", "Lkotlin/jvm/functions/Function0;",
        "L", "L", "L", "L", "L", "L", "I",
    ),
    filters = listOf(string("promoted_post_unit")),
    custom = { _, classDef ->
        classDef.type.startsWith("Lcom/reddit/ads/impl/feeds/composables/")
    },
)

// yz9 (classes5 obfuscated)
// .method public final static b(Lxs9;ZZF ... Lzkb;III)V  .registers 49
// const-string "blank_ad_container"
internal object CommentAdComposableFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    filters = listOf(string("blank_ad_container")),
)

// com.reddit.comments.presentation.CommentsViewModel (stable class name)
// .method public final <method>(Z, L, I)V — arms comment ad loading
internal object CommentsViewModelAdLoaderFingerprint : Fingerprint(
    definingClass = "Lcom/reddit/comments/presentation/CommentsViewModel;",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Z", "L", "I"),
    filters = listOf(
        methodCall(
            opcode = Opcode.INVOKE_DIRECT,
            name = "<init>",
            parameters = listOf("Z", "I"),
            returnType = "V",
        ),
    ),
)

// CommentsAdState.toString() — used to dynamically find adsLoadCompleted field
internal object CommentsAdStateToStringFingerprint : Fingerprint(
    name = "toString",
    returnType = "Ljava/lang/String;",
    filters = listOf(
        string("CommentsAdState(conversationAdViewState="),
        string(", adsLoadCompleted="),
    ),
)
