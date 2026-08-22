@file:Suppress("ktlint:standard:property-naming")

package app.template.patches.reddit.misc.version

import app.morphe.patcher.patch.bytecodePatch
import kotlin.properties.Delegates

// These are set once during execute; any access before that throws.
var is_2026_04_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_11_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_16_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_18_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_21_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_25_0_or_greater: Boolean by Delegates.notNull()
    private set
var is_2026_32_0_or_greater: Boolean by Delegates.notNull()
    private set

val versionCheckPatch = bytecodePatch(
    description = "Reddit version gate flags.",
) {
    execute {
        val versionName = packageMetadata.versionName

        fun isAtLeast(version: String) = versionName >= version

        is_2026_04_0_or_greater = isAtLeast("2026.04.0")
        is_2026_11_0_or_greater = isAtLeast("2026.11.0")
        is_2026_16_0_or_greater = isAtLeast("2026.16.0")
        is_2026_18_0_or_greater = isAtLeast("2026.18.0")
        is_2026_21_0_or_greater = isAtLeast("2026.21.0")
        is_2026_25_0_or_greater = isAtLeast("2026.25.0")
        is_2026_32_0_or_greater = isAtLeast("2026.32.0")
    }
}
