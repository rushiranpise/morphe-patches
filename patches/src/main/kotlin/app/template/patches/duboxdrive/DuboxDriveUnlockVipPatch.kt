package app.template.patches.duboxdrive

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.template.patches.shared.Constants.DUBOXDRIVE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val duboxDriveUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks Dubox Drive VIP/SVIP (Premium+)."
) {
    compatibleWith(DUBOXDRIVE_COMPATIBILITY)

    execute {

        val vipInfoClass = "Lcom/dubox/drive/vip/model/VipInfo;"
        val memberInfoClass = "Lcom/dubox/drive/vip/domain/job/server/response/MemberInfo;"
        val volumeMemberInfoClass = "Lcom/dubox/drive/vip/domain/job/server/response/VolumeMemberInfo;"
        val vipRightsManagerClass = "Lcom/dubox/drive/vip/manager/VipRightsManager;"

        // ── VipInfo boolean getters → true ────────────────────────────────────
        for (fp in listOf(
            VipInfoIsVipFingerprint,
            VipInfoGetCurrentLoginCountryEnableVip,
            VipInfoGetRegisterCountryEnableVip,
            VipInfoGetVipHasSpacePri,
            VipInfoIsSub,
            VipInfoIsSubSpaceProduct
        )) {
            fp.match(classDefBy(vipInfoClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipInfo integer getters → 2 (SVIP) ───────────────────────────────
        for (fp in listOf(VipInfoGetVipLevelFingerprint, VipInfoGetVipIdentityFingerprint)) {
            fp.match(classDefBy(vipInfoClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }
        }

        // ── VipInfo expiry timestamps (seconds) → 2099 ───────────────────────
        val secEpoch2099 = "const-wide v0, 0xf2bf6800L\nreturn-wide v0"
        for (fp in listOf(VipInfoGetExpireTimeSeconds, VipInfoGetVipEndTimeWithoutGrace, VipInfoGetRenewTime)) {
            fp.match(classDefBy(vipInfoClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, secEpoch2099)
            }
        }

        // ── MemberInfo boolean getters → 1/true ──────────────────────────────
        for (fp in listOf(
            MemberInfoIsVipFingerprint,
            MemberInfoGetHasSpacePri,
            MemberInfoGetHasIapRecord
        )) {
            fp.match(classDefBy(memberInfoClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── MemberInfo integer getter → 2 ────────────────────────────────────
        MemberInfoGetVipLevelFingerprint.match(classDefBy(memberInfoClass)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        }

        // ── MemberInfo expiry timestamps (millis) → 2099 ─────────────────────
        val msEpoch2099 = "const-wide v0, 0x3b453f1a800L\nreturn-wide v0"
        for (fp in listOf(
            MemberInfoGetVipEndTime, MemberInfoGetVipEndTimeWithoutGrace,
            MemberInfoGetVipLeftTime, MemberInfoGetRenewTime
        )) {
            fp.match(classDefBy(memberInfoClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, msEpoch2099)
            }
        }

        // ── VolumeMemberInfo.isVip()I → 1 ────────────────────────────────────
        VolumeMemberInfoIsVipFingerprint.match(classDefBy(volumeMemberInfoClass)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Passport SDK MemberInfo.isVip()I → 1 ─────────────────────────────
        PassportMemberInfoIsVip.match(classDefBy(PassportMemberInfoIsVip.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── VipRightsManager string-anchored gates → true ────────────────────
        for (fp in listOf(VipRightsGateIFingerprint, VipRightsGateJFingerprint)) {
            fp.match(classDefBy(vipRightsManagerClass)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipRightsManager catch-all ()Z → true ────────────────────────────
        mutableClassDefBy(vipRightsManagerClass)
            .methods
            .filter { m ->
                m.returnType == "Z" && m.parameters.isEmpty() &&
                AccessFlags.PUBLIC.isSet(m.accessFlags) &&
                AccessFlags.FINAL.isSet(m.accessFlags) &&
                m.implementation != null
            }
            .forEach { it.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        // ── Global VipInfo cache gate (gm0/hm0.t.m0) → true ─────────────────
        for (fp in listOf(Gm0TM0Fingerprint, Hm0TM0Fingerprint)) {
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // ── Account.T(Context)V — block server-forced logout ──────────────────
        AccountLogoutFingerprint.match(classDefBy(AccountLogoutFingerprint.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── az/_._____() / bz/_._____(Activity, RemoteExceptionInfo) — ServerBanInfo handler
        // Server sends banCode=-98761 → triggers "Account has expired". Return false.
        for (fp in listOf(ServerBanHandlerA182Fingerprint, ServerBanHandlerA186Fingerprint)) {
            runCatching { fp.match().method.toMutable()
                .addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
        }

        // ── az/_.____() / bz/_.____() — error dispatch; -98761 → "Account has expired" ──
        // custom lambda fingerprint — try both class paths, suppress absent one
        for (fp in listOf(AccountExpiredDialogA182Fingerprint, AccountExpiredDialogA186Fingerprint)) {
            runCatching { fp.match().method.toMutable()
                .addInstructions(0, "const/4 v0, 0x0\nreturn v0") }
        }

        // ── errno 132 (0x84) checker → false (root cause of expired popup) ───
        for (fp in listOf(Errno132CheckerA182Fingerprint, Errno132CheckerA186Fingerprint)) {
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "const/4 v0, 0x0\nreturn v0")
                }
            }
        }
    }
}
