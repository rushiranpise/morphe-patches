package app.template.patches.protonmail.composer

import app.morphe.patcher.Fingerprint

/**
 * Matches uniffi.mail_uniffi.DraftScheduleSendOptions.isCustomOptionAvailable()
 *
 * Uses definingClass + name directly — the only reliable approach for uniffi
 * classes where multiple zero-param boolean methods share the same field access.
 *
 * .registers 1 (p0 only) — patch uses addInstructions with p0 not v0.
 */
object IsCustomTimeSendOptionAvailableFingerprint : Fingerprint(
    definingClass = "Luniffi/mail_uniffi/DraftScheduleSendOptions;",
    name = "isCustomOptionAvailable",
)
