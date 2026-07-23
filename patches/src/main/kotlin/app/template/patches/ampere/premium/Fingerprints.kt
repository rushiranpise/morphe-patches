package app.template.patches.ampere.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// bz0.f()Z — isProVersion() getter   [classes3.dex]
//
// Reads "isProVersion" boolean from PreferenceManager.getDefaultSharedPreferences()
// via the b01 SharedPrefs boolean delegate. DEEPEST check: this single method is
// called by every premium gate and drives SettingsData propagation to all UI consumers.
//
// Smali verified (.registers 3):
//   sget-object v0, Lbz0;->b:[Lkotlin/reflect/KProperty;
//   const/4 v1, 0
//   aget-object v0, v0, v1
//   sget-object v1, Lbz0;->e:Lb01;
//   invoke-virtual {v1, p0, v0}, Lb01;->getValue(Object, KProperty)Object
//   move-result-object p0
//   check-cast p0, Boolean
//   invoke-virtual {p0}, Boolean->booleanValue()Z
//   move-result p0
//   return p0
//
// Fingerprint: only method in this class calling Lb01;->getValue with a KProperty arg.
// Use fieldAccess on the Lb01; sget-object + methodCall on getValue to anchor uniquely.
val IsProVersionFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            type = "Lb01;",
        ),
        methodCall(
            definingClass = "Lb01;",
            name = "getValue",
        ),
        methodCall(
            definingClass = "Ljava/lang/Boolean;",
            name = "booleanValue",
        ),
    ),
)

// ze.a(Purchase)Z — verifyPurchase()   [classes3.dex]
//
// Verifies a Google Play purchase receipt via RSA SHA1withRSA using the app's
// embedded public key (assembled via StringBuilder.reverse() in 3 parts).
// Also checks that purchase.getProducts().contains("ampere_no_ads").
//
// Smali verified (.registers 13, 7 catch blocks):
//   const-string v3, "ampere_no_ads"
//   invoke-virtual {v2, v3}, ArrayList->contains(Object)Z
//   ...
//   invoke-static {v5}, TextUtils->isEmpty(CharSequence)Z  [signature null-guard]
//   const-string v8, "RSA"
//   invoke-static {v8}, KeyFactory->getInstance(String)KeyFactory
//
// Fingerprint: only method in the app calling TextUtils.isEmpty after a
// "ampere_no_ads" const-string and taking a Purchase parameter.
val VerifyPurchaseFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/android/billingclient/api/Purchase;"),
    filters = listOf(
        string("ampere_no_ads"),
        methodCall(
            definingClass = "Ljava/util/ArrayList;",
            name = "contains",
        ),
        methodCall(
            definingClass = "Landroid/text/TextUtils;",
            name = "isEmpty",
        ),
    ),
)

// kf.d()Z — checkIsPaymentNeeded()   [classes3.dex]
//
// Returns true = user must pay, false = already pro.
// Logic: if (bz0.isProVersion()) return false; else check billing state == PURCHASED.
// Defence-in-depth: even if IsProVersionFingerprint is bypassed, this gate returns false.
//
// Smali verified (.registers 2):
//   sget-object v0, Lbz0;->a:Lbz0;        singleton
//   invoke-virtual {v0}, Lbz0;->f()Z       isProVersion()
//   move-result v0
//   if-eqz v0, :L0                         if not pro → check billing
//   goto :L3                               else → return false (= already paid)
//   :L0
//   iget-object p0, p0, Lkf;->b:Lze;      BillingService field
//   ...
//   iget-object p0, p0, Lbu0;->c:Laf;     billing state enum
//   sget-object v0, Laf;->m:Laf;           af.POSSIBLE (= PURCHASED)
//   if-ne p0, v0, :L3
//   const/4 p0, 1
//   return p0                              → true (needs payment)
//   :L3
//   const/4 p0, 0
//   return p0                              → false (already paid)
//
// Fingerprint: unique chain — sget Lbz0;->a → invoke Lbz0;->f()Z → iget Lbu0;->c:Laf;
val CheckIsPaymentNeededFingerprint = Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        fieldAccess(
            opcode = Opcode.SGET_OBJECT,
            type = "Lbz0;",
        ),
        methodCall(
            definingClass = "Lbz0;",
            name = "f",
        ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Lbu0;",
        ),
        fieldAccess(
            opcode = Opcode.IGET_OBJECT,
            type = "Laf;",
        ),
    ),
)

// x70.f(EnumC2245hf)V — updateProKeyMenuItemState()   [classes3.dex]
//
// Called from onViewCreated (line 317) and drives the PRO_KEY toolbar icon
// visibility based on the billing state enum:
//
//   ordinal 0 (NOT_INITIALIZED) → z7(true, false)  = visible, disabled
//   ordinal 1 (FAILURE)         → z7(true, false)  = visible, disabled
//   ordinal 2 (PURCHASED)       → z7(false, false) = HIDDEN  ← what we want
//   ordinal 3 (NO_PURCHASE)     → z7(true, true)   = visible, enabled (buy prompt)
//
// Our bz0.f()→true patch bypasses all billing gates but does NOT influence this
// StateFlow observer. The billing state stays NOT_INITIALIZED on patched builds
// so the icon stays visible (ordinals 0/1 = visible/disabled).
//
// Fix: return-void immediately — the icon retains its initial state (false, false)
// from the C2091d8() constructor default: new C0009a8(enum) = z7(false, false).
// This matches the PURCHASED branch behavior exactly.
//
// Smali verified (.registers 6):
//   invoke-virtual {p1}, Ljava/lang/Enum;->ordinal()I     ← unique anchor
//   ...
//   new-instance p1, Lz7;
//   invoke-direct {p1, v0, v0}, Lz7;-><init>(ZZ)V         ← PURCHASED branch: false,false
//   OR invoke-direct {p1, v1, v0}, Lz7;-><init>(ZZ)V      ← other branches
//
// Fingerprint: only void method with Lhf; param calling Enum.ordinal() and
// constructing Lz7; with (ZZ) in class x70.
val UpdateProKeyMenuItemStateFingerprint = Fingerprint(
    returnType = "V",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Lhf;"),
    filters = listOf(
        methodCall(
            definingClass = "Ljava/lang/Enum;",
            name = "ordinal",
        ),
        methodCall(
            definingClass = "Lz7;",
            name = "<init>",
        ),
    ),
)
