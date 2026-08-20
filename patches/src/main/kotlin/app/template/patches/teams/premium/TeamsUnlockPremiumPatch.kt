package app.template.patches.teams.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.TEAMS_COMPATIBILITY
import app.template.patches.shared.returnEarly
import app.template.patches.teams.integrity.teamsIntegrityBypassDependency

@Suppress("unused")
val teamsUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Teams",
    description = "Unlocks Teams Premium features.",
    default = true,
) {
    compatibleWith(TEAMS_COMPATIBILITY)
    dependsOn(teamsIntegrityBypassDependency())

    execute {

        // ── Layer 1: SkypeTokenLicenseDetails constructor (enterprise token) ──
        //
        // The deepest patch: override all premium boolean fields before return-void
        // (instruction index 37, .registers 3: p0=this, p1=LicenseDetails, v0=scratch).
        // One constructor override cascades to all 99k smali files.
        val licClass = "Lcom/microsoft/skype/teams/models/responses/skypetoken/SkypeTokenLicenseDetails;"

        licenseDetailsConstructorFingerprint.method.addInstructions(
            37,
            """
                const/4 v0, 0x1
                iput-boolean v0, p0, $licClass->mIsTeamsPremiumSelfAssigned:Z
                iput-boolean v0, p0, $licClass->mIsTPManagement:Z
                iput-boolean v0, p0, $licClass->mIsTPProtection:Z
                iput-boolean v0, p0, $licClass->mHasTPCustomizationLicense:Z
                iput-boolean v0, p0, $licClass->mHasM365CopilotLicense:Z
                iput-boolean v0, p0, $licClass->mIsGroupCopilot:Z
                iput-boolean v0, p0, $licClass->mHasM365CopilotBusinessChatLicense:Z
                iput-boolean v0, p0, $licClass->mHasAdvCommsLicense:Z
                iput-boolean v0, p0, $licClass->mIsInfoProtectionPremium:Z
                iput-boolean v0, p0, $licClass->mIsBasicLiveEventsEnabled:Z
                iput-boolean v0, p0, $licClass->mHasMixedRealityShare:Z
                const/4 v0, 0x0
                iput-boolean v0, p0, $licClass->mIsFreemium:Z
                iput-boolean v0, p0, $licClass->mIsTrial:Z
            """.trimIndent(),
        )

        // ── Layer 2: AuthenticatedUser getter overrides ───────────────────────
        // Covers cached/deserialized AuthenticatedUser instances.

        isTeamsPremiumFingerprint.method.returnEarly(true)
        hasCopilotLicenseNoParamFingerprint.method.returnEarly(true)
        hasCopilotLicenseFingerprint.method.returnEarly(true)
        hasGroupCopilotFingerprint.method.returnEarly(true)
        hasBizChatCopilotFingerprint.method.returnEarly(true)
        isFreemiumUserFingerprint.method.returnEarly(false)

        // ── Layer 3: TflUserConfiguration Copilot gates ───────────────────────
        // isCopilotLicenseRequired() reads server ECS flag (default=true) → false.
        // isCopilotLicenseAvailable() reads local pref → true.

        copilotLicenseRequiredFingerprint.method.returnEarly(false)
        copilotLicenseAvailableFingerprint.method.returnEarly(true)

        // ── Layer 4: Consumer license parsing — bypass the upsell screen ──────
        //
        // The upsell screen (60 min / 100 participants) is driven by the CONSUMER
        // SkypeToken path, not the Enterprise token. Server sends hasTeamsLicense=false
        // → FreeLicenseInfo(60min, 100 participants) → UpsellBenefitsFragment.
        //
        // Inject at index 0: construct PaidLicenseInfo with max integer limits and return,
        // bypassing the server response entirely.
        val restrictionsClass = "Lcom/microsoft/teams/license/model/TeamsLicenseRestrictions;"
        val paidInfoClass = "Lcom/microsoft/teams/license/model/TeamsLicenseInfo\$PaidLicenseInfo;"
        val consumerDetailsClass = "Lcom/microsoft/teams/license/model/ConsumerLicenseDetails;"

        validateLicenseDetailsFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, $restrictionsClass
                const v1, 0x7fffffff
                const v2, 0x7fffffff
                const/4 v3, 0x1
                const/4 v4, 0x1
                invoke-direct { v0, v1, v2, v3, v4 }, $restrictionsClass-><init>(IIZZ)V
                invoke-static { }, Lkotlin/collections/CollectionsKt;->emptyList()Ljava/util/List;
                move-result-object v1
                new-instance v2, $paidInfoClass
                invoke-direct { v2, v1, v0 }, $paidInfoClass-><init>(Ljava/util/List;$restrictionsClass)V
                new-instance v0, $consumerDetailsClass
                invoke-direct { v0, v2 }, $consumerDetailsClass-><init>(Lcom/microsoft/teams/license/model/TeamsLicenseInfo;)V
                return-object v0
            """.trimIndent(),
        )
    }
}

@JvmSynthetic
internal fun teamsUnlockPremiumDependency() = teamsUnlockPremiumPatch
