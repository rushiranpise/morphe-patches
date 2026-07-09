package app.template.patches.blurams

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.BLURAMS_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val bluramsUnlockCloudServicePatch = bytecodePatch(
    name = "Unlock Cloud Service",
    description = "Unlocks BluramsGuard cloud storage, AI detection, and playback features.",
    default = true,
) {
    compatibleWith(BLURAMS_COMPATIBILITY)

    execute {
        // ── Global subscription checks ──────────────────────────────────────

        // Unlock basic cloud service (cloud storage subscription gate)
        HasAvailableBasicMealFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Unlock AI detection features
        HasAvailableAIMealFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Unlock AI history/ever-subscribed gate
        HasAIMealEverFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Unlock cloud playback timeline (money pkg gate)
        GetHasMoneyPkgFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Return Using status for AI service per-camera
        GetAIStatusFingerprint.method.apply {
            clearBody()
            addInstructions(
                0,
                "sget-object v0, Lcom/blurams/common/util/b1\$a;->Using:Lcom/blurams/common/util/b1\$a;\nreturn-object v0"
            )
        }

        // ── Per-camera service status (0x1 = ServiceStatus_UnExpired) ───────

        // Return UnExpired for serviceStatus
        GetServiceStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Return UnExpired for serviceStatusNew
        GetServiceStatusNewFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // Return active for dvrStatus (0 = no service → isExpired returns true)
        GetDvrStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── Expiry checks ────────────────────────────────────────────────────

        // CameraInfo.isExpired() → always false
        CameraInfoIsExpiredFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // devicecomponents.b.isExpired() → always false (hides "Expired" label)
        DeviceComponentsIsExpiredFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // devicecomponents.b.isServiceEnable() → always true
        IsServiceEnableFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // devicecomponents.b.isBindCloudService() → always true
        IsBindCloudServiceFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // ── closeli model/a (parallel device model used in Mine page) ────────

        // closeli getDvrStatus() → 0x1 (active)
        CloseliGetDvrStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // closeli getServiceStatus() → 0x1 (UnExpired)
        CloseliGetServiceStatusFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // closeli getServiceStatusNew() → 0x1 (UnExpired)
        CloseliGetServiceStatusNewFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // closeli isExpired() → false (removes "Expired" label from Mine UI)
        CloseliIsExpiredFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ── Block server data from overwriting patched state ─────────────────

        // parseMealResult() → return true without parsing (prevents empty server
        // goods list from zeroing out aiGoods/basicGoods/hasMoneyPkg)
        ParseMealResultFingerprint.method.apply {
            clearBody()
            addInstructions(0, "const/4 v0, 0x1\nreturn v0")
        }

        // filterPurchase() → no-op (prevents empty list from clearing goods state)
        FilterPurchaseFingerprint.method.apply {
            clearBody()
            addInstructions(0, "return-void")
        }
    }
}
