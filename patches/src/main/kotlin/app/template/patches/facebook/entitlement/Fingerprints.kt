package app.template.patches.facebook.entitlement

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.WideLiteralInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

// ─── Facebook Benefit / Entitlement System (v569) ────────────────────────────
//
// Facebook uses a server-driven benefit entitlement system, not a local billing SDK.
// The active premium benefits are pushed from the server into LX/84K.A01:Set<String>
// on login. UI components read this set (directly or via LX/9Re.A03) to decide
// whether a feature is unlocked.
//
// Reverse-engineered from MetaPlusXposed (github.com/rushiranpise/MetaPlusXposed)
// and verified against com.facebook.katana 569.0.0.42.72 smali (classes17, classes18).
//
// Benefit key enum (LX/AZX, classes18) — 130+ keys including all consumer plus keys:
//   CUSTOM_APP_ICON, CUSTOM_APP_THEME, CUSTOM_PROFILE_BIO_FONT, STORY_EXTEND,
//   STORY_PREVIEW, STORY_SUPERLIKES, STORY_REWATCH, SEARCH_STORY_VIEWERS,
//   BIZ_LINKS_IN_REELS, ENHANCED_CONTENT_SCHEDULING, ENHANCED_CONTENT_PROTECTION,
//   AI_CREDITS, AI_VOICE, IMAGE_GENERATION, IMAGINE_VIDEO, ADVANCED_REASONING, etc.
//
// Full patch chain:
//
//  1. BenefitCheckerFingerprint (LX/9Re.A03) — the dispatcher that maps incoming
//     string keys to canonical names and delegates to LX/84K.A08. Returning true here
//     short-circuits the whole chain before reaching the Set check.
//
//  2. BenefitSetCheckFingerprint (LX/84K.A08) — the deep check: reads A01:Set and
//     calls Set.contains(benefitKey). Returning true here covers all call sites that
//     bypass 9Re and call 84K directly.
//
//  3. BenefitRelock_U0t (LX/U0t.Cqz) — CGN interface listener that receives the
//     live benefit set from the server and updates S5O (the app-icon subscription
//     state holder), potentially RE-LOCKING the custom app icon after our patches.
//     Returning void (return-void) from Cqz prevents the re-lock from happening.
//
//  4–6. MetaAI gates (LX/2kD): previously written, kept in same fingerprint file.

// ─── 1. Benefit checker dispatcher (LX/9Re.A03) ─────────────────────────────
// Verified: classes18/X/9Re.smali → method A03(String)Z
//
// A03 takes a benefit key string (e.g. "CUSTOM_APP_ICON"), normalises aliases via
// a sparse-switch/hashCode dispatch, then calls LX/84K;->A08(String)Z.
//
// Fingerprint discriminators (verified unique across all 21 DEX files):
//   • PUBLIC FINAL, returnType Z, parameter String
//   • classDef contains "STORY_REWATCH" string constant (benefit key enum stub)
//   • method body invokes LX/84K;->A08(Ljava/lang/String;)Z via invoke-virtual
internal val BenefitCheckerFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    custom = { method, classDef ->
        // Class must reference the "STORY_REWATCH" benefit key (marks the benefit provider)
        val classHasStoryRewatch = classDef.methods.any { m ->
            m.implementation?.instructions?.any { insn ->
                (insn as? ReferenceInstruction)?.reference?.toString() == "STORY_REWATCH"
            } == true
        }
        // Method must call 84K.A08(String)Z — the underlying Set check
        val methodCallsSetCheck = method.implementation?.instructions?.any { insn ->
            insn.opcode == Opcode.INVOKE_VIRTUAL &&
                (insn as? ReferenceInstruction)?.reference.let { ref ->
                    ref is MethodReference &&
                        ref.definingClass == "LX/84K;" &&
                        ref.name == "A08"
                }
        } == true
        classHasStoryRewatch && methodCallsSetCheck
    },
)

// ─── 2. Benefit Set.contains check (LX/84K.A08) ─────────────────────────────
// Verified: classes17/X/84K.smali → method A08(String)Z, line 1047
//
// A08 calls Set.contains(benefitKey) on A01:Set, then does analytics logging.
// Returning true here covers call sites that reach 84K directly (bypassing 9Re).
//
// Fingerprint discriminators (verified unique):
//   • PUBLIC FINAL, returnType Z, parameter String
//   • classDef has "network_sync_failed" AND "is_benefit_active" strings
//     (unique to the 84K entitlement manager class)
internal val BenefitSetCheckFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Ljava/lang/String;"),
    custom = { method, classDef ->
        // Class-level anchor: both strings only appear together in LX/84K
        val classHasAnchors = classDef.methods.any { m ->
            val insns = m.implementation?.instructions?.toList() ?: return@any false
            val strs = insns.filterIsInstance<ReferenceInstruction>()
                .mapNotNull { it.reference?.toString() }.toSet()
            "network_sync_failed" in strs && "is_benefit_active" in strs
        }
        // Method-level: calls Set.contains — the actual benefit check
        val methodCallsContains = method.implementation?.instructions?.any { insn ->
            insn.opcode == Opcode.INVOKE_INTERFACE &&
                (insn as? ReferenceInstruction)?.reference.let { ref ->
                    ref is MethodReference &&
                        ref.definingClass == "Ljava/util/Set;" &&
                        ref.name == "contains"
                }
        } == true
        classHasAnchors && methodCallsContains
    },
)

// ─── 3. CGN re-locking listener for app icons (LX/U0t.Cqz) ──────────────────
// Verified: classes5/X/U0t.smali → method Cqz(Set)V
//
// U0t implements LX/CGN (the benefit-update listener interface).
// Its Cqz(Set)V is called by the provider when the server sends a new benefit set.
// It copies p1 (the new set) into S5O.A06:Set and, if CUSTOM_APP_ICON is NOT in the
// new set, dispatches a runnable (U8f) that re-locks the app icon picker.
//
// Returning void (returnEarly/return-void) from Cqz prevents the re-lock runnable
// from ever being posted, keeping custom app icons unlocked even after a sync.
//
// Fingerprint discriminators (verified unique — only U0t among CGN implementors has
// "CUSTOM_APP_ICON" referenced inside its Cqz body):
//   • implements LX/CGN (Cqz method name is stable — part of the interface contract)
//   • method name "Cqz"
//   • method body references "CUSTOM_APP_ICON" string
internal val BenefitRelockListenerFingerprint = Fingerprint(
    name = "Cqz",
    returnType = "V",
    parameters = listOf("Ljava/util/Set;"),
    custom = { method, classDef ->
        // Must implement CGN interface
        val implementsCGN = classDef.interfaces.any { it == "LX/CGN;" }
        // Cqz body must reference "CUSTOM_APP_ICON" to distinguish from other CGN impls
        val hasCustomAppIcon = method.implementation?.instructions?.any { insn ->
            (insn as? ReferenceInstruction)?.reference?.toString() == "CUSTOM_APP_ICON"
        } == true
        implementsCGN && hasCustomAppIcon
    },
)

// ─── 4–6. MetaAI premium gates (LX/2kD, classes13) ──────────────────────────
// (Unchanged from original commit — kept here for single-import convenience)

internal val MetaOnePremiumRowFingerprint = Fingerprint(
    definingClass = "Lcom/facebook/messaging/aibot/plugins/core/threadsettings/metaonepremium/ThreadSettingsMetaOnePremiumRow;",
    name = "A00",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
)

internal val MetaAIAvailableFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/facebook/auth/usersession/FbUserSession;"),
    custom = { method, classDef ->
        val methodHasLiteral = method.implementation?.instructions?.any { insn ->
            insn.opcode == Opcode.CONST_WIDE &&
                (insn as? WideLiteralInstruction)?.wideLiteral == 0x101060900003930L
        } == true
        val classHasAnchor = classDef.methods.any { m ->
            m.implementation?.instructions?.any { insn ->
                insn.opcode == Opcode.CONST_WIDE &&
                    (insn as? WideLiteralInstruction)?.wideLiteral == 0x810901001d381fL
            } == true
        }
        methodHasLiteral && classHasAnchor
    },
)

internal val MetaAIPremiumEnabledFingerprint = Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    returnType = "Z",
    parameters = listOf("Lcom/facebook/auth/usersession/FbUserSession;", "L"),
    custom = { method, _ ->
        method.implementation?.instructions?.any { insn ->
            insn.opcode == Opcode.CONST_WIDE &&
                (insn as? WideLiteralInstruction)?.wideLiteral == 0x810901001d381fL
        } == true
    },
)
