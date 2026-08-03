package app.template.patches.stickerly

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import app.morphe.patcher.methodCall
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ── Type constants ────────────────────────────────────────────────────────────

private const val SUBSCRIPTION_MODEL =
    "Lcom/snowcorp/stickerly/android/base/domain/payment/SubscriptionModel;"
private const val SUBSCRIPTION_RESPONSE =
    "Lcom/snowcorp/stickerly/android/base/data/serverapi/SubscriptionResponse;"

// ── Targets ─────────────────────────────────────────────────────────────────
//
// 1. SubscriptionCacheReadFingerprint
//    Class:   obfuscated (rx/l in 3.36.0 — changes every update)
//    Method:  a()SubscriptionModel — reads the cached SubscriptionModel from SharedPreferences.
//             This is the single root source of truth for subscription state.
//             Both ty/n.a()Z (boolean gate) and ty/n.b() (full-object accessor) delegate here.
//             Patching here covers ALL consumers in one shot.
//    Access:  PUBLIC FINAL (non-static)
//    DEX:     classes5
//    Smali verified (.registers 5 = v0..v3 + p0):
//      iget-object v0, p0, <cache_field>       // get SharedPrefs wrapper
//      ...
//      invoke-interface {v0, v1, v2}, SharedPreferences;->getString("subscription_model", null)
//      move-result-object v0
//      if-nez v0, :cond_12
//      sget-object v0, SubscriptionModel;->e:SubscriptionModel;  // default empty model
//      return-object v0
//      :cond_12
//      ... Moshi JSON parse ...
//      return-object v0
//    Fingerprint anchors (zero obfuscated references):
//      - returnType = SubscriptionModel, PUBLIC FINAL, no params
//      - string("subscription_model")     — stable app-defined SharedPrefs key
//      - fieldAccess SGET_OBJECT on SubscriptionModel with type SubscriptionModel
//        (reads the static default empty-model sentinel — non-obfuscated class on both sides)
//    Patch: clearBody() + construct synthetic premium SubscriptionModel using v0..v3,p0.
//           .registers 5 = exactly v0,v1,v2,v3 (locals) + p0=this.
//           p0 is overwritten as a scratch register for the second Date object —
//           valid because the method returns before p0 is ever read as 'this'.
//
// 2. SubscriptionResponseMapperFingerprint
//    Class:   obfuscated (rx/k in 3.36.0 — changes every update)
//    Method:  h(SubscriptionResponse)SubscriptionModel — maps server JSON response to domain.
//             Called when the billing server responds. Result is written to SharedPreferences
//             via ty/n.c(). Patching here ensures the cache is seeded with a premium model
//             after the first server sync, complementing the cache-read patch.
//    Access:  PUBLIC STATIC
//    DEX:     classes5
//    Smali verified (.registers 10):
//      new-instance v0, SubscriptionModel
//      ...
//      invoke-direct {v0,...}, SubscriptionModel;-><init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V
//      return-object v0
//    Fingerprint anchors (zero obfuscated references):
//      - returnType = SubscriptionModel, PUBLIC STATIC, params = [SubscriptionResponse]
//      - methodCall on SubscriptionModel.<init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V

// Fingerprint 1: the SharedPreferences cache reader — single root of all subscription checks
internal val SubscriptionCacheReadFingerprint = Fingerprint(
    returnType = SUBSCRIPTION_MODEL,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        // The SharedPreferences key used to read the persisted subscription JSON.
        // Stable app-defined constant — never obfuscated.
        string("subscription_model"),
        // Reads the static default empty-model field as a fallback.
        // definingClass = SubscriptionModel, type = SubscriptionModel → no obfuscated name needed.
        fieldAccess(
            definingClass = SUBSCRIPTION_MODEL,
            type = SUBSCRIPTION_MODEL,
            opcode = Opcode.SGET_OBJECT,
        ),
    ),
)

// Fingerprint 2: server-response-to-domain mapper — seeds the cache with a premium model
internal val SubscriptionResponseMapperFingerprint = Fingerprint(
    returnType = SUBSCRIPTION_MODEL,
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
    parameters = listOf(SUBSCRIPTION_RESPONSE),
    filters = listOf(
        // Constructs the domain SubscriptionModel — stable non-obfuscated constructor.
        methodCall(
            smali = "$SUBSCRIPTION_MODEL-><init>(ZLjava/lang/String;Ljava/util/Date;Ljava/util/Date;)V",
        ),
    ),
)
