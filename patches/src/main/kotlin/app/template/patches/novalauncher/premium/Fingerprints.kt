package app.template.patches.novalauncher.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// ─── Target 1: IsPrimeStaticFingerprint — wu/v0.b()Z ───────────────────────
//
// Obfuscated class: Lwu/v0; (classes.dex)
// Method: public final static b()Z
//
// This is the primary isPrime() gate consulted by 8 call sites including
// NovaLauncher, NovaApplication, NovaFeedContentView, and several settings
// fragments. It ORs three sources:
//   - oy/h2.h:Z         (runtime prime state from SharedPreferences)
//   - wu/v0.b:Z         (static prime flag)
//   - lv/f0.a(Context)Z (prime grant from "nova_prime_grant" prefs with expiry)
//
// Fingerprint strategy:
//   Two stable filter anchors unique to this method body:
//   1. fieldAccess IGET_BOOLEAN on Loy/h2;->h:Z — the runtime prime state field
//   2. methodCall Llv/f0;->a(Context)Z — the grant expiry check
//   This combination has exactly 1 match across all 46,786 smali files (5 DEX shards).
//
// Access flags: PUBLIC FINAL STATIC
// Return type:  Z
// Parameters:   none
// DEX: classes.dex — smali verified against versionCode 88800.
internal object IsPrimeStaticFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = emptyList(),
    filters = listOf(
        // iget-boolean v0, v0, Loy/h2;->h:Z
        fieldAccess(
            opcode = Opcode.IGET_BOOLEAN,
            definingClass = "Loy/h2;",
            name = "h",
        ),
        // invoke-static {v2}, Llv/f0;->a(Landroid/content/Context;)Z
        methodCall(
            definingClass = "Llv/f0;",
            name = "a",
        ),
    ),
)

// ─── Target 2: PrimeStateInitFingerprint — oy/h2 settings class ────────────
//
// Obfuscated class: Loy/h2; (classes.dex)
// Method: a(SharedPreferences)V — public final
//
// This method initialises the runtime prime state (h:Z) on startup by reading
// SharedPreferences. It is called once during Application.onCreate().
// There are 3 iput-boolean writes to h:Z in this method, the last of which
// (at :L16) produces the final value read by 45+ classes directly accessing
// oy/h2.h:Z throughout the app lifecycle.
//
// Fingerprint strategy:
//   "ro.razer.internal.api" is a globally unique string (1 file, 1 method).
//   It appears in the Razer device detection branch that also sets h:Z = true.
//   This makes it the sharpest possible anchor for this method.
//
// Access flags: PUBLIC FINAL
// Return type:  V
// Parameters:   Landroid/content/SharedPreferences;
// DEX: classes.dex — smali verified against versionCode 88800.
internal object PrimeStateInitFingerprint : Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Landroid/content/SharedPreferences;"),
    filters = listOf(
        string("ro.razer.internal.api"),
    ),
)

// ─── Target 3: SubscriptionActiveFingerprint — lv/c0.w(Context)Z ───────────
//
// Obfuscated class: Llv/c0; (classes.dex)
// Method: public final static w(Landroid/content/Context;)Z
//
// Reads "nova_billing" SharedPreferences file and checks "subscription_active"
// boolean. Called by 6 classes in the billing and feature-gate path including
// wu/v0.c(), ly/w0, ly/a0, nx/p2, cv/d.
// Returns false if not subscribed or if subscription verification has expired
// (last_verified > 48h via lv/c0.y()).
//
// Fingerprint strategy:
//   Three stable string constants co-present only in lv/c0:
//   "nova_billing" + "subscription_active" + "last_verified" → unique to this class.
//   The method itself contains the first two; lv/c0.y() (called from w()) holds
//   "last_verified". Using "subscription_active" as the filter pins to w() within
//   the class (not x() which reads subscription_active differently).
//
// Access flags: PUBLIC FINAL STATIC
// Return type:  Z
// Parameters:   Landroid/content/Context;
// DEX: classes.dex — smali verified against versionCode 88800.
internal object SubscriptionActiveFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL, AccessFlags.STATIC),
    parameters = listOf("Landroid/content/Context;"),
    filters = listOf(
        string("nova_billing"),
        string("subscription_active"),
        // xor-int/lit8 p0, p0, 1 — inverts y() result; appears only in w(), not x()
        opcode(Opcode.XOR_INT_LIT8),
    ),
)
