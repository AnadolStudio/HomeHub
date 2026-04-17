plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
//    id("com.google.firebase.crashlytics") // TODO
//    id("com.google.gms.google-services") // TODO
}

apply(plugin = libs.plugins.kotlin.serialization.get().pluginId)

android {

    signingConfigs {
        getByName("debug") {

        }

        create("release") {

        }
    }

    with(defaultConfig) {
        applicationId(libs.versions.appId.get())
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        versionCode(libs.versions.versionCode.get().toInt())
        versionName(libs.versions.versionName.get())

        testInstrumentationRunner = libs.versions.testInstrumentationRunner.get()

        setProperty(
                "archivesBaseName",
                "${project.android.defaultConfig.applicationId}" +
                        "-" +
                        "${project.android.defaultConfig.versionName}" +
                        "-" +
                        "${project.android.defaultConfig.versionCode}"
        )

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64"))
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            applicationIdSuffix = ".debug"
            isDebuggable = true
            isMinifyEnabled = false
        }
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = false
            setProguardFiles(
                    listOf(project.file("proguard-rules.pro")) +
                            getDefaultProguardFile("proguard-android-optimize.txt")
            )
        }
    }

    buildFeatures {
        compose = true
    }
    val javaVersion = JavaVersion.VERSION_17

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    kotlinOptions {
        jvmTarget = javaVersion.toString()
        freeCompilerArgs = freeCompilerArgs + listOf("-opt-in=androidx.compose.foundation.ExperimentalFoundationApi")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }

    lint {
        abortOnError = false
    }

    kapt {
        correctErrorTypes = true
        generateStubs = true
    }

    namespace = "com.anadolstudio.template"
}

dependencies {
    implementation(project(":core:compose-ui"))
    implementation(project(":core:utils"))

    implementation(libs.bundles.android.ui)
    annotationProcessor(libs.lifecycle.compiler)

    implementation(libs.bundles.firebase)
    implementation(platform(libs.firebase.bom))

    implementation(libs.bundles.utils)

    implementation(libs.webkit)
    implementation(libs.accompanist.webview)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.room)
    kapt(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.bundles.android.test)
}
