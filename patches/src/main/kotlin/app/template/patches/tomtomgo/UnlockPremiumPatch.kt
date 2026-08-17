package app.template.patches.tomtomgo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.TOMTOMGO_COMPATIBILITY

@Suppress("unused")
val unlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks TomTom GO premium features for the selected vehicle type.",
) {
    compatibleWith(TOMTOMGO_COMPATIBILITY)

    val vehicleType by stringOption(
        key = "vehicleType",
        default = "car",
        values = mapOf("Car" to "car", "Truck" to "truck"),
        title = "Vehicle type",
        description = "Choose which premium path to unlock.",
    )

    execute {
        val unlockTruck = vehicleType == "truck"
        val primaryType = if (unlockTruck) "c" else "a"
        val fallbackType = if (unlockTruck) "a" else "c"

        // ── CurrentSubscription ───────────────────────────────────────────────
        // Reads the subscription store (H1:LX9/r) and returns the first match for
        // the chosen vehicle type (tb/a$b.a=car, tb/a$b.b=both, tb/a$b.c=truck).
        CurrentSubscriptionFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                iget-object v0, p0, Le9/u2;->H1:LX9/r;
                sget-object v1, Ltb/a${'$'}b;->$primaryType:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/r;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                iget-object v0, p0, Le9/u2;->H1:LX9/r;
                sget-object v1, Ltb/a${'$'}b;->b:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/r;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                iget-object v0, p0, Le9/u2;->H1:LX9/r;
                sget-object v1, Ltb/a${'$'}b;->$fallbackType:Ltb/a${'$'}b;
                invoke-virtual {v0, v1}, LX9/r;->a(Ltb/a${'$'}b;)Ljava/util/ArrayList;
                move-result-object v0
                invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
                move-result v1
                if-eqz v1, :cond_found
                const/4 v0, 0x0
                return-object v0
                :cond_found
                const/4 v1, 0x0
                invoke-virtual {v0, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;
                move-result-object v0
                check-cast v0, Ltb/a;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Car: CombineLatest combiner → always active ───────────────────────
        HasActiveSubscriptionsCombinerFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Car: per-provider mapper → always active ──────────────────────────
        HasActiveSubscriptionsMapperFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Billing: short-circuit IAP flow ──────────────────────────────────
        BillingPurchaseStarterFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                invoke-static {v0}, LCj/u;->g(Ljava/lang/Object;)LRj/m;
                move-result-object v0
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Subscription type flags ───────────────────────────────────────────
        if (!unlockTruck) {
            // Car mode: isCar=true, isTruck=false
            SubscriptionTypeCarFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
            SubscriptionTypeTruckFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
            SubscriptionDetailsIsTruckFingerprint.method.apply {
                removeInstructions(0, instructions.size)
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
            return@execute
        }

        // Truck mode: isCar=false, isTruck=true
        SubscriptionTypeCarFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }
        SubscriptionTypeTruckFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }
        SubscriptionDetailsIsTruckFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Truck path 1: Db/d default branch → active ───────────────────────
        TruckGateDefaultBranchFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Truck path 2: showstopper gate → disabled ─────────────────────────
        TruckShowstopperGateFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Truck path 3: upsell toast gate → disabled ────────────────────────
        // v3.6.320: class changed from Lv9/t; → LH9/u0; (same method sig, same pref strings).
        TruckPurchasedToastGateFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Truck path 4: "Are You A Truck Driver?" dialog ────────────────────
        // DROPPED in v3.6.320: Le9/x0; is now MobileIncompatibleVoiceLocaleDialog.
        // No stable replacement found; truck dialog unreachable with paths 2,3 disabled.

        // ── Truck path 5: NavBanner subscribe button → no-op for case a==1 ────
        // v3.6.320: class changed from Le9/P0; → Le9/U0; (field a:I still present).
        TruckNavBannerSubscribeFingerprint.method.addInstructions(
            0,
            """
            iget v0, p0, Le9/U0;->a:I
            const/4 v1, 0x1
            if-ne v0, v1, :cond_original
            return-void
            :cond_original
            """.trimIndent(),
        )

        // ── Truck path 6: subscription screen truck tab → suppressed ──────────
        SubscriptionScreenTruckTabFingerprint.method.addInstructions(0, "return-void")

        // ── Truck path 7: truck NavBanner remote flag → always visible ─────────
        ShowLargeVehiclesBannerFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                return-object v0
                """.trimIndent(),
            )
        }

        // ── Truck path 8: NavBanner body tap → no-op for case a==4 ───────────
        // v3.6.320: class changed from LPc/v; → Le9/T0; (field a:I still present).
        TruckBannerMessageClickFingerprint.method.addInstructions(
            0,
            """
            iget v0, p0, Le9/T0;->a:I
            const/4 v1, 0x4
            if-ne v0, v1, :cond_original
            return-void
            :cond_original
            """.trimIndent(),
        )

        // ── Truck path 9: Urban Airship IAM → suppressed ──────────────────────
        AirshipIAMLauncherFingerprint.method.apply {
            removeInstructions(0, instructions.size)
            addInstructions(
                0,
                """
                sget-object v0, Lmk/u;->a:Lmk/u;
                return-object v0
                """.trimIndent(),
            )
        }
    }
}
