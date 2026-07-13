package app.template.patches.dailyhunt.subscription

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.opcode
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

// Dailyhunt (com.eterno) v33.5.4
//
// PremiumSubscriptionRepository.a0()Z — static method in classes2.dex
//
// Logic:
//   sget-object v0, PremiumSubscriptionRepository->b (MutableLiveData<SubscriptionPlan>)
//   invoke-virtual {v0}, LiveData.getValue()
//   move-result-object v0
//   if-eqz v0, :false   ← non-null means active plan exists
//   const/4 v0, 0x1     ← return true (subscribed)
//   return v0
//   const/4 v0, 0x0     ← return false (not subscribed)
//   return v0
//
// Patch: prepend const/4 v0, 0x1 + return v0 → always returns true.

internal val IsPremiumSubscribedFingerprint = Fingerprint(
    definingClass = "Lcom/newshunt/subscription/helper/PremiumSubscriptionRepository;",
    name = "a0",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = emptyList(),
    filters = listOf(
        opcode(Opcode.SGET_OBJECT),
        opcode(Opcode.INVOKE_VIRTUAL),
        opcode(Opcode.MOVE_RESULT_OBJECT),
        opcode(Opcode.IF_EQZ),
        opcode(Opcode.CONST_4),
        opcode(Opcode.RETURN),
        opcode(Opcode.CONST_4),
        opcode(Opcode.RETURN),
    ),
)
