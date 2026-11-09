plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

import java.util.Properties

// Release signing comes from local.properties (never committed). A checkout
// without it must still configure, so tests and the manifest task can run.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.davidgarcia.karoo"
    compileSdk = 34

    signingConfigs {
        if (localProps.containsKey("signing.storeFile")) {
            create("release") {
                storeFile = file(localProps["signing.storeFile"] as String)
                storePassword = localProps["signing.storePassword"] as String
                keyAlias = localProps["signing.keyAlias"] as String
                keyPassword = localProps["signing.keyPassword"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.davidgarcia.karoo"
        minSdk = 31
        targetSdk = 31
        versionCode = 12
        versionName = "1.2.2"
        manifestPlaceholders["manifestUrl"] = "https://github.com/DavidGarciaLLion/DavidGarciaKaroo/releases/latest/download/manifest.json"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Two flavors, same code: k3 is the normal build; k2 is the same app allowed to install on a
    // Karoo 2 (Android 8, API 26), with its own manifest so a K2 install is never offered the K3 APK.
    flavorDimensions += "device"
    productFlavors {
        create("k3") { dimension = "device"; isDefault = true }
        create("k2") {
            dimension = "device"
            minSdk = 26
            versionNameSuffix = "-K2"
            manifestPlaceholders["manifestUrl"] = "https://raw.githubusercontent.com/DavidGarciaLLion/DavidGarciaKaroo/master/k2/manifest.json"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Falls back to the debug key when local.properties has no release keystore,
            // so the release APK is installable for personal sideloading.
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        variant.outputs.forEach { output ->
            val versionName = output.versionName.orNull ?: "unknown"
            (output as? com.android.build.api.variant.impl.VariantOutputImpl)
                ?.outputFileName?.set("DavidGarciaKaroo-${versionName}.apk")
        }
    }
}

// Karoo extension manifest: the JSON the Karoo fetches from MANIFEST_URL (see
// AndroidManifest.xml) to list the app in its Extension Library and offer
// updates. Written on every build to app/manifest.json (ignored); attach it to
// each GitHub release next to DavidGarciaKaroo.apk. latestVersionCode must be the
// versionCode of that APK — it is what decides whether an update is offered.
// Schema: io.hammerhead.karooext.models.KarooAppManifest.
val generateManifest by tasks.registering {
    val out = layout.projectDirectory.file("manifest.json")
    val outK2 = rootProject.layout.projectDirectory.file("k2/manifest.json")
    val version = android.defaultConfig.versionName ?: "0"
    val code = android.defaultConfig.versionCode ?: 0
    val notes = System.getenv("RELEASE_NOTES").orEmpty()
    inputs.property("version", version); inputs.property("code", code); inputs.property("notes", notes)
    outputs.files(out, outK2)
    doLast {
        fun q(v: String) = "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"")
            .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\""
        fun manifest(label: String, apkUrl: String, versionName: String) =
            """
            {
              "label": ${q(label)},
              "packageName": "com.davidgarcia.karoo",
              "latestApkUrl": ${q(apkUrl)},
              "latestVersion": ${q(versionName)},
              "latestVersionCode": $code,
              "iconUrl": "https://raw.githubusercontent.com/DavidGarciaLLion/DavidGarciaKaroo/master/icon_foreground.png",
              "developer": "David Garcia",
              "description": ${q("Automatically scrolls through your ride data pages so you can focus on riding. Navigation-aware: switches to the map page before a turn and resumes scrolling after it. Per-page dwell time, skip pages, optional sound feedback.")},
              "releaseNotes": ${q(notes)},
              "screenshotUrls": [],
              "tags": ["performance"]
            }
            """.trimIndent()
        out.asFile.writeText(manifest("David Garcia Karoo",
            "https://github.com/DavidGarciaLLion/DavidGarciaKaroo/releases/latest/download/DavidGarciaKaroo.apk", version))
        outK2.asFile.parentFile.mkdirs()
        outK2.asFile.writeText(manifest("David Garcia Karoo (Karoo 2)",
            "https://github.com/DavidGarciaLLion/DavidGarciaKaroo/releases/download/v$version-k2/DavidGarciaKaroo-K2.apk", "$version-K2"))
    }
}
tasks.named("preBuild") { dependsOn(generateManifest) }

dependencies {
    implementation(libs.karoo.ext)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
