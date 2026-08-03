package app.template.patches.aaenabler.premium

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * MainUiState.getLicenseActive() — boolean getter for the Firestore-backed license flag.
 *
 * This is the single read point consumed by:
 *   - MainViewModel.installWithShizuku(): if (!uiState.licenseActive) → "A premium licence is required"
 *   - Compose UI: controls buy-button visibility and premium feature display
 *
 * Class and method names are stable (non-obfuscated Kotlin data class + property getter).
 * Safe to use definingClass + name directly.
 */
object LicenseActiveFingerprint : Fingerprint(
    definingClass = "Lcom/aaenabler/app/ui/MainUiState;",
    name = "getLicenseActive",
    returnType = "Z",
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    parameters = emptyList()
)
