buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven("https://jitpack.io")
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.kotlin.serialization.plugin)
        classpath(libs.compose.compiler.gradle.plugin)
        classpath(libs.google.services)
        classpath(libs.firebase.crashlytics.plugin)
    }
}

configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.google.dagger") {
            useVersion("HEAD-SNAPSHOT")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
