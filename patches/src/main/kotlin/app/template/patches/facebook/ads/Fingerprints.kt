package app.template.patches.facebook.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.TypeReference
import com.android.tools.smali.dexlib2.iface.value.StringEncodedValue

// ─── Helpers ────────────────────────────────────────────────────────────────

/**
 * Matches a Redex-obfuscated Runnable lambda by __redex_internal_original_name field.
 * Target method is always: public final run()V on the same class.
 */
private fun runMethod(fieldValue: String) = Fingerprint(
    returnType = "V",
    parameters = listOf(),
    custom = { method, classDef ->
        method.name == "run" &&
            classDef.fields.any { field ->
                if (field.name != "__redex_internal_original_name") return@any false
                (field.initialValue as? StringEncodedValue)?.value == fieldValue
            }
    },
)

// ─── Story ad insertion Runnables (AdBucketDataSourceUtil) ──────────────────
// Verified: classes18/X/9rw,9rv and classes6/X/Wh4,Wh5,Wh6

internal val AdsInsertionMethodFingerprint     = runMethod("AdBucketDataSourceUtil\$attemptAdsInsertion\$1")
internal val FetchDeferredAdsMethodFingerprint = runMethod("AdBucketDataSourceUtil\$fetchDeferredAds\$1")
internal val FetchMoreAdsMethodFingerprint     = runMethod("AdBucketDataSourceUtil\$attemptFetchMoreAds\$1")
internal val TriggerCtaTailloadFingerprint     = runMethod("AdBucketDataSourceUtil\$triggerCtaTailload\$1")
internal val TriggerDwellTailloadFingerprint   = runMethod("AdBucketDataSourceUtil\$triggerDwellTailload\$1")

// ─── Main feed sponsored stories (LX/2UY.A06) ───────────────────────────────
// Verified: classes13/X/2UY.smali → method A06(LX/Ruj;)Ljava/lang/String;
//
// KEY: strings field is checked PER METHOD (must be in the target method body, not just
// the class). "This should not be called for base class object" IS in A06's own body. ✓
//
// Fingerprint: strings fast-path via string in A06 body + custom discriminates A06 from A07.
// A06 has NarrowLiteral 0x2b0083ed (GraphQL ad type ID) — absent in A07.
// A06 has instance-of GraphQLCreativePagesYouMayLikeFeedUnit — absent in 2t7.A02/A03.
internal val GetAdVisibilityDispatcherFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Ljava/lang/String;",
    strings = listOf("This should not be called for base class object"),
    custom = { method, _ ->
        val instructions = method.implementation?.instructions?.toList()
            ?: return@Fingerprint false

        val hasAdTypeLiteral = instructions.any { insn ->
            insn is NarrowLiteralInstruction && insn.narrowLiteral == 0x2b0083ed.toInt()
        }
        val hasCreativePagesInstanceOf = instructions.any { insn ->
            insn.opcode == Opcode.INSTANCE_OF &&
                (insn as? ReferenceInstruction)?.reference
                    ?.let { it as? TypeReference }
                    ?.type == "Lcom/facebook/graphql/model/GraphQLCreativePagesYouMayLikeFeedUnit;"
        }
        hasAdTypeLiteral && hasCreativePagesInstanceOf
    },
)

// ─── Main feed sponsored pool gate (LX/1zK.A03) ─────────────────────────────
// Verified: classes13/X/1zK.smali → method A03(GraphQLFeedUnitEdge)Z
//
// KEY: "SponsoredPoolContainerAdapter" string is in the CLASS but NOT in A03's method body.
// Cannot use strings field. Use filters with getCachedBoolean — stable non-obfuscated call
// that IS in A03's body (called twice on BaseModelWithTree to check sponsor flags).
//
// Fingerprint: PUBLIC FINAL, Z return, GraphQLFeedUnitEdge param +
// filter on BaseModelWithTree.getCachedBoolean (non-obfuscated SDK method).
internal val SponsoredPoolAddFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/facebook/graphql/model/GraphQLFeedUnitEdge;"),
    filters = listOf(
        methodCall(
            definingClass = "Lcom/facebook/graphql/modelutil/BaseModelWithTree;",
            name = "getCachedBoolean",
        ),
    ),
)

// ─── Main feed sponsored pool secondary gate (LX/1zK.A79) ───────────────────
// Verified: classes13/X/1zK.smali → method A79(L,L)Z
//
// A79 DOES contain "SponsoredPoolContainerAdapter" in its body → strings works here.
// Also has "Edge type mismatch; not added" (unique to A79).
// Discriminate from A03 by: 2 params (not 1) + strings in body.
internal val SponsoredPoolNetworkAddFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("L", "L"),
    strings = listOf("Edge type mismatch; not added"),
)

// ─── Reels ads floating CTA eligibility (LX/ABK.A05) ────────────────────────
// Verified: classes18/X/ABK.smali → method A05(ABF, ABG, I)Z
//
// Previous filter approach (methodCall OrganicAffiliateFloatingCtaPlugin.A00) failed
// to match despite the call existing in A05's body.
//
// FIX: WideLiteralInstruction custom lambda.
// A05 contains const-wide v0, 0x8102e100891736L — a MobileConfig parameter ID that
// appears ONLY in classes18/X/ABK.smali across all 21 DEX files (verified unique).
// Combined with PUBLIC STATIC + Z return + (L,L,I) params, uniquely identifies A05.
internal val ReelsAdIndicatorPillFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    returnType = "Z",
    parameters = listOf("L", "L", "I"),
    custom = { method, _ ->
        method.implementation?.instructions?.any { insn ->
            insn.opcode == Opcode.CONST_WIDE &&
                (insn as? com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction)
                    ?.wideLiteral == 0x8102e100891736L
        } == true
    },
)

// ─── Neko playable ad activity (non-obfuscated) ─────────────────────────────
// Verified: classes7/com/facebook/neko/playables/activity/NekoPlayableAdActivity.smali
internal val NekoPlayableAdActivityFingerprint = Fingerprint(
    definingClass = "Lcom/facebook/neko/playables/activity/NekoPlayableAdActivity;",
    name = "onCreate",
    returnType = "V",
    parameters = listOf("Landroid/os/Bundle;"),
)
