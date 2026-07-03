package app.template.patches.duboxdrive

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.template.patches.shared.Constants.DUBOXDRIVE_COMPATIBILITY
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.MethodReference
import com.android.tools.smali.dexlib2.iface.reference.StringReference

// ── Cert constants (extracted from original com.dubox.drive APK v4.19.6) ──────
private const val CERT_SHA1   = "11F8C73FD20D39CF55FD7F3F0F6A88C7E8909858"
private const val CERT_BASE64 = "MIIDlzCCAn+gAwIBAgIEc6Fu6TANBgkqhkiG9w0BAQsFADB7MRgwFgYDVQQGDA/jgavjgbvjgpPj" +
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
    description = "Unlocks Dubox Drive VIP/SVIP (Premium+)",
) {
    compatibleWith(DUBOXDRIVE_COMPATIBILITY)

    extendWith("extensions/extension.mpe")

    execute {

        // ── VipInfo boolean getters → true ────────────────────────────────────
        for (fp in listOf(
            VipInfoIsVip, VipInfoGetCountryLogin, VipInfoGetCountryRegister,
            VipInfoGetHasSpacePri, VipInfoIsSub, VipInfoIsSubSpace,
        )) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // ── VipInfo int getters → 2 (SVIP) ───────────────────────────────────
        for (fp in listOf(VipInfoGetLevel, VipInfoGetIdentity)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 v0, 0x2\nreturn v0")
            }
        }

        // ── VipInfo timestamp getters → 2099 ─────────────────────────────────
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

        // ── MemberInfo timestamp getters → 2099 (millis) ─────────────────────
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
        for (fp in listOf(VipRightsGateI, VipRightsGateJ)) {
            fp.match(classDefBy(fp.definingClass!!)).method.apply {
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

        // ── Global VipInfo cache gate (class changes per version) → true ──────
        for (fp in listOf(GlobalVipGate4182, GlobalVipGate4186, GlobalVipGate4196, GlobalVipGate4201)) {
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
        AccountBanHandler.match(classDefBy(AccountBanHandler.definingClass!!)).method.apply {
            if (implementation == null) return@apply
            addInstructions(0, "return-void")
        }

        // ── lq/_._____(Activity, RemoteExceptionInfo)Z — account expired gate ─
        // Called by many API result receivers when server returns ServerBanInfo.
        // Returns false = "not handled" so the caller proceeds normally.
        runCatching {
            AccountExpiredGate.match(classDefBy(AccountExpiredGate.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "const/4 p1, 0x0\nreturn p1")
            }
        }

        // ── Passport SDK error parser — suppress "invalid signature" code ─────
        // ____$_.__(String)I maps "invalid signature" server response → 0x970ff7
        // → shows "current version carries a risk" blocking login.
        runCatching {
            PassportSignatureErrorParser.match(classDefBy(PassportSignatureErrorParser.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                removeInstructions(0, instructions.count())
                addInstructions(0, "const/4 p1, 0x0\nreturn p1")
            }
        }

        // ── Login error display safety net ────────────────────────────────────
        runCatching {
            AccountFragmentLoginErrorDisplay.match(classDefBy(AccountFragmentLoginErrorDisplay.definingClass!!)).method.apply {
                if (implementation == null) return@apply
                addInstructions(0, "return-void")
            }
        }

        // ══ SPOOF SIGNATURE VERIFICATION ══════════════════════════════════════
        // Port of spoofSignatureVerificationPatch — inlines cert, no user input.
        // Replaces <package-name> and <signature> placeholders in SignatureHookApp.

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

            val hookClassType = EXTENSION_CLASS_SIG
            classDefForEach { classDef ->
                if (classDef.type != hookClassType &&
                    classDef.superclass == "Landroid/app/Application;"
                ) {
                    mutableClassDefBy(classDef).setSuperClass(hookClassType)
                }
            }
            println("Spoof signature: injected package=com.dubox.drive cert=${CERT_BASE64.take(20)}...")
        } else {
            println("Spoof signature: SignatureHookApp not found in extension — skipped.")
        }

        // ══ SPOOF FIREBASE CERT HASH ══════════════════════════════════════════
        // Port of spoofFirebaseCertHashPatch — inlines SHA-1, no user input.
        // Replaces X-Android-Cert header value before it's sent to Firebase.

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
                println("Spoof Firebase cert: injected SHA-1=$CERT_SHA1 at index $insertIdx.")
            }
        } else {
            println("Spoof Firebase cert: Firebase Installations SDK not found — skipped.")
        }

        // ══ SPOOF INSTALL SOURCE ══════════════════════════════════════════════
        // Port of spoofInstallSourcePatch — Layer 1 DEX + Layer 2 binder proxy.

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

        var installerPatchCount = 0
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
                    installerPatchCount++
                }
            }
        }

        // Layer 2: binder proxy via extension
        val appOnCreate = Fingerprint(
            accessFlags = listOf(AccessFlags.PUBLIC),
            returnType = "V",
            parameters = emptyList(),
            custom = { method, classDef ->
                method.name == "onCreate" && classDef.superclass == "Landroid/app/Application;"
            },
        ).methodOrNull

        if (appOnCreate != null) {
            appOnCreate.addInstructions(
                0,
                "const-string v0, \"$TARGET_INSTALLER\"\n" +
                "invoke-static {v0}, $EXTENSION_CLASS_INST->init(Ljava/lang/String;)V",
            )
            println("Spoof install source: binder proxy injected, $installerPatchCount DEX call sites patched.")
        } else {
            println("Spoof install source: $installerPatchCount DEX call sites patched (no Application.onCreate found).")
        }
    }
}
