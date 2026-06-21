package app.template.patches.duboxdrive

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
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

        // Account.T(Context)V — master logout: clears session, sends ACTION_LOGOUT broadcast
        // Triggered by periodic server sync (AccountChangeHandler) on session expiry.
        // Make it a no-op to block all server-forced logouts.
        AccountLogoutFingerprint
            .match(classDefBy(AccountLogoutFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }

        // nu/m.a+b (4.18.2) / ou/m.a+b (4.18.6+) — backup guide shown checks
        // No definingClass — matched by unique string anchor across all classes
        for (fp in listOf(BackupGuideShownAFingerprint, BackupGuideShownBFingerprint)) {
            fp.match().method.toMutable()
                .addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // az/_.a(Activity)Z (4.18.2) / bz/_.a(Activity)Z (4.18.6+)
        // The actual "Account has expired, please log in again" popup — return false (don't show)
        for (fp in listOf(AccountExpiredDialogA182Fingerprint, AccountExpiredDialogA186Fingerprint)) {
            runCatching {
                fp.match(classDefBy(fp.definingClass!!))
                    .method
                    .apply {
                        if (implementation == null) return@apply
                        addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                    }
            }
        }

        // i0.__() — noop SubscribeActivity launcher (account expired/subscribe screen)
        // tutorial/k._() — noop PlusSubscribeGuideActivity launcher
        // Both called from BuckupSettingGuideActivity (6x total) and f3.g()
        for (fp in listOf(SubscribeActivityLaunchFingerprint, PlusSubscribeGuideActivityLaunchFingerprint)) {
            fp.match(classDefBy(fp.definingClass!!))
                .method
                .apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "return-void")
                }
        }

        // i0._()Z — "new user subscription guide not shown" → suppress expired/subscribe screen
        NewUserSubGuideFingerprint
            .match(classDefBy(NewUserSubGuideFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // QuotaExtraInfo.isTimeLimitQuotaType()Z — root gate for all expiry dialogs (a3, k0).
        // Patching here is more stable than targeting obfuscated ww/xw/_____. 
        QuotaIsTimeLimitTypeFingerprint
            .match(classDefBy(QuotaIsTimeLimitTypeFingerprint.definingClass!!))
            .method
            .apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }

        // Global cached-VipInfo isVip gate (ads/mediation/badges)
        // 4.18.2: gm0/t  4.18.6+: hm0/t — try both, suppress whichever is absent
        for (fp in listOf(Gm0TM0Fingerprint, Hm0TM0Fingerprint)) {
            runCatching {
                fp.match(classDefBy(fp.definingClass!!))
                    .method
                    .apply {
                        if (implementation == null) return@apply
                        addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                    }
            }
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
