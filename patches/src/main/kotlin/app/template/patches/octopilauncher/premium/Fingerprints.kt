package app.template.patches.octopilauncher.premium

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

// ─── Anchor: ln2.v() — pro SKU purchase coroutine ──────────────────────────
//
// Obfuscated class: Lln2; (classes.dex)
// Method: public final v(Object)Object
//
// This coroutine body fires when Google Play Billing returns the owned-purchase
// list. It iterates the subscription list checking for "pro_snack" (the sub SKU)
// and on match:
//   1. Dispatches wq5 coroutine → py → "UPDATE user SET pro=?" to Room DB
//   2. Sets yq5.s (LiveData<Boolean>) = true via zo4.setValue()
//
// This fingerprint serves as the anchor for dynamic yq5 class resolution in the
// execute block — we extract the FieldReference from `iget-object v0, v2, Lyq5;->s:Lzo4;`
// to get the obfuscated yq5 class name, then navigate to yq5.e()Z.
//
// Fingerprint strategy:
//   "pro_snack" is the Google Play subscription SKU — unique globally across
//   all 8214 smali files. Single string filter is sufficient and maximally stable.
//
// Access flags: PUBLIC FINAL
// Return type:  Ljava/lang/Object;
// Parameters:   Ljava/lang/Object;
// DEX: classes.dex — smali verified against versionCode 2161.
internal object ProSnackCoroutineFingerprint : Fingerprint(
    returnType = "Ljava/lang/Object;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf("Ljava/lang/Object;"),
    filters = listOf(
        string("pro_snack"),
    ),
)
