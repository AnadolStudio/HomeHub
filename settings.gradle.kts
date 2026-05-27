dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }

    versionCatalogs {
        create("libs") {
            from(files("./libs.versions.toml"))
        }
    }
}

rootProject.name = "HomeHub"
include(
        ":app",
        ":core:compose-ui",
        ":core:utils",
        ":ha_resources",
)
project(":core:compose-ui").projectDir = File(rootDir, "/compose-ui")
project(":core:utils").projectDir = File(rootDir, "/AndroidCoreBase/utils")
