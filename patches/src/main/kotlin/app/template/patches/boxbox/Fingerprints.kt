package app.template.patches.boxbox

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Box Box — F1 & Formula Racing News (club.boxbox.android) v5.4.13
//
// Anti-tamper: No PairIP. No Play Integrity app-level usage.
//
// Subscription architecture — RevenueCat + DataStore persistence:
//   RevenueCat listener → kv6.m(CustomerInfo):
//     1. Emits CustomerInfo to kv6.k StateFlow
//     2. Launches jv6 coroutine with isProUser:Z
//   jv6.invokeSuspend:
//     Reads jv6.c:Z (isProUser), calls pd6.b(rb8.b_Pro/rb8.a_Free, nd8):
//       pd6.b() writes rb8 (plan type) to "plans_data_store" DataStore
//       via key pd6.d = pl6("plan_type")
//   l0.invokeSuspend (BoxBoxFirebaseMessagingService coroutine):
//     Calls pd6.a() → reads rb8 from DataStore
//     Creates Purchases (confused naming), calls k06 coroutine
//     Result unwrapped via blb.j() → rb8 enum value
//     Compares to rb8.b (Pro): if-ne v0, rb8.b, :cond_62
//       true → enables pro features
//       false → shows paywall
//
// Root cause of failure: subscription state is PERSISTED in DataStore ("plans_data_store").
// kv6.m() is only called when RevenueCat delivers CustomerInfo (async, may not fire).
// l0 reads directly from the persisted DataStore value, so even if kv6.m() is patched,
// the DataStore already has rb8.a (Free) stored from a prior legitimate check.
//
// Patch strategy — two-pronged:
//   1. pd6.b(rb8, nd8)V → always write rb8.b (Pro) regardless of input p1.
//      Intercepts every DataStore plan write and forces Pro, covering:
//        - jv6 emitting rb8.a (Free) after isActive()=false
//        - Any other write path
//   2. l0.invokeSuspend DataStore read result → force v0 = rb8.b (Pro).
//      Intercepts the rb8 value IMMEDIATELY after blb.j() unwraps it
//      from the DataStore read, before the comparison at if-ne v0, rb8.b, :cond_62.
//      Identified by "is eligible to get LA" string (nearby unique in same method).

/**
 * Targets pd6.b(rb8, nd8)Ljava/lang/Object — the DataStore plan writer.
 *
 * Called by jv6 coroutine to persist rb8.a (Free) or rb8.b (Pro) to
 * "plans_data_store" DataStore. Patching p1 = rb8.b (Pro) at method entry
 * ensures every plan write persists Pro, covering all write paths.
 */
internal object PlanDataStoreWriterFingerprint : Fingerprint(
    definingClass = "Lpd6;",
    name = "b",
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lrb8;", "Lnd8;"),
    filters = listOf(
        opcode(Opcode.SGET_OBJECT),  // pd6.b = md6
        opcode(Opcode.IGET_OBJECT),  // pd6.a = Context
    ),
)

/**
 * Targets l0.invokeSuspend at the DataStore plan read result.
 *
 * After blb.j() unwraps the DataStore coroutine result into an rb8 value (v0),
 * the next instruction is check-cast v0, Lrb8; followed by the Pro comparison.
 * We inject sget-object v0, Lrb8;->b:Lrb8; between the check-cast and the
 * comparison to force rb8.Pro regardless of what was persisted in DataStore.
 *
 * Identified by "is eligible to get LA" (unique string in this method, near the
 * check-cast Lrb8 at line 9082) combined with CHECK_CAST opcode targeting rb8.
 */
internal object PlanDataStoreReaderFingerprint : Fingerprint(
    strings = listOf("is eligible to get LA"),
    filters = listOf(
        opcode(Opcode.CHECK_CAST),  // check-cast v0, Lrb8;
    ),
)
