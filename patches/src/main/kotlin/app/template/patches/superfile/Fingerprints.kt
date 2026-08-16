package app.template.patches.superfile

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

// Super File (com.esuper.file.explorer) v1.5.6.3
//
// BILLING ARCHITECTURE
// SDK: Google Play Billing 7.1.1 (in classes2/frames/, public API in classes2/com/android/billingclient/api/)
// Products: "idesuper_lifetime" (inapp, one-time), "esuper_1month" + "idesuper_1year" (subs)
// Premium state manager: frames/rh7.java (RuntimePreferences.java) — obfuscated class name
// Singleton: rh7.f() returns the instance
// Token read: rh7.l()String → reads SP key "key_p_encrypt_st"
// Premium gate: rh7.u()Z = !TextUtils.isEmpty(l()) — token non-empty means premium
//
// PUBLIC GATE: SubscriptionManager.p()Z
//   Defined in com/frames/filemanager/billing/SubscriptionManager — NON-OBFUSCATED class name.
//   Source: SubscriptionManager.java
//   Body: invoke-static rh7.f() → invoke-virtual u()Z → move-result → return
//   Only public ()Z method in that class. No filters needed.
//   rh7.u()Z is ONLY called from within SubscriptionManager — one patch covers everything.
//
// CHANGED FROM PREVIOUS VERSION:
//   Old: IsSubscribedFingerprint targeted SubscriptionManager.m()Z and SubscriptionTokenCheckFingerprint
//        targeted ih7.q()Z with string("key_p_encrypt_st").
//        In v1.5.6.3: m()Z no longer exists as a public Z-returning method (a private m(String)I
//        helper replaced it). ih7 was renamed to rh7.
//        Both fingerprints failed to match.
//   New: Single fingerprint on SubscriptionManager.p()Z using only the non-obfuscated definingClass.
//        No obfuscated names. No filters (class+returnType+accessFlags is already unique).
//        rh7.u()Z is only reachable through p() — no separate fingerprint needed.
//
// SMALI VERIFIED (classes4.dex, v1.5.6.3):
//   .class public Lcom/frames/filemanager/billing/SubscriptionManager;
//   .source "SubscriptionManager.java"
//   .method public p()Z  .registers 2
//   [0] invoke-static {}, Lframes/rh7;->f()Lframes/rh7;
//   [1] move-result-object v0
//   [2] invoke-virtual {v0}, Lframes/rh7;->u()Z
//   [3] move-result v0
//   [4] return v0
//
// FINGERPRINT ANCHOR: definingClass alone.
//   "Lcom/frames/filemanager/billing/SubscriptionManager;" is fully non-obfuscated.
//   Confirmed unique: only one public ()Z method in that class.
internal val IsSubscribedFingerprint = Fingerprint(
    definingClass = "Lcom/frames/filemanager/billing/SubscriptionManager;",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC),
    parameters = emptyList(),
)
