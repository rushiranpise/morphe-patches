package app.template.patches.duboxdrive

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DUBOXDRIVE_COMPATIBILITY

@Suppress("unused")
val duboxDriveUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks Dubox Drive VIP/SVIP (Premium+)"
) {
    compatibleWith(DUBOXDRIVE_COMPATIBILITY)

    execute {
        // isVip / isVip()Z -> true
        VipInfoIsVipFingerprint
            .match(classDefBy(VipInfoIsVipFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        MemberInfoIsVipFingerprint
            .match(classDefBy(MemberInfoIsVipFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        VolumeMemberInfoIsVipFingerprint
            .match(classDefBy(VolumeMemberInfoIsVipFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // vip level / identity -> 2 (SVIP / Premium+)
        VipInfoGetVipLevelFingerprint
            .match(classDefBy(VipInfoGetVipLevelFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }

        VipInfoGetVipIdentityFingerprint
            .match(classDefBy(VipInfoGetVipIdentityFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }

        MemberInfoGetVipLevelFingerprint
            .match(classDefBy(MemberInfoGetVipLevelFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }

        // VipRightsManager privilege gates -> always granted
        for (fp in listOf(
            VipRightsI, VipRightsJ, VipRightsK, VipRightsL,
            VipRightsZ, VipRightsA, VipRightsB, VipRightsC
        )) {
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
        }

        // Global cached-VipInfo isVip gate (ads/mediation/badges)
        Gm0TM0Fingerprint
            .match(classDefBy(Gm0TM0Fingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }

        // Expiry timestamps -> year 2099 (epoch milliseconds)
        for (fp in listOf(
            VipInfoGetExpireTimeSeconds, VipInfoGetVipEndTimeWithoutGrace,
            MemberInfoGetVipEndTime, MemberInfoGetVipEndTimeWithoutGrace, MemberInfoGetVipLeftTime, VipInfoGetRenewTime, MemberInfoGetRenewTime
        )) {
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    addInstructions(
                        0,
                        """
                        const-wide v0, 0x3b453f1a800L
                        return-wide v0
                        """.trimIndent()
                    )
                }
        }
    }
}
