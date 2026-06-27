package app.template.patches.accuweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val unlockPremiumPlusPatch = bytecodePatch(
    name = "Unlock Premium+",
    description = "Unlocks AccuWeather's Premium+ subscription tier without a Play Store purchase. " +
        "Enables the full 15-day and hourly forecast detail, MinuteCast extended precision, " +
        "air quality and health indexes, real-feel temperature, severe weather notifications, " +
        "and widget customisation.",
) {
    compatibleWith(Constants.ACCUWEATHER_COMPATIBILITY)

    execute {
        // Full flow analysis:
        //
        // The subscription coroutine (z9/c$a) does:
        //   1. Calls storefront API → gets product list → z9/c.f(list) → storefront Set<u9/c>
        //   2. Reads Play Billing subscriptions → Play Set<u9/c>
        //   3. Computes "missing" = items in storefront NOT in Play Billing
        //   4. If missing.isEmpty() → wraps storefront set in u9/a$b → emits downstream
        //   5. If NOT empty → calls z9/j.h(missing) to verify Play Billing entries → may return
        //      error String → wraps in u9/a$a (Mismatch) which triggers MismatchDialog in HomeViewModel
        //
        // Previous approach of patching BOTH u9/a$a and u9/a$b caused the MismatchDialog
        // ("something went wrong") because:
        //   - Patching u9/a$b.a() returns {PREMIUM_PLUS} from storefront set
        //   - The "missing subs" loop: storefront has {PREMIUM_PLUS}, Play Billing has {}
        //   - PREMIUM_PLUS is "missing" from Play → z9/j.h() called → Play Billing error → u9/a$a emitted
        //   - HomeViewModel detects u9/a$a (Mismatch) + isPremiumPlus=true → shows MismatchDialog
        //
        // Correct two-patch solution:
        //
        // PATCH 1: z9/c.f(List<Product>)Set → return empty Set
        //   - Storefront set = {} → "missing subs" loop finds NOTHING missing → z9/j.h() never called
        //   - u9/a$b({}) emitted → no Mismatch, no MismatchDialog
        //   Pinned by: 3-string intersection "loginManager"+"Bearer "+"uploadSubscriptionsToStorefrontUseCase"
        //   (unique to z9/c). Method: private instance (List)Set.
        //
        // PATCH 2: u9/a$b.a()Set → return {PREMIUM_PLUS}
        //   - u9/b$a$a.emit() unwraps u9/a → calls a()Set → gets {PREMIUM_PLUS} → emits downstream
        //   - All ViewModels: Set.contains(u9/c(PREMIUM_PLUS)) = true → features unlocked
        //   Pinned by: unique string "Success(active=" in u9/a$b.toString()
        //   Method: instance ()Set.
        //
        // u9/a$a (Mismatch class) is intentionally NOT patched to avoid triggering MismatchDialog.

        // --- Patch 1: z9/c.f(List)Set → always return empty Set ---
        val subFlowClass = classDefByStrings("loginManager")
            .intersect(classDefByStrings("Bearer ").toSet())
            .intersect(classDefByStrings("uploadSubscriptionsToStorefrontUseCase").toSet())
            .firstOrNull()
            ?: throw PatchException(
                "AccuWeather: subscription flow class not found. " +
                    "Re-derive from the class containing \"loginManager\", \"Bearer \", " +
                    "and \"uploadSubscriptionsToStorefrontUseCase\".",
            )

        val buildSubSetMethod = mutableClassDefBy(subFlowClass).methods
            .firstOrNull { method ->
                AccessFlags.PRIVATE.isSet(method.accessFlags) &&
                    !AccessFlags.STATIC.isSet(method.accessFlags) &&
                    method.returnType == "Ljava/util/Set;" &&
                    method.parameterTypes.size == 1 &&
                    method.parameterTypes[0] == "Ljava/util/List;"
            }
            ?: throw PatchException(
                "AccuWeather: subscription Set builder (private (List)Set) not found " +
                    "in ${subFlowClass.type}.",
            )

        buildSubSetMethod.clearBody()
        buildSubSetMethod.ensureRegisters(2)
        buildSubSetMethod.addInstructions(
            0,
            """
                invoke-static {}, LZf/a0;->e()Ljava/util/Set;
                move-result-object v0
                return-object v0
            """,
        )

        // --- Patch 2: u9/a$b.a()Set → always return {PREMIUM_PLUS} ---
        val successClass = classDefByStrings("Success(active=").firstOrNull()
            ?: throw PatchException(
                "AccuWeather: subscription Success wrapper class (containing \"Success(active=\") " +
                    "not found.",
            )

        val successGetSet = mutableClassDefBy(successClass).methods
            .firstOrNull { method ->
                !AccessFlags.STATIC.isSet(method.accessFlags) &&
                    method.returnType == "Ljava/util/Set;" &&
                    method.parameterTypes.isEmpty()
            }
            ?: throw PatchException(
                "AccuWeather: a()Set getter not found in ${successClass.type}.",
            )

        successGetSet.clearBody()
        successGetSet.ensureRegisters(5)
        successGetSet.addInstructions(
            0,
            """
                new-instance v0, Lu9/c;
                sget-object v1, Lu9/p;->e:Lu9/p;
                const-string v2, ""
                invoke-direct {v0, v1, v2}, Lu9/c;-><init>(Lu9/p;Ljava/lang/String;)V
                invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
                move-result-object v0
                return-object v0
            """,
        )
    }
}
