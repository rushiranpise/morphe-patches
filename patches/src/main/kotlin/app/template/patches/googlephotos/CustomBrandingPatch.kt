package app.template.patches.googlephotos

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.folderOption
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.GOOGLE_PHOTOS_COMPATIBILITY
import org.w3c.dom.Element
import java.io.File

private const val CUSTOM_LAUNCHER_ICON = "morphe_launcher_custom"
private const val CUSTOM_BACKGROUND = "morphe_adaptive_background_custom.png"
private const val CUSTOM_FOREGROUND = "morphe_adaptive_foreground_custom.png"
private const val CUSTOM_MONOCHROME = "morphe_adaptive_monochrome_custom.xml"

private val launcherMipmapDirectories = listOf(
    "mipmap-mdpi",
    "mipmap-hdpi",
    "mipmap-xhdpi",
    "mipmap-xxhdpi",
    "mipmap-xxxhdpi",
)

@Suppress("unused")
val googlePhotosCustomBrandingPatch = resourcePatch(
    name = "Custom branding",
    description = "Adds an option to replace the Google Photos launcher icon.",
    default = false,
) {
    compatibleWith(GOOGLE_PHOTOS_COMPATIBILITY)

    val customIcon by folderOption(
        key = "customIcon",
        title = "Custom icon",
        description = """
            Folder containing a Morphe adaptive icon set.

            Supported density folders:
            - mipmap-mdpi
            - mipmap-hdpi
            - mipmap-xhdpi
            - mipmap-xxhdpi
            - mipmap-xxxhdpi

            Each included density folder must contain:
            - $CUSTOM_BACKGROUND
            - $CUSTOM_FOREGROUND

            Optional themed icon:
            - drawable/$CUSTOM_MONOCHROME
        """.trimIndent(),
        required = true,
    )

    execute {
        val iconRoot = customIcon
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::File)
            ?: throw PatchException("Custom icon folder is required.")

        if (!iconRoot.isDirectory) {
            throw PatchException("Custom icon folder was not found: ${iconRoot.absolutePath}")
        }

        val resDirectory = get("res")
        var copiedDensityCount = 0

        launcherMipmapDirectories.forEach { directoryName ->
            val sourceDirectory = iconRoot.resolve(directoryName)
            if (!sourceDirectory.exists()) return@forEach

            if (!sourceDirectory.isDirectory) {
                throw PatchException("Expected a directory: ${sourceDirectory.absolutePath}")
            }

            val background = sourceDirectory.resolve(CUSTOM_BACKGROUND)
            val foreground = sourceDirectory.resolve(CUSTOM_FOREGROUND)

            if (!background.isFile || !foreground.isFile) {
                throw PatchException(
                    "$directoryName must contain both $CUSTOM_BACKGROUND and $CUSTOM_FOREGROUND.",
                )
            }

            val targetDirectory = resDirectory.resolve(directoryName).apply { mkdirs() }
            background.copyTo(targetDirectory.resolve(CUSTOM_BACKGROUND), overwrite = true)
            foreground.copyTo(targetDirectory.resolve(CUSTOM_FOREGROUND), overwrite = true)
            copiedDensityCount++
        }

        if (copiedDensityCount == 0) {
            throw PatchException(
                "No supported mipmap density folders were found in ${iconRoot.absolutePath}.",
            )
        }

        val monochromeSource = iconRoot.resolve("drawable/$CUSTOM_MONOCHROME")
        val hasMonochrome = monochromeSource.isFile

        if (hasMonochrome) {
            val targetDrawableDirectory = resDirectory.resolve("drawable").apply { mkdirs() }
            monochromeSource.copyTo(
                targetDrawableDirectory.resolve(CUSTOM_MONOCHROME),
                overwrite = true,
            )
        }

        writeLauncherResources(resDirectory, hasMonochrome)

        document("AndroidManifest.xml").use { document ->
            val application = document.getElementsByTagName("application").item(0) as? Element
                ?: throw PatchException("AndroidManifest.xml does not contain an application element.")

            // App info, APK preview, and launchers that inherit the application icon.
            application.setAttribute("android:icon", "@mipmap/$CUSTOM_LAUNCHER_ICON")
            application.setAttribute("android:roundIcon", "@mipmap/$CUSTOM_LAUNCHER_ICON")

            // Some launchers use an explicit icon from the launcher activity/activity-alias,
            // which overrides android:icon on <application>. Patch those too.
            listOf("activity", "activity-alias").forEach { tagName ->
                val nodes = document.getElementsByTagName(tagName)
                for (index in 0 until nodes.length) {
                    val component = nodes.item(index) as? Element ?: continue
                    if (component.hasLauncherIntentFilter()) {
                        component.setAttribute("android:icon", "@mipmap/$CUSTOM_LAUNCHER_ICON")
                    component.setAttribute("android:roundIcon", "@mipmap/$CUSTOM_LAUNCHER_ICON")
                    }
                }
            }
        }

        println("Custom branding: installed custom Google Photos launcher icon.")
    }
}

private fun writeLauncherResources(resDirectory: File, hasMonochrome: Boolean) {
    // Legacy fallback for Android < 8: compose the foreground/background as a layer list.
    resDirectory.resolve("mipmap-anydpi").apply { mkdirs() }
        .resolve("$CUSTOM_LAUNCHER_ICON.xml")
        .writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <layer-list xmlns:android="http://schemas.android.com/apk/res/android">
                <item>
                    <bitmap
                        android:gravity="fill"
                        android:src="@mipmap/morphe_adaptive_background_custom" />
                </item>
                <item>
                    <bitmap
                        android:gravity="fill"
                        android:src="@mipmap/morphe_adaptive_foreground_custom" />
                </item>
            </layer-list>
            """.trimIndent(),
        )

    // Adaptive icon for Android 8+.
    resDirectory.resolve("mipmap-anydpi-v26").apply { mkdirs() }
        .resolve("$CUSTOM_LAUNCHER_ICON.xml")
        .writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                <background android:drawable="@mipmap/morphe_adaptive_background_custom" />
                <foreground android:drawable="@mipmap/morphe_adaptive_foreground_custom" />
            </adaptive-icon>
            """.trimIndent(),
        )

    // Android 13+ themed icon, when the icon set supplies a monochrome layer.
    if (hasMonochrome) {
        resDirectory.resolve("mipmap-anydpi-v33").apply { mkdirs() }
            .resolve("$CUSTOM_LAUNCHER_ICON.xml")
            .writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
                    <background android:drawable="@mipmap/morphe_adaptive_background_custom" />
                    <foreground android:drawable="@mipmap/morphe_adaptive_foreground_custom" />
                    <monochrome android:drawable="@drawable/morphe_adaptive_monochrome_custom" />
                </adaptive-icon>
                """.trimIndent(),
            )
    }
}

private fun Element.hasLauncherIntentFilter(): Boolean {
    val children = childNodes

    for (index in 0 until children.length) {
        val intentFilter = children.item(index) as? Element ?: continue
        if (intentFilter.tagName != "intent-filter") continue

        var hasMainAction = false
        var hasLauncherCategory = false

        val intentChildren = intentFilter.childNodes
        for (childIndex in 0 until intentChildren.length) {
            val child = intentChildren.item(childIndex) as? Element ?: continue
            val name = child.getAttribute("android:name")

            when (child.tagName) {
                "action" -> {
                    if (name == "android.intent.action.MAIN") {
                        hasMainAction = true
                    }
                }

                "category" -> {
                    if (
                        name == "android.intent.category.LAUNCHER" ||
                        name == "android.intent.category.LEANBACK_LAUNCHER"
                    ) {
                        hasLauncherCategory = true
                    }
                }
            }
        }

        if (hasMainAction && hasLauncherCategory) return true
    }

    return false
}
