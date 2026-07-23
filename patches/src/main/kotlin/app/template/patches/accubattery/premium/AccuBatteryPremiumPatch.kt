package app.template.patches.accubattery.premium

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.ACCUBATTERY_COMPATIBILITY
import app.template.patches.shared.clearBody
import app.template.patches.shared.returnEarly

/**
 * AccuBattery Premium Patch
 *
 * AccuBattery uses a custom server-side check-in system:
 *   - Sends Google Play purchase tokens + promo codes to Digibites' server
 *   - Server returns "Offer" (s7) with featureIds: List<String> and expiryDate
 *   - Only one Feature enum: PRO (id = "pro")
 *
 * Premium state is driven by TWO separate mechanisms:
 *
 * A) r8lambda878h.write()Z (isPro): the boolean gate consulted by feature checks.
 *    Checks Google Play purchase (Lab/s2) OR promo code license (Lab/isReady$read).
 *
 * B) Lab/r4 LiveData pipeline: drives the UI banner and StatsOverlay.
 *    r4.write()V computes PURCHASED/NOT_PURCHASED/UNKNOWN from the same sources
 *    and posts to r4.IconCompatParcelizer LiveData<r4$write>.
 *    Lab/setClickableViews observes this LiveData via write(r4$write)V and
 *    calls read(Z=true/false) to show/hide Pro UI.
 *
 * Patch layers:
 *   1. s7.isValid() → returnEarly(true): server offers permanently valid
 *   2. s7.isExpired() → returnEarly(false): offers never expire
 *   3. write()Z (isPro) → classDefForEach → returnEarly(true): boolean gate bypassed
 *   4. r4.write()V → classDefForEach → always post PURCHASED to LiveData: clears banner
 *   5. setClickableViews.write(r4$write) → classDefForEach → always call read(true):
 *      StatsOverlay always shows Pro features regardless of LiveData state
 */
@Suppress("unused")
val accuBatteryPremiumPatch = bytecodePatch(
    name = "AccuBattery Premium",
    description = "Unlocks AccuBattery Pro by bypassing server offer validity, the Google Play purchase gate, and all UI premium banners.",
    default = true
) {
    compatibleWith(ACCUBATTERY_COMPATIBILITY)

    execute {
        // Patch 1 & 2: server offer validity
        OfferIsValidFingerprint.method.returnEarly(true)
        OfferIsExpiredFingerprint.method.returnEarly(false)

        // Patches 3, 4, 5: classDefForEach scan
        var isProPatched = false
        var r4WritePatched = false
        var statsOverlayPatched = false

        classDefForEach { classDef ->
            if (isProPatched && r4WritePatched && statsOverlayPatched) return@classDefForEach

            // --- Patch 3: isPro() = write()Z ---
            // The class has unique field: public static read:Ljava/lang/Integer;
            if (!isProPatched) {
                val hasReadIntegerField = classDef.fields.any { field ->
                    field.name == "read" &&
                        field.type == "Ljava/lang/Integer;" &&
                        field.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PUBLIC.value != 0 &&
                        field.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value != 0
                }
                if (hasReadIntegerField) {
                    mutableClassDefByOrNull(classDef.type)
                        ?.methods
                        ?.firstOrNull { m ->
                            m.name == "write" && m.returnType == "Z" && m.parameterTypes.isEmpty() &&
                                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PUBLIC.value != 0 &&
                                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value != 0
                        }
                        ?.apply {
                            clearBody()
                            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
                            isProPatched = true
                        }
                }
            }

            // --- Patch 4: r4.write()V → always post PURCHASED to LiveData ---
            // r4 is the ONLY OnSharedPreferenceChangeListener with ≥4 AppLovinIncentivizedInterstitial fields.
            // r4.write()V is private void no-params; it computes PURCHASED/NOT_PURCHASED/UNKNOWN
            // and calls the private MediaBrowserCompatCustomActionResultReceiver(r4$write)V.
            // We replace it to always sget PURCHASED and call that private setter.
            if (!r4WritePatched) {
                val hasEnoughAdFields = classDef.fields.count { field ->
                    field.type == "Lab/AppLovinIncentivizedInterstitial;"
                } >= 4
                val isOnSharedPrefListener = classDef.interfaces.contains(
                    "Landroid/content/SharedPreferences\$OnSharedPreferenceChangeListener;"
                )
                if (hasEnoughAdFields && isOnSharedPrefListener) {
                    val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach
                    val classType = classDef.type // e.g. "Lab/r4;"

                    // Find the r4$write inner enum class type — it's the type used in
                    // the private MediaBrowserCompatCustomActionResultReceiver(r4$write)V method param
                    val purchaseStateType = classDef.methods
                        .firstOrNull { m ->
                            m.name == "MediaBrowserCompatCustomActionResultReceiver" &&
                                m.parameterTypes.size == 1 &&
                                m.returnType == "V" &&
                                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PRIVATE.value != 0 &&
                                m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.STATIC.value == 0
                        }
                        ?.parameterTypes?.firstOrNull()?.toString()
                        ?: return@classDefForEach

                    // Patch write()V (private void no-params) to always post PURCHASED
                    mutableClass.methods.firstOrNull { m ->
                        m.name == "write" && m.returnType == "V" && m.parameterTypes.isEmpty() &&
                            m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PRIVATE.value != 0
                    }?.apply {
                        clearBody()
                        addInstructions(
                            0,
                            """
                                sget-object v0, $purchaseStateType->PURCHASED:$purchaseStateType
                                invoke-direct { p0, v0 }, ${classType}->MediaBrowserCompatCustomActionResultReceiver(${purchaseStateType})V
                                return-void
                            """.trimIndent()
                        )
                        r4WritePatched = true
                    }
                }
            }

            // --- Patch 5: setClickableViews.write(r4$write)V → always call read(true) ---
            // setClickableViews is the StatsOverlay UI class. Its write(r4$write)V compares
            // the incoming enum to PURCHASED and calls read(Z)V with true/false.
            // We patch it to always call read(true).
            // Unique identifier: has a private method named "write" taking a single non-primitive
            // parameter AND a private method named "read" taking Z.
            if (!statsOverlayPatched) {
                val hasWriteEnumMethod = classDef.methods.any { m ->
                    m.name == "write" && m.returnType == "V" &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes[0].toString().startsWith("Lab/") &&
                        m.parameterTypes[0].toString().contains("\$write;") &&
                        m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PRIVATE.value != 0
                }
                val hasReadBoolMethod = classDef.methods.any { m ->
                    m.name == "read" && m.returnType == "V" &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes[0].toString() == "Z" &&
                        m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PRIVATE.value != 0
                }
                if (hasWriteEnumMethod && hasReadBoolMethod) {
                    val mutableClass = mutableClassDefByOrNull(classDef.type) ?: return@classDefForEach

                    // Find write(r4$write)V and replace with: invoke-direct {p0, true}, read(Z)V
                    mutableClass.methods.firstOrNull { m ->
                        m.name == "write" && m.returnType == "V" &&
                            m.parameterTypes.size == 1 &&
                            m.parameterTypes[0].toString().contains("\$write;") &&
                            m.accessFlags and com.android.tools.smali.dexlib2.AccessFlags.PRIVATE.value != 0
                    }?.apply {
                        val classType = classDef.type
                        clearBody()
                        addInstructions(
                            0,
                            """
                                const/4 v0, 0x1
                                invoke-direct { p0, v0 }, ${classType}->read(Z)V
                                return-void
                            """.trimIndent()
                        )
                        statsOverlayPatched = true
                    }
                }
            }
        }

        if (!isProPatched) throw Exception(
            "AccuBattery: isPro() method not found. Re-verify 'public static read:Integer' field."
        )
        if (!r4WritePatched) throw Exception(
            "AccuBattery: r4.write()V not found. Re-verify OnSharedPreferenceChangeListener + 4 LiveData fields."
        )
        if (!statsOverlayPatched) throw Exception(
            "AccuBattery: StatsOverlay.write(r4\$write)V not found. Re-verify private write+read(Z) pattern."
        )
    }
}
