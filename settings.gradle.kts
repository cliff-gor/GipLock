pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
//        mavenCentral()
        gradlePluginPortal()
        maven("https://maven-other.tuya.com/repository/maven-releases/")
        maven("https://maven-other.tuya.com/repository/maven-commercial-releases/")
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
        //maven("https://central.maven.org/maven2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://developer.huawei.com/repo/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        //mavenCentral()
        maven("https://maven-other.tuya.com/repository/maven-releases/")
        maven("https://maven-other.tuya.com/repository/maven-commercial-releases/")
        maven("https://jitpack.io")
        maven("https://maven.aliyun.com/repository/public")
        //maven("https://central.maven.org/maven2/")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://developer.huawei.com/repo/")
    }
}

rootProject.name = "MySmartLock"
include(":app")
 