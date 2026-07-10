package app.template.patches.flightsky

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.FLIGHTSKY_COMPATIBILITY
import app.template.patches.shared.clearBody

private const val PREMIUM_HELPER = "Lu5/h;"
private const val PREMIUM_PREFS = "Lu6/b;"
private const val ADAPTY_ACCESS_LEVEL = "Lcom/adapty/models/AdaptyProfile\$AccessLevel;"
private const val MAP_HOME = "Lcom/live/flight/tracker/ui/map/b;"

@Suppress("unused")
val flightskyBypassPairipPatch = bytecodePatch(
    description = "Disables PairIP license checks."
) {
    compatibleWith(FLIGHTSKY_COMPATIBILITY)

    execute {
        mutableClassDefByOrNull("Lcom/pairip/licensecheck/LicenseContentProvider;")
            ?.methods
            ?.firstOrNull { it.name == "onCreate" && it.parameterTypes.isEmpty() && it.returnType == "Z" }
            ?.apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        mutableClassDefByOrNull("Lcom/pairip/licensecheck/LicenseClient;")
            ?.methods
            ?.filter { it.name == "checkLicense" || it.name == "initializeLicenseCheck" || it.name == "start" }
            ?.forEach { it.clearBody(); it.addInstructions(0, "return-void") }

        mutableClassDefByOrNull(MAP_HOME)
            ?.methods
            ?.firstOrNull {
                it.name == "b" &&
                    it.parameterTypes == listOf(
                        "Z",
                        "Landroidx/compose/ui/Modifier;",
                        "Lcom/live/flight/tracker/ui/map/d;",
                        "Lkotlin/jvm/functions/Function0;",
                        "Lkotlin/jvm/functions/Function0;",
                        "Lkotlin/jvm/functions/Function0;",
                        "Lcb/b;",
                        "Lcb/b;",
                        "Lcb/b;",
                        "Lkotlin/jvm/functions/Function0;",
                        "Lcb/b;",
                        "F",
                        "LL0/p;",
                        "I",
                    ) &&
                    it.returnType == "V"
            }
            ?.let { method ->
                val instructions = method.implementation!!.instructions
                val remoteConfigIndex = instructions.indexOfFirst {
                    it.toString().contains("cockpit_3d_map_enabled")
                }.takeIf { it >= 0 }
                val moveResultIndex = remoteConfigIndex?.let { index ->
                    instructions.drop(index)
                        .indexOfFirst { it.toString() == "move-result v6" }
                        .takeIf { it >= 0 }
                        ?.let { index + it }
                }

                if (moveResultIndex != null) {
                    method.addInstructions(moveResultIndex + 1, "const/4 v6, 0x0")
                }
            }
    }
}

@Suppress("unused")
val flightskyUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks premium features in app.",
    default = true
) {
    compatibleWith(FLIGHTSKY_COMPATIBILITY)

    dependsOn(flightskyBypassPairipPatch)

    execute {
        mutableClassDefBy(ADAPTY_ACCESS_LEVEL).methods
            .first { it.name == "isActive" && it.returnType == "Z" }
            .apply { clearBody(); addInstructions(0, "const/4 v0, 0x1\nreturn v0") }

        mutableClassDefBy(PREMIUM_HELPER).methods
            .first {
                it.name == "M" &&
                    it.parameterTypes == listOf("Lcom/adapty/models/AdaptyProfile;", "[Ljava/lang/String;") &&
                    it.returnType == "Ljava/util/LinkedHashMap;"
            }
            .apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        new-instance v0, Ljava/util/LinkedHashMap;
                        invoke-direct {v0}, Ljava/util/LinkedHashMap;-><init>()V
                        array-length v1, p1
                        if-nez v1, :loop_start
                        const-string v2, "premium"
                        sget-object v3, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                        invoke-virtual {v0, v2, v3}, Ljava/util/LinkedHashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                        goto :done
                        :loop_start
                        const/4 v2, 0x0
                        :loop
                        if-ge v2, v1, :done
                        aget-object v3, p1, v2
                        if-eqz v3, :next
                        sget-object v4, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
                        invoke-virtual {v0, v3, v4}, Ljava/util/LinkedHashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                        :next
                        add-int/lit8 v2, v2, 0x1
                        goto :loop
                        :done
                        return-object v0
                    """.trimIndent(),
                )
            }

        mutableClassDefBy(PREMIUM_PREFS).methods
            .first { it.name == "e" && it.parameterTypes == listOf("Z") && it.returnType == "V" }
            .apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 p1, 0x0
                        iget-object v0, p0, Lu6/b;->b:Landroid/content/SharedPreferences;
                        invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        const-string v1, "ads_enabled"
                        invoke-interface {v0, v1, p1}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        const-string v1, "user_premium_premium"
                        const/4 p1, 0x1
                        invoke-interface {v0, v1, p1}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                        const/4 p1, 0x0
                        iput-boolean p1, p0, Lu6/b;->c:Z
                        return-void
                    """.trimIndent(),
                )
            }

        mutableClassDefBy(PREMIUM_PREFS).methods
            .first { it.name == "f" && it.parameterTypes == listOf("Ljava/lang/String;", "Z") && it.returnType == "V" }
            .apply {
                clearBody()
                addInstructions(
                    0,
                    """
                        const/4 p2, 0x1
                        iget-object v0, p0, Lu6/b;->b:Landroid/content/SharedPreferences;
                        invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        const-string v1, "user_premium_premium"
                        invoke-interface {v0, v1, p2}, Landroid/content/SharedPreferences${'$'}Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences${'$'}Editor;
                        move-result-object v0
                        invoke-interface {v0}, Landroid/content/SharedPreferences${'$'}Editor;->apply()V
                        return-void
                    """.trimIndent(),
                )
            }

    }
}
