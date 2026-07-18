package app.template.patches.mapy

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.getInstruction
import app.morphe.patcher.extensions.InstructionExtensions.instructionsOrNull
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MAPY_COMPATIBILITY
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

/**
 * cz.seznam.auth.profile.UserInfo$Companion.fromJson()
 * Reads "premium" from login JSON → drives UserInfo.isPremium → My Maps premium gating.
 */
private val UserInfoFromJsonFingerprint = Fingerprint(
    name = "fromJson",
    definingClass = "/UserInfo\$Companion;",
    strings = listOf("premium"),
)

/**
 * FeaturesApiModel synthetic constructor — called when subscription API response is parsed.
 * Params: (I bitmask, Z userBadge, Z advancedMyMaps, Z premiumSupport,
 *          I offlineMapCount, Z customNavigationSpeeds, Z advancedRouting,
 *          Z watchSupport, Z peakfinder, Integer peakfinderUseLimit)
 */
private val FeaturesApiModelSyntheticInitFingerprint = Fingerprint(
    name = "<init>",
    definingClass = "/FeaturesApiModel;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.SYNTHETIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf("I", "Z", "Z", "Z", "I", "Z", "Z", "Z", "Z", "Ljava/lang/Integer;"),
)

/**
 * PremiumFeatures full constructor — sets all premium feature fields.
 * Called from: default ctor (no subscription), cached data restore, API-parsed data.
 * Params: (Z userBadge, Z advancedMyMaps, Z premiumSupport, OfflineMapCount,
 *          Z customNavigationSpeeds, Z advancedRouting, Z watchSupport,
 *          PremiumFeatureAccess peakfinderAccess, PremiumFeatureAccess proPlannerAccess)
 *
 * Patching this covers ALL code paths including cached/default state where
 * FeaturesApiModel is never parsed. This is the root cause of the "Get Premium"
 * button still showing even though features work — the menu premium style is driven
 * by hasPremiumFeature(UserBadge) which reads PremiumFeatures.userBadge directly.
 */
private val PremiumFeaturesFullInitFingerprint = Fingerprint(
    name = "<init>",
    definingClass = "/PremiumFeatures;",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.CONSTRUCTOR),
    parameters = listOf(
        "Z", "Z", "Z",
        "Lcom/mapy/premium/model/OfflineMapCount;",
        "Z", "Z", "Z",
        "Lcom/mapy/premium/model/PremiumFeatureAccess;",
        "Lcom/mapy/premium/model/PremiumFeatureAccess;",
    ),
)

/**
 * Mapy.com (v26.7.1, cz.seznam.mapy, APKS) — Unlock Premium.
 *
 * Three gates patched:
 *
 * 1. UserInfo$Companion.fromJson() — forces isPremium=true from login response.
 *
 * 2. FeaturesApiModel synthetic <init>() — forces all subscription feature flags
 *    when the server API response is parsed (online, logged-in path).
 *
 * 3. PremiumFeatures full <init>() — forces all feature fields at construction time.
 *    This covers ALL code paths including:
 *    - Default (no subscription data): PremiumFeatures() → <init>() → all false → "Get Premium" shows
 *    - Cached state restore from datastore
 *    - API response converted via toModel()
 *    By patching at this level, AppMenuViewModel.hasPremiumFeature(UserBadge) always
 *    returns true → premiumStyle = PremiumUser → "Get Premium" button hidden.
 */
@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks Premium features in app after login.",
) {
    compatibleWith(MAPY_COMPATIBILITY)

    execute {
        // ── 1. UserInfo.isPremium → always true ───────────────────────────────
        UserInfoFromJsonFingerprint.method.apply {
            val stringIndex = UserInfoFromJsonFingerprint.stringMatches.first().index
            val moveResultIndex = indexOfFirstInstructionOrThrow(stringIndex, Opcode.MOVE_RESULT)
            val reg = getInstruction<OneRegisterInstruction>(moveResultIndex).registerA
            addInstruction(moveResultIndex + 1, "const/16 v$reg, 0x1")
        }

        // ── 2. FeaturesApiModel → all features enabled (API response path) ────
        FeaturesApiModelSyntheticInitFingerprint.method.addInstructions(
            0,
            """
                const/4 p2, 0x1
                const/4 p3, 0x1
                const/4 p4, 0x1
                const/4 p5, -0x1
                const/4 p6, 0x1
                const/4 p7, 0x1
                const/4 p8, 0x1
                const/4 p9, 0x1
                const/16 p10, 0x270f
                invoke-static {p10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p10
            """,
        )

        // ── 3. PremiumFeatures → always constructed with all features enabled ──
        // Covers default/cached/converted paths — fixes "Get Premium" in menu.
        PremiumFeaturesFullInitFingerprint.method.addInstructions(
            0,
            """
                const/4 p1, 0x1
                const/4 p2, 0x1
                const/4 p3, 0x1
                sget-object p4, Lcom/mapy/premium/model/OfflineMapCount${'$'}Unlimited;->INSTANCE:Lcom/mapy/premium/model/OfflineMapCount${'$'}Unlimited;
                const/4 p5, 0x1
                const/4 p6, 0x1
                const/4 p7, 0x1
                sget-object p8, Lcom/mapy/premium/model/PremiumFeatureAccess${'$'}Unlimited;->INSTANCE:Lcom/mapy/premium/model/PremiumFeatureAccess${'$'}Unlimited;
                sget-object p9, Lcom/mapy/premium/model/PremiumFeatureAccess${'$'}Unlimited;->INSTANCE:Lcom/mapy/premium/model/PremiumFeatureAccess${'$'}Unlimited;
            """,
        )
    }
}

private fun Method.indexOfFirstInstructionOrThrow(startIndex: Int, opcode: Opcode): Int {
    val instructions = instructionsOrNull ?: throw PatchException("Method has no instructions: $this")
    for (index in startIndex + 1 until instructions.count()) {
        if (instructions.elementAt(index).opcode == opcode) return index
    }
    throw PatchException("Could not find opcode $opcode after index $startIndex in $this")
}
