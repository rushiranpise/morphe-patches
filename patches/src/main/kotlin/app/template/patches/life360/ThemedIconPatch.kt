package app.template.patches.life360

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.LIFE360_COMPATIBILITY
import app.template.patches.shared.childElementsSequence
import org.w3c.dom.Element
import java.io.File

private const val ANDROID_ICON_ATTRIBUTE = "android:icon"
private const val ANDROID_ROUND_ICON_ATTRIBUTE = "android:roundIcon"
private const val ANDROID_DRAWABLE_ATTRIBUTE = "android:drawable"
private const val MONOCHROME_DRAWABLE_NAME = "morphe_life360_monochrome"
private const val MONOCHROME_DRAWABLE_REF = "@drawable/$MONOCHROME_DRAWABLE_NAME"
private const val DEFAULT_VIEWPORT = "108"
private const val DEFAULT_ICON_DP = "108dp"
private const val FILL_TYPE_EVEN_ODD = "evenOdd"
private const val QUALIFIER_V26 = "anydpi-v26"
private const val QUALIFIER_V33 = "anydpi-v33"
private const val ACTION_MAIN = "android.intent.action.MAIN"
private const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"
private const val CATEGORY_LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER"

/**
 * Fallback Life360 360-spiral glyph, taken from the official launcher
 * foreground vector (108dp adaptive-icon safe zone). Used only when the
 * installed APK's foreground is not a vector we can read.
 */
private const val FALLBACK_LOGO_PATH =
    "M53.841,48.936C53.211,48.936 52.62,49.201 52.13,49.697C50.569,51.282 " +
        "50.264,54.91 50.279,57.676C50.315,63.47 52.157,66.005 53.39,66.297L53.488," +
        "66.319L53.693,66.331C56.041,66.331 56.64,60.026 56.768,58.094C57.015," +
        "54.333 57.009,49.294 54.116,48.953C54.022,48.942 53.932,48.936 53.841," +
        "48.936V48.936ZM54.001,40.318C50.34,40.318 46.998,41.726 44.59,44.285C41.654," +
        "47.401 40.253,52.043 40.536,57.715C40.869,64.446 43.331,70.019 47.469," +
        "73.399L47.881,73.734L48.365,73.522C49.832,72.877 51.077,71.829 52.172," +
        "70.313L52.686,69.602L51.984,69.083C50.374,67.889 49.114,66.113 48.239," +
        "63.804C47.232,61.15 46.329,52.79 49.153,48.649C50.309,46.947 51.986,46.048 " +
        "53.996,46.048L54.147,46.05C57.965,46.132 59.854,49.177 59.762,55.104C59.687," +
        "59.94 59.166,62.954 57.845,66.222L57.347,67.449C57.347,67.449 58.071,67.664 " +
        "59.948,67.146C64.1,65.922 67.171,60.483 67.089,54.493C67.015,49.081 63.449," +
        "40.446 54.197,40.32L54.001,40.318ZM55.31,71.378C54.48,72.686 53.503,73.83 " +
        "52.443,74.728L51.287,75.704C51.287,75.704 52.042,76.097 52.772,76.285C54.521," +
        "76.827 56.137,77.032 57.94,77.032C58.855,77.032 59.782,76.963 60.7,76.824C68.322," +
        "75.682 76.517,67.537 77.035,55.88C77.337,49.108 75.149,42.84 70.875,38.229C66.663," +
        "33.687 60.85,31.111 54.509,30.974L54.002,30.968C41.341,30.968 31.212,41.014 " +
        "30.943,53.843C30.802,60.501 32.873,66.537 36.625,70.407C37.745,71.563 39.268," +
        "72.667 40.37,73.206C42.555,74.245 43.814,74.283 43.814,74.283C43.814,74.283 " +
        "42.746,73.206 41.902,72.089C39.239,68.434 37.627,63.401 37.353,57.876C36.94," +
        "49.575 39.805,44.697 42.28,42.067C45.299,38.866 49.461,37.1 54.001,37.1L54.248," +
        "37.102C60.611,37.189 64.425,40.55 66.504,43.353C68.812,46.463 70.223,50.611 " +
        "70.276,54.449C70.379,61.952 66.409,68.591 60.838,70.235C59.608,70.599 58.414," +
        "70.782 57.288,70.782C56.975,70.782 56.665,70.77 56.36,70.74L55.791,70.686L55.31," +
        "71.378ZM57.924,80.25C54.205,80.25 50.626,79.263 47.571,77.394L47.291,77.223L46.967," +
        "77.282C46.53,77.36 46.087,77.415 45.635,77.451C45.346,77.473 45.049,77.485 44.754," +
        "77.485C41.06,77.485 37.365,75.77 34.348,72.656C30,68.175 27.6,61.294 27.757," +
        "53.775C28.063,39.183 39.586,27.75 53.987,27.75C54.183,27.75 54.378,27.753 54.574," +
        "27.757C61.784,27.912 68.398,30.851 73.203,36.031C78.068,41.278 80.56,48.38 80.219," +
        "56.026C79.604,69.84 69.944,78.692 61.167,80.006C60.08,80.169 58.99,80.25 57.924," +
        "80.25V80.25Z"

private val resourceRefRegex = Regex("^@(\\+?)([a-zA-Z]+)(?:-[\\w.]+)*\\/([a-zA-Z0-9_.]+)$")

@Suppress("unused")
val life360ThemedIconPatch = resourcePatch(
    name = "Material You themed icon",
    description = "Adds a proper Android adaptive launcher icon and a monochrome " +
        "Material You themed icon.",
    default = true,
) {
    compatibleWith(LIFE360_COMPATIBILITY)

    execute {
        val resDirectory = get("res")
        if (!resDirectory.isDirectory) {
            throw PatchException("Decoded APK does not contain a res/ directory.")
        }

        val launcherIcons = linkedMapOf<String, ResourceRef>()

        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("AndroidManifest.xml does not contain an application element.")

            val applicationIcon = parseResourceRef(application.getAttribute(ANDROID_ICON_ATTRIBUTE))
                ?: throw PatchException("AndroidManifest.xml application element has no android:icon.")

            application.patchIconAttributes(applicationIcon, launcherIcons)

            for (tagName in listOf("activity", "activity-alias")) {
                val nodes = document.getElementsByTagName(tagName)
                for (index in 0 until nodes.length) {
                    val component = nodes.item(index) as? Element ?: continue
                    if (component.hasLauncherIntent()) {
                        component.patchIconAttributes(applicationIcon, launcherIcons)
                    }
                }
            }
        }

        val layersByIcon = launcherIcons.values.mapNotNull { icon ->
            resolveAdaptiveLayers(resDirectory, icon)?.let { icon to it }
        }
        if (layersByIcon.isEmpty()) {
            throw PatchException(
                "Could not locate adaptive-icon foreground/background resources for Life360 launcher icons.",
            )
        }

        val monochromeVector = layersByIcon
            .asSequence()
            .mapNotNull { (_, layers) -> extractMonochromeVector(resDirectory, layers.foreground) }
            .firstOrNull()
            ?: buildMonochromeVector(
                ExtractedVector(DEFAULT_VIEWPORT, DEFAULT_VIEWPORT, DEFAULT_ICON_DP, DEFAULT_ICON_DP),
                listOf(VectorPath(FALLBACK_LOGO_PATH, FILL_TYPE_EVEN_ODD)),
            )

        resDirectory.resolve("drawable").apply { mkdirs() }
            .resolve("$MONOCHROME_DRAWABLE_NAME.xml")
            .writeText(monochromeVector)

        for ((icon, layers) in layersByIcon) {
            writeAdaptiveIcon(resDirectory, icon, layers, QUALIFIER_V26, monochrome = false)
            writeAdaptiveIcon(resDirectory, icon, layers, QUALIFIER_V33, monochrome = true)
        }

        println(
            "Life360 themed icon: patched ${layersByIcon.size} launcher icon(s) " +
                "(${layersByIcon.joinToString { it.first.reference }}) with adaptive + monochrome layers.",
        )
    }
}

private data class ResourceRef(val type: String, val name: String) {
    val reference = "@$type/$name"
    val key = "$type/$name"
}

private data class AdaptiveLayers(val background: String, val foreground: String)

private data class VectorPath(val pathData: String, val fillType: String?)

private data class ExtractedVector(
    val viewportWidth: String,
    val viewportHeight: String,
    val width: String,
    val height: String,
)

private fun parseResourceRef(value: String?): ResourceRef? {
    if (value.isNullOrBlank()) return null
    val match = resourceRefRegex.matchEntire(value.trim()) ?: return null
    val type = match.groupValues[2]
    val name = match.groupValues[3]
    return if (type == "android") null else ResourceRef(type, name)
}

private fun Element.patchIconAttributes(
    fallback: ResourceRef,
    icons: MutableMap<String, ResourceRef>,
) {
    val icon = parseResourceRef(getAttribute(ANDROID_ICON_ATTRIBUTE)) ?: fallback
    icons[icon.key] = icon
    val roundIcon = parseResourceRef(getAttribute(ANDROID_ROUND_ICON_ATTRIBUTE))
    if (roundIcon == null) {
        setAttribute(ANDROID_ROUND_ICON_ATTRIBUTE, icon.reference)
    } else {
        icons[roundIcon.key] = roundIcon
    }
}

private fun Element.hasLauncherIntent(): Boolean {
    for (filter in childElementsSequence()) {
        if (filter.tagName != "intent-filter") continue
        var hasMainAction = false
        var hasLauncherCategory = false
        for (child in filter.childElementsSequence()) {
            val name = child.getAttribute("android:name")
            if (child.tagName == "action" && name == ACTION_MAIN) {
                hasMainAction = true
            }
            if (
                child.tagName == "category" &&
                (name == CATEGORY_LAUNCHER || name == CATEGORY_LEANBACK_LAUNCHER)
            ) {
                hasLauncherCategory = true
            }
        }
        if (hasMainAction && hasLauncherCategory) return true
    }
    return false
}

private fun ResourcePatchContext.readXmlRoot(xmlFile: File): Element? =
    xmlFile.inputStream().use { input ->
        document(input).use { it.documentElement }
    }

private fun ResourcePatchContext.resolveAdaptiveLayers(
    resDirectory: File,
    icon: ResourceRef,
): AdaptiveLayers? {
    for (xmlFile in resDirectory.findResourceXml(icon.type, icon.name)) {
        val root = readXmlRoot(xmlFile) ?: continue
        if (root.tagName != "adaptive-icon") continue

        var background: String? = null
        var foreground: String? = null
        for (child in root.childElementsSequence()) {
            val drawable = child.getAttribute(ANDROID_DRAWABLE_ATTRIBUTE)
            if (drawable.isBlank()) continue
            when (child.tagName) {
                "background" -> background = drawable
                "foreground" -> foreground = drawable
            }
        }
        if (background != null && foreground != null) {
            return AdaptiveLayers(background, foreground)
        }
    }

    val background = ResourceRef(icon.type, "${icon.name}_background")
    val foreground = ResourceRef(icon.type, "${icon.name}_foreground")
    val hasBackground = resDirectory.findResourceXml(background.type, background.name).isNotEmpty()
    val hasForeground = resDirectory.findResourceXml(foreground.type, foreground.name).isNotEmpty()
    return if (hasBackground && hasForeground) {
        AdaptiveLayers(background.reference, foreground.reference)
    } else {
        null
    }
}

private fun ResourcePatchContext.extractMonochromeVector(
    resDirectory: File,
    foregroundRef: String,
): String? {
    val parsed = parseResourceRef(foregroundRef) ?: return null
    for (xmlFile in resDirectory.findResourceXml(parsed.type, parsed.name)) {
        val root = readXmlRoot(xmlFile) ?: continue
        if (root.tagName != "vector") continue

        val paths = mutableListOf<VectorPath>()
        val nodes = root.getElementsByTagName("path")
        for (index in 0 until nodes.length) {
            val element = nodes.item(index) as? Element ?: continue
            val pathData = element.getAttribute("android:pathData")
            if (pathData.isBlank()) continue
            val fillType = when (val raw = element.getAttribute("android:fillType").trim()) {
                "", "0" -> null
                "1", FILL_TYPE_EVEN_ODD -> FILL_TYPE_EVEN_ODD
                "nonZero" -> "nonZero"
                else -> raw.takeIf { it.isNotEmpty() }
            }
            paths += VectorPath(pathData, fillType)
        }

        val unique = paths.distinctBy { it.pathData }
        if (unique.isEmpty()) continue
        val shortest = unique.minOf { it.pathData.length }
        // Drop vector-export stroke expansions, which are typically far longer
        // than the filled logo glyph they outline.
        val logoPaths = unique.filter { path ->
            path.pathData.length >= 40 && path.pathData.length <= shortest * 2
        }.ifEmpty { listOf(unique.minBy { it.pathData.length }) }

        return buildMonochromeVector(
            ExtractedVector(
                viewportWidth = root.getAttribute("android:viewportWidth").ifBlank { DEFAULT_VIEWPORT },
                viewportHeight = root.getAttribute("android:viewportHeight").ifBlank { DEFAULT_VIEWPORT },
                width = root.getAttribute("android:width").ifBlank { DEFAULT_ICON_DP },
                height = root.getAttribute("android:height").ifBlank { DEFAULT_ICON_DP },
            ),
            logoPaths,
        )
    }
    return null
}

private fun buildMonochromeVector(vector: ExtractedVector, paths: List<VectorPath>): String {
    val pathXml = paths.joinToString("\n") { path ->
        val fillType = path.fillType?.let { "\n        android:fillType=\"$it\"" } ?: ""
        """    <path
        android:fillColor="#FF000000"
        android:pathData="${path.pathData.xmlEscape()}"$fillType />"""
    }

    return """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="${vector.width.xmlEscape()}"
    android:height="${vector.height.xmlEscape()}"
    android:viewportWidth="${vector.viewportWidth.xmlEscape()}"
    android:viewportHeight="${vector.viewportHeight.xmlEscape()}">
$pathXml
</vector>
"""
}

private fun writeAdaptiveIcon(
    resDirectory: File,
    icon: ResourceRef,
    layers: AdaptiveLayers,
    qualifier: String,
    monochrome: Boolean,
) {
    val target = resDirectory.resolve("${icon.type}-$qualifier").apply { mkdirs() }
        .resolve("${icon.name}.xml")
    if (target.exists() && !monochrome) return

    val monochromeXml = if (monochrome) {
        "\n    <monochrome android:drawable=\"$MONOCHROME_DRAWABLE_REF\" />"
    } else {
        ""
    }

    target.writeText(
        """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="${layers.background.xmlEscape()}" />
    <foreground android:drawable="${layers.foreground.xmlEscape()}" />$monochromeXml
</adaptive-icon>
""",
    )
}

private fun File.findResourceXml(type: String, name: String): List<File> =
    listFiles()
        ?.filter { it.isDirectory && (it.name == type || it.name.startsWith("$type-")) }
        ?.map { it.resolve("$name.xml") }
        ?.filter { it.isFile }
        .orEmpty()

private fun String.xmlEscape(): String =
    replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
