extension {
    name = "extensions/extension.mpe"
}

android {
    namespace = "app.template.extension"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        fun envBuildConfigField(name: String) {
            val value = providers.environmentVariable(name).orNull.orEmpty()
            buildConfigField("String", name, "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
        }

        envBuildConfigField("CITYMAPPER_MAPS_API_KEY")
        envBuildConfigField("FLIGHTRADAR_MAPS_API_KEY")
        envBuildConfigField("NETMONSTER_MAPS_API_KEY")
        envBuildConfigField("SHARED_MAPS_API_KEY")
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
}
