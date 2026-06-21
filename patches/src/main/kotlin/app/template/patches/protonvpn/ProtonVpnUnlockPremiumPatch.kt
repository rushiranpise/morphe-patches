package app.template.patches.protonvpn

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.OpcodesFilter
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.fieldAccess
import app.morphe.patcher.methodCall
import app.morphe.patcher.opcode
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.string
import app.template.patches.shared.Constants.PROTONVPN_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

private object CustomDnsSettingViewStateCtorFingerprint : Fingerprint(
    strings = listOf("customDns"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.CONST_4,
        Opcode.CONST_4,
        Opcode.MOVE_OBJECT,
        Opcode.MOVE,
        Opcode.INVOKE_DIRECT_RANGE
    )
)

private object SplitTunnelingSettingViewStateCtorFingerprint : Fingerprint(
    strings = listOf("currentModeAppNames"),
    filters = OpcodesFilter.opcodesToFilters(
        Opcode.MOVE_OBJECT,
        Opcode.MOVE_FROM16,
        Opcode.INVOKE_DIRECT_RANGE
    )
)

private object LanConnectionsSettingViewStateCtorFingerprint : Fingerprint(
    name = "<init>",
    filters = listOf(
        fieldAccess(name = "settings_advanced_allow_lan_description"),
        opcode(Opcode.CONST_4),
        opcode(Opcode.MOVE_OBJECT),
        opcode(Opcode.MOVE),
        opcode(Opcode.INVOKE_DIRECT_RANGE)
    )
)

private object ApplyCustomDnsRestrictionsFingerprint : Fingerprint(
    name = "applyRestrictions",
    definingClass = "/BaseApplyEffectiveUserSettings;",
    filters = listOf(methodCall(name = "getCustomDns"))
)

private object ApplySplitTunnelingRestrictionsFingerprint : Fingerprint(
    name = "applyRestrictions",
    definingClass = "/BaseApplyEffectiveUserSettings;",
    filters = listOf(methodCall(name = "getSplitTunneling"))
)

private object ApplyLanConnectionsRestrictionsFingerprint : Fingerprint(
    name = "applyRestrictions",
    definingClass = "/BaseApplyEffectiveUserSettings;",
    filters = listOf(methodCall(name = "getLanConnections"))
)

private object GetLongDelayFingerprint : Fingerprint(
    name = "getChangeServerLongDelayInSeconds"
)

private object GetShortDelayFingerprint : Fingerprint(
    name = "getChangeServerShortDelayInSeconds"
)

private object ServerListFilterFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Z",
        "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;",
        "Ljava/lang/String;",
        "Lcom/protonvpn/android/redesign/CityStateId;",
        "Z",
        "Ljava/lang/String;",
        "Lcom/protonvpn/android/servers/Server;"
    ),
    filters = listOf(methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "isFreeServer"))
)

private object ServerGroupGetAvailableFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/redesign/countries/ui/ServerGroupUiItem\$ServerGroup;",
    name = "getAvailable",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object HasAccessToServerFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "Lcom/protonvpn/android/auth/data/VpnUser;",
        "Lcom/protonvpn/android/servers/Server;"
    ),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "getTier"),
        methodCall(definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;", name = "getUserTier")
    )
)

private object GetNetShieldAvailabilityFingerprint : Fingerprint(
    returnType = "Lcom/protonvpn/android/netshield/NetShieldAvailability;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf("Lcom/protonvpn/android/auth/data/VpnUser;"),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;", name = "isFreeUser"),
        fieldAccess(name = "AVAILABLE")
    )
)

private object GetUserTierFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getUserTier",
    returnType = "I",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object IsFreeUserFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "isFreeUser",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object IsUserPlusOrAboveFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "isUserPlusOrAbove",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object GetHasSubscriptionFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getHasSubscription",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object GetUserTierNameFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getUserTierName",
    returnType = "Ljava/lang/String;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object GetMaxTierFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/auth/data/VpnUser;",
    name = "getMaxTier",
    returnType = "Ljava/lang/Integer;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = listOf()
)

private object IsFeatureFlagEnabledFingerprint : Fingerprint(
    definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;",
    name = "invoke",
    returnType = "Z",
    parameters = listOf("Lme/proton/core/domain/entity/UserId;"),
    filters = listOf(
        methodCall(definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;", name = "isLocalEnabled"),
        methodCall(definingClass = "Lme/proton/core/featureflag/data/IsFeatureFlagEnabledImpl;", name = "isRemoteEnabled")
    )
)

private object GetBestScoreServerFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/utils/ServerManager;",
    name = "getBestScoreServer",
    returnType = "Lcom/protonvpn/android/servers/Server;",
    parameters = listOf(
        "Ljava/lang/Iterable;",
        "Lcom/protonvpn/android/auth/data/VpnUser;",
        "Lcom/protonvpn/android/vpn/ProtocolSelection;",
        "Ljava/util/List;"
    )
)

private object ConnectionServerFilterFingerprint : Fingerprint(
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PRIVATE, AccessFlags.STATIC, AccessFlags.FINAL),
    parameters = listOf(
        "I",
        "Lcom/protonvpn/android/vpn/ProtocolSelection;",
        "Ljava/util/List;",
        "Lcom/protonvpn/android/servers/Server;"
    ),
    filters = listOf(
        methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "getTier"),
        methodCall(definingClass = "Lcom/protonvpn/android/servers/Server;", name = "getOnline")
    )
)

private object GetFilterButtonsFingerprint : Fingerprint(
    returnType = "Ljava/util/List;",
    accessFlags = listOf(AccessFlags.PROTECTED, AccessFlags.FINAL),
    parameters = listOf(
        "Ljava/util/Set;",
        "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;",
        "I",
        "Ljava/util/Set;",
        "Lkotlin/jvm/functions/Function1;"
    ),
    filters = listOf(
        string("availableTypes"),
        methodCall(definingClass = "Lcom/protonvpn/android/redesign/countries/ui/ServerFilterType;", name = "getEntries")
    )
)

private object ProfileAvailableTypesFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/profiles/ui/TypeAndLocationScreenState\$Standard;",
    name = "getAvailableTypes",
    returnType = "Ljava/util/List;",
    parameters = listOf()
)

private object ProfileCountriesFingerprint : Fingerprint(
    definingClass = "Lcom/protonvpn/android/profiles/ui/ProfilesServerDataAdapter;",
    name = "countries",
    filters = listOf(methodCall(definingClass = "Lcom/protonvpn/android/servers/ServerManager2;", name = "getVpnCountries"))
)

@Suppress("unused")
val protonVpnUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks local Proton VPN premium  features.",
    default = true
) {
    compatibleWith(PROTONVPN_COMPATIBILITY)

    execute {
        listOf(
            CustomDnsSettingViewStateCtorFingerprint,
            SplitTunnelingSettingViewStateCtorFingerprint,
            LanConnectionsSettingViewStateCtorFingerprint,
        ).forEach { fingerprint ->
            val isRestrictedIndex = fingerprint.instructionMatches.last().index - 1
            val isRestrictedRegister = fingerprint.method
                .getInstruction<OneRegisterInstruction>(isRestrictedIndex)
                .registerA

            fingerprint.method.replaceInstruction(isRestrictedIndex, "const/4 v$isRestrictedRegister, 0x0")
        }

        listOf(
            ApplyCustomDnsRestrictionsFingerprint,
            ApplySplitTunnelingRestrictionsFingerprint,
            ApplyLanConnectionsRestrictionsFingerprint,
        ).forEach { fingerprint ->
            fingerprint.method.removeInstruction(fingerprint.instructionMatches.first().index - 1)
        }

        listOf(GetLongDelayFingerprint, GetShortDelayFingerprint).forEach { fingerprint ->
            fingerprint.method.clearBody()
            fingerprint.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        ServerListFilterFingerprint.apply {
            val index = instructionMatches.first().index
            method.removeInstruction(index + 2)
            method.removeInstruction(index + 1)
            method.removeInstruction(index)
            method.removeInstruction(index - 1)
            method.addInstructionsWithLabels(
                index - 1,
                """
                    invoke-virtual {p6}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
                    move-result p0
                    if-nez p0, :keepFreeServer
                    const/4 p0, 0x0
                    return p0
                    :keepFreeServer
                    nop
                """
            )
        }

        ServerGroupGetAvailableFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        HasAccessToServerFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        GetNetShieldAvailabilityFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    sget-object v0, Lcom/protonvpn/android/netshield/NetShieldAvailability;->AVAILABLE:Lcom/protonvpn/android/netshield/NetShieldAvailability;
                    return-object v0
                """
            )
        }

        GetUserTierFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        }

        IsFreeUserFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        IsUserPlusOrAboveFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        GetHasSubscriptionFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        GetUserTierNameFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const-string v0, \"bundle2022\"\nreturn-object v0")
        }

        GetMaxTierFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    const/4 v0, 0x2
                    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                    move-result-object v0
                    return-object v0
                """
            )
        }

        IsFeatureFlagEnabledFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        GetBestScoreServerFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Ljava/util/ArrayList;
                invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
                invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
                move-result-object v1
                :freeServerLoop
                invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
                move-result v2
                if-eqz v2, :freeServerDone
                invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
                move-result-object v2
                check-cast v2, Lcom/protonvpn/android/servers/Server;
                invoke-virtual {v2}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
                move-result v3
                if-eqz v3, :freeServerLoop
                invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
                goto :freeServerLoop
                :freeServerDone
                invoke-interface {v0}, Ljava/util/List;->isEmpty()Z
                move-result v1
                if-nez v1, :keepOriginalServers
                move-object p1, v0
                :keepOriginalServers
            """
        )

        ConnectionServerFilterFingerprint.method.addInstructionsWithLabels(
            0,
            """
                invoke-virtual {p3}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
                move-result v0
                if-nez v0, :freeFallbackServer
                const/4 v0, 0x0
                return v0
                :freeFallbackServer
                nop
            """
        )

        GetFilterButtonsFingerprint.method.apply {
            clearBody()
            addInstructions(0, "invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;\nmove-result-object v0\nreturn-object v0")
        }

        ProfileAvailableTypesFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                """
                    new-instance v0, Ljava/util/ArrayList;
                    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
                    sget-object v1, Lcom/protonvpn/android/profiles/ui/ProfileType;->Standard:Lcom/protonvpn/android/profiles/ui/ProfileType;
                    invoke-virtual {v0, v1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
                    return-object v0
                """
            )
        }

        ProfileCountriesFingerprint.apply {
            method.replaceInstruction(
                instructionMatches.first().index,
                "invoke-virtual {p2, v0}, Lcom/protonvpn/android/servers/ServerManager2;->getFreeCountries(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;"
            )
        }

        mutableClassDefByOrNull("Lcom/protonvpn/android/auth/data/VpnUser;")?.methods
            ?.firstOrNull { it.name == "getHasNetShieldLevelThreeAvailable" && it.returnType == "Z" }
            ?.apply {
                clearBody()
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        mutableClassDefByOrNull("Lcom/protonvpn/android/models/login/NetShieldConfig;")?.methods
            ?.filter {
                it.name in setOf(
                    "getMalwareBlockingAvailable",
                    "getAdsAndTrackersBlockingAvailable",
                    "getAdultContentBlockingAvailable",
                ) && it.returnType == "Z"
            }
            ?.forEach { method ->
                method.clearBody()
                method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        mutableClassDefByOrNull("Lcom/protonvpn/android/ui/vpn/VpnUiActivityDelegate;")?.methods
            ?.firstOrNull {
                it.name == "onServerRestricted" &&
                    it.returnType == "Z" &&
                    it.parameterTypes.singleOrNull() == "Lcom/protonvpn/android/vpn/ReasonRestricted;"
            }
            ?.addInstructions(
                0,
                """
                    sget-object v0, Lcom/protonvpn/android/vpn/ReasonRestricted;->PlusUpgradeNeeded:Lcom/protonvpn/android/vpn/ReasonRestricted;
                    if-eq p1, v0, :allowPremiumServer
                    sget-object v0, Lcom/protonvpn/android/vpn/ReasonRestricted;->SecureCoreUpgradeNeeded:Lcom/protonvpn/android/vpn/ReasonRestricted;
                    if-ne p1, v0, :keepServerRestriction
                    :allowPremiumServer
                    const/4 v0, 0x0
                    return v0
                    :keepServerRestriction
                """
            )

        mutableClassDefByOrNull("Lcom/protonvpn/android/ui/planupgrade/UpgradeDialogLauncher;")?.methods
            ?.filter { it.name.startsWith("launch") && it.returnType == "V" }
            ?.forEach { method ->
                method.clearBody()
                method.addInstructions(0, "return-void")
            }

        listOf(
            "Lcom/protonvpn/android/ui/planupgrade/CarouselUpgradeDialogActivity\$Companion;",
            "Lcom/protonvpn/android/ui/planupgrade/UpgradeOnboardingDialogActivity\$Companion;",
            "Lcom/protonvpn/android/ui/planupgrade/comparison_table/UpgradeDialogActivityV2\$Companion;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { it.name == "launch" && it.returnType == "V" }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }
        }

        listOf(
            "Lcom/protonvpn/android/ui/planupgrade/BaseUpgradeDialogActivity;",
            "Lcom/protonvpn/android/ui/planupgrade/CarouselUpgradeDialogActivity;",
            "Lcom/protonvpn/android/ui/planupgrade/PlusOnlyUpgradeDialogActivity;",
            "Lcom/protonvpn/android/ui/planupgrade/UpgradeOnboardingDialogActivity;",
            "Lcom/protonvpn/android/ui/planupgrade/comparison_table/UpgradeDialogActivityV2;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { it.name == "onCreate" && it.returnType == "V" }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")
                }
        }

        listOf(
            "Lcom/protonvpn/android/api/GuestHole\$GuestHoleVpnUiDelegate;",
            "Lcom/protonvpn/android/restrictonsupsell/StreamingUpsellRestrictionsDialogTrigger;",
            "Lcom/protonvpn/android/restrictonsupsell/StreamingUpsellRestrictionsNotificationTrigger;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter {
                    it.name in setOf("showPlusUpgradeDialog", "showSecureCoreUpgradeDialog", "start") &&
                        it.returnType == "V"
                }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }
        }

        listOf(
            "Lcom/protonvpn/android/promooffers/usecase/PromoActivityOpener;",
            "Lcom/protonvpn/android/promooffers/usecase/PromoIapActivityOpener;",
            "Lcom/protonvpn/android/promooffers/usecase/NpsActivityOpener;",
        ).forEach { type ->
            mutableClassDefByOrNull(type)?.methods
                ?.filter { it.name == "open" && it.returnType == "V" }
                ?.forEach { method ->
                    method.clearBody()
                    method.addInstructions(0, "return-void")
                }
        }
    }
}
