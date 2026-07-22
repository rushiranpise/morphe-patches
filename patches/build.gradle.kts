group = "app.rushiranpise.morphe-patches"

patches {
    about {
        name = "Rushi's Patches"
        description = "Patches for apps I like"
        source = "https://github.com/rushiranpise/morphe-patches"
        author = "rushiranpise"
        contact = "https://github.com/rushiranpise"
        website = "https://github.com/rushiranpise/morphe-patches"
        license = "GPLv3"
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

val generatedSecretsDir = layout.buildDirectory.dir("generated/secrets/kotlin")

sourceSets {
    main {
        kotlin.srcDir(generatedSecretsDir)
    }
}

fun String.kotlinStringLiteral() = replace("\\", "\\\\").replace("\"", "\\\"")

val generateSecrets = tasks.register("generateSecrets") {
    val sharedMapsApiKey = providers.environmentVariable("SHARED_MAPS_API_KEY")
    inputs.property("SHARED_MAPS_API_KEY", sharedMapsApiKey.orElse(""))
    outputs.dir(generatedSecretsDir)

    doLast {
        val outputDir = generatedSecretsDir.get().asFile.resolve("app/template/patches/shared")
        outputDir.mkdirs()
        outputDir.resolve("BuildSecrets.kt").writeText(
            """
            package app.template.patches.shared

            internal object BuildSecrets {
                const val SHARED_MAPS_API_KEY = "${sharedMapsApiKey.orNull.orEmpty().kotlinStringLiteral()}"
            }
            """.trimIndent(),
        )
    }
}

tasks.named("compileKotlin") {
    dependsOn(generateSecrets)
}

tasks.named("sourcesJar") {
    dependsOn(generateSecrets)
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
