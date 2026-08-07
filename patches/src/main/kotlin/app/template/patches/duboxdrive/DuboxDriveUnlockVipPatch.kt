package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.DUBOXDRIVE_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// ── Certificate constants (original com.dubox.drive APK, unchanged across versions) ─
private const val CERT_SHA1 = "11F8C73FD20D39CF55FD7F3F0F6A88C7E8909858"
private const val CERT_BASE64 =
    "MIIDlzCCAn+gAwIBAgIEc6Fu6TANBgkqhkiG9w0BAQsFADB7MRgwFgYDVQQGDA/jgavjgbvjgpPj" +
    "gZPjgY8xDzANBgNVBAgMBuadseS6rDESMBAGA1UEBwwJ6YO95riv5Yy6MQ4wDAYDVQQKEwVkdWJv" +
    "eDEaMBgGA1UECwwRcG9wSW7moKrlvI/kvJrnpL4xDjAMBgNVBAMTBWR1Ym94MCAXDTIwMDQyMzA4" +
    "NTU0NVoYDzIxMDIwNjEzMDg1NTQ1WjB7MRgwFgYDVQQGDA/jgavjgbvjgpPjgZPjgY8xDzANBgNV" +
    "BAgMBuadseS6rDESMBAGA1UEBwwJ6YO95riv5Yy6MQ4wDAYDVQQKEwVkdWJveDEaMBgGA1UECwwR" +
    "cG9wSW7moKrlvI/kvJrnpL4xDjAMBgNVBAMTBWR1Ym94MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A" +
    "MIIBCgKCAQEAhZiMg5FNw8L1wwzB+aymJmOB7azSvlIGUW3Ec/Qu88Y6lth+FbsbV264JPWvcb88" +
    "prMxg5kQONziZUmc179uTd7OiLbAucd3tPyKs5IJQsNQEx5imDCYFZgeqG/Y8KObNUcDIAhxpYlZ" +
    "yt4SZcxKzU36i0ua4dI/5mJb+vaNB+3RR5OPtLYl7XHvi+HrxV2AAyp9bcMuazNBA+HBAzzg6Mus" +
    "e9SiYMuvI+ZtOHdEhvL97hFcsOHB+FoI9BHeI7NNWpZbdAkeCOYcDvgNsVuhYTKvGRm9HNbeD2xK" +
    "r/lQOl4q3i9iqiiDmh4NEkSjmvoTa2Kqf1ExxqE2ymETn/4DmQIDAQABoyEwHzAdBgNVHQ4EFgQU" +
    "EGV7ZWwywfvRtPEbfR1x4ghNe2owDQYJKoZIhvcNAQELBQADggEBAEQ4+XkcSnxGszeglnVy8vsY" +
    "gqTYUBBmzQiFvTVaYAJErWT4Onn9X9NZReCtlToe+tZrB5N2prlPg2bBnEiaV0AFGWAvV69iptbU" +
    "cv82RKK+hqE0sdlEmgffCTirJnWQa+NGedEuP0T+S3hYt/QVvaiwxQqwfwIMONhBdwvvvq099fWY" +
    "PjALv/hNvudNhHSlQFDsYdWhgGxSJ1ILW53Bdd9SdnilvtYy91Zcqo4GDkhK4GEgfr0IyPawseUi" +
    "3bmnjiAdvV4CEUZvCRQm8M6DVkhGSJk6PpzJKdcpyosB4JRlB7/khpUiwPXQ4NOWMOpSFnNNfNtL" +
    "krukYxe28Ozd0ME="

private const val EXTENSION_CLASS_SIG  = "Lapp/template/extension/extension/SignatureHookApp;"
private const val EXTENSION_CLASS_INST = "Lapp/template/extension/extension/InstallSourceHelper;"
private const val TARGET_INSTALLER     = "com.android.vending"

@Suppress("unused")
val duboxDriveUnlockVipPatch = bytecodePatch(
    name = "Unlock VIP",
    description = "Unlocks TeraBox / Dubox Drive VIP/SVIP (Premium+).",
) {
    compatibleWith(DUBOXDRIVE_COMPATIBILITY)
    extendWith("extensions/extension.mpe")

    execute {

        // ── VipInfo boolean getters → true ────────────────────────────────────
        // Covers all boolean gates on the local VipInfo cache object.
        // getHasIapRecord and getCanTrial are new in v4.22.1.
        for (fp in listOf(
            VipInfoIsVip, VipInfoGetCountryLogin, VipInfoGetCountryRegister,
            VipInfoGetHasSpacePri, VipInfoIsSub, VipInfoIsSubSpace,
            VipInfoGetHasIapRecord, VipInfoGetCanTrial,
        )) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipInfo int getters → 2 (SVIP level) ─────────────────────────────
        for (fp in listOf(VipInfoGetLevel, VipInfoGetIdentity)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }
        }

        // ── VipInfo long getters → year 2099 (0xf2bf6800 seconds) ────────────
        for (fp in listOf(VipInfoGetExpireSeconds, VipInfoGetEndTimeNoGrace, VipInfoGetRenewTime)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const-wide v0, 0xf2bf6800L\nreturn-wide v0")
            }
        }

        // ── MemberInfo boolean getters → 1 ───────────────────────────────────
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

        // ── MemberInfo long getters → year 2099 (0x3b453f1a800 millis) ───────
        for (fp in listOf(
            MemberInfoGetEndTime, MemberInfoGetEndTimeNoGrace,
            MemberInfoGetLeftTime, MemberInfoGetRenewTime,
        )) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const-wide v0, 0x3b453f1a800L\nreturn-wide v0")
            }
        }

        // ── VolumeMemberInfo.isVip()I → 1 ────────────────────────────────────
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
        // VipRightsGateByType:   I(String)Z — general privilege type gate
        // VipRightsGateNaStudio: j()Z       — NA_STUDIO_CREATE feature gate
        for (fp in listOf(VipRightsGateByType, VipRightsGateNaStudio)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipRightsManager catch-all ()Z → true ────────────────────────────
        // Covers all parameterless boolean gates (obfuscated method names like G, H, l, etc.)
        // without needing individual fingerprints for each one.
        mutableClassDefBy("Lcom/dubox/drive/vip/manager/VipRightsManager;")
            .methods
            .filter { m ->
                m.returnType == "Z" && m.parameters.isEmpty() &&
                AccessFlags.PUBLIC.isSet(m.accessFlags) &&
                AccessFlags.FINAL.isSet(m.accessFlags) &&
                m.implementation != null
            }
            .forEach { it.addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        // ── Global VipInfo cache gate → true ──────────────────────────────────
        // ApisKt.L()Z — unchanged from v4.20.1 to v4.22.1.
        // runCatching to remain safe if the method is renamed in a future version;
        // the catch-all VipInfo scanner above covers the underlying isSub() call.
        runCatching {
            GlobalVipGate.match(classDefBy(GlobalVipGate.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── Account.V(Context)V — block server-forced logout ──────────────────
        // Method name changed T→V in v4.22.1. Fingerprint now uses stable string
        // anchors ("mContext" Kotlin null-check + "com.dubox.drive.ACTION_LOGOUT")
        // so the definingClass+name are used for disambiguation only.
        AccountLogout.match(classDefBy(AccountLogout.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── BaseResultReceiver.onHandlerAccountBanError — suppress ban dialog ─
        AccountBanHandler.match(classDefBy(AccountBanHandler.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── BaseResultReceiver.onHandlerAccountCommonError — suppress expired popup ─
        // Triggered by server error codes -6 / -25 (isAccountCommonError).
        // Server sends "account has expired" (or similar) in the Bundle under key
        // "com.dubox.drive.server_alert_message". Without patching, the handler
        // shows the message in a dialog then calls onFailed() → ___.h() →
        // routes to the login/setup screen.
        // return-void silences the dialog and prevents the routing.
        AccountCommonError.match(classDefBy(AccountCommonError.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── OnLoginCallBack._(I) — suppress cold-start login-failure routing ────
        // AccountStartup$__._() fires when autoLogin() returns errorCode=2
        // (not licensed) on every cold launch. It calls ___.e(context) to
        // start the login/setup Activity, then clears account keys and
        // launches background sync coroutines. return-void kills all routing
        // and secondary side-effects in one patch, cleanly.
        mutableClassDefBy("Lcom/dubox/drive/initialize/AccountStartup\$__;")
            .methods.first { it.name == "_" && it.parameters.map { p -> p.type } == listOf("I") }
            .addInstructions(0, "return-void")

        // ── Passport error parser — suppress "invalid signature" code ─────────
        // ____$_.__(String)I: maps "invalid signature" → 0x970ff5 → login blocked.
        // Replaced with: return 0  (treated as "no error" by the caller).
        runCatching {
            PassportSignatureErrorParser.match(
                classDefBy(PassportSignatureErrorParser.definingClass!!)
            ).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 p1, 0x0\nreturn p1")
            }
        }

        // ══ SPOOF SIGNATURE VERIFICATION ══════════════════════════════════════
        val sigHookInit = Fingerprint(
            accessFlags = listOf(AccessFlags.STATIC, AccessFlags.CONSTRUCTOR),
            parameters = emptyList(),
            custom = { method, classDef ->
                classDef.type == EXTENSION_CLASS_SIG &&
                    method.implementation?.instructions
                        ?.filterIsInstance<Instruction21c>()
                        ?.count { instr ->
                            (instr.reference as? StringReference)
                                ?.string in setOf("<package-name>", "<signature>")
                        } == 2
            },
        ).methodOrNull

        if (sigHookInit != null) {
            val instrList = sigHookInit.implementation?.instructions?.toList() ?: emptyList()
            var pkgIdx = -1; var sigIdx = -1
            instrList.forEachIndexed { i, instr ->
                val ref = (instr as? Instruction21c)?.reference as? StringReference
                when (ref?.string) {
                    "<package-name>" -> pkgIdx = i
                    "<signature>"    -> sigIdx = i
                }
            }
            if (pkgIdx >= 0) sigHookInit.replaceInstruction(pkgIdx, "const-string v0, \"com.dubox.drive\"")
            if (sigIdx  >= 0) sigHookInit.replaceInstruction(sigIdx,  "const-string v1, \"$CERT_BASE64\"")

            classDefForEach { classDef ->
                if (classDef.type != EXTENSION_CLASS_SIG &&
                    classDef.superclass == "Landroid/app/Application;"
                ) {
                    mutableClassDefBy(classDef).setSuperClass(EXTENSION_CLASS_SIG)
                }
            }
        }

        // ══ SPOOF FIREBASE CERT HASH ══════════════════════════════════════════
        val firebaseFp = Fingerprint(
            returnType = "Ljava/net/HttpURLConnection;",
            parameters = listOf("Ljava/net/URL;", "Ljava/lang/String;"),
            strings = listOf(
                "X-Android-Cert",
                "Firebase Installations Service is unavailable. Please try again later.",
            ),
        ).methodOrNull

        if (firebaseFp != null) {
            val instrList = firebaseFp.instructions.toList()
            val addReqPropInstr = instrList
                .dropWhile { instr ->
                    !(instr.opcode == Opcode.INVOKE_VIRTUAL &&
                        ((instr as? ReferenceInstruction)?.reference as? MethodReference)
                            ?.name == "addRequestProperty")
                }
                .firstOrNull { instr ->
                    instr.opcode == Opcode.INVOKE_VIRTUAL &&
                        ((instr as? ReferenceInstruction)?.reference as? MethodReference)
                            ?.name == "addRequestProperty"
                }
            if (addReqPropInstr != null) {
                val valueReg = (addReqPropInstr as FiveRegisterInstruction).registerE
                val insertIdx = instrList.indexOf(addReqPropInstr)
                firebaseFp.addInstruction(insertIdx, "const-string v$valueReg, \"$CERT_SHA1\"")
            }
        }

        // ══ SPOOF INSTALL SOURCE ══════════════════════════════════════════════
        val pmClass       = "Landroid/content/pm/PackageManager;"
        val installSrcCls = "Landroid/content/pm/InstallSourceInfo;"
        val sessionCls    = "Landroid/content/pm/PackageInstaller\$SessionInfo;"

        fun MethodReference.isInstallerGetter() =
            (definingClass == pmClass &&
                name == "getInstallerPackageName" &&
                parameterTypes.size == 1 &&
                parameterTypes[0].toString() == "Ljava/lang/String;" &&
                returnType == "Ljava/lang/String;") ||
            (definingClass == installSrcCls &&
                name in setOf(
                    "getInitiatingPackageName", "getInstallingPackageName",
                    "getOriginatingPackageName", "getUpdateOwnerPackageName",
                ) &&
                parameterTypes.isEmpty() && returnType == "Ljava/lang/String;") ||
            (definingClass == sessionCls &&
                name in setOf(
                    "getInstallerPackageName", "getInstallInitiatingPackageName",
                    "getInstallOriginatingPackageName",
                ) &&
                parameterTypes.isEmpty() && returnType == "Ljava/lang/String;")

        classDefForEach { classDef ->
            val hasCalls = classDef.methods.any { m ->
                m.instructionsOrNull?.any { instr ->
                    instr.opcode in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE) &&
                        ((instr as? ReferenceInstruction)?.reference as? MethodReference)
                            ?.isInstallerGetter() == true
                } == true
            }
            if (!hasCalls) return@classDefForEach

            mutableClassDefBy(classDef).methods.forEach { method ->
                val instrList = method.instructionsOrNull?.toList() ?: return@forEach
                instrList.forEachIndexed { idx, instr ->
                    if (instr.opcode !in setOf(Opcode.INVOKE_VIRTUAL, Opcode.INVOKE_VIRTUAL_RANGE)) return@forEachIndexed
                    val ref = (instr as? ReferenceInstruction)?.reference as? MethodReference
                    if (ref?.isInstallerGetter() != true) return@forEachIndexed
                    val moveRes = instrList.getOrNull(idx + 1) as? OneRegisterInstruction ?: return@forEachIndexed
                    if (moveRes.opcode != Opcode.MOVE_RESULT_OBJECT) return@forEachIndexed
                    method.replaceInstruction(idx + 1, "const-string v${moveRes.registerA}, \"$TARGET_INSTALLER\"")
                }
            }
        }

        val appOnCreate = Fingerprint(
            accessFlags = listOf(AccessFlags.PUBLIC),
            returnType = "V",
            parameters = emptyList(),
            custom = { method, classDef ->
                method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
            },
        ).methodOrNull

        appOnCreate?.addInstructions(
            0,
            "const-string v0, \"$TARGET_INSTALLER\"\n" +
            "invoke-static {v0}, $EXTENSION_CLASS_INST->init(Ljava/lang/String;)V",
        )
    }
}
