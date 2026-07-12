package app.template.patches.strava

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.STRAVA_COMPATIBILITY
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

@Suppress("unused")
val unlockSubscriptionPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium features in app. Also re-enables password login after OTP.",
    default = true,
) {
    compatibleWith(STRAVA_COMPATIBILITY)

    execute {

        // ── REST path ──────────────────────────────────────────────────────────
        // SubscriptionDetailResponse.getSubscribed() is called by
        // toSubscriptionDetail() to build VE/f domain model.
        // VE/f.b = true → SE/l.b() writes subscribed=true to DataStore.
        Fingerprint(
            definingClass = "Lcom/strava/subscriptions/data/models/SubscriptionDetailResponse;",
            name = "getSubscribed",
            returnType = "Z",
            parameters = emptyList(),
        ).method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        // ── GraphQL path ───────────────────────────────────────────────────────
        // SubscriptionDetailGraphQLMapper.toDomain() reads SE/c$d.j via iget-boolean.
        // Replace that read with const/4 v2, 0x1 so VE/f.b=true from GraphQL too.
        val graphqlInstrs = Fingerprint(
            definingClass = "Lcom/strava/subscriptions/data/SubscriptionDetailGraphQLMapper;",
            name = "toDomain",
            returnType = "LVE/f;",
            parameters = listOf("LSE/c\$d;"),
        ).method.implementation!!.instructions.toList()

        val subscribedIgetIndex = graphqlInstrs.indexOfFirst { instr ->
            instr.opcode == Opcode.IGET_BOOLEAN &&
                instr is ReferenceInstruction &&
                (instr.reference as? FieldReference)?.name == "j" &&
                (instr.reference as? FieldReference)?.definingClass == "LSE/c\$d;"
        }
        check(subscribedIgetIndex >= 0) { "Could not find SE/c\$d.j iget-boolean in toDomain" }

        Fingerprint(
            definingClass = "Lcom/strava/subscriptions/data/SubscriptionDetailGraphQLMapper;",
            name = "toDomain",
            returnType = "LVE/f;",
            parameters = listOf("LSE/c\$d;"),
        ).method.replaceInstruction(subscribedIgetIndex, "const/4 v2, 0x1")

        // ── Domain model gates ─────────────────────────────────────────────────
        // VE/f boolean getters gate all subscription-protected UI:
        //   c() = isPaid  (status == PAID)   – subscriptionsui, settings, aG/z, rG/v/w
        //   d() = isActive (platform==ANDROID && subStatus==EMPTY)   – UE/q, lH/Y, tG/a
        //   e() = isActiveOrTrial  – subscription management screens
        val subDetailClass = classDefBy("LVE/f;")
        listOf("c", "d", "e").forEach { name ->
            subDetailClass
                .methods.first { it.name == name && it.returnType == "Z" && it.parameters.isEmpty() }
                .toMutable()
                .addInstructions(0, """
                    const/4 v0, 0x1
                    return v0
                """)
        }

        // ── DataStore layer (cold-start gap) ───────────────────────────────────
        // SE/l.j() reads the persisted "subscribed" boolean (key 0x7f1413cf).
        // Many features (mapstab, flyover, routebuilder, fitness, etc.) read j()
        // directly without going through VE/f, so patching ensures premium is
        // visible even before the first API response is received.
        classDefBy("LSE/l;")
            .methods.first { it.name == "j" && it.returnType == "Z" && it.parameters.isEmpty() }
            .toMutable()
            .addInstructions(0, """
                const/4 v0, 0x1
                return v0
            """)

        // ── OTP / Password login ───────────────────────────────────────────────
        // Strava forces OTP-only login after the first OTP use. Patching
        // getUsePassword() → true on both response classes re-enables password login.
        Fingerprint(
            definingClass = "Lcom/strava/authorization/data/RequestOtpLogInNetworkResponse;",
            name = "getUsePassword",
            returnType = "Z",
            parameters = emptyList(),
        ).method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)

        Fingerprint(
            definingClass = "Lcom/strava/settings/data/RequestEmailChangeWithOtpOrPasswordResponse;",
            name = "getUsePassword",
            returnType = "Z",
            parameters = emptyList(),
        ).method.addInstructions(0, """
            const/4 v0, 0x1
            return v0
        """)
    }
}
