package app.template.patches.wetteronline.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// v45 — FusedAccessProvider implementation (classes2.dex)
//
// v45.d()Z is the isPro gate. It checks:
//   1. iget-boolean Lv45;->c:Z            (build-time debug flag)
//   2. invoke-virtual Lur7;->b()Llr7;     (membership object)
//      iget-boolean Llr7;->h:Z            (membership boolean)
//   3. invoke-virtual Lebc;->b()Lvac;     (subscription cache)
//      iget-wide Lvac;->d:J               (subscription expiry millis)
//      String.valueOf(J) → compare with stored token
//
// Unique identifiers:
//   - Only class implementing non-obfuscated interface FusedAccessProvider
//   - Only method in v45 with signature ()Z PUBLIC FINAL
//   - Filter: iget-wide on a J field — the Long subscription expiry timestamp read
//
// DEX: classes2.dex. Smali verified.

internal object IsFusedAccessProviderProFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_WIDE,
            type = "J",
        ),
    ),
    custom = { _, classDef ->
        classDef.interfaces.contains(
            "Lde/wetteronline/wetterapp/shared/access/FusedAccessProvider;"
        )
    },
)

// n6 — IsProUseCase implementation (classes2.dex)
//
// n6.a()Z:
//   iget-object p0, p0, Ln6;->a:Lde/wetteronline/wetterapp/shared/access/FusedAccessProvider;
//   check-cast p0, Lv45;
//   invoke-virtual {p0}, Lv45;->d()Z
//   return p0
//
// Unique: only class implementing IsProUseCase; only virtual method returning Z.
// Filter: iget-object on the FusedAccessProvider-typed field (non-obfuscated type).

internal object IsProUseCaseFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Lde/wetteronline/wetterapp/shared/access/FusedAccessProvider;",
        ),
    ),
    custom = { _, classDef ->
        classDef.interfaces.contains(
            "Lde/wetteronline/wetterapp/shared/access/IsProUseCase;"
        )
    },
)

// BillingClientStateListener.onBillingSetupFinished (vpa.j, classes2.dex)
//
// vpa.j(Le11;)V — called when BillingClient.startConnection() completes.
// Le11 = BillingResult; e11.a = responseCode (int).
//
// Flow:
//   iget v0, p1, Le11;->a:I      ← read responseCode field
//   iget-object p0, p0, Lvpa;->c:Ljava/lang/Object;
//   check-cast p0, Lde/wetteronline/wetterapp/shared/access/e;
//   iget-object p0, p0, Lde/wetteronline/wetterapp/shared/access/e;->c:Lzxb;
//   if-nez v0, :cond_1a           ← if nonzero → emit error
//   [OK branch: emit Success singleton to StateFlow]
//   :cond_1a [error: emit SubscriptionException]
//
// Patch: return-void — the StateFlow stays in initial state; premium gate
// (v45.d()Z) already returns true so the app proceeds normally.
//
// Fingerprint anchors (no Instruction.toString() — that only returns opcode name):
//   1. fieldAccess(IGET, name="a", type="I") — reads responseCode from BillingResult
//   2. fieldAccess(IGET_OBJECT, definingClass="...access/e;", name="c") —
//      reads the MutableStateFlow field on the non-obfuscated BillingStateHolder class
//   custom: classDef implements BillingClientStateListener (stable SDK interface)
//
// DEX: classes2.dex. Smali verified line 1623.

internal object BillingSetupFinishedFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = listOf("L"), // BillingResult — obfuscated Le11;
    filters = listOf(
        // reads responseCode (int field "a") from BillingResult parameter
        fieldAccess(
            opcode = Opcode.IGET,
            name = "a",
            type = "I",
        ),
        // reads StateFlow field "c" from the non-obfuscated BillingStateHolder class
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            definingClass = "Lde/wetteronline/wetterapp/shared/access/e;",
            name = "c",
        ),
    ),
    custom = { _, classDef ->
        classDef.interfaces.contains(
            "Lcom/android/billingclient/api/BillingClientStateListener;"
        )
    },
)

// BillingClientStateListener.onBillingServiceDisconnected (vpa.o, classes2.dex)
//
// vpa.o()V — called when billing service disconnects.
// Emits a SubscriptionException("Lost connection to the billing service")
// which puts the StateFlow into error state, blocking the app.
//
// Patch: return-void — suppress disconnect events entirely.
// Smali anchor: implements BillingClientStateListener, returns V, no parameters,
// contains the literal string "Lost connection to the billing service".

internal object BillingServiceDisconnectedFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
    filters = listOf(
        string("Lost connection to the billing service"),
    ),
    custom = { _, classDef ->
        classDef.interfaces.contains(
            "Lcom/android/billingclient/api/BillingClientStateListener;"
        )
    },
)
