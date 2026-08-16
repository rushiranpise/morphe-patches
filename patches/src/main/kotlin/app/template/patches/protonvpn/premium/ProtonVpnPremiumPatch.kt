package app.template.patches.protonvpn.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.extensions.InstructionExtensions.removeInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.PROTONVPN_COMPATIBILITY
import app.template.patches.shared.returnEarly

// ProtonVPN premium strategy:
//
// TIER INJECTION (ProtonPass approach):
//   Inject into VpnUser.<init>() at index 10 (after super.<init>(), before first iput).
//   Forces maxTier=Integer(3) and subscribed=1 at construction time.
//   All derived getters (getUserTier, getMaxTier, isFreeUser, isUserPlusOrAbove,
//   getSubscribed, isUserPlusOrAbove) derive correct values naturally — no getter patching needed.
//   Tier 3 = PMTeam/Visionary (highest) — satisfies all >= 2 checks.
//
//   Injection at index 10 (right after super.<init>()):
//     const/16 p2, 0x1                                   → subscribed = 1
//     const/4 v3, 0x3
//     invoke-static {v3}, Integer;->valueOf(I)Integer     → v3 = Integer(3)
//     move-object/from16 p12, v3                         → maxTier param = Integer(3)
//   The constructor then stores these naturally:
//     iput p2 → subscribed=1, iput-object p1 (from p12) → maxTier=Integer(3)
//
// FREE-SERVER-ONLY CONNECTION (required for server-side validation):
//   ProtonVPN's Go core validates session server-side — Plus servers reject free accounts.
//   getBestScoreServer pre-filters to isFreeServer()=true before scoring.
//   ServerListFilter shows only free servers in Countries tab.
//   Connections always go to free servers which Proton accepts on any account.

@Suppress("unused")
val protonVpnPremiumPatch = bytecodePatch(
    name = "Unlock VPN Plus",
    description = "Injects max tier (3=PMTeam) into VpnUser at construction for all UI unlocks, while routing connections through free servers for server-side compatibility.",
) {
    compatibleWith(PROTONVPN_COMPATIBILITY)

    execute {
        // === Tier injection at VpnUser constructor (ProtonPass approach) ===
        // Index 10 = first instruction after super.<init>()
        // .locals 4: v0-v3 available; v3 is free after preamble null-checks
        // Inject subscribed=1 before iput p2->subscribed (index 11).
        // p2 is int type — const/16 is safe.
        VpnUserConstructorFingerprint.method.addInstructions(
            11,
            "const/16 p2, 0x1",
        )

        // Inject maxTier=Integer(3) before move-object/from16 p1,p12 (now at index 21
        // after the +1 shift from subscribed injection, originally index 20).
        // p2 is int (safe for const), p12 is Integer (safe for move-result-object).
        // iput p2->subscribed already ran by this point so reusing p2 as scratch is safe.
        VpnUserConstructorFingerprint.method.addInstructions(
            22,
            """
                const/4 p2, 0x3
                invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
                move-result-object p12
            """,
        )

        // Belt-and-suspenders: patch getters too for any cached/existing VpnUser objects
        VpnUserGetUserTierFingerprint.method.addInstructions(0, "const/4 v0, 0x3\nreturn v0")
        VpnUserGetMaxTierFingerprint.method.addInstructions(
            0,
            "const/4 v0, 0x3\n" +
            "invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;\n" +
            "move-result-object v0\n" +
            "return-object v0",
        )
        VpnUserIsFreeUserFingerprint.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        VpnUserIsUserPlusOrAboveFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        VpnUserGetUserTierNameFingerprint.method.addInstructions(0,
            "const-string v0, \"vpn2022\"\nreturn-object v0")

        // === Server access gates ===
        HasAccessToServerFingerprint.method.addInstructions(0, "const/4 p0, 0x1\nreturn p0")
        HaveAccessWithFingerprint.method.returnEarly(true)
        ServerGroupGetAvailableFingerprint.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // === Free-servers-only (makes VPN connection work server-side) ===

        // Countries tab: only show free servers
        ServerListFilterFingerprint.let {
            val idx = it.instructionMatches[0].index
            it.method.removeInstruction(idx + 2)
            it.method.removeInstruction(idx + 1)
            it.method.removeInstruction(idx)
            it.method.removeInstruction(idx - 1)
            it.method.addInstructionsWithLabels(idx - 1, """
                invoke-virtual {p6}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
                move-result p0
                if-nez p0, :pass
                const/4 p0, 0x0
                return p0
                :pass
                nop
            """)
        }

        // Connection: pre-filter pool to free servers before scoring
        GetBestScoreServerFingerprint.method.addInstructions(0, """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            invoke-interface {p1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
            move-result-object v1
            :loop
            invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z
            move-result v2
            if-eqz v2, :done
            invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;
            move-result-object v2
            check-cast v2, Lcom/protonvpn/android/servers/Server;
            invoke-virtual {v2}, Lcom/protonvpn/android/servers/Server;->isFreeServer()Z
            move-result v3
            if-eqz v3, :loop
            invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z
            goto :loop
            :done
            invoke-interface {v0}, Ljava/util/List;->isEmpty()Z
            move-result v1
            if-nez v1, :skip
            move-object p1, v0
            :skip
            nop
        """)

        // === Feature flags and UI ===
        IsFeatureFlagEnabledFingerprint.method.addInstructions(0, "const/4 p1, 0x1\nreturn p1")

        GetNetShieldAvailabilityFingerprint.method.addInstructions(0, """
            sget-object p0, Lcom/protonvpn/android/netshield/NetShieldAvailability;->AVAILABLE:Lcom/protonvpn/android/netshield/NetShieldAvailability;
            return-object p0
        """)

        // Hide SecureCore/P2P/Tor tabs (not available on free servers)
        GetFilterButtonsFingerprint.method.addInstructions(0, """
            invoke-static {}, Ljava/util/Collections;->emptyList()Ljava/util/List;
            move-result-object v0
            return-object v0
        """)

        // Profile: Standard type only
        ProfileAvailableTypesFingerprint.method.addInstructions(0, """
            new-instance v0, Ljava/util/ArrayList;
            invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
            sget-object v1, Lcom/protonvpn/android/profiles/ui/ProfileType;->Standard:Lcom/protonvpn/android/profiles/ui/ProfileType;
            invoke-virtual {v0, v1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
            return-object v0
        """)

        // Profile countries: free only
        ProfileCountriesFingerprint.method.replaceInstruction(
            ProfileCountriesFingerprint.instructionMatches[0].index,
            "invoke-virtual {p2, v0}, Lcom/protonvpn/android/servers/ServerManager2;->getFreeCountries(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;",
        )
    }
}
