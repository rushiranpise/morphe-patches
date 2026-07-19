package app.template.patches.googlephotos

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File

private const val AMOLED_BLACK = "#ff000000"
private const val FORCE_DARK_ATTRIBUTE = "android:forceDarkAllowed"

private val photosAmoledAnyFileColorNames = setOf(
    "background_floating_material_dark",
    "background_material_dark",
    "cardview_dark_background",
    "color_surface_elevation_plus_two_dark",
    "design_dark_default_color_background",
    "design_dark_default_color_surface",
    "gm3_legacy_sys_color_dark_background",
    "gm3_sys_color_dark_background",
    "gm3_sys_color_dark_surface",
    "gm3_sys_color_dark_surface_bright",
    "gm3_sys_color_dark_surface_container",
    "gm3_sys_color_dark_surface_container_high",
    "gm3_sys_color_dark_surface_container_highest",
    "gm3_sys_color_dark_surface_container_low",
    "gm3_sys_color_dark_surface_container_lowest",
    "gm3_sys_color_dark_surface_dim",
    "gm_sys_color_dark_background",
    "gm_sys_color_dark_surface",
    "google_dark_default_color_background",
    "google_dark_default_color_primary",
    "google_dark_default_color_primary_dark",
    "google_dark_default_color_primary_variant",
    "google_dark_default_color_surface",
    "google_dark_default_color_surface_variant",
    "m3_sys_color_dark_background",
    "m3_sys_color_dark_surface",
    "m3_sys_color_dark_surface_bright",
    "m3_sys_color_dark_surface_container",
    "m3_sys_color_dark_surface_container_high",
    "m3_sys_color_dark_surface_container_highest",
    "m3_sys_color_dark_surface_container_low",
    "m3_sys_color_dark_surface_container_lowest",
    "m3_sys_color_dark_surface_dim",
    "sud_color_background_dark",
    "sud_glif_v3_dialog_background_color_dark",
    "sud_system_background_dark",
    "sud_system_surface_bright_dark",
    "sud_system_surface_container_dark",
    "sud_system_surface_container_high_dark",
    "sud_system_surface_container_highest_dark",
    "sud_system_surface_container_low_dark",
    "sud_system_surface_container_lowest_dark",
    "sud_system_surface_dark",
    "sud_system_surface_dim_dark",
)

private val photosAmoledNightOnlyColorNames = setOf(
    "photos_daynight_white",
    "photos_daynight_white_elevation_1dp",
    "photos_daynight_white_elevation_3dp",
    "photos_daynight_white_elevation_8dp",
    "photos_daynight_white_elevation_12dp",
    "photos_pager_background_daynight_white",
    "photos_photoeditor_background_daynight_white",
    "photos_printingskus_editing_background",
)

private val neutralDarkSurfaceRgb = setOf(
    "0e0e0e",
    "121212",
    "131313",
    "131314",
    "1b1b1b",
    "1e1f20",
    "1f1f1f",
    "202124",
    "28292c",
    "282a2c",
    "2a2a2a",
    "2d2e30",
    "303030",
    "303134",
    "333537",
    "343434",
    "353639",
    "37393b",
    "3b3b3e",
    "3c4043",
    "424242",
)

private val ignoredColorNameParts = listOf(
    "accent",
    "alert",
    "border",
    "button",
    "disabled",
    "divider",
    "error",
    "foreground",
    "gradient",
    "hairline",
    "icon",
    "inverse",
    "label",
    "on_",
    "outline",
    "primary_container",
    "progress",
    "secondary_container",
    "stroke",
    "tertiary_container",
    "text",
    "tint",
    "warning",
)

private val amoledStyleItems = mapOf(
    "android:colorBackground" to AMOLED_BLACK,
    "android:navigationBarColor" to AMOLED_BLACK,
    "android:statusBarColor" to AMOLED_BLACK,
    "android:windowBackground" to AMOLED_BLACK,
    "android:windowLightNavigationBar" to "false",
    "android:windowLightStatusBar" to "false",
    "colorBackgroundFloating" to AMOLED_BLACK,
    "colorSurface" to AMOLED_BLACK,
    "colorSurfaceBright" to AMOLED_BLACK,
    "colorSurfaceContainer" to AMOLED_BLACK,
    "colorSurfaceContainerHigh" to AMOLED_BLACK,
    "colorSurfaceContainerHighest" to AMOLED_BLACK,
    "colorSurfaceContainerLow" to AMOLED_BLACK,
    "colorSurfaceContainerLowest" to AMOLED_BLACK,
    "colorSurfaceDim" to AMOLED_BLACK,
)

// Resource-only AMOLED approach adapted from xob0t and hoo-dles Morphe example patches.
@Suppress("unused")
val googlePhotosAmoledDarkThemePatch = resourcePatch(
    name = "AMOLED dark theme",
    description = "Makes Google Photos dark surfaces true black.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)

    execute {
        val resDirectory = get("res")
        if (!resDirectory.isDirectory) {
            throw PatchException("Decoded res directory was not found.")
        }

        var changedColors = 0
        var changedStyles = 0

        resDirectory.walkTopDown()
            .filter(File::isValuesXml)
            .forEach { file ->
                val source = file.readText()
                if (!source.contains("<color") && !source.contains("<style")) return@forEach

                val resourcePath = "res/${file.relativeTo(resDirectory).invariantSeparatorsPath}"
                document(resourcePath).use { document ->
                    changedColors += document.blackenAmoledColors(file.isNightValuesXml())
                    changedStyles += document.forceAmoledThemeItems()
                }
            }

        val changed = changedColors + changedStyles
        if (changed == 0) {
            throw PatchException("No Google Photos AMOLED theme resources were patched.")
        }

        println("AMOLED dark theme: patched $changedColors color(s), $changedStyles style item(s).")
    }
}

private fun Document.blackenAmoledColors(isNightFile: Boolean): Int {
    var changed = 0
    val colors = getElementsByTagName("color")
    for (i in 0 until colors.length) {
        val color = colors.item(i) as? Element ?: continue
        val name = color.getAttribute("name")
        if (!name.shouldBlackenAmoledColor(isNightFile, color.textContent)) continue
        if (color.textContent == AMOLED_BLACK) continue

        color.textContent = AMOLED_BLACK
        changed++
    }
    return changed
}

private fun Document.forceAmoledThemeItems(): Int {
    var changed = 0
    val styles = getElementsByTagName("style")
    for (i in 0 until styles.length) {
        val style = styles.item(i) as? Element ?: continue
        if (!style.isThemeStyle()) continue

        style.ensureStyleItem(this, FORCE_DARK_ATTRIBUTE, "true")?.let { changed++ }
        amoledStyleItems.forEach { (name, value) ->
            style.ensureStyleItem(this, name, value)?.let { changed++ }
        }
    }
    return changed
}

private fun String.shouldBlackenAmoledColor(isNightFile: Boolean, value: String): Boolean {
    val colorName = lowercase()
    if (colorName in photosAmoledAnyFileColorNames) return true
    if (isNightFile && colorName in photosAmoledNightOnlyColorNames) return true
    if (!isNightFile && "dark" !in colorName) return false
    if (ignoredColorNameParts.any(colorName::contains)) return false
    if (!listOf("background", "surface", "container", "canvas", "panel", "sheet", "bar").any(colorName::contains)) {
        return false
    }
    return value.amoledRgb() in neutralDarkSurfaceRgb
}

private fun Element.isThemeStyle(): Boolean {
    val name = getAttribute("name").lowercase()
    val parent = getAttribute("parent").lowercase()
    if ("theme" !in name && "theme" !in parent) return false
    if ("light" in name && "dark" !in name && "daynight" !in name) return false
    if ("light" in parent && "dark" !in parent && "daynight" !in parent) return false
    return "dark" in name ||
        "daynight" in name ||
        "dark" in parent ||
        "daynight" in parent ||
        hasDarkThemeItem()
}

private fun Element.hasDarkThemeItem(): Boolean {
    val children = childNodes
    for (i in 0 until children.length) {
        val item = children.item(i) as? Element ?: continue
        if (item.tagName != "item") continue
        val value = item.textContent.lowercase()
        if (
            "design_dark" in value ||
            "gm3_dark" in value ||
            "gm3_sys_color_dark" in value ||
            "gm_sys_color_dark" in value ||
            "m3_sys_color_dark" in value ||
            "photos_daynight_white" in value
        ) return true
    }
    return false
}

private fun Element.ensureStyleItem(document: Document, name: String, value: String): Boolean? {
    val children = childNodes
    for (i in 0 until children.length) {
        val item = children.item(i) as? Element ?: continue
        if (item.tagName != "item" || item.getAttribute("name") != name) continue
        if (item.textContent == value) return null

        item.textContent = value
        return true
    }

    val item = document.createElement("item")
    item.setAttribute("name", name)
    item.textContent = value
    appendChild(item)
    return true
}

private fun String.amoledRgb(): String {
    var color = trim().removePrefix("#").lowercase()
    if (color.length == 8 && color.startsWith("ff")) color = color.substring(2)
    return color
}

private fun File.isValuesXml(): Boolean {
    if (!isFile || !extension.equals("xml", ignoreCase = true)) return false
    val directoryName = parentFile?.name ?: return false
    return directoryName == "values" || directoryName.startsWith("values-")
}

private fun File.isNightValuesXml(): Boolean {
    val directoryName = parentFile?.name ?: return false
    return directoryName == "values-night" || directoryName.startsWith("values-night-")
}
