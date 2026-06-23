package app.template.patches.duboxdrive

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DUBOXDRIVE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags

@Suppress("unused")
val duboxDriveUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks Dubox Drive VIP/SVIP (Premium+)",
) {
    compatibleWith(DUBOXDRIVE_COMPATIBILITY)

    execute {

        // ── VipInfo boolean getters → true ─────────────────────────────────────
        for (fp in listOf(
            VipInfoIsVip, VipInfoGetCountryLogin, VipInfoGetCountryRegister,
            VipInfoGetHasSpacePri, VipInfoIsSub, VipInfoIsSubSpace,
        )) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipInfo int getters → 2 (SVIP) ────────────────────────────────────
        for (fp in listOf(VipInfoGetLevel, VipInfoGetIdentity)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }
        }

        // ── VipInfo timestamp getters → 2099 (epoch seconds) ─────────────────
        for (fp in listOf(VipInfoGetExpireSeconds, VipInfoGetEndTimeNoGrace, VipInfoGetRenewTime)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const-wide v0, 0xf2bf6800L\nreturn-wide v0")
            }
        }

        // ── MemberInfo boolean getters → 1 ────────────────────────────────────
        for (fp in listOf(MemberInfoIsVip, MemberInfoGetHasSpacePri, MemberInfoGetHasIap)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── MemberInfo int getter → 2 ─────────────────────────────────────────
        MemberInfoGetLevel.match(classDefBy(MemberInfoGetLevel.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        }

        // ── MemberInfo timestamp getters → 2099 (epoch millis) ───────────────
        for (fp in listOf(MemberInfoGetEndTime, MemberInfoGetEndTimeNoGrace,
                          MemberInfoGetLeftTime, MemberInfoGetRenewTime)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const-wide v0, 0x3b453f1a800L\nreturn-wide v0")
            }
        }

        // ── VolumeMemberInfo.isVip()I → 1 ─────────────────────────────────────
        VolumeMemberInfoIsVip.match(classDefBy(VolumeMemberInfoIsVip.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Passport SDK MemberInfo.isVip()I → 1 ─────────────────────────────
        PassportMemberInfoIsVip.match(classDefBy(PassportMemberInfoIsVip.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── VipRightsManager string-anchored gates → true ────────────────────
        for (fp in listOf(VipRightsGateI, VipRightsGateJ)) {
            fp.match(classDefBy(VipRightsGateI.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipRightsManager catch-all ()Z methods → true ────────────────────
        mutableClassDefBy("Lcom/dubox/drive/vip/manager/VipRightsManager;")
            .methods
            .filter { m ->
                m.returnType == "Z" && m.parameters.isEmpty() &&
                AccessFlags.PUBLIC.isSet(m.accessFlags) &&
                AccessFlags.FINAL.isSet(m.accessFlags) &&
                m.implementation != null
            }
            .forEach { it.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        // ── Global VipInfo cache gate → true (class path changes per version) ─
        for (fp in listOf(GlobalVipGate4182, GlobalVipGate4186)) {
            runCatching {
                fp.match(classDefBy(fp.definingClass!!)).method.apply {
                    if (implementation == null) return@apply
                    addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                }
            }
        }

        // ── Account.T(Context)V — block server-forced logout ──────────────────
        AccountLogout.match(classDefBy(AccountLogout.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── BaseResultReceiver.onHandlerAccountBanError — suppress ban dialog ─
        // Server sends ServerBanInfo(banCode=-98761) on patched-client detection.
        // This is the only method that shows "Account has expired, please log in again."
        // Stable class+method name across all versions. Noop it entirely.
        AccountBanHandler.match(classDefBy(AccountBanHandler.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }
    }
}
