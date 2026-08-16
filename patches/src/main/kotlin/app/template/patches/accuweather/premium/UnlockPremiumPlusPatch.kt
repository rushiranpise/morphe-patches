package app.template.patches.accuweather.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

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
        // ── Architecture ──────────────────────────────────────────────────────
        //
        // Subscription pipeline:
        //   1. Storefront API → buildSubscriptionSet(List) → Set<SkuClass>
        //   2. Play Billing query → Set<SkuClass>
        //   3. missing = storefront − Play Billing
        //   4. missing.isEmpty() → Success(storefrontSet) emitted downstream
        //      missing.notEmpty() → re-verify Play → may emit Mismatch → dialog
        //   5. ViewModels: successSet.contains(SkuClass(PREMIUM_PLUS, "")) → feature gate
        //
        // Two-patch strategy:
        //   PATCH 1 — buildSubscriptionSet(List)Set → Collections.emptySet()
        //     Storefront = {} → missing = {} → no Mismatch → Success({}) emitted
        //
        //   PATCH 2 — Success.getActiveSet()Set → {SkuClass(PREMIUM_PLUS, "")}
        //     ViewModels unwrap Success → get {PREMIUM_PLUS sku} → all gates pass
        //
        // ── Stability contract ────────────────────────────────────────────────
        //
        // ZERO obfuscated identifiers written as literals anywhere in this file.
        // All class/field/method refs in the injected bytecode are either:
        //   (a) Java SDK — Collections.emptySet() / Collections.singleton() — ABI stable
        //   (b) resolved at patch time from stable semantic anchors:
        //
        //   SubFlowClass  "loginManager"                           Intrinsics param-name
        //                 "Bearer "                                HTTP header literal
        //                 "uploadSubscriptionsToStorefrontUseCase" Intrinsics param-name
        //   SkuClass      "ActiveSubscription(userEntitlement="    kotlinc toString prefix
        //   TierEnum      derived from SkuClass constructor first param type
        //   PREMIUM_PLUS  "PREMIUM_PLUS" in TierEnum <clinit>     Java enum name
        //   SuccessClass  "Success(active="                        kotlinc toString prefix

        // ── 1. Locate SubFlowClass ────────────────────────────────────────────
        val subFlowClass = classDefByStrings("loginManager")
            .intersect(classDefByStrings("Bearer ").toSet())
            .intersect(classDefByStrings("uploadSubscriptionsToStorefrontUseCase").toSet())
            .firstOrNull()
            ?: throw PatchException(
                "AccuWeather [SubFlowClass]: not found. " +
                    "Expected one class containing all of: " +
                    "\"loginManager\", \"Bearer \", \"uploadSubscriptionsToStorefrontUseCase\".",
            )

        // ── 2. Locate buildSubscriptionSet(List)Set ───────────────────────────
        // Identified by shape: private, non-static, (List)Set. No name used.
        val buildSubSetMethod = mutableClassDefBy(subFlowClass).methods
            .firstOrNull { m ->
                AccessFlags.PRIVATE.isSet(m.accessFlags) &&
                    !AccessFlags.STATIC.isSet(m.accessFlags) &&
                    m.returnType == "Ljava/util/Set;" &&
                    m.parameterTypes.size == 1 &&
                    m.parameterTypes[0] == "Ljava/util/List;"
            }
            ?: throw PatchException(
                "AccuWeather [buildSubscriptionSet]: private (List)Set not found in ${subFlowClass.type}.",
            )

        // ── 3. Locate SkuClass ────────────────────────────────────────────────
        // Anchor: kotlinc toString() prefix — encodes both class name and field name.
        val skuClass = classDefByStrings("ActiveSubscription(userEntitlement=").firstOrNull()
            ?: throw PatchException(
                "AccuWeather [SkuClass]: not found. " +
                    "Expected class with toString containing \"ActiveSubscription(userEntitlement=\".",
            )
        val skuClassType = skuClass.type

        // ── 4. Derive TierEnum type from SkuClass constructor ─────────────────
        // SkuClass.<init>(TierEnum, String)V — first param type = TierEnum. No name hardcoded.
        val skuCtorParamType = skuClass.methods
            .firstOrNull { m ->
                m.name == "<init>" &&
                    m.parameterTypes.size == 2 &&
                    m.parameterTypes[1] == "Ljava/lang/String;" &&
                    m.returnType == "V" &&
                    !AccessFlags.SYNTHETIC.isSet(m.accessFlags)
            }
            ?.parameterTypes?.get(0)?.toString()
            ?: throw PatchException(
                "AccuWeather [SkuClass ctor]: <init>(TierEnum, String)V not found in $skuClassType.",
            )
        val tierEnumType: String = skuCtorParamType

        // ── 5. Resolve PREMIUM_PLUS field from TierEnum.<clinit> ─────────────
        // Walk <clinit> instructions. After const-string "PREMIUM_PLUS", the very next
        // sput-object stores the enum constant into a static field of the enum's own type.
        // "PREMIUM_PLUS" is the Java enum name — part of the storefront public API,
        // never obfuscated by R8.
        val tierClinit = mutableClassDefBy(tierEnumType).methods
            .firstOrNull { it.name == "<clinit>" }
            ?: throw PatchException(
                "AccuWeather [TierEnum]: <clinit> not found in $tierEnumType.",
            )

        val clinitInstructions = tierClinit.instructionsOrNull?.toList()
            ?: throw PatchException(
                "AccuWeather [TierEnum]: <clinit> has no instructions in $tierEnumType.",
            )

        val ppIdx = clinitInstructions.indexOfFirst { instr ->
            ((instr as? ReferenceInstruction)?.reference as? StringReference)?.string == "PREMIUM_PLUS"
        }
        if (ppIdx < 0) throw PatchException(
            "AccuWeather [TierEnum]: const-string \"PREMIUM_PLUS\" not found in $tierEnumType.<clinit>.",
        )

        // sput-object follows within a few instructions after the string
        val premiumPlusField = clinitInstructions
            .drop(ppIdx + 1).take(6)
            .firstOrNull { instr ->
                instr.opcode == Opcode.SPUT_OBJECT &&
                    ((instr as? ReferenceInstruction)?.reference as? FieldReference)?.type == tierEnumType
            }
            ?.let { instr -> (instr as? ReferenceInstruction)?.reference as? FieldReference }
            ?: throw PatchException(
                "AccuWeather [TierEnum]: sput-object for PREMIUM_PLUS not found " +
                    "within 6 instructions after const-string in $tierEnumType.<clinit>.",
            )

        // These values are computed at patch time — obfuscated names appear only as
        // runtime-derived string values substituted into smali, never as source literals.
        val premiumPlusSmali =
            "sget-object v1, ${premiumPlusField.definingClass}->${premiumPlusField.name}:${premiumPlusField.type}"

        val skuCtorDescriptor =
            "$skuClassType-><init>($tierEnumType Ljava/lang/String;)V"

        // ── PATCH 1: buildSubscriptionSet(List)Set → Collections.emptySet() ──
        // Java platform ABI — never renamed by R8 or any toolchain.
        buildSubSetMethod.clearBody()
        buildSubSetMethod.ensureRegisters(2)
        buildSubSetMethod.addInstructions(
            0,
            """
                invoke-static {}, Ljava/util/Collections;->emptySet()Ljava/util/Set;
                move-result-object v0
                return-object v0
            """,
        )

        // ── PATCH 2: Success.getActiveSet()Set → {SkuClass(PREMIUM_PLUS, "")} ─
        val successClass = classDefByStrings("Success(active=").firstOrNull()
            ?: throw PatchException(
                "AccuWeather [SuccessClass]: not found. " +
                    "Expected class with toString containing \"Success(active=\".",
            )

        val successGetSet = mutableClassDefBy(successClass).methods
            .firstOrNull { m ->
                !AccessFlags.STATIC.isSet(m.accessFlags) &&
                    m.returnType == "Ljava/util/Set;" &&
                    m.parameterTypes.isEmpty()
            }
            ?: throw PatchException(
                "AccuWeather [SuccessClass]: getActiveSet()Set not found in ${successClass.type}.",
            )

        successGetSet.clearBody()
        successGetSet.ensureRegisters(5)
        successGetSet.addInstructions(
            0,
            """
                new-instance v0, $skuClassType
                $premiumPlusSmali
                const-string v2, ""
                invoke-direct {v0, v1, v2}, $skuCtorDescriptor
                invoke-static {v0}, Ljava/util/Collections;->singleton(Ljava/lang/Object;)Ljava/util/Set;
                move-result-object v0
                return-object v0
            """,
        )
    }
}
